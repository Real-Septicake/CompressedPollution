package io.github.real_septicake.compressed_pollution.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.real_septicake.compressed_pollution.CompressedPollution;
import io.github.real_septicake.compressed_pollution.Pollution;
import io.github.real_septicake.compressed_pollution.TaggedPollutionEntry;
import io.github.real_septicake.compressed_pollution.events.PollutionEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.registries.DataPackRegistryEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The base class for handling object types whose pollution values are managed by datapack registries
 * @param <T> The type of object handled by the instance
 */
@Nonnull
public abstract class TaggedPollutionRegistryResolver<T> {
    private final Cache<ResourceLocation, Pollution> CACHE;
    private final Class<T> clazz;
    private final ResourceKey<Registry<TaggedPollutionEntry<T>>> registryKey;
    private final String profilerEntry;

    /**
     * Creates a resolver for the class
     * @param cacheTimer The number of minutes the entries should remain within the cache
     * @param tClass The class to fire the {@link PollutionEvent} for
     * @param rKey The registry to access for the pollution values. Must be visible server-side
     */
    public TaggedPollutionRegistryResolver(long cacheTimer, Class<T> tClass, ResourceKey<Registry<TaggedPollutionEntry<T>>> rKey) {
        CACHE = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(cacheTimer)).build();
        clazz = tClass;
        registryKey = rKey;
        profilerEntry = "Pollution" + clazz.getSimpleName();
    }

    /**
     * Creates a resolver based on the provided arguments
     * @param cacheTimer The number of minutes the entries should remain in the cache
     * @param clazz The class to fire the {@link PollutionEvent} for
     * @param registryLocation The location to place the registry at
     * @param event The {@link net.minecraftforge.registries.DataPackRegistryEvent.NewRegistry NewRegistry} event to use to create the registry
     * @param tagRegistry The registry to use for parsing the tags
     * @param toRL The function to use to convert the object into its {@link ResourceLocation}
     * @param isTag The function to use to check if a given tag applies to the provided object
     * @return The resolver created from the provided arguments
     * @param <R> The type of object to resolve for
     */
    public static <R> TaggedPollutionRegistryResolver<R> create(
            long cacheTimer, Class<R> clazz,
            ResourceLocation registryLocation, DataPackRegistryEvent.NewRegistry event,
            ResourceKey<Registry<R>> tagRegistry, Function<R, ResourceLocation> toRL,
            BiFunction<R, TagKey<R>, Boolean> isTag
    ) {
        ResourceKey<Registry<TaggedPollutionEntry<R>>> key = ResourceKey.createRegistryKey(registryLocation);
        event.dataPackRegistry(key, TaggedPollutionEntry.codec(tagRegistry));
        return new TaggedPollutionRegistryResolver<>(cacheTimer, clazz, key) {
            @Override
            public ResourceLocation toRL(R obj) {
                return toRL.apply(obj);
            }

            @Override
            public boolean isTag(R obj, TagKey<R> tag) {
                return isTag.apply(obj, tag);
            }
        };
    }

    /**
     * Transforms the given object into its <code>ResourceLocation</code>
     * @param obj The object to transform
     * @return The object's <code>ResourceLocation</code>
     */
    public abstract ResourceLocation toRL(T obj);

    /**
     * Returns if the tag is applicable to the given object
     * @param obj The object to check
     * @param tag The tag to check
     * @return Whether the tag is applicable
     */
    public abstract boolean isTag(T obj, TagKey<T> tag);

    /**
     * Resolves the object's pollution values based on the register's contents
     * @param access Registry access for the <b>server</b>, providing client registry access <i><b>will error.</b></i>
     * @param obj The object to get the pollution values of
     * @param profiler Optional profiler for debugging
     * @return The pollution values for the object
     */
    public final Pollution resolve(RegistryAccess access, T obj, Optional<ProfilerFiller> profiler) {
        profiler.ifPresent(p -> p.push(profilerEntry));
        ResourceLocation loc = toRL(obj);
        if (loc == null) {
            profiler.ifPresent(ProfilerFiller::pop);
            return Pollution.PollutionBuilder.EMPTY.copy();
        }
        Pollution cached = CACHE.getIfPresent(loc);
        if (cached != null) {
            profiler.ifPresent(ProfilerFiller::pop);
            return cached.copy();
        }
        Pollution.PollutionBuilder builder = new Pollution.PollutionBuilder();
        access.registryOrThrow(registryKey).entrySet().forEach(
                entry -> {
                    Long value = entry.getValue().values().getOrDefault(loc, null);
                    if(value == null) {
                        profiler.ifPresent(p -> p.push(profilerEntry + "Tag"));
                        TaggedPollutionEntry.PollutionTag<T> tag = null;
                        for(TaggedPollutionEntry.PollutionTag<T> t : entry.getValue().tags()) {
                            if(isTag(obj, t.tag())) {
                                tag = t;
                                break;
                            }
                        }
                        if(tag != null)
                            value = tag.value();
                        profiler.ifPresent(ProfilerFiller::pop);
                    }
                    if(value == null)
                        return;
                    builder.put(entry.getKey().location().toString(), value);
                }
        );
        profiler.ifPresent(ProfilerFiller::pop);
        Pollution created = builder.build();
        CACHE.put(loc, created.copy());
        return created;
    }

    /**
     * Calls {@link TaggedPollutionRegistryResolver#resolve} on the provided object, and fires the {@link PollutionEvent}
     * for its class
     * @param level The level the pollution will be applied to
     * @param obj The object causing the pollution
     * @param sourcePos The position of the object causing the pollution, or null if no appropriate position exists
     */
    public final void fireEvent(ServerLevel level, T obj, @Nullable BlockPos sourcePos) {
        CompressedPollution.handlePollution(
                resolve(level.registryAccess(), obj, Optional.of(level.getProfiler())),
                level, obj, clazz, sourcePos
        );
    }

    /**
     * Similar to {@link TaggedPollutionRegistryResolver#fireEvent(ServerLevel, Object, BlockPos)}, but applies <code>trans</code> before
     * firing the event
     * @param level The level the pollution will be applied to
     * @param obj The object causing the pollution
     * @param sourcePos The position of the object causing the pollution, or null if no appropriate position exists
     * @param trans The {@link Pollution} transformer to be applied
     */
    public final void fireEvent(ServerLevel level, T obj, @Nullable BlockPos sourcePos, Consumer<Pollution> trans) {
        Pollution p = resolve(level.registryAccess(), obj, Optional.of(level.getProfiler()));
        trans.accept(p);
        CompressedPollution.handlePollution(
                p, level, obj, clazz, sourcePos
        );
    }

    public final ResourceKey<Registry<TaggedPollutionEntry<T>>> getRegistryKey() {
        return registryKey;
    }
}

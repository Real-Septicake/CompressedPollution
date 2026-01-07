package io.github.real_septicake.compressed_pollution.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.real_septicake.compressed_pollution.CompressedPollution;
import io.github.real_septicake.compressed_pollution.Pollution;
import io.github.real_septicake.compressed_pollution.TaggedPollutionEntry;
import io.github.real_septicake.compressed_pollution.events.ClassedPollutionEvent;
import io.github.real_septicake.compressed_pollution.events.PollutionEventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The base class for handling object types whose pollution values are managed by datapack registries
 * @param <T> The type of object handled by the instance
 */
@Nonnull
public abstract class PollutionRegistryResolver<T> {
    private final Cache<ResourceLocation, Pollution> CACHE;
    private final Class<T> clazz;
    private final ResourceKey<Registry<TaggedPollutionEntry<T>>> registryKey;
    private final String profilerEntry;

    /**
     * Creates a resolver for the class
     * @param cacheTimer The number of minutes the entries should remain within the cache
     * @param tClass The class to fire the {@link ClassedPollutionEvent} for
     * @param rKey The registry to access for the pollution values. Must be visible server-side
     */
    public PollutionRegistryResolver(long cacheTimer, Class<T> tClass, ResourceKey<Registry<TaggedPollutionEntry<T>>> rKey) {
        CACHE = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(cacheTimer)).build();
        clazz = tClass;
        registryKey = rKey;
        profilerEntry = "Pollution" + clazz.getSimpleName();
    }

    /**
     * Transforms the given object into its ResourceLocation
     * @param obj The object to transform
     * @return The object's ResourceLocation
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
     * Calls {@link PollutionRegistryResolver#resolve} on the provided object, and fires the {@link ClassedPollutionEvent}
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
     * Similar to {@link PollutionRegistryResolver#fireEvent(ServerLevel, Object, BlockPos)}, but applies <code>trans</code> before
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
}

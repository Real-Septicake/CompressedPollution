package io.github.real_septicake.compressed_pollution.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.real_septicake.compressed_pollution.CompressedPollution;
import io.github.real_septicake.compressed_pollution.Pollution;
import io.github.real_septicake.compressed_pollution.UntaggedPollutionEntry;
import io.github.real_septicake.compressed_pollution.events.PollutionEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The base class for handling object types whose pollution values are managed by datapack registries.
 * The difference between this and {@link TaggedPollutionRegistryResolver} is that this class is for objects that do not have tags
 * @param <T> The type of object handled by the instance
 */
@Nonnull
public abstract class UntaggedPollutionRegistryResolver<T> {
    private final Cache<ResourceLocation, Pollution> CACHE;
    private final Class<T> clazz;
    private final ResourceKey<Registry<UntaggedPollutionEntry>> registryKey;
    private final String profilerEntry;

    /**
     * Creates a resolver for the class
     * @param cacheTimer The number of minutes the entries should remain within the cache
     * @param clazz The class to fire the event for
     * @param rKey The registry to access for the pollution values. Must visible server-side
     */
    public UntaggedPollutionRegistryResolver(long cacheTimer, Class<T> clazz, ResourceKey<Registry<UntaggedPollutionEntry>> rKey) {
        this.CACHE = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(cacheTimer)).build();
        this.clazz = clazz;
        this.registryKey = rKey;
        this.profilerEntry = "PollutionUntagged" + clazz.getSimpleName();
    }

    /**
     * Transforms the given object into its <code>ResourceLocation</code>
     * @param obj The obejct to transform
     * @return The object's <code>ResourceLocation</code>
     */
    public abstract ResourceLocation toRL(T obj);

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
        if(loc == null) {
            profiler.ifPresent(ProfilerFiller::pop);
            return Pollution.PollutionBuilder.EMPTY.copy();
        }
        Pollution cached = CACHE.getIfPresent(loc);
        if(cached != null) {
            profiler.ifPresent(ProfilerFiller::pop);
            return cached.copy();
        }
        Pollution.PollutionBuilder builder = new Pollution.PollutionBuilder();
        access.registryOrThrow(registryKey).entrySet().forEach(
                entry -> builder.put(entry.getKey().location().toString(), entry.getValue().values().getOrDefault(loc, 0L))
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
}

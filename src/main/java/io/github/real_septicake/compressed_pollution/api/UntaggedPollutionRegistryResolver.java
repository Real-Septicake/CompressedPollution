package io.github.real_septicake.compressed_pollution.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.real_septicake.compressed_pollution.CompressedPollution;
import io.github.real_septicake.compressed_pollution.Pollution;
import io.github.real_septicake.compressed_pollution.UntaggedPollutionEntry;
import io.github.real_septicake.compressed_pollution.events.PollutionEventFactory;
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

@Nonnull
public abstract class UntaggedPollutionRegistryResolver<T> {
    private final Cache<ResourceLocation, Pollution> CACHE;
    private final Class<T> clazz;
    private final ResourceKey<Registry<UntaggedPollutionEntry>> registryKey;
    private final String profilerEntry;

    public UntaggedPollutionRegistryResolver(long cacheTimer, Class<T> clazz, ResourceKey<Registry<UntaggedPollutionEntry>> rKey) {
        this.CACHE = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(cacheTimer)).build();
        this.clazz = clazz;
        this.registryKey = rKey;
        this.profilerEntry = "PollutionUntagged" + clazz.getSimpleName();
    }

    public abstract ResourceLocation toRL(T obj);

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
                entry -> {
                    builder.put(entry.getKey().location().toString(), entry.getValue().values().getOrDefault(loc, 0L));
                }
        );
        profiler.ifPresent(ProfilerFiller::pop);
        Pollution created = builder.build();
        CACHE.put(loc, created.copy());
        return created;
    }

    public final void fireEvent(ServerLevel level, T obj, @Nullable BlockPos sourcePos) {
        CompressedPollution.handlePollution(
                resolve(level.registryAccess(), obj, Optional.of(level.getProfiler())),
                level, obj, clazz, sourcePos
        );
    }

    public final void fireEvent(ServerLevel level, T obj, @Nullable BlockPos sourcePos, Consumer<Pollution> trans) {
        Pollution p = resolve(level.registryAccess(), obj, Optional.of(level.getProfiler()));
        trans.accept(p);
        CompressedPollution.handlePollution(
                p, level, obj, clazz, sourcePos
        );
    }

    public final void fireEvent(ServerLevel level, T obj, @Nullable BlockPos sourcePos, Consumer<Pollution> trans, PollutionEventFactory<T> factory) {
        Pollution p = resolve(level.registryAccess(), obj, Optional.of(level.getProfiler()));
        trans.accept(p);
        CompressedPollution.handlePollution(
                p, level, obj, clazz, sourcePos, factory
        );
    }
}

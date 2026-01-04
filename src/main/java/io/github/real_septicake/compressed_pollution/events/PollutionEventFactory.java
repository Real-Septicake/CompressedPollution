package io.github.real_septicake.compressed_pollution.events;

import io.github.real_septicake.compressed_pollution.Pollution;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * A functional interface representing the constructors of {@link PollutionEvent} subclasses
 * @param <T> The type of object the event will fire for
 */
@FunctionalInterface
public interface PollutionEventFactory<T> {
    PollutionEvent<T> create(Class<T> clazz, Pollution pollution, T obj, ServerLevel level, BlockPos sourcePos);
}

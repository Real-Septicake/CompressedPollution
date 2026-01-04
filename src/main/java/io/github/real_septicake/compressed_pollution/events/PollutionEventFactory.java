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
    /**
     * Creates the {@link PollutionEvent} with the given values
     * @param clazz The class object of the type to fire the event for
     * @param pollution The object representing the pollution to be applied
     * @param obj The object causing the pollution
     * @param level The level the pollution will be applied to
     * @param sourcePos The position the source of the pollution is at currently
     * @return The <code>PollutionEvent</code> with the given values
     */
    PollutionEvent<T> create(Class<T> clazz, Pollution pollution, T obj, ServerLevel level, BlockPos sourcePos);
}

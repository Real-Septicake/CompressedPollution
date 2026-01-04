package io.github.real_septicake.compressed_pollution.events;

import io.github.real_septicake.compressed_pollution.Pollution;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * A type of {@link PollutionEvent} for groups of objects that do not have a common super class, such as types of energy
 * @param <T> The type to filter this event to. Must be a superclass of {@link NonClassedType}
 */
public class NonClassedPollutionEvent<T extends NonClassedType> extends PollutionEvent<T> {
    private final T type;
    public NonClassedPollutionEvent(Class<T> clazz, Pollution pollution, T type, ServerLevel level, BlockPos sourcePos) {
        super(clazz, level, pollution, sourcePos);
        this.type = type;
    }

    /**
     * @return The object representing the type of object causing the pollution
     */
    public T getType() {
        return type;
    }
}

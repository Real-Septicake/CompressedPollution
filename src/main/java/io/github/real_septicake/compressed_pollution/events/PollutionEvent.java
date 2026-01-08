package io.github.real_septicake.compressed_pollution.events;

import io.github.real_septicake.compressed_pollution.Pollution;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.GenericEvent;

import javax.annotation.Nullable;

/**
 * Fired when pollution is about to be applied to a level. Can be canceled to prevent it from doing so
 * or modified to change what values each pollution causes.
 * @param <T> The type to filter this event to
 */
@Cancelable
public class PollutionEvent<T> extends GenericEvent<T> {
    private final Pollution pollution;
    private final T obj;
    private final ServerLevel level;
    private final @Nullable BlockPos sourcePos;
    public PollutionEvent(Class<T> clazz, Pollution pollution, T obj, ServerLevel level, @Nullable BlockPos sourcePos) {
        super(clazz);
        this.level = level;
        this.pollution = pollution;
        this.sourcePos = sourcePos;
        this.obj = obj;
    }

    /**
     * @return The object causing the pollution
     */
    public T getObj() {
        return obj;
    }

    /**
     * @return The level the pollution is being applied to
     */
    public ServerLevel getLevel() {
        return level;
    }

    /**
     * @return The pollution being applied
     */
    public Pollution getPollution() {
        return pollution;
    }

    /**
     * @return The position of the object causing the pollution, or null if no appropriate position exists
     */
    public @Nullable BlockPos getSourcePos() {
        return sourcePos;
    }
}

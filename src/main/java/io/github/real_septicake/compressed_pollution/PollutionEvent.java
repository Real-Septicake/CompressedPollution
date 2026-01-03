package io.github.real_septicake.compressed_pollution;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.GenericEvent;

/**
 * Fired when pollution is about to be applied to a level. Can be canceled to prevent it from doing so
 * or modified to change what values each pollution causes.
 * @param <T> The type to filter this event to
 */
@Cancelable
public class PollutionEvent<T> extends GenericEvent<T> {
    private final T obj;
    private final Pollution pollution;
    private final ServerLevel level;
    public PollutionEvent(Class<T> type, Pollution pollution, T obj, ServerLevel level) {
        super(type);
        this.obj = obj;
        this.pollution = pollution;
        this.level = level;
    }

    /**
     * @return The object causing the pollution
     */
    public T getObj() {
        return obj;
    }

    /**
     * @return The pollution being applied
     */
    public Pollution getPollution() {
        return pollution;
    }

    /**
     * @return The level the pollution is being applied to
     */
    public ServerLevel getLevel() {
        return this.level;
    }
}

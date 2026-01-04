package io.github.real_septicake.compressed_pollution.events;

import io.github.real_septicake.compressed_pollution.Pollution;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.GenericEvent;

/**
 * Fired when pollution is about to be applied to a level. Can be canceled to prevent it from doing so
 * or modified to change what values each pollution causes.
 * @param <T> The type to filter this event to
 */
public abstract class PollutionEvent<T> extends GenericEvent<T> {
    private final ServerLevel level;
    private final Pollution pollution;

    public PollutionEvent(Class<T> clazz, ServerLevel level, Pollution pollution) {
        super(clazz);
        this.level = level;
        this.pollution = pollution;
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
}

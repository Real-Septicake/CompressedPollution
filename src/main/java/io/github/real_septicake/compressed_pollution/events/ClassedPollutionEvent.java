package io.github.real_septicake.compressed_pollution.events;

import io.github.real_septicake.compressed_pollution.Pollution;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.GenericEvent;

/**
 * A type of {@link PollutionEvent} for groups of objects that have common superclasses e.g. <code>Item</code> and <code>Fluid</code>
 * @param <T> The type to filter the event to
 */
@Cancelable
public class ClassedPollutionEvent<T> extends PollutionEvent<T> {
    private final T obj;
    public ClassedPollutionEvent(Class<T> type, Pollution pollution, T obj, ServerLevel level) {
        super(type, level, pollution);
        this.obj = obj;
    }

    /**
     * @return The object causing the pollution
     */
    public T getObj() {
        return obj;
    }
}

package io.github.real_septicake.compressed_pollution.compat.ae2;

import appeng.api.stacks.AEKey;
import com.mojang.datafixers.util.Pair;
import io.github.real_septicake.compressed_pollution.CompressedPollution;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Compatibility layer for AE2 drives
 * <p>
 * Make sure to check that AE2 is loaded before using
 */
public class AE2CompatHandler {
    /**
     * A function for handling a specific drive key type
     * @param <T> The key to be handled
     */
    @FunctionalInterface
    public interface KeyHandler<T extends AEKey> {
        void handle(T key, long amount, ServerLevel level, @Nullable BlockPos sourcePos);
    }

    /**
     * Handler singleton
     */
    public static final AE2CompatHandler INSTANCE = new AE2CompatHandler();
    private final Map<Class<? extends AEKey>, Pair<KeyHandler<? extends AEKey>, String>> handlers = new HashMap<>();

    private AE2CompatHandler() {}

    /**
     * An exception representing when a handler is attempting to be added for a key that's already handled
     */
    public static class AlreadyPresentException extends Exception {
        public AlreadyPresentException(String message) {
            super(message);
        }
    }

    /**
     * Attempt to add a handler for a type of drive
     * @param clazz The class of the key type to handle
     * @param handler The handler for the key type
     * @param modId The id of the mod adding the handler
     * @param <T> The type of key being handled
     * @throws AlreadyPresentException If there is already a handler present for that key type
     */
    public <T extends AEKey> void addHandler(Class<T> clazz, KeyHandler<T> handler, String modId) throws AlreadyPresentException {
        var present = handlers.get(clazz);
        if(present != null)
            throw new AlreadyPresentException("Handler for type " + clazz.getSimpleName() + " already exists. Registered by mod \"" + present.getSecond() + "\"");
        CompressedPollution.LOGGER.debug("Registering handler for {} type by \"{}\"", clazz.getSimpleName(), modId);
        handlers.put(clazz, Pair.of(handler, modId));
    }

    /**
     * Gets the handler for the specified key type class
     * @param clazz The class of key type to get the handler for
     * @return The handler, or null if no such handler is registered
     * @param <T> The type of key
     */
    @SuppressWarnings("unchecked") // It's a guarantee granted by above method
    public <T extends AEKey> @Nullable KeyHandler<AEKey> getHandler(Class<T> clazz) {
        var handler = handlers.getOrDefault(clazz, null);
        if(handler != null)
            return (KeyHandler<AEKey>) handler.getFirst();
        return null;
    }
}

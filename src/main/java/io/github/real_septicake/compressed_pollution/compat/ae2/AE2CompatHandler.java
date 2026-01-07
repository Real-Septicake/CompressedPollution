package io.github.real_septicake.compressed_pollution.compat.ae2;

import appeng.api.stacks.AEKey;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class AE2CompatHandler {
    @FunctionalInterface
    public interface KeyHandler<T extends AEKey> {
        void handle(T key, long amount, ServerLevel level, @Nullable BlockPos sourcePos);
    }

    private static final AE2CompatHandler INSTANCE = new AE2CompatHandler();
    private final Map<Class<? extends AEKey>, Pair<KeyHandler<? extends AEKey>, String>> handlers = new HashMap<>();

    private AE2CompatHandler() {}

    public static AE2CompatHandler instance() {
        return INSTANCE;
    }

    public static class AlreadyPresentException extends Exception {
        public AlreadyPresentException(String message) {
            super(message);
        }
    }

    public <T extends AEKey> void addHandler(Class<T> clazz, KeyHandler<T> handler, String modId) throws AlreadyPresentException {
        var present = handlers.get(clazz);
        if(present != null)
            throw new AlreadyPresentException("Handler for type " + clazz.getSimpleName() + " already exists. Registered by mod \"" + present.getSecond() + "\"");
        handlers.put(clazz, Pair.of(handler, modId));
    }

    @SuppressWarnings("unchecked") // It's a guarantee granted by above method
    public <T extends AEKey> @Nullable KeyHandler<AEKey> getHandler(Class<T> clazz) {
        var handler = handlers.getOrDefault(clazz, null);
        if(handler != null)
            return (KeyHandler<AEKey>) handler.getFirst();
        return null;
    }
}

package io.github.real_septicake.compressed_pollution;

import io.github.real_septicake.compressed_pollution.events.PollutionEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class EventBatcher {
    private final HashMap<BatchKey<?>, Pollution> batches = new HashMap<>();

    private record BatchKey<T>(ServerLevel level, Class<T> clazz, T obj, @Nullable BlockPos sourcePos) {}

    public <T> void add(ServerLevel level, Class<T> clazz, T obj, @Nullable BlockPos sourcePos, Pollution pollution) {
        BatchKey<T> key = new BatchKey<>(level, clazz, obj, sourcePos);
        Pollution p = batches.get(key);
        if(p == null) {
            batches.put(key, pollution);
        } else {
            batches.put(key, p.merge(pollution));
        }
    }

    @SuppressWarnings("unchecked") // it's probably fine?
    public void dispatch(Optional<ProfilerFiller> profiler) {
        profiler.ifPresent(p -> p.push("PollutionApplication"));
        for(Map.Entry<BatchKey<?>, Pollution> entry : batches.entrySet()) {
            BatchKey<Object> key = (BatchKey<Object>) entry.getKey();
            Pollution pollution = entry.getValue();
            if(!MinecraftForge.EVENT_BUS.post(new PollutionEvent<>(
                    key.clazz,
                    pollution,
                    key.obj,
                    key.level,
                    key.sourcePos
            )) && !pollution.isEmpty())
                LevelPollution.getFromLevel(key.level).apply(pollution);
        }
        batches.clear();
        profiler.ifPresent(ProfilerFiller::pop);
    }
}

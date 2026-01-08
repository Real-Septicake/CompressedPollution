package io.github.real_septicake.compressed_pollution;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the current pollution values in a level
 */
public class LevelPollution {
    private final HashMap<String, Long> pollutants;

    public LevelPollution(Map<String, Long> pollutants) {
        this.pollutants = new HashMap<>(pollutants); // needs to be mutable
    }

    public LevelPollution() {
        pollutants = new HashMap<>();
    }

    /**
     * Applies the provided {@link Pollution} to this <code>LevelPollution</code>.
     * <p>
     * For each pollution type in the provided <code>Pollution</code>, its value is added to the current value
     * of that type, up to a point. If the pollution value of a type would go past 9.2 quintillion, positive or
     * negative, then it is clamped to it.
     * @param pollution The pollution to apply
     */
    public void apply(@Nonnull Pollution pollution) {
        for(String key : pollution.values().keySet()) {
            long current = pollutants.getOrDefault(key, 0L);
            long applying = pollution.values().get(key);
            if(applying == 0L) // Nothing to apply
                continue;
            if(current == 0L) { // Can't overflow
                pollutants.put(key, applying);
                continue;
            }
            long v = LongUtil.safeAdd(current, applying);
            if(v != 0)
                pollutants.put(key, v);
            else
                pollutants.remove(key);
        }
    }

    public static final Codec<LevelPollution> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.LONG).fieldOf("values").forGetter(it -> it.pollutants)
    ).apply(instance, instance.stable(LevelPollution::new)));

    /**
     * Obtains the current {@link LevelPollution} for the provided level
     * @param level The level to get the current pollution of
     * @return The pollution of the provided level
     */
    public static LevelPollution getFromLevel(@Nonnull ServerLevel level) {
        return level.getCapability(CompressedPollution.LEVEL_POLLUTION_CAPABILITY)
                .resolve().orElseThrow().getPollution();
    }

    /**
     * Sets the {@link LevelPollution} for the provided level. <b><i>Voids the previous values.</i></b>
     * @param level The level to set the pollution of
     * @param pollution The pollution to set for the level
     */
    public static void setForLevel(@Nonnull ServerLevel level, @Nonnull LevelPollution pollution) {
        level.getCapability(CompressedPollution.LEVEL_POLLUTION_CAPABILITY)
                .resolve().orElseThrow().setPollution(pollution);
    }
}

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
     * Gets the current value of the pollutant specified by the key
     * @param key The pollutant to get the value of
     * @return The current value of the pollutant
     */
    public Long getPollutant(String key) {
        return pollutants.getOrDefault(key, 0L);
    }

    /**
     * Sets the value of the pollutant specified by the key, or removes it if setting to 0
     * @param key The pollutant to set the value of
     * @param value The value to set the pollutant to
     */
    public void setPollutant(String key, Long value) {
        if(value == 0L)
            pollutants.remove(key);
        else
            pollutants.put(key, value);
    }

    /**
     * Adds a value to the pollutant specified by the key, clamping the resulting value to within the values of a long
     * while avoiding overflows.
     * @param key The pollutant to add the value to
     * @param value The amount to add to the pollutant
     */
    public void addPollutant(String key, Long value) {
        if(value == 0L) // Nothing to apply
            return;
        long current = pollutants.getOrDefault(key, 0L);
        if(current == 0L) { // Can't overflow
            pollutants.put(key, value);
            return;
        }
        long v = LongUtil.safeAdd(current, value);
        if(v != 0)
            pollutants.put(key, v);
        else
            pollutants.remove(key);
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
            long applying = pollution.values().get(key);
            addPollutant(key, applying);
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

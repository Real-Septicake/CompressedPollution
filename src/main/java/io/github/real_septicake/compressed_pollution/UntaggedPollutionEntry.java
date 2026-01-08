package io.github.real_septicake.compressed_pollution;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * The java representation of values present in untagged pollution registries
 * @param values The specific values for this pollution
 */
public record UntaggedPollutionEntry(Map<ResourceLocation, Long> values) {
    public static final Codec<UntaggedPollutionEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(ResourceLocation.CODEC, Codec.LONG).fieldOf("values").forGetter(it -> it.values)
            ).apply(instance, instance.stable(UntaggedPollutionEntry::new))
    );

    /**
     * A convenience factory for a {@link UntaggedPollutionEntry} object. Intended for use with datagen
     */
    public static class Builder {
        private final Map<ResourceLocation, Long> values;

        /**
         * Creates a fresh factory
         */
        public Builder() {
            this.values = new HashMap<>();
        }

        /**
         * Puts a value for a specific object
         * @param loc The object the value is for
         * @param value The amount of the pollution to produce
         * @return Self for chaining
         */
        public Builder put(ResourceLocation loc, long value) {
            long v = values.getOrDefault(loc, 0L);
            values.put(loc, LongUtil.safeAdd(v, value));
            return this;
        }

        /**
         * Creates the {@link TaggedPollutionEntry} this builder currently represents
         * @return The created PollutionEntry
         */
        public UntaggedPollutionEntry build() {
            return new UntaggedPollutionEntry(values);
        }
    }
}

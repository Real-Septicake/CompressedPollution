package io.github.real_septicake.compressed_pollution;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record UntaggedPollutionEntry(Map<ResourceLocation, Long> values) {
    public static final Codec<UntaggedPollutionEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(ResourceLocation.CODEC, Codec.LONG).fieldOf("values").forGetter(it -> it.values)
            ).apply(instance, instance.stable(UntaggedPollutionEntry::new))
    );

    public static class Builder {
        private final Map<ResourceLocation, Long> values;

        public Builder() {
            this.values = new HashMap<>();
        }

        public Builder put(ResourceLocation loc, long value) {
            long v = values.getOrDefault(loc, 0L);
            values.put(loc, v + value);
            return this;
        }

        public UntaggedPollutionEntry build() {
            return new UntaggedPollutionEntry(values);
        }
    }
}

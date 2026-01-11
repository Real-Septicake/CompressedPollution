package io.github.real_septicake.compressed_pollution;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The java representation of values in the pollution registries
 * @param values The specific values for this pollution
 * @param tags The values of tags for this pollution
 * @param <T> The type of object represented e.g. Item or Fluid
 */
public record TaggedPollutionEntry<T>(Map<ResourceLocation, Long> values, List<PollutionTag<T>> tags) {
    /**
     * Creates a codec for the values of the specified registry
     * @param registry The registry to create the entry codec for
     * @return The codec for the values
     * @param <R> The type of object represented e.g. Item or Fluid
     */
    public static <R> Codec<TaggedPollutionEntry<R>> codec(ResourceKey<Registry<R>> registry) {
        return RecordCodecBuilder.create(instance -> instance.
                group(
                        Codec.unboundedMap(ResourceLocation.CODEC, Codec.LONG).fieldOf("values").forGetter(it -> it.values),
                        Codec.list(
                                RecordCodecBuilder.create(
                                         (RecordCodecBuilder.Instance<PollutionTag<R>> tag) -> tag.group(
                                                TagKey.codec(registry).fieldOf("tag").forGetter(PollutionTag::tag),
                                                Codec.LONG.fieldOf("value").forGetter(PollutionTag::value)
                                        ).apply(tag, tag.stable(PollutionTag<R>::new))
                                )
                        ).optionalFieldOf("tags", List.of()).forGetter(it -> it.tags)
                ).apply(instance, instance.stable(TaggedPollutionEntry::new))
        );
    }

    /**
     * Java representation of the objects under the "tags" key in the JSON file
     * @param tag The tag the value is for
     * @param value The amount of this pollution to produce
     * @param <T> The type of object represented e.g. Item or Fluid
     */
    public record PollutionTag<T>(TagKey<T> tag, long value) {}

    /**
     * A convenience factory for a {@link TaggedPollutionEntry} object. Intended for use with datagen
     * @param <T> The type of object represented e.g. Item or Fluid
     */
    public static class Builder<T> {
        private final Map<ResourceLocation, Long> values;
        private final List<PollutionTag<T>> tags;

        /**
         * Creates a fresh factory
         */
        public Builder() {
            values = new HashMap<>();
            this.tags = new ArrayList<>();
        }

        /**
         * Puts a value for a specific object
         * @param loc The object the value is for
         * @param value The amount of the pollution to produce
         * @return Self for chaining
         */
        public Builder<T> putValue(ResourceLocation loc, long value) {
            long v = values.getOrDefault(loc, 0L);
            values.put(loc, LongUtil.safeAdd(v, value));
            return this;
        }

        /**
         * Puts a value for a tag
         * @param tag The tag the value is for
         * @param value The amount of pollution to produce
         * @return Self for chaining
         */
        public Builder<T> putTag(TagKey<T> tag, long value) {
            tags.add(new PollutionTag<>(tag, value));
            return this;
        }

        /**
         * Creates the {@link TaggedPollutionEntry} this builder currently represents
         * @return The created PollutionEntry
         */
        public TaggedPollutionEntry<T> build() {
            return new TaggedPollutionEntry<>(values, tags);
        }
    }
}

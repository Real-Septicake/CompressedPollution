package io.github.real_septicake.compressed_pollution;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * An object representing amounts of pollutants
 * @param values Map from pollution to its value
 */
public record Pollution(@Nonnull Map<String, Long> values) {
    /**
     * A convenience factory for a Pollution object
     */
    public static class PollutionBuilder {
        /**
         * Creates a fresh factory
         */
        public PollutionBuilder() {
            values = new HashMap<>();
        }

        /**
         * An empty pollution object
         * <p>
         * <b>Copy when using this.</b> Any attempt to modify directly will result in an error
         */
        public static final Pollution EMPTY = new Pollution(Map.of());

        private final Map<String, Long> values;

        /**
         * Sets the provided pollution's value. A value of 0 removes the pollution
         * @param pollution The name of the pollution
         * @param value The value to set the pollution to
         * @return Self for chaining
         */
        public PollutionBuilder put(String pollution, long value) {
            if(value == 0L) {
                values.remove(pollution);
                return this;
            }
            values.put(pollution, value);
            return this;
        }

        /**
         * Adds to the provided pollution's value
         * @param pollution The name of the pollution
         * @param value The value to add
         * @return Self for chaining
         */
        public PollutionBuilder add(String pollution, long value) {
            if(value == 0)
                return this;
            long init = values.getOrDefault(pollution, 0L);
            long v = LongUtil.safeAdd(init, value);
            if(v != 0L)
                values.put(pollution, v);
            else
                values.remove(pollution);
            return this;
        }

        /**
         * Creates the {@link Pollution} object this builder currently represents
         * @return The created Pollution object
         */
        public Pollution build() {
            return new Pollution(values);
        }
    }

    /**
     * @return Whether there are any pollution values present
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * Multiplies the values of this pollution instance by the value provided, the results of which are capped to
     * 9.2 quintillion to prevent possible overflows
     *
     * @param val The value to multiply, 0 clears all values
     * @return Self for chaining
     */
    public Pollution multiply(long val) {
        if (val != 0)
            values.replaceAll((s, v) -> LongUtil.safeMult(v, val));
        else
            values.clear();
        return this;
    }

    /**
     * Sets the value of the specified pollution
     * @param pollution The name of the pollution
     * @param value The value to set the pollution to
     * @return Self for chaining
     */
    public Pollution put(String pollution, long value) {
        if(value == 0L) {
            values.remove(pollution);
            return this;
        }
        this.values.put(pollution, value);
        return this;
    }

    /**
     * Adds to the provided pollution's value
     * @param pollution The name of the pollution
     * @param value The value to add
     * @return Self for chaining
     */
    public Pollution add(String pollution, long value) {
        if(value == 0)
            return this;
        long init = values.getOrDefault(pollution, 0L);
        long val = LongUtil.safeAdd(value, init);
        if(val != 0)
            values.put(pollution, val);
        else
            values.remove(pollution);
        return this;
    }

    public Pollution merge(Pollution pollution) {
        for (String key : pollution.values.keySet()) {
            add(key, pollution.values.get(key));
        }
        return this;
    }

    /**
     * @return A copy of this Pollution. Changes made to the returned object will not affect the original
     */
    public Pollution copy() {
        Map<String, Long> copy = new HashMap<>(values);
        return new Pollution(copy);
    }

    @Override
    public @NotNull String toString() {
        return values.toString();
    }
}

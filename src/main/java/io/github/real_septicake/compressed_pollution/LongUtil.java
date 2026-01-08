package io.github.real_septicake.compressed_pollution;

/**
 * Because math gets slightly annoying sometimes
 */
public class LongUtil {

    /**
     * A util function for getting the sign of a long without the cast that {@link Math#signum(float)} requires
     * @param v The value to get the sign of
     * @return The sign of <code>v</code>
     */
    public static long longSignum(long v) {
        if(v == 0)
            return 0;
        return ~((v >>> 63) << 1) + 2;
    }

    /**
     * A util function for guaranteeing that the result will remain within a reasonable range
     * @param v1 The first value
     * @param v2 The second value
     * @return Either {@link Long#MAX_VALUE} of the proper sign if the resulting value would be too large, or the result if not
     */
    public static long safeMult(long v1, long v2) {
        try {
            return Math.multiplyExact(v1, v2);
        } catch(ArithmeticException e) {
            return Long.MAX_VALUE * (longSignum(v1) * longSignum(v2));
        }
    }

    /**
     * A util function for guaranteeing that the result will remain within a reasonable range
     * @param v1 The first value
     * @param v2 The second value
     * @return Either {@link Long#MAX_VALUE} of the proper sign if the resulting value would be too large, or the result if not
     */
    public static long safeAdd(long v1, long v2) {
        try {
            return Math.addExact(v1, v2);
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE * ((longSignum(v1) + longSignum(v2)) >> 1);
        }
    }
}

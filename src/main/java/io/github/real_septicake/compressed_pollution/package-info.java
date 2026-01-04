/**
 * Current optimizations:<ul>
 *     <li>Caching for calculated pollutant values, preventing expensive iterations over registry contents</li>
 *     <li>Remove 0 values from {@link io.github.real_septicake.compressed_pollution.Pollution}
 *     and {@link io.github.real_septicake.compressed_pollution.LevelPollution} objects</li>
 * </ul>
 *
 */
package io.github.real_septicake.compressed_pollution;
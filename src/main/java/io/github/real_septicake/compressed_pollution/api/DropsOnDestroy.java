package io.github.real_septicake.compressed_pollution.api;

/**
 * Marks that an object will drop its contents on destruction, meaning it does not need to be handled
 * when being destroyed upon e.g. throwing it into lava.
 * <p>
 * Pretty much only ever needs to be used on {@link net.minecraft.world.level.block.Block}s
 * that have an internal inventory but not a dedicated {@link net.minecraft.world.item.BlockItem} subclass
 * due to the way it's been set up.
 * <p>
 * <sub>*cough* Shulker boxes *cough*</sub>
 */
public interface DropsOnDestroy {}

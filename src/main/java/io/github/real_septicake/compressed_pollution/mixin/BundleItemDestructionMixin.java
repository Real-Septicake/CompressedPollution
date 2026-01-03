package io.github.real_septicake.compressed_pollution.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BundleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BundleItem.class)
public class BundleItemDestructionMixin {
    @Inject(method = "onDestroyed", at = @At("HEAD"))
    private void pollutionTime(ItemEntity itemEntity, CallbackInfo ci) {
        // NO-OP
        // Items are already dropped by base implementation
        // This mixin is exclusively here to prevent me from thinking it's not already handled
    }
}

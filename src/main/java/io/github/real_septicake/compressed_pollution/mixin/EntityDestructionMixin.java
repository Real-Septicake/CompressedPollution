package io.github.real_septicake.compressed_pollution.mixin;

import io.github.real_septicake.compressed_pollution.CompressedPollution;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityDestructionMixin {
    @Inject(method = "onBelowWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;discard()V", shift = At.Shift.AFTER))
    private void polluteOnVoid(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if(self instanceof ItemEntity ie && !ie.level().isClientSide) {
            CompressedPollution.handlePollution(
                    CompressedPollution.pollutionForItem(ie.level().registryAccess(), ie.getItem(), ie.level().getProfiler()),
                    (ServerLevel) ie.level(),
                    ie.getItem().getItem(),
                    Item.class
            );
        }
    }
}

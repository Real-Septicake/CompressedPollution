package io.github.real_septicake.compressed_pollution.mixin;

import io.github.real_septicake.compressed_pollution.CompressedPollution;
import io.github.real_septicake.compressed_pollution.api.PollutionContainer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
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
            if(ie.getItem().getItem() instanceof PollutionContainer c) {
                c.compressedPollution$handleContents(ie.getItem(), (ServerLevel) ie.level(), ie.getItem().getCount(), ie.blockPosition());
            }
            CompressedPollution.ITEM_RESOLVER.fireEvent(
                    (ServerLevel) ie.level(), ie.getItem().getItem(),
                    ie.blockPosition(), p -> p.multiply(ie.getItem().getCount())
            );
        }
    }
}

package io.github.real_septicake.compressed_pollution.mixin;

import io.github.real_septicake.compressed_pollution.CompressedPollution;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class ItemDestructionMixin {
    @Inject(method = "onDestroyed", at = @At("HEAD"))
    private void pollutionTime(ItemEntity item, CallbackInfo ci) {
        if(!item.level().isClientSide) {
            CompressedPollution.handlePollution(
                    CompressedPollution.pollutionForItem(item.level().registryAccess(), item.getItem(), item.level().getProfiler()).multiply(item.getItem().getCount()),
                    (ServerLevel) item.level(),
                    item.getItem().getItem(),
                    Item.class
            );
        }
    }
}

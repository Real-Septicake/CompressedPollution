package io.github.real_septicake.compressed_pollution.mixin;

import io.github.real_septicake.compressed_pollution.CompressedPollution;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemDestructionMixin {
    @Inject(method = "onDestroyed", at = @At("HEAD"))
    private void pollutionTime(ItemEntity item, CallbackInfo ci) {
        if(!item.level().isClientSide) {
            CompressedPollution.ITEM_RESOLVER.fireEvent(
                    (ServerLevel) item.level(), item.getItem().getItem(),
                    item.blockPosition(), p -> p.multiply(item.getItem().getCount())
            );
        }
    }

    @Inject(method = "placeBlock", at = @At("HEAD"))
    private void onFluidReplace(BlockPlaceContext ctx, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if(!ctx.getLevel().isClientSide) {
            FluidState fs = ctx.getLevel().getFluidState(ctx.getClickedPos());
            if (fs.isSource()) {
                CompressedPollution.FLUID_RESOLVER.fireEvent(
                        (ServerLevel) ctx.getLevel(), fs.getType(),
                        ctx.getClickedPos(), p -> p.multiply(1_000)
                );
            }
        }
    }
}

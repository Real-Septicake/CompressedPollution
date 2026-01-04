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
            CompressedPollution.ITEM_RESOLVER.fireEvent((ServerLevel) item.level(), item.getItem().getItem(), p -> p.multiply(item.getItem().getCount()));
//            CompressedPollution.handlePollution(
//                    CompressedPollution.pollutionForItem(item.level().registryAccess(), item.getItem(), item.level().getProfiler()).multiply(item.getItem().getCount()),
//                    (ServerLevel) item.level(),
//                    item.getItem().getItem(),
//                    Item.class
//            );
        }
    }

    @Inject(method = "placeBlock", at = @At("HEAD"))
    private void onFluidReplace(BlockPlaceContext ctx, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if(!ctx.getLevel().isClientSide) {
            FluidState fs = ctx.getLevel().getFluidState(ctx.getClickedPos());
            if (fs.isSource()) {
                CompressedPollution.FLUID_RESOLVER.fireEvent((ServerLevel) ctx.getLevel(), fs.getType(), p -> p.multiply(1_000));
//                CompressedPollution.handlePollution(
//                        CompressedPollution.pollutionForFluid(ctx.getLevel().registryAccess(), fs.getType(), ctx.getLevel().getProfiler()).multiply(1_000),
//                        (ServerLevel) ctx.getLevel(),
//                        fs.getType(),
//                        Fluid.class
//                );
            }
        }
    }
}

package io.github.real_septicake.compressed_pollution.mixin;

import io.github.real_septicake.compressed_pollution.CompressedPollution;
import io.github.real_septicake.compressed_pollution.api.PollutionContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemDestructionMixin implements PollutionContainer {

    @Shadow
    public abstract Block getBlock();

    @Inject(method = "onDestroyed", at = @At("HEAD"))
    private void pollutionTime(ItemEntity item, CallbackInfo ci) {
        if(!item.level().isClientSide) {
            if(item.getItem().getItem() instanceof PollutionContainer c)
                c.compressedPollution$handleContents(item.getItem(), (ServerLevel) item.level(), item.getItem().getCount(), item.blockPosition());
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

    @Override
    public void compressedPollution$handleContents(ItemStack self, ServerLevel level, long count, @Nullable BlockPos sourcePos) {
        if(this.getBlock() instanceof PollutionContainer c) {
            c.compressedPollution$handleContents(self, level, count, sourcePos);
        }
    }
}

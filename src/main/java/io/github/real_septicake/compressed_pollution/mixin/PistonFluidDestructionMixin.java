package io.github.real_septicake.compressed_pollution.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.real_septicake.compressed_pollution.CompressedPollution;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBaseBlock.class)
public class PistonFluidDestructionMixin extends Block {
    public PistonFluidDestructionMixin(Properties p_54695_) {
        super(p_54695_);
    }

    @Inject(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V", shift = At.Shift.AFTER))
    private void polluteOnFluidDestroy(Level level, BlockPos pos, Direction dir, boolean extending, CallbackInfoReturnable<Boolean> cir, @Local(name = "blockstate1") BlockState state) {
        if(state.getFluidState().isSource() && !level.isClientSide()) {
            CompressedPollution.FLUID_RESOLVER.fireEvent((ServerLevel) level, state.getFluidState().getType(), p -> p.multiply(1_000));
//            CompressedPollution.handlePollution(
//                    CompressedPollution.pollutionForFluid(level.registryAccess(), state.getFluidState().getType(), level.getProfiler()).multiply(1_000L),
//                    (ServerLevel) level,
//                    state.getFluidState().getType(),
//                    Fluid.class
//            );
        }
    }
}

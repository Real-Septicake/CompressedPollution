package io.github.real_septicake.compressed_pollution.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.FlarestackTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.IPTileEntityBase;
import io.github.real_septicake.compressed_pollution.BuiltInResolvers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlarestackTileEntity.class)
@Pseudo
public abstract class FlareFluidDestructionMixin extends IPTileEntityBase {
    public FlareFluidDestructionMixin(BlockEntityType<?> blockEntityType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(blockEntityType, pWorldPosition, pBlockState);
    }

    @Inject(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fluids/capability/templates/FluidTank;drain(ILnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;", ordinal = 1, shift = At.Shift.AFTER), remap = false)
    private void pollutionTime(CallbackInfo ci, @Local(name = "fs") FluidStack fs) {
        if(this.level != null && !this.level.isClientSide)
            BuiltInResolvers.getFluidResolver().fireEvent(
                    (ServerLevel) this.level, fs.getFluid(),
                    this.worldPosition, p -> p.multiply(fs.getAmount())
            );
    }
}

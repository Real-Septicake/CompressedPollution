package io.github.real_septicake.compressed_pollution.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.real_septicake.compressed_pollution.CompressedPollution;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockFluidDestructionMixin extends Entity {
    public FallingBlockFluidDestructionMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Inject(method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;level()Lnet/minecraft/world/level/Level;"
    ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
                    )
            )
    )
    private void onFluidDestroy(CallbackInfo ci, @Local(name = "blockpos") BlockPos blockpos) {
        if(!level().isClientSide) {
            FluidState fs = level().getFluidState(blockpos);
            if (fs.isSource()) {
                CompressedPollution.FLUID_RESOLVER.fireEvent((ServerLevel) level(), fs.getType(), p -> p.multiply(1_000));
//                CompressedPollution.handlePollution(
//                        CompressedPollution.pollutionForFluid(level().registryAccess(), fs.getType(), level().getProfiler()),
//                        (ServerLevel) level(),
//                        fs.getType(),
//                        Fluid.class
//                );
            }
        }
    }
}

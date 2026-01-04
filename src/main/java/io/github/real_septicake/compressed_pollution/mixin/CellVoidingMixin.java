package io.github.real_septicake.compressed_pollution.mixin;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.me.cells.BasicCellInventory;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.real_septicake.compressed_pollution.CompressedPollution;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BasicCellInventory.class)
@Pseudo
public abstract class CellVoidingMixin {
    @Shadow(remap = false)
    @Final
    private boolean hasVoidUpgrade;

    @Inject(method = "insert", at = @At(value = "RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lappeng/me/cells/BasicCellInventory;innerInsert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;)J")), remap = false)
    private void polluteOnVoid(AEKey what, long amount, Actionable mode, IActionSource source, CallbackInfoReturnable<Long> cir, @Local(name = "inserted") long inserted) {
        if(hasVoidUpgrade && cir.getReturnValue() != inserted && !mode.isSimulate()) {
            long count = amount - inserted;
            ServerLevel level = null;
            if(source.machine().isPresent()) {
                IGridNode host = source.machine().get().getActionableNode();
                if(host != null) // Just in case
                    level = host.getLevel();
            } else if(source.player().isPresent()) {
                Level l = source.player().get().level();
                if(!l.isClientSide) // Prevent attempting to get a client level
                    level = (ServerLevel) l;
            }
            if(level != null) {
                if (what instanceof AEItemKey) {
                    CompressedPollution.ITEM_RESOLVER.fireEvent(
                            level, ((AEItemKey) what).getItem(),
                            null, p -> p.multiply(count)
                    );
                }
                if(what instanceof AEFluidKey) {
                    CompressedPollution.FLUID_RESOLVER.fireEvent(
                            level, ((AEFluidKey) what).getFluid(),
                            null, p -> p.multiply(count)
                    );
                }
            }
        }
    }
}

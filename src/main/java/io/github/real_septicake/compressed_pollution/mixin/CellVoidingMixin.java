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
import io.github.real_septicake.compressed_pollution.compat.ae2.AE2CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.*;
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

    @Unique
    private final static Logger compressedPollution$LOGGER = LoggerFactory.getLogger("CellItemVoiding");

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
                AE2CompatHandler.KeyHandler<AEKey> handler = AE2CompatHandler.instance().getHandler(what.getClass());
                if(handler != null)
                    handler.handle(what, count, level, null);
                else
                    compressedPollution$LOGGER.warn("Unhandled AE2 key type: {}", what.getClass().getSimpleName());
            }
        }
    }
}

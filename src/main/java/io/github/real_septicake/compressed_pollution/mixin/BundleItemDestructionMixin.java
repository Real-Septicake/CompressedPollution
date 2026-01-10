package io.github.real_septicake.compressed_pollution.mixin;

import io.github.real_septicake.compressed_pollution.LongUtil;
import io.github.real_septicake.compressed_pollution.BuiltInResolvers;
import io.github.real_septicake.compressed_pollution.api.PollutionContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

@Mixin(BundleItem.class)
public abstract class BundleItemDestructionMixin implements PollutionContainer {
    @Shadow
    private static Stream<ItemStack> getContents(ItemStack p_150783_) {
        return Stream.empty(); // contents don't matter, shadowed
    }

    @Inject(method = "onDestroyed", at = @At("HEAD"))
    private void pollutionTime(ItemEntity itemEntity, CallbackInfo ci) {
        // NO-OP
        // Items are already dropped by base implementation
        // This mixin is exclusively here to prevent me from thinking it's not already handled
    }

    @Override
    public void compressedPollution$handleContents(ItemStack self, ServerLevel level, long count, @Nullable BlockPos sourcePos) {
        getContents(self).forEach(itemStack -> {
            if(itemStack.getItem() instanceof PollutionContainer c)
                c.compressedPollution$handleContents(itemStack, level, LongUtil.safeMult(count, itemStack.getCount()), sourcePos);
            BuiltInResolvers.getItemResolver().fireEvent(
                    level, itemStack.getItem(), sourcePos
            );
        });
    }
}

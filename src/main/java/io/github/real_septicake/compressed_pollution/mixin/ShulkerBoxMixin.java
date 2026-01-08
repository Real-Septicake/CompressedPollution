package io.github.real_septicake.compressed_pollution.mixin;

import io.github.real_septicake.compressed_pollution.CompressedPollution;
import io.github.real_septicake.compressed_pollution.LongUtil;
import io.github.real_septicake.compressed_pollution.api.DropsOnDestroy;
import io.github.real_septicake.compressed_pollution.api.PollutionContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.stream.Stream;

@Mixin(ShulkerBoxBlock.class)
public class ShulkerBoxMixin implements PollutionContainer, DropsOnDestroy {
    @Override
    public void compressedPollution$handleContents(ItemStack self, ServerLevel level, long count, @Nullable BlockPos sourcePos) {
        CompoundTag src = BlockItem.getBlockEntityData(self);
        if(src != null && src.contains("Items", ListTag.TAG_LIST)) {
            ListTag list = src.getList("Items", CompoundTag.TAG_COMPOUND);
            Stream<ItemStack> contents = list.stream().map(CompoundTag.class::cast).map(ItemStack::of);
            contents.forEach(itemStack -> {
                if(itemStack.getItem() instanceof PollutionContainer c) {
                    c.compressedPollution$handleContents(itemStack, level, LongUtil.safeMult(count, itemStack.getCount()), sourcePos);
                }
                CompressedPollution.ITEM_RESOLVER.fireEvent(
                        level, itemStack.getItem(), sourcePos, p -> p.multiply(LongUtil.safeMult(count, itemStack.getCount()))
                );
            });
        }
    }
}

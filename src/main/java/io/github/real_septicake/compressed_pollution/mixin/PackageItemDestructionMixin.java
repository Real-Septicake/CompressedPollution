package io.github.real_septicake.compressed_pollution.mixin;

import agency.highlysuspect.packages.craftful.item.PackageItem;
import agency.highlysuspect.packages.craftful.junk.PackageContainer;
import io.github.real_septicake.compressed_pollution.CompressedPollution;
import io.github.real_septicake.compressed_pollution.LongUtil;
import io.github.real_septicake.compressed_pollution.api.PollutionContainer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Mixin(PackageItem.class)
@Pseudo
public class PackageItemDestructionMixin extends BlockItem {
    public PackageItemDestructionMixin(Block p_40565_, Properties p_40566_) {
        super(p_40565_, p_40566_);
    }

    @Override
    public void onDestroyed(ItemEntity item) {
        if(!item.level().isClientSide) {
            PackageContainer container = PackageContainer.fromItemStack(item.getItem());
            if (container == null)
                return;
            PackageContainer.TooltipStats stats = container.computeTooltipStats();
            ItemStack stack = stats.rootContents();
            if(stack.getItem() instanceof PollutionContainer c)
                c.compressedPollution$handleContents(
                        stack, (ServerLevel) item.level(),
                        LongUtil.safeMult(stats.fullyMultipliedCount(), item.getItem().getCount()), item.blockPosition()
                );
            CompressedPollution.ITEM_RESOLVER.fireEvent(
                    (ServerLevel) item.level(), stats.rootContents().getItem(),
                    item.blockPosition(), p -> p.multiply(stats.fullyMultipliedCount())
            );
        }
    }
}

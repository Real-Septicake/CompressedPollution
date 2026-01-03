package io.github.real_septicake.compressed_pollution.mixin;

import agency.highlysuspect.packages.craftful.item.PackageItem;
import agency.highlysuspect.packages.craftful.junk.PackageContainer;
import io.github.real_septicake.compressed_pollution.CompressedPollution;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
            CompressedPollution.handlePollution(
                    CompressedPollution.pollutionForItem(item.level().registryAccess(), stats.rootContents(), item.level().getProfiler()).multiply(stats.fullyMultipliedCount()),
                    (ServerLevel) item.level(),
                    item.getItem().getItem(),
                    Item.class
            );
        }
    }
}

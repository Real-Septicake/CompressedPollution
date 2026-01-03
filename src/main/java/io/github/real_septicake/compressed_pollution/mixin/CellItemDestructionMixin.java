package io.github.real_septicake.compressed_pollution.mixin;

import appeng.items.AEBaseItem;
import appeng.items.storage.BasicStorageCell;
import io.github.real_septicake.compressed_pollution.CompressedPollution;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Mixin(BasicStorageCell.class)
@Pseudo
public class CellItemDestructionMixin extends AEBaseItem {
    public CellItemDestructionMixin(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation") // forge isn't real
    public void onDestroyed(ItemEntity item) {
        if(!item.level().isClientSide) {
            ItemStack stack = item.getItem();

            CompoundTag src = stack.getTag();
            if (src == null)
                return;

            long[] amounts = src.getLongArray("amts");
            ListTag keys = src.getList("keys", Tag.TAG_COMPOUND);

            for (int i = 0; i < amounts.length; i++) {
                long amt = amounts[i];
                CompoundTag t = keys.getCompound(i);
                System.out.println(t);
                ResourceLocation loc = ResourceLocation.parse(t.getString("id"));
                String s = t.getString("#c");
                switch (s) {
                    case "ae2:i" -> {
                        Item itemType = ForgeRegistries.ITEMS.getValue(loc);
                        if (itemType == null)
                            continue;
                        CompressedPollution.handlePollution(
                                CompressedPollution.pollutionForItem(item.level().registryAccess(), new ItemStack(itemType), item.level().getProfiler()).multiply(amt),
                                (ServerLevel) item.level(),
                                itemType,
                                Item.class
                        );
                    }
                    case "ae2:f" -> {
                        Fluid fluidType = ForgeRegistries.FLUIDS.getValue(loc);
                        if (fluidType == null)
                            continue;
                        CompressedPollution.handlePollution(
                                CompressedPollution.pollutionForFluid(item.level().registryAccess(), fluidType, item.level().getProfiler()).multiply(amt),
                                (ServerLevel) item.level(),
                                fluidType,
                                Fluid.class
                        );
                    }
                    default -> {}
                }
            }
        }
    }
}

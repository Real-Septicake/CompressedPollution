package io.github.real_septicake.compressed_pollution.mixin;

import io.github.real_septicake.compressed_pollution.CompressedPollution;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Mixin(BackpackItem.class)
@Pseudo
public abstract class BackpackDestructionMixin extends ItemBase {
    public BackpackDestructionMixin(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation") // shut up
    public void onDestroyed(ItemEntity item) {
        if(!item.level().isClientSide) {
            BackpackWrapper wrapper = new BackpackWrapper(item.getItem());
            InventoryHandler handler = wrapper.getInventoryHandler();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    CompressedPollution.ITEM_RESOLVER.fireEvent((ServerLevel) item.level(), stack.getItem(), p -> p.multiply(stack.getCount()));
//                    CompressedPollution.handlePollution(
//                            CompressedPollution.pollutionForItem(item.level().registryAccess(), stack, item.level().getProfiler()).multiply(stack.getCount()),
//                            (ServerLevel) item.level(),
//                            stack.getItem(),
//                            Item.class
//                    );
                }
            }
        }
    }
}

package io.github.real_septicake.compressed_pollution.mixin;

import io.github.real_septicake.compressed_pollution.CompressedPollution;
import io.github.real_septicake.compressed_pollution.api.PollutionContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Mixin(BackpackItem.class)
@Pseudo
public abstract class BackpackDestructionMixin extends ItemBase implements PollutionContainer {
    public BackpackDestructionMixin(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation") // shut up
    public void onDestroyed(ItemEntity item) {
        if(!item.level().isClientSide) {
            this.compressedPollution$handleContents(item.getItem(), (ServerLevel) item.level(), item.getItem().getCount(), item.blockPosition());
        }
    }

    @Override
    public void compressedPollution$handleContents(ItemStack self, ServerLevel level, long count, @Nullable BlockPos sourcePos) {
        InventoryHandler handler = new BackpackWrapper(self).getInventoryHandler();
        for(int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if(!stack.isEmpty()) {
                if(stack.getItem() instanceof PollutionContainer c)
                    c.compressedPollution$handleContents(stack, level, CompressedPollution.safeMult(count, stack.getCount()), sourcePos);
            }
        }
    }
}

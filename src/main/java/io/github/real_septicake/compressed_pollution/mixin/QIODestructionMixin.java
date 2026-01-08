package io.github.real_septicake.compressed_pollution.mixin;

import io.github.real_septicake.compressed_pollution.CompressedPollution;
import io.github.real_septicake.compressed_pollution.LongUtil;
import io.github.real_septicake.compressed_pollution.api.PollutionContainer;
import mekanism.api.NBTConstants;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.item.ItemQIODrive;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.UUID;

@Mixin(ItemQIODrive.class)
@Pseudo
public abstract class QIODestructionMixin extends Item implements IQIODriveItem, PollutionContainer {
    public QIODestructionMixin(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    @SuppressWarnings("deprecation") // Pokemon #0218
    public void onDestroyed(@NotNull ItemEntity item) {
        if(!item.level().isClientSide) {
            compressedPollution$handleContents(item.getItem(), (ServerLevel) item.level(), item.getItem().getCount(), item.blockPosition());
        }
    }

    // Basically copied 1-to-1 from the loadItemMap implementation, because they don't have a normal util function I can use
    @Override
    public void compressedPollution$handleContents(ItemStack self, ServerLevel level, long count, @Nullable BlockPos sourcePos) {
        CompoundTag src = self.getTag();
        if(src == null)
            return;

        CompoundTag mekData = src.getCompound(NBTConstants.MEK_DATA);

        if(mekData.isEmpty())
            return;

        long[] list = mekData.getLongArray(NBTConstants.QIO_ITEM_MAP);

        if(list.length % 3 == 0) {
            for(int i = 0; i < list.length;) {
                UUID uuid = new UUID(list[i++], list[i++]);
                long amt = list[i++];
                HashedItem type = QIOGlobalItemLookup.INSTANCE.getTypeByUUID(uuid);
                if(type != null) {
                    if(type.getItem() instanceof PollutionContainer c) { // fuck
                        c.compressedPollution$handleContents(type.getInternalStack().copy(), level, LongUtil.safeMult(amt, count), sourcePos);
                    }
                    CompressedPollution.ITEM_RESOLVER.fireEvent(
                            level, type.getItem(), sourcePos,
                            p -> p.multiply(LongUtil.safeMult(amt, count))
                    );
                }
            }
        }
    }
}

package io.github.real_septicake.compressed_pollution.mixin;

import appeng.api.stacks.AEKey;
import appeng.items.AEBaseItem;
import appeng.items.storage.BasicStorageCell;
import com.google.common.collect.Streams;
import io.github.real_septicake.compressed_pollution.api.PollutionContainer;
import io.github.real_septicake.compressed_pollution.compat.ae2.AE2CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

import java.util.Arrays;

@Mixin(BasicStorageCell.class)
@Pseudo
public class CellItemDestructionMixin extends AEBaseItem implements PollutionContainer {
    public CellItemDestructionMixin(Properties properties) {
        super(properties);
    }

    @Unique
    private final static Logger compressedPollution$LOGGER = LoggerFactory.getLogger("CellItemDestruction");

    @Override
    @SuppressWarnings("deprecation") // forge isn't real
    public void onDestroyed(ItemEntity item) {
        if(!item.level().isClientSide) {
            ItemStack stack = item.getItem();
            compressedPollution$handleContents(stack, (ServerLevel) item.level(), stack.getCount(), item.blockPosition());
        }
    }

    @Override
    public void compressedPollution$handleContents(ItemStack self, ServerLevel level, long count, @Nullable BlockPos sourcePos) {
        CompoundTag src = self.getTag();
        if(src == null)
            return;

        long[] amounts = src.getLongArray("amts");
        ListTag keys = src.getList("keys", Tag.TAG_COMPOUND);

        Streams.forEachPair(
                Arrays.stream(amounts).boxed(),
                keys.stream().map(CompoundTag.class::cast),
                (amt, tag) -> {
                    AEKey key = AEKey.fromTagGeneric(tag);
                    if(key != null) {
                        AE2CompatHandler.KeyHandler<AEKey> handler = AE2CompatHandler.instance().getHandler(key.getClass());
                        if (handler != null)
                            handler.handle(key, amt * count, level, sourcePos);
                        else
                            compressedPollution$LOGGER.warn("Unhandled AE2 key type: {}", key.getClass().getSimpleName());
                    }
                }
        );

//        for (int i = 0; i < amounts.length; i++) {
//            long amt = amounts[i];
//            CompoundTag t = keys.getCompound(i);
//
//        }
    }
}

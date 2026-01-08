package io.github.real_septicake.compressed_pollution.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Marks an item as having an internal inventory that need to be taken into account
 * <p>
 * Please only use this on {@link net.minecraft.world.item.Item} and {@link net.minecraft.world.level.block.Block} subclasses,
 * I can't use <code>sealed</code> so I'm just gonna have to trust y'all on this
 */
public interface PollutionContainer {
    /**
     * The method that gets called when handling the contents of this item
     * @param self An item stack representing the item to be handled
     * @param level The level to apply pollution to
     * @param count The number of this <code>Container</code> present
     * @param sourcePos The position the pollution is caused from, or null if not applicable
     */
    void compressedPollution$handleContents(ItemStack self, ServerLevel level, long count, @Nullable BlockPos sourcePos);
}

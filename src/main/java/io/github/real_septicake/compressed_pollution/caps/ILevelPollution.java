package io.github.real_septicake.compressed_pollution.caps;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

public interface ILevelPollution extends INBTSerializable<CompoundTag> {
    @Nonnull LevelPollution getPollution();
    void setPollution(@Nonnull LevelPollution pollution);
}

package io.github.real_septicake.compressed_pollution.caps;

import io.github.real_septicake.compressed_pollution.LevelPollution;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class LevelPollutionCapability implements ILevelPollution {
    @Nonnull
    LevelPollution pollution = new LevelPollution();

    @Override
    public @NotNull LevelPollution getPollution() {
        return pollution;
    }

    @Override
    public void setPollution(@NotNull LevelPollution pollution) {
        this.pollution = pollution;
    }

    @Override
    public CompoundTag serializeNBT() {
        return (CompoundTag) LevelPollution.CODEC.encodeStart(NbtOps.INSTANCE, pollution).result().orElse(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.pollution = LevelPollution.CODEC.parse(NbtOps.INSTANCE, nbt).result().orElse(new LevelPollution());
    }
}

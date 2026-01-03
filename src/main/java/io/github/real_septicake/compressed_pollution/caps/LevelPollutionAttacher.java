package io.github.real_septicake.compressed_pollution.caps;

import io.github.real_septicake.compressed_pollution.CompressedPollution;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LevelPollutionAttacher {
    public static final ResourceLocation ID = CompressedPollution.id("pollution");
    public static class LevelPollutionProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final ILevelPollution backing = new LevelPollutionCapability();
        private final LazyOptional<ILevelPollution> optional = LazyOptional.of(() -> backing);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return CompressedPollution.LEVEL_POLLUTION_CAPABILITY.orEmpty(cap, this.optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            return backing.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            backing.deserializeNBT(nbt);
        }
    }
}

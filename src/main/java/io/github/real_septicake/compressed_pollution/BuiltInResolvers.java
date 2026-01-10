package io.github.real_septicake.compressed_pollution;

import io.github.real_septicake.compressed_pollution.api.TaggedPollutionRegistryResolver;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class BuiltInResolvers {
    private static TaggedPollutionRegistryResolver<Item> itemResolver = null;
    private static TaggedPollutionRegistryResolver<Fluid> fluidResolver = null;

    public static void init(DataPackRegistryEvent.NewRegistry evt) {
        itemResolver = TaggedPollutionRegistryResolver.create(
                5L, Item.class, CompressedPollution.id("pollutions/item"),
                evt, ForgeRegistries.ITEMS.getRegistryKey(), ForgeRegistries.ITEMS::getKey,
                (item, itemTagKey) -> new ItemStack(item).is(itemTagKey)
        );

        fluidResolver = TaggedPollutionRegistryResolver.create(
                5L, Fluid.class, CompressedPollution.id("pollutions/fluid"),
                evt, ForgeRegistries.FLUIDS.getRegistryKey(), ForgeRegistries.FLUIDS::getKey,
                Fluid::is
        );
    }

    /**
     * @return The built-in <code>Item</code> registry resolver
     */
    public static TaggedPollutionRegistryResolver<Item> getItemResolver() {
        return itemResolver;
    }

    /**
     * @return The built-in <code>Fluid</code> registry resolver
     */
    public static TaggedPollutionRegistryResolver<Fluid> getFluidResolver() {
        return fluidResolver;
    }
}

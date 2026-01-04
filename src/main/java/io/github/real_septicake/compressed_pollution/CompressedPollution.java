package io.github.real_septicake.compressed_pollution;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.real_septicake.compressed_pollution.api.PollutionRegistryResolver;
import io.github.real_septicake.compressed_pollution.caps.ILevelPollution;
import io.github.real_septicake.compressed_pollution.caps.LevelPollution;
import io.github.real_septicake.compressed_pollution.caps.LevelPollutionAttacher;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Duration;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CompressedPollution.MODID)
public class CompressedPollution
{
    /**
     * Capability token for {@link LevelPollution} capability
     */
    public static final Capability<ILevelPollution> LEVEL_POLLUTION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    /**
     * A pollution value cannot go above this value, or below its negative
     */
    public static final long POLLUTION_VALUE_CAP = 9_200_000_000_000_000_000L;

    private static final Logger LOGGER = LoggerFactory.getLogger("CompressedPollution");

    // Define mod id in a common place for everything to reference
    /**
     * Mod ID
     */
    public static final String MODID = "compressed_pollution";

    /** Registry key for Item pollution values */
    public static final ResourceKey<Registry<PollutionEntry<Item>>> POLLUTION_ITEM_REGISTRY_KEY = ResourceKey.createRegistryKey(id("pollutions/item"));
    /** Registry key for Fluid pollution values */
    public static final ResourceKey<Registry<PollutionEntry<Fluid>>> POLLUTION_FLUID_REGISTRY_KEY = ResourceKey.createRegistryKey(id("pollutions/fluid"));

    /**
     * The resolver for {@link Item}s
     */
    public static final PollutionRegistryResolver<Item> ITEM_RESOLVER = new PollutionRegistryResolver<>(
            5L,
            Item.class,
            POLLUTION_ITEM_REGISTRY_KEY
    ) {
        @Override
        public ResourceLocation toRL(Item obj) {
            return ForgeRegistries.ITEMS.getKey(obj.asItem());
        }

        @Override
        public boolean isTag(Item obj, TagKey<Item> tag) {
            return new ItemStack(obj).is(tag);
        }
    };

    /**
     * The resolver for {@link Fluid}s
     */
    public static final PollutionRegistryResolver<Fluid> FLUID_RESOLVER = new PollutionRegistryResolver<>(
            5L,
            Fluid.class,
            POLLUTION_FLUID_REGISTRY_KEY
    ) {
        @Override
        public ResourceLocation toRL(Fluid obj) {
            return ForgeRegistries.FLUIDS.getKey(obj);
        }

        @Override
        public boolean isTag(Fluid obj, TagKey<Fluid> tag) {
            return obj.is(tag);
        }
    };

    /**
     * Creates a ResourceLocation with {@link CompressedPollution#MODID} as the namespace
     * @param path The path for the ResourceLocation
     * @return The ResourceLocation
     */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public CompressedPollution(FMLJavaModLoadingContext context)
    {
        context.getModEventBus().addListener(CompressedPollution::dataGen);
        context.getModEventBus().addListener((DataPackRegistryEvent.NewRegistry evt) -> {
            evt.dataPackRegistry(POLLUTION_ITEM_REGISTRY_KEY, PollutionEntry.codec(ForgeRegistries.ITEMS.getRegistryKey()));
            evt.dataPackRegistry(POLLUTION_FLUID_REGISTRY_KEY, PollutionEntry.codec(ForgeRegistries.FLUIDS.getRegistryKey()));
        });

        MinecraftForge.EVENT_BUS.addGenericListener(Level.class, (AttachCapabilitiesEvent<Level> evt) -> {
            evt.addCapability(
                    LevelPollutionAttacher.ID,
                    new LevelPollutionAttacher.LevelPollutionProvider()
            );
        });
        MinecraftForge.EVENT_BUS.addGenericListener(Fluid.class, (PollutionEvent<Fluid> evt) -> {
            System.out.println(evt.getObj().getFluidType());
            if(evt.getObj().isSame(Fluids.LAVA))
                evt.setCanceled(true);
        });
    }

    public static final ResourceKey<PollutionEntry<Item>> CARBON_MONOXIDE_POLLUTION = ResourceKey.create(
            POLLUTION_ITEM_REGISTRY_KEY,
            id("carbon_monoxide")
    );

    public static final ResourceKey<PollutionEntry<Fluid>> CARBON_DIOXIDE_POLLUTION = ResourceKey.create(
            POLLUTION_FLUID_REGISTRY_KEY,
            id("carbon_dioxide")
    );

    @SubscribeEvent
    public static void dataGen(GatherDataEvent evt) {
        DataGenerator generator = evt.getGenerator();
        final var packOutput = generator.getPackOutput();

        PollutionEntry<Item> pb = new PollutionEntry.Builder<Item>()
                .putValue(ResourceLocation.withDefaultNamespace("dirt"), 10L)
                .putValue(ResourceLocation.withDefaultNamespace("cherry_log"), 5L)
                .putValue(ResourceLocation.withDefaultNamespace("birch_log"), 0L)
                .putTag(ForgeRegistries.ITEMS.tags().createTagKey(ResourceLocation.withDefaultNamespace("logs")), 50)
                .build();

        generator.addProvider(evt.includeServer(), new DatapackBuiltinEntriesProvider(
                packOutput,
                evt.getLookupProvider(),
                new RegistrySetBuilder().add(
                        POLLUTION_ITEM_REGISTRY_KEY,
                        bootstrap -> {
                            System.out.println("DATAGEN");
                            bootstrap.register(
                                    CARBON_MONOXIDE_POLLUTION,
                                    pb
                            );
                        }
                ).add(
                        POLLUTION_FLUID_REGISTRY_KEY,
                        bootstrap -> {
                            bootstrap.register(
                                    CARBON_DIOXIDE_POLLUTION,
                                    new PollutionEntry.Builder<Fluid>().putValue(
                                            ResourceLocation.fromNamespaceAndPath("immersivepetroleum", "gasoline"),
                                            1
                                    ).putTag(
                                            ForgeRegistries.FLUIDS.tags().createTagKey(ResourceLocation.fromNamespaceAndPath("immersivepetroleum", "burnable_in_flarestack")),
                                            15L
                                    ).build()
                            );
                        }),
                null
        ));
    }

    /**
     * Method for applying a {@link Pollution} to <code>level</code>'s {@link LevelPollution}, fires the
     * {@link PollutionEvent} event
     * @param pollution The pollution to apply. <b>This can be modified by event handlers.</b>
     *                  Use {@link Pollution#copy()} if it should not directly modify the instance passed in
     * @param level The level to apply the pollution to
     * @param obj The object causing the pollution
     * @param clazz The class to post the PollutionEvent to
     * @param <T> The class to post the PollutionEvent to
     */
    public static <T> void handlePollution(@Nonnull Pollution pollution, @Nonnull ServerLevel level, T obj, Class<T> clazz) {
        level.getProfiler().push("PollutionApplication");
        if(!MinecraftForge.EVENT_BUS.post(new PollutionEvent<>(clazz, pollution, obj, level)) && !pollution.isEmpty()) {
                LOGGER.debug("Pollution Applied {}", pollution);
                LevelPollution.getFromLevel(level).apply(pollution);
        }
        level.getProfiler().pop();
    }
}

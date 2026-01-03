package io.github.real_septicake.compressed_pollution;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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

    private static final Cache<ResourceLocation, Pollution> ITEM_CACHE = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(5)).build();

    /**
     * Gets the pollution values for the specified item
     * @param access Registry access for the <b>server</b>, providing client registry access <i><b>will error.</b></i>
     * @param item Item to get the pollution values of
     * @return The pollution base values for the specified item
     */
    public static Pollution pollutionForItem(@Nonnull RegistryAccess access, @Nonnull ItemStack item, @Nonnull ProfilerFiller profiler) {
        profiler.push("PollutionItem");
        ResourceLocation loc = ForgeRegistries.ITEMS.getKey(item.getItem());
        if(loc == null) {
            profiler.pop();
            return Pollution.PollutionBuilder.EMPTY;
        }
        Pollution cached = ITEM_CACHE.getIfPresent(loc);
        if(cached != null) {
            profiler.pop();
            return cached.copy();
        }
        Pollution.PollutionBuilder builder = new Pollution.PollutionBuilder();
        access.registryOrThrow(POLLUTION_ITEM_REGISTRY_KEY).entrySet().forEach(
                entry -> {
                    Long value = entry.getValue().values().getOrDefault(loc, null);
                    if(value == null) {
                        profiler.push("PollutionItemTag");
                        PollutionEntry.PollutionTag<Item> tag = null;
                        for(PollutionEntry.PollutionTag<Item> t : entry.getValue().tags()) {
                            if(item.is(t.tag())) {
                                tag = t;
                                break;
                            }
                        }
                        if(tag != null)
                            value = tag.value();
                        profiler.pop();
                    }
                    if(value == null)
                        value = 0L;
                    builder.put(entry.getKey().location().toString(), value);
                }
        );
        profiler.pop();
        Pollution created = builder.build();
        ITEM_CACHE.put(loc, created);
        return created.copy();
    }

    private static final Cache<ResourceLocation, Pollution> FLUID_CACHE = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofMinutes(5)).build();

    /**
     * Gets the pollution values for the specified fluid
     * @param access Registry access for the <b>server</b>, providing client registry access <i><b>will error.</b></i>
     * @param fluid The fluid to get the pollution values of
     * @return The pollution base values for the specified item
     */
    public static Pollution pollutionForFluid(@Nonnull RegistryAccess access, @Nonnull Fluid fluid, @Nonnull ProfilerFiller profiler) {
        profiler.push("PollutionFluid");
        ResourceLocation loc = ForgeRegistries.FLUIDS.getKey(fluid);
        if(loc == null) {
            profiler.pop();
            return Pollution.PollutionBuilder.EMPTY.copy();
        }
        Pollution cached = FLUID_CACHE.getIfPresent(loc);
        if(cached != null) {
            profiler.pop();
            return cached.copy();
        }
        Pollution.PollutionBuilder builder = new Pollution.PollutionBuilder();
        access.registryOrThrow(POLLUTION_FLUID_REGISTRY_KEY).entrySet().forEach(
                entry -> {
                    Long value = entry.getValue().values().getOrDefault(loc, null);
                    if(value == null) {
                        profiler.push("PollutionFluidTag");
                        PollutionEntry.PollutionTag<Fluid> tag = null;
                        for(PollutionEntry.PollutionTag<Fluid> t : entry.getValue().tags()) {
                            if(fluid.is(t.tag())) {
                                tag = t;
                                break;
                            }
                        }
                        if(tag != null)
                            value = tag.value();
                        profiler.pop();
                    }
                    if(value == null)
                        value = 0L;
                    builder.put(entry.getKey().location().toString(), value);
                }
        );
        profiler.pop();
        Pollution created = builder.build();
        FLUID_CACHE.put(loc, created);
        return created.copy();
    }

    /**
     * Method for applying a {@link Pollution} to <code>level</code>'s {@link LevelPollution}, fires the
     * {@link PollutionEvent} event
     * @param pollution The pollution to apply
     * @param level The level to apply the pollution to
     * @param obj The object causing the pollution
     * @param clazz The class to post the PollutionEvent to
     * @param <T> The class to post the PollutionEvent to
     */
    public static <T> void handlePollution(@Nonnull Pollution pollution, @Nonnull ServerLevel level, T obj, Class<T> clazz) {
        level.getProfiler().push("PollutionApplication");
        if(!MinecraftForge.EVENT_BUS.post(new PollutionEvent<>(clazz, pollution, obj, level))) {
            LOGGER.debug("Pollution created {}", pollution);
            LevelPollution.getFromLevel(level).apply(pollution);
        }
        level.getProfiler().pop();
    }
}

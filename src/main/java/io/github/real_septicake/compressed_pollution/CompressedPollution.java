package io.github.real_septicake.compressed_pollution;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import io.github.real_septicake.compressed_pollution.api.PollutionContainer;
import io.github.real_septicake.compressed_pollution.caps.ILevelPollution;
import io.github.real_septicake.compressed_pollution.caps.LevelPollutionAttacher;
import io.github.real_septicake.compressed_pollution.compat.ae2.AE2CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CompressedPollution.MODID)
public class CompressedPollution
{
    /**
     * Capability token for {@link LevelPollution} capability
     */
    public static final Capability<ILevelPollution> LEVEL_POLLUTION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    /**
     * Logger
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("CompressedPollution");

    /**
     * Mod ID
     */
    public static final String MODID = "compressed_pollution";

    private static final EventBatcher BATCHER = new EventBatcher();

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
        context.getModEventBus().addListener(BuiltInResolvers::init); // create the resolvers
        MinecraftForge.EVENT_BUS.addGenericListener(Level.class, (AttachCapabilitiesEvent<Level> evt) -> {
            evt.addCapability(
                    LevelPollutionAttacher.ID,
                    new LevelPollutionAttacher.LevelPollutionProvider()
            );
        });

        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent evt) -> {
            if(evt.phase == TickEvent.Phase.END)
                BATCHER.dispatch(Optional.of(evt.getServer().getProfiler()));
        });

        if(ModList.get().isLoaded("ae2")) {
            try {
                AE2CompatHandler.INSTANCE.addHandler(
                        AEItemKey.class,
                        (key, amount, level, sourcePos) -> {
                            if(key.getItem() instanceof PollutionContainer c) {
                                c.compressedPollution$handleContents(key.toStack(), level, amount, sourcePos);
                            }
                            BuiltInResolvers.getItemResolver().fireEvent(
                                    level, key.getItem(), sourcePos, p -> p.multiply(amount)
                            );
                        },
                        MODID
                );
                AE2CompatHandler.INSTANCE.addHandler(
                        AEFluidKey.class,
                        (key, amount, level, sourcePos) -> {
                            BuiltInResolvers.getFluidResolver().fireEvent(
                                    level, key.getFluid(), sourcePos, p -> p.multiply(amount)
                            );
                        },
                        MODID
                );
            } catch (AE2CompatHandler.AlreadyPresentException ignored) {} // should never happen for any reason
        }
    }

    /**
     * Method for applying a {@link Pollution} to <code>level</code>'s {@link LevelPollution}, passes <code>pollution</code>
     * to the batcher for merging if a duplicate is present. Events are fired at the end of the tick by the batcher
     * @param pollution The pollution to apply. <b>This can be modified by event handlers.</b>
     *                  Use {@link Pollution#copy()} if it should not directly modify the instance passed in
     * @param level The level to apply the pollution to
     * @param obj The object causing the pollution
     * @param clazz The class to post the PollutionEvent to
     * @param sourcePos The position of the object causing the pollution, or null if no appropriate position exists
     * @param <T> The class to post the PollutionEvent to
     */
    public static <T> void handlePollution(@Nonnull Pollution pollution, @Nonnull ServerLevel level, T obj, Class<T> clazz, @Nullable BlockPos sourcePos) {
        BATCHER.add(level, clazz, obj, sourcePos, pollution);
    }
}

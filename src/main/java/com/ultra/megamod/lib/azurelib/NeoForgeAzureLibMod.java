package com.ultra.megamod.lib.azurelib;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.ultra.megamod.lib.azurelib.common.animation.cache.AzBakedAnimationCache;
import com.ultra.megamod.lib.azurelib.common.model.cache.AzBakedModelCache;
import com.ultra.megamod.lib.azurelib.common.network.packet.AzItemStackDispatchCommandPacket;

/**
 * AzureLib setup class for NeoForge. Not a separate mod - called from MegaMod's entry point.
 * Call {@link #register(IEventBus)} from your mod constructor to initialize AzureLib.
 */
public final class NeoForgeAzureLibMod {

    @SuppressWarnings("unchecked")
    public static final DeferredRegister.DataComponents DATA_COMPONENTS_REGISTER = DeferredRegister
        .createDataComponents(
            net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE,
            AzureLib.MOD_ID
        );

    private NeoForgeAzureLibMod() {
        throw new UnsupportedOperationException();
    }

    /**
     * Register AzureLib with the given mod event bus. Call this from your mod constructor.
     */
    public static void register(IEventBus modEventBus) {
        AzureLib.initialize();
        DATA_COMPONENTS_REGISTER.register(modEventBus);
        modEventBus.addListener(NeoForgeAzureLibMod::registerMessages);

        // Register geo+animation reload listeners on the client. The legacy AzureLibCache
        // approach calls Minecraft.getInstance() which returns null during mod construction,
        // so we register via NeoForge's AddClientReloadListenersEvent instead.
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modEventBus.addListener(NeoForgeAzureLibMod::onAddClientReloadListeners);
        }
    }

    private static void onAddClientReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(
            net.minecraft.resources.Identifier.fromNamespaceAndPath(AzureLib.MOD_ID, "geo_models"),
            new net.minecraft.server.packs.resources.SimplePreparableReloadListener<Void>() {
                @Override
                protected Void prepare(net.minecraft.server.packs.resources.ResourceManager rm,
                                       net.minecraft.util.profiling.ProfilerFiller profiler) {
                    return null;
                }
                @Override
                protected void apply(Void nothing,
                                     net.minecraft.server.packs.resources.ResourceManager rm,
                                     net.minecraft.util.profiling.ProfilerFiller profiler) {
                    try {
                        AzBakedAnimationCache.getInstance().loadAnimations(Runnable::run, rm).join();
                        AzBakedModelCache.getInstance().loadModels(Runnable::run, rm).join();
                        com.ultra.megamod.MegaMod.LOGGER.info("[AzureLib] Loaded geo models + animations from resource packs");
                    } catch (Exception e) {
                        com.ultra.megamod.MegaMod.LOGGER.error("[AzureLib] Failed to reload caches", e);
                    }
                }
            }
        );
    }

    public static void registerMessages(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(AzureLib.MOD_ID);
        // Register as playToClient — animation dispatch commands are sent server→client
        registrar.playToClient(
            AzItemStackDispatchCommandPacket.TYPE,
            AzItemStackDispatchCommandPacket.CODEC,
            (msg, ctx) -> msg.handle()
        );
    }
}

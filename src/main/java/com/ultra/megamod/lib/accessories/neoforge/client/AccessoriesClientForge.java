package com.ultra.megamod.lib.accessories.neoforge.client;

import com.ultra.megamod.lib.accessories.Accessories;
import com.ultra.megamod.lib.accessories.client.AccessoriesClient;
import com.ultra.megamod.lib.accessories.client.AccessoriesPipelines;
import com.ultra.megamod.lib.accessories.client.AccessoriesRenderLayer;
import com.ultra.megamod.lib.accessories.data.api.SyncedDataHelperManager;
import com.ultra.megamod.lib.accessories.impl.event.AccessoriesEventHandler;
import com.ultra.megamod.lib.accessories.menu.AccessoriesMenuTypes;
import com.ultra.megamod.lib.accessories.neoforge.AccessoriesNeoforgeInternals;
import com.ultra.megamod.lib.accessories.networking.AccessoriesNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.ClientTooltipFlag;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Stream;

public class AccessoriesClientForge {

    public static void init(final IEventBus eventBus) {
        new AccessoriesClientForge(eventBus);
    }

    private AccessoriesClientForge(final IEventBus eventBus) {
        // Populate the network channel's client-side handler registrations BEFORE
        // RegisterPayloadHandlersEvent fires on the mod bus. initClient() only appends
        // to internal lists; the server-side AccessoriesForge listener does the actual
        // NeoForge payload binding for both sides.
        AccessoriesNetworking.initClient();

        eventBus.addListener(this::registerMenuType);
        eventBus.addListener(this::onInitializeClient);
        eventBus.addListener(this::initKeybindings);
        eventBus.addListener(this::addRenderLayer);
        eventBus.addListener(this::registerReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::onJoin);
        eventBus.<RegisterRenderPipelinesEvent>addListener(event -> AccessoriesPipelines.registerPipelines(event::registerPipeline));

        AccessoriesClient.initConfigStuff();
    }

    public void registerReloadListeners(AddClientReloadListenersEvent event){
        var loaders = AccessoriesNeoforgeInternals.TO_BE_LOADED.getOrDefault(PackType.CLIENT_RESOURCES, new LinkedHashSet<>());

        loaders.forEach((endecDataLoader) -> event.addListener(endecDataLoader.getId(), endecDataLoader));

        loaders.forEach((endecDataLoader) -> {
            for (var dependencyId : endecDataLoader.getDependencyIds()) {
                event.addDependency(dependencyId, endecDataLoader.getId());
            }
        });
    }

    public void registerMenuType(RegisterMenuScreensEvent event) {
        AccessoriesMenuTypes.registerClientMenuConstructors(event::register);
    }

    public void onJoin(ClientPlayerNetworkEvent.LoggingIn loggingInEvent) {
        AccessoriesClient.initalConfigDataSync();
    }

    public void onInitializeClient(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(AccessoriesClientForge::clientTick);
        NeoForge.EVENT_BUS.addListener(AccessoriesClientForge::itemTooltipCallback);

//        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> {
//            return (minecraft, parent) -> AutoConfig.getConfigScreen(AccessoriesConfig.class, parent).get();
//        });

        AccessoriesClient.init();

        // AccessoriesNetworking.initClient() moved to the constructor so its
        // registrations are present before RegisterPayloadHandlersEvent fires.
        SyncedDataHelperManager.initClient(AccessoriesNetworking.CHANNEL);
    }

    public void initKeybindings(RegisterKeyMappingsEvent event) {
        event.registerCategory(AccessoriesClient.KEY_CATEGORY);
        event.register(AccessoriesClient.OPEN_SCREEN);
        event.register(AccessoriesClient.OPEN_OTHERS_SCREEN);
    }

    public static void clientTick(ClientTickEvent.Pre event) {
        AccessoriesClient.handleKeyMappings(Minecraft.getInstance());
    }

    public static void itemTooltipCallback(ItemTooltipEvent event) {
        var player = event.getEntity();
        var stackTooltip = event.getToolTip();
        var stack = event.getItemStack();
        var tooltipDisplay = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);

        var tooltipData = new ArrayList<Component>();

        AccessoriesEventHandler.getTooltipData(player, stack, tooltipData, tooltipDisplay,  event.getContext(), event.getFlags());

        if (!tooltipData.isEmpty()) stackTooltip.addAll(1, tooltipData);
    }

    public void addRenderLayer(EntityRenderersEvent.AddLayers event) {
        for (var entityType : event.getEntityTypes()) {
            try {
                var renderer = event.getRenderer(entityType);

                if (renderer instanceof LivingEntityRenderer<? extends LivingEntity, ? extends LivingEntityRenderState, ?> livingEntityRenderer && livingEntityRenderer.getModel() instanceof HumanoidModel) {
                    livingEntityRenderer.addLayer(new AccessoriesRenderLayer(livingEntityRenderer));
                }
            } catch (ClassCastException ignore) {}
        }

        event.getSkins().stream()
            .flatMap(type -> Stream.<AvatarRenderer<?>>of(event.getPlayerRenderer(type), event.getMannequinRenderer(type)))
            .filter(Objects::nonNull)
            .forEach(renderer -> renderer.addLayer(new AccessoriesRenderLayer<>(renderer)));
    }
}

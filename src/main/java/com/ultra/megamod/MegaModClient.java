/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.IExtensionPoint
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.common.Mod
 *  net.neoforged.neoforge.client.gui.ConfigurationScreen
 *  net.neoforged.neoforge.client.gui.IConfigScreenFactory
 */
package com.ultra.megamod;

import com.ultra.megamod.feature.combat.animation.client.SwingAnimationState;
import com.ultra.megamod.feature.ambientsounds.AmbientSoundsFeature;
import com.ultra.megamod.feature.clocks.ReadableClocks;
import com.ultra.megamod.feature.casino.CasinoRegistry;
import com.ultra.megamod.feature.casino.blackjack.client.BlackjackTableRenderer;
import com.ultra.megamod.feature.casino.wheel.client.WheelRenderer;
import com.ultra.megamod.feature.combat.spell.CombatEntityRegistry;
import com.ultra.megamod.feature.combat.spell.client.SpellCloudRenderer;
import com.ultra.megamod.feature.combat.spell.client.SpellProjectileRenderer;
import com.ultra.megamod.feature.dimensions.DimensionRegistry;
import com.ultra.megamod.feature.dimensions.client.PortalBlockRenderer;
import com.ultra.megamod.feature.mobhealth.MobHealthDisplay;
import com.ultra.megamod.feature.museum.MuseumRegistry;
import com.ultra.megamod.feature.museum.client.MuseumDoorRenderer;
import com.ultra.megamod.feature.relics.client.AbilityHudOverlay;
import com.ultra.megamod.feature.relics.client.AbilityKeybind;
import com.ultra.megamod.feature.relics.client.AccessoryKeybind;
import com.ultra.megamod.feature.skills.client.SkillHudOverlay;
import com.ultra.megamod.feature.skills.client.SkillTreeKeybind;
import com.ultra.megamod.feature.attributes.network.CombatTextRenderer;
import com.ultra.megamod.feature.baritone.screen.BotPathRenderHandler;
import com.ultra.megamod.feature.citizen.client.CitizenHudOverlay;
import com.ultra.megamod.feature.economy.client.PlayerInfoHudOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value="megamod", dist={Dist.CLIENT})
public class MegaModClient {
    public MegaModClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (IConfigScreenFactory) ConfigurationScreen::new);
        AmbientSoundsFeature.init(modEventBus);
        com.ultra.megamod.feature.shouldersurfing.ShoulderSurfingClientEvents.init(modEventBus, container);
        com.ultra.megamod.lib.accessories.neoforge.client.AccessoriesClientForge.init(modEventBus);

        // Register PlayerAnimationLib spell animation layers (per-player event)
        com.ultra.megamod.feature.combat.animation.client.SpellAnimationManager.registerFactories();

        // === HUD Layer Registration ===
        // Registered in visual priority order (last registered renders on top)
        // Group 1: Background effects (vignettes, transitions)
        // Group 2: World-space overlays (mob health, compass)
        // Group 3: Game info (status effects, quest tracker, crop maturity)
        // Group 4: Combat HUD (abilities, combos, kill combos, arena waves)
        // Group 5: Social/party (party health, citizen HUD)
        // Group 6: Economy/info (player info, loot log, death recap)
        // Group 7: Debug/admin (bot paths)

        // Group 1: Background effects
        modEventBus.addListener(com.ultra.megamod.feature.hud.LowHealthVignette::onRegisterGuiLayers);
        modEventBus.addListener(com.ultra.megamod.feature.dimensions.client.DimensionTransitionOverlay::onRegisterGuiLayers);

        // Group 2: World-space overlays
        modEventBus.addListener(MobHealthDisplay::onRegisterGuiLayers);
        modEventBus.addListener(com.ultra.megamod.feature.hud.CompassDisplay::onRegisterGuiLayers);
        modEventBus.addListener(ReadableClocks::onRegisterGuiLayers);

        // Group 3: Game info
        modEventBus.addListener(com.ultra.megamod.feature.hud.StatusEffectBar::onRegisterGuiLayers);
        modEventBus.addListener(com.ultra.megamod.feature.hud.QuestTrackerDisplay::onRegisterGuiLayers);
        modEventBus.addListener(com.ultra.megamod.feature.hud.CropMaturityDisplay::onRegisterGuiLayers);
        modEventBus.addListener(SkillHudOverlay::onRegisterGuiLayers);

        // Group 4: Combat HUD
        modEventBus.addListener(AbilityHudOverlay::onRegisterGuiLayers);
        modEventBus.addListener(CombatTextRenderer::onRegisterGuiLayers);
        modEventBus.addListener(com.ultra.megamod.feature.combat.spell.SpellCastOverlay::onRegisterGuiLayers);
        modEventBus.addListener(com.ultra.megamod.feature.combat.spell.client.SpellBookHudOverlay::onRegisterGuiLayers);
        modEventBus.addListener(com.ultra.megamod.feature.hud.combos.CombatComboDisplay::onRegisterGuiLayers);
        modEventBus.addListener(com.ultra.megamod.feature.hud.KillComboDisplay::onRegisterGuiLayers);
        modEventBus.addListener(com.ultra.megamod.feature.hud.AbilityTriggerHud::onRegisterGuiLayers);
        modEventBus.addListener(com.ultra.megamod.feature.arena.client.ArenaWaveHud::onRegisterGuiLayers);
        modEventBus.addListener(com.ultra.megamod.feature.combat.animation.client.InGameHudOverlay::onRegisterGuiLayers);

        // Group 5: Social/party
        modEventBus.addListener(com.ultra.megamod.feature.hud.PartyHealthDisplay::onRegisterGuiLayers);
        modEventBus.addListener(CitizenHudOverlay::onRegisterGuiLayers);

        // Group 6: Economy/info
        modEventBus.addListener(PlayerInfoHudOverlay::onRegisterGuiLayers);
        modEventBus.addListener(com.ultra.megamod.feature.hud.LootPickupLog::onRegisterGuiLayers);
        modEventBus.addListener(com.ultra.megamod.feature.hud.DeathRecapOverlay::onRegisterGuiLayers);

        // Group 7: Debug/admin
        modEventBus.addListener(BotPathRenderHandler::onRegisterGuiLayers);

        // Group 8: Schematic system
        modEventBus.addListener(com.ultra.megamod.feature.schematic.client.SchematicHudOverlay::onRegisterGuiLayers);

        // Group 9: Minimap — disabled, map is computer-app only
        // modEventBus.addListener(com.ultra.megamod.feature.computer.screen.map.MapMinimapHud::onRegisterGuiLayers);

        // Keybind registrations
        modEventBus.addListener(AccessoryKeybind::onRegisterKeyMappings);
        modEventBus.addListener(AbilityKeybind::onRegisterKeyMappings);
        modEventBus.addListener(SkillTreeKeybind::onRegisterKeyMappings);
        modEventBus.addListener(com.ultra.megamod.feature.casino.CasinoClientEvents::onRegisterKeyMappings);
        // PortableCraft keybind NOT registered here — uses direct key detection to stay hidden from non-admins
        modEventBus.addListener(com.ultra.megamod.feature.sorting.client.SortKeybind::onRegisterKeyMappings);
        modEventBus.addListener(com.ultra.megamod.feature.backpacks.client.BackpackKeybind::onRegisterKeyMappings);
        modEventBus.addListener(com.ultra.megamod.feature.schematic.client.SchematicKeybind::onRegisterKeyMappings);
        modEventBus.addListener(com.ultra.megamod.feature.combat.animation.client.CombatKeybindings::onRegisterKeyMappings);
        modEventBus.addListener(com.ultra.megamod.lib.combatroll.client.CombatRollClientInit::registerKeys);
        modEventBus.addListener(com.ultra.megamod.lib.combatroll.client.CombatRollClientInit::onClientSetup);
        // Map keybind disabled — map is computer-app only
        // modEventBus.addListener(com.ultra.megamod.feature.computer.screen.map.MapKeybind::onRegisterKeyMappings);

        // Menu screen registrations
        modEventBus.addListener(com.ultra.megamod.feature.citizen.block.TownChestRegistry::onRegisterMenuScreens);
        modEventBus.addListener(com.ultra.megamod.feature.backpacks.BackpackRegistry::onRegisterMenuScreens);
        // AlchemyRegistry menu screens deleted — Reliquary apothecary replaces it.
        modEventBus.addListener(com.ultra.megamod.feature.combat.runes.RuneWorkbenchRegistry::onRegisterMenuScreens);
        modEventBus.addListener(com.ultra.megamod.feature.combat.runes.RuneRegistry::onRegisterMenuScreens);
        modEventBus.addListener(com.ultra.megamod.feature.citizen.ornament.OrnamentRegistry::onRegisterMenuScreens);
        // Colony entity renderers (raiders)
        modEventBus.addListener(com.ultra.megamod.feature.citizen.raid.RaiderRenderers::onRegisterRenderers);

        // Combat swing animation system
        // SwingAnimationRenderer and SwingParticleRenderer are auto-registered via @EventBusSubscriber.
        // Clear cached swing state on level unload to prevent stale data across world changes.
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                (net.neoforged.neoforge.event.level.LevelEvent.Unload e) -> SwingAnimationState.clearAll());

        // Wizard effect renderers and animated armor (deferred until FMLClientSetupEvent,
        // when DeferredHolder values are bound)
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) -> {
            event.enqueueWork(() -> com.ultra.megamod.feature.combat.wizards.client.WizardsClientInit.init());
        });

        // Particle provider registration
        modEventBus.addListener(com.ultra.megamod.feature.combat.spell.client.particle.SpellParticleProviders::registerParticleProviders);

        // === SpellEngine library client wiring (Phase A.3) ===
        // Register SE-port SpellProjectile / SpellCloud entity renderers, SpellBinding menu screens,
        // and SpellEngine particle providers. SpellEngineClient.init() performs the remaining client
        // initialization (HUD config refresh, effect particle spawners, RPG series client setup)
        // during FMLClientSetupEvent once registries are bound.
        modEventBus.addListener(com.ultra.megamod.lib.spellengine.client.SpellEngineClient::onRegisterEntityRenderers);
        modEventBus.addListener(com.ultra.megamod.lib.spellengine.client.SpellEngineClient::onRegisterMenuScreens);
        modEventBus.addListener(com.ultra.megamod.lib.spellengine.client.SpellEngineClient::registerParticleProviders);
        // Phase A.2: register SpellEngine HUD overlay (cast bar + spell hotbar widgets)
        // and the SpellEngine keybindings (9 spell hotbar keys + alt-bypass key).
        modEventBus.addListener(com.ultra.megamod.lib.spellengine.client.SpellEngineClient::onRegisterGuiLayers);
        modEventBus.addListener(com.ultra.megamod.lib.spellengine.client.SpellEngineClient::onRegisterKeyMappings);
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent seClientSetup) ->
                seClientSetup.enqueueWork(com.ultra.megamod.lib.spellengine.client.SpellEngineClient::init));

        // Initialize PlayerAnimationLib (registers factory, sound keyframe handler)
        com.ultra.megamod.lib.playeranim.minecraft.PlayerAnimLibMod.init();

        // Register BlockUI XML loader and PlayerAnimResources as client reload listeners
        modEventBus.addListener((net.neoforged.neoforge.client.event.AddClientReloadListenersEvent event) -> {
            event.addListener(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("megamod", "blockui_xml_loader"),
                    com.ultra.megamod.feature.citizen.blockui.Loader.INSTANCE);
            event.addListener(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("megamod", "player_animations"),
                    new com.ultra.megamod.lib.playeranim.minecraft.animation.PlayerAnimResources());
            // ETF Phase B: reset variator/texture/directory caches on resource-pack reload
            event.addListener(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("megamod", "etf"),
                    com.ultra.megamod.lib.etf.ETFReloadListener.INSTANCE);
            // EMF Phase E: clear compiled .jem cache on resource-pack reload
            event.addListener(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("megamod", "emf"),
                    com.ultra.megamod.lib.emf.EmfReloadListener.INSTANCE);
        });

        // ETF Phase B: eagerly init the manager on client setup so it populates
        // KNOWN_RESOURCEPACK_ORDER before the first render frame
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent etfInit) -> {
            etfInit.enqueueWork(() -> com.ultra.megamod.lib.etf.features.ETFManager.getInstance());
        });

        // EMF Phase E: eagerly init the model manager on client setup so the
        // first .jem load doesn't race with reload listener registration.
        // EMF Phase F: also construct the config handler, install the texture
        // redirector, and register the debug HUD + config keybind.
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent emfInit) -> {
            emfInit.enqueueWork(() -> {
                com.ultra.megamod.lib.emf.runtime.EmfModelManager.getInstance();
                com.ultra.megamod.lib.emf.EMF.config();
                com.ultra.megamod.lib.emf.runtime.EmfTextureRedirect.install();
                com.ultra.megamod.lib.emf.api.EMFApi.registerBuiltins();
            });
        });
        modEventBus.addListener(com.ultra.megamod.lib.emf.config.screen.EMFConfigKeybind::onRegisterKeyMappings);
        modEventBus.addListener(com.ultra.megamod.lib.emf.debug.EMFDebugHud::onRegisterGuiLayers);

        // ETF Phase C: config keybind registration + emissive feature layer on players
        modEventBus.addListener(com.ultra.megamod.lib.etf.config.screen.ETFConfigKeybind::onRegisterKeyMappings);
        modEventBus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers e) ->
                com.ultra.megamod.lib.etf.features.ETFEmissiveFeatureLayer.onAddLayers(e));
        modEventBus.addListener(com.ultra.megamod.lib.etf.debug.ETFDebugHud::onRegisterGuiLayers);

        // Initialize Archers client (armor renderers, effect renderers, tooltips)
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event2) -> {
            event2.enqueueWork(() -> com.ultra.megamod.feature.combat.archers.client.ArchersClientMod.init());
        });

        // Initialize Paladins client (armor renderers, effect renderers)
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event4) -> {
            event4.enqueueWork(() -> com.ultra.megamod.feature.combat.paladins.client.PaladinsClientMod.init());
        });

        // Initialize Rogues & Warriors client (armor renderers)
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event5) -> {
            event5.enqueueWork(() -> com.ultra.megamod.feature.combat.rogues.client.RoguesClientMod.init());
        });

        // Initialize Arsenal client (spell tooltips, effect particle spawners)
        // Deferred until registry values are bound
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event3) -> {
            event3.enqueueWork(() -> com.ultra.megamod.feature.combat.arsenal.client.ArsenalClientMod.init());
        });

        // Reliquary port — client wiring (particles, renderers, HUD, key handlers)
        com.ultra.megamod.reliquary.Reliquary.initClient(modEventBus, container);

        // MultiPiston client-side screen opener wiring (bypasses the common
        // class referencing Minecraft/Screen directly — keeps the dedicated
        // server's classloader happy).
        com.ultra.megamod.feature.citizen.multipiston.MultiPistonClientProxy.init();

        // Blueprint tool client-side screen opener wiring (same proxy pattern
        // as MultiPiston — keeps Screen/Minecraft references out of the
        // common bytecode loaded by the dedicated server).
        com.ultra.megamod.feature.citizen.blueprint.BlueprintClientProxy.init();

        // Class selection screen opener — route ClassSelectionPayload through
        // a client-only proxy so the common payload class never links against
        // Minecraft / Screen (would be rejected by NeoForgeDevDistCleaner on
        // the dedicated server).

        // Renderer and pipeline registrations
        modEventBus.addListener(MegaModClient::onRegisterRenderers);
        modEventBus.addListener(MegaModClient::onRegisterLayerDefinitions);
        modEventBus.addListener(com.ultra.megamod.feature.adminmodules.modules.render.ESPRenderHelper::registerPipelines);
        modEventBus.addListener(com.ultra.megamod.feature.backpacks.client.BackpackClientEvents::onAddLayers);
        modEventBus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers e) ->
                com.ultra.megamod.feature.combat.spell.client.SpellEffectLayerRenderer.onAddLayers(e));
    }

    private static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Citizen model layer: 128x64 UV mapping for MineColonies-style citizen textures
        event.registerLayerDefinition(
                com.ultra.megamod.feature.citizen.entity.mc.client.MCCitizenRenderer.CITIZEN_LAYER,
                com.ultra.megamod.feature.citizen.entity.mc.client.MCCitizenRenderer::createCitizenLayer);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(DimensionRegistry.PORTAL_BE.get(), PortalBlockRenderer::new);
        event.registerBlockEntityRenderer(MuseumRegistry.MUSEUM_DOOR_BE.get(), MuseumDoorRenderer::new);
        event.registerBlockEntityRenderer(CasinoRegistry.WHEEL_BE.get(), WheelRenderer::new);
        event.registerBlockEntityRenderer(CasinoRegistry.BLACKJACK_TABLE_BE.get(), BlackjackTableRenderer::new);

        // Spell combat entity renderers
        event.registerEntityRenderer(CombatEntityRegistry.SPELL_PROJECTILE.get(), SpellProjectileRenderer::new);
        event.registerEntityRenderer(CombatEntityRegistry.SPELL_CLOUD.get(), SpellCloudRenderer::new);

        // Paladin entity renderers (Barrier + Battle Banner)
        com.ultra.megamod.feature.combat.paladins.client.PaladinsClientMod.registerEntityRenderers(event);

        // MC Citizen renderer (unified citizen entity)
        event.registerEntityRenderer(com.ultra.megamod.feature.citizen.CitizenRegistry.MC_CITIZEN.get(),
                com.ultra.megamod.feature.citizen.entity.mc.client.MCCitizenRenderer::new);

        // Relic projectile/effect entities
        com.ultra.megamod.feature.relics.entity.RelicEntityRenderers.registerAll(event);
    }
}


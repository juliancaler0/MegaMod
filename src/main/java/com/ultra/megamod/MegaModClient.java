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
        modEventBus.addListener(com.ultra.megamod.feature.corruption.client.CorruptionOverlay::onRegisterGuiLayers);

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
        // Map keybind disabled — map is computer-app only
        // modEventBus.addListener(com.ultra.megamod.feature.computer.screen.map.MapKeybind::onRegisterKeyMappings);

        // Menu screen registrations
        modEventBus.addListener(com.ultra.megamod.feature.citizen.block.TownChestRegistry::onRegisterMenuScreens);
        modEventBus.addListener(com.ultra.megamod.feature.backpacks.BackpackRegistry::onRegisterMenuScreens);
        modEventBus.addListener(com.ultra.megamod.feature.alchemy.AlchemyRegistry::onRegisterMenuScreens);
        modEventBus.addListener(com.ultra.megamod.feature.combat.runes.RuneWorkbenchRegistry::onRegisterMenuScreens);
        modEventBus.addListener(com.ultra.megamod.feature.citizen.ornament.OrnamentRegistry::onRegisterMenuScreens);
        // Colony entity renderers (raiders)
        modEventBus.addListener(com.ultra.megamod.feature.citizen.raid.RaiderRenderers::onRegisterRenderers);

        // Combat swing animation system
        // SwingAnimationRenderer and SwingParticleRenderer are auto-registered via @EventBusSubscriber.
        // Clear cached swing state on level unload to prevent stale data across world changes.
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                (net.neoforged.neoforge.event.level.LevelEvent.Unload e) -> SwingAnimationState.clearAll());

        // Particle provider registration
        modEventBus.addListener(com.ultra.megamod.feature.combat.spell.client.particle.SpellParticleProviders::registerParticleProviders);

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
        });

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

        // MC Citizen renderer (unified citizen entity)
        event.registerEntityRenderer(com.ultra.megamod.feature.citizen.CitizenRegistry.MC_CITIZEN.get(),
                com.ultra.megamod.feature.citizen.entity.mc.client.MCCitizenRenderer::new);
    }
}


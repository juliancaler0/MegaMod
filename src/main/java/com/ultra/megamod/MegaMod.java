/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.common.Mod
 *  org.slf4j.Logger
 */
package com.ultra.megamod;

import com.mojang.logging.LogUtils;
import com.ultra.megamod.feature.attributes.MegaModAttributes;
import com.ultra.megamod.feature.computer.ComputerRegistry;
import com.ultra.megamod.feature.dimensions.DimensionRegistry;
import com.ultra.megamod.feature.dungeons.DungeonRegistry;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.items.DungeonKeyRegistry;
import com.ultra.megamod.feature.museum.MuseumRegistry;
import com.ultra.megamod.feature.relics.RelicRegistry;
import com.ultra.megamod.feature.economy.network.AtmNetwork;
import com.ultra.megamod.feature.relics.network.RelicNetwork;
import com.ultra.megamod.feature.skills.network.SkillNetwork;

import com.ultra.megamod.feature.villagerrefresh.VillagerTradeRefresh;
import com.ultra.megamod.feature.furniture.FurnitureRegistry;
import com.ultra.megamod.feature.dungeons.generation.DNLBlockRegistry;
import com.ultra.megamod.feature.dungeons.generation.DungeonProcessorRegistry;
import com.ultra.megamod.feature.dungeons.generation.DungeonFeatureRegistry;
import com.ultra.megamod.feature.museum.paintings.MasterpieceRegistry;
import com.ultra.megamod.feature.casino.CasinoRegistry;
import com.ultra.megamod.feature.citizen.CitizenRegistry;
import com.ultra.megamod.feature.citizen.network.CitizenNetwork;
import com.ultra.megamod.feature.citizen.request.network.RequestNetwork;
import com.ultra.megamod.feature.dimensions.resource.ResourceDimensionRegistry;
import com.ultra.megamod.feature.dungeons.insurance.network.InsuranceNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(value="megamod")
public class MegaMod {
    public static final String MODID = "megamod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MegaMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register BetterCombat config
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON,
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.SPEC, "megamod-combat.toml");
        modEventBus.addListener((net.neoforged.fml.event.config.ModConfigEvent event) -> {
            if (event.getConfig().getSpec() == com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.SPEC) {
                com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.syncFromSpec();
            }
        });

        ComputerRegistry.init(modEventBus);
        VillagerTradeRefresh.init(modEventBus);
        MuseumRegistry.init(modEventBus);
        RelicRegistry.init(modEventBus);
        MegaModAttributes.init(modEventBus);
        DimensionRegistry.init(modEventBus);
        DungeonRegistry.init(modEventBus);
        DungeonEntityRegistry.init(modEventBus);
        DungeonKeyRegistry.init(modEventBus);
        DungeonProcessorRegistry.init(modEventBus);
        DungeonFeatureRegistry.init(modEventBus);
        DNLBlockRegistry.init(modEventBus);

        MasterpieceRegistry.init(modEventBus);
        FurnitureRegistry.init(modEventBus);
        com.ultra.megamod.feature.recovery.GravestoneRegistry.init(modEventBus);
        com.ultra.megamod.feature.backpacks.BackpackRegistry.init(modEventBus);
        CasinoRegistry.init(modEventBus);
        // --- Citizen/Colony system ---
        com.ultra.megamod.feature.citizen.block.TownChestRegistry.init(modEventBus);
        CitizenRegistry.init(modEventBus);
        com.ultra.megamod.feature.citizen.raid.RaiderEntityRegistry.init(modEventBus);
        com.ultra.megamod.feature.citizen.visitor.VisitorEntityRegistry.init(modEventBus);
        ResourceDimensionRegistry.init(modEventBus);
        com.ultra.megamod.feature.loot.LootModifierRegistry.init(modEventBus);
        com.ultra.megamod.feature.corruption.CorruptionRegistry.init(modEventBus);
        com.ultra.megamod.feature.marketplace.MarketplaceRegistry.init(modEventBus);
        modEventBus.addListener(AtmNetwork::registerPayloads);
        modEventBus.addListener(RelicNetwork::registerPayloads);
        modEventBus.addListener(SkillNetwork::registerPayloads);
        modEventBus.addListener(CitizenNetwork::registerPayloads);
        modEventBus.addListener(RequestNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.hud.network.HudNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.adminmodules.network.PortableCraftNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.sorting.network.SortNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.backpacks.network.BackpackNetwork::registerPayloads);
        modEventBus.addListener(InsuranceNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.arena.network.ArenaNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.furniture.QuestBoardNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.corruption.network.CorruptionNetwork::registerPayloads);
        com.ultra.megamod.feature.alchemy.AlchemyRegistry.init(modEventBus);
        modEventBus.addListener(com.ultra.megamod.feature.combat.spell.SpellNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.combat.network.ClassNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.combat.animation.BetterCombatNetwork::registerPayloads);
        // Combat overhaul registries
        com.ultra.megamod.lib.combatroll.CombatRollInit.register(modEventBus);
        com.ultra.megamod.lib.combatroll.CombatRollInit.init();
        modEventBus.addListener(com.ultra.megamod.lib.combatroll.network.NetworkEvents::registerConfigurationTasks);
        modEventBus.addListener(com.ultra.megamod.lib.combatroll.network.NetworkEvents::registerPayloadHandlers);
        com.ultra.megamod.feature.combat.spell.SpellEffects.init(modEventBus);
        // Initialize ported RPG combat libraries
        com.ultra.megamod.lib.accessories.neoforge.AccessoriesForge.init(modEventBus);
        com.ultra.megamod.lib.spellpower.SpellPowerMod.init(modEventBus);
        com.ultra.megamod.lib.rangedweapon.RangedWeaponMod.init();
        com.ultra.megamod.lib.azurelib.NeoForgeAzureLibMod.register(modEventBus);
        com.ultra.megamod.feature.combat.paladins.PaladinsMod.init(modEventBus);
        com.ultra.megamod.feature.combat.wizards.WizardsMod.init(modEventBus);

        com.ultra.megamod.feature.combat.spell.CombatEntityRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.items.ClassWeaponRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.items.ClassArmorRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.items.JewelryRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.jewelry.JewelrySounds.SOUND_EVENTS.register(modEventBus);
        com.ultra.megamod.feature.combat.items.ArcherItemRegistry.init(modEventBus);
        com.ultra.megamod.lib.spellengine.api.effect.Effects.init(modEventBus);
        com.ultra.megamod.lib.spellengine.rpg_series.item.RPGItemRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.archers.ArchersMod.init(modEventBus);
        com.ultra.megamod.feature.combat.arsenal.ArsenalMod.init(modEventBus);
        com.ultra.megamod.feature.combat.rogues.RoguesMod.init();
        com.ultra.megamod.feature.combat.spell.SpellItemRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.runes.RuneRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.runes.RuneWorkbenchRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.runes.RuneCrafting.init(modEventBus);
        com.ultra.megamod.feature.combat.items.GemOreRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.village.CombatVillagerRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.spell.client.particle.SpellParticleRegistry.init(modEventBus);
        modEventBus.addListener(com.ultra.megamod.feature.combat.CombatCreativeTab::onBuildContents);
        modEventBus.addListener(com.ultra.megamod.feature.alchemy.network.AlchemyNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.map.network.MapTileSyncNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.schematic.network.SchematicNetwork::registerPayloads);
        com.ultra.megamod.feature.citizen.blueprint.BlueprintRegistry.init(modEventBus);
        com.ultra.megamod.feature.citizen.building.ColonyBuildingRegistry.init(modEventBus);
        com.ultra.megamod.feature.citizen.ornament.OrnamentRegistry.init(modEventBus);
        com.ultra.megamod.feature.citizen.colonyblocks.ColonyBlockRegistry.init(modEventBus);
        com.ultra.megamod.feature.citizen.colonyblocks.ColonyItemRegistry.init(modEventBus);
        com.ultra.megamod.feature.citizen.colonyblocks.ColonyCropRegistry.init(modEventBus);
        com.ultra.megamod.feature.citizen.colonyblocks.ColonyFoodRegistry.init(modEventBus);
        com.ultra.megamod.feature.citizen.multipiston.MultiPistonRegistry.init(modEventBus);
        com.ultra.megamod.feature.citizen.worldgen.ColonyWorldGenRegistry.init(modEventBus);
        com.ultra.megamod.feature.citizen.enchantment.ColonyEnchantmentRegistry.init(modEventBus);
        com.ultra.megamod.lib.spellengine.api.effect.SpellEngineSyncAttachments.init(modEventBus);
        LOGGER.info("MegaMod loading - all systems enabled");
    }
}


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
        com.ultra.megamod.feature.relics.effect.RelicEffectRegistry.init(modEventBus);
        // Custom relic entity registry scrapped (task #52) — re-port later
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
        com.ultra.megamod.feature.marketplace.MarketplaceRegistry.init(modEventBus);
        modEventBus.addListener(AtmNetwork::registerPayloads);
        // RelicNetwork scrapped (task #52)
        // SkillNetwork removed — Pufferfish Skills system is wired via NeoForgeMain
        modEventBus.addListener(CitizenNetwork::registerPayloads);
        modEventBus.addListener(RequestNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.hud.network.HudNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.adminmodules.network.PortableCraftNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.sorting.network.SortNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.backpacks.network.BackpackNetwork::registerPayloads);
        modEventBus.addListener(InsuranceNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.arena.network.ArenaNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.furniture.QuestBoardNetwork::registerPayloads);
        // MegaMod alchemy system deleted — Reliquary apothecary replaces it.
        modEventBus.addListener(com.ultra.megamod.feature.combat.spell.SpellNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.combat.animation.BetterCombatNetwork::registerPayloads);
        modEventBus.addListener(com.ultra.megamod.feature.attributes.network.CombatTextNetwork::registerPayloads);
        // BetterCombat slash-trail particle types (12 particles — top/bot × slash45/90/180/270/360/stab)
        com.ultra.megamod.feature.combat.animation.particle.BetterCombatParticles.init(modEventBus);
        // Dummy items backing spell projectile Blockbench models — the renderer resolves
        // them via BuiltInRegistries.ITEM so ItemStackRenderState.submit() can draw the model.
        com.ultra.megamod.feature.combat.spell.SpellProjectileModelItems.init(modEventBus);
        // Combat overhaul registries
        com.ultra.megamod.lib.combatroll.CombatRollInit.register(modEventBus);
        com.ultra.megamod.lib.combatroll.CombatRollInit.init();
        modEventBus.addListener(com.ultra.megamod.lib.combatroll.network.NetworkEvents::registerConfigurationTasks);
        modEventBus.addListener(com.ultra.megamod.lib.combatroll.network.NetworkEvents::registerPayloadHandlers);
        com.ultra.megamod.feature.combat.spell.SpellEffects.init(modEventBus);
        // Initialize ported RPG combat libraries
        com.ultra.megamod.lib.accessories.neoforge.AccessoriesForge.init(modEventBus);
        com.ultra.megamod.lib.spellpower.SpellPowerMod.init(modEventBus);
        com.ultra.megamod.lib.puffish_attributes.neoforge.AttributesForge.init(modEventBus);
        com.ultra.megamod.lib.rangedweapon.RangedWeaponMod.init();
        com.ultra.megamod.lib.rangedweapon.RangedWeaponEvents.register();
        com.ultra.megamod.lib.azurelib.NeoForgeAzureLibMod.register(modEventBus);
        com.ultra.megamod.feature.combat.paladins.PaladinsMod.init(modEventBus);
        com.ultra.megamod.feature.combat.wizards.WizardsMod.init(modEventBus);
        // NamespaceAliases removed — definitions.json is now remapped to use megamod: namespace
        // directly, so alias items are no longer needed.

        com.ultra.megamod.feature.combat.spell.CombatEntityRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.items.ClassWeaponRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.items.ClassArmorRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.items.JewelryRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.jewelry.JewelrySounds.SOUND_EVENTS.register(modEventBus);
        // Relics port (Relics-1.21.1). initEarly must run before DeferredRegister binds
        // so the accessories factory swap is already installed by the time items construct.
        com.ultra.megamod.feature.combat.relics.RelicsFeature.initEarly();
        com.ultra.megamod.feature.combat.relics.RelicsFeature.init(modEventBus);
        com.ultra.megamod.feature.combat.items.ArcherItemRegistry.init(modEventBus);
        com.ultra.megamod.lib.spellengine.api.effect.Effects.init(modEventBus);
        // SpellEngine bootstrap: registers data-pack registry for Spell, entity types,
        // network handlers, and wires common init during FMLCommonSetup.
        // Must run before RPGItemRegistry so SpellDataComponents (SPELL_CONTAINER etc.)
        // are registered before Weapon items reference them at init time.
        com.ultra.megamod.lib.spellengine.SpellEngineNeoForge.init(modEventBus);
        com.ultra.megamod.lib.spellengine.rpg_series.item.RPGItemRegistry.init(modEventBus);
        // Register the SpellEngine universal spell-book + scroll items (needed for
        // megamod:spell_books / megamod:grindable / megamod:spell_book_mergeable
        // tags and the spell_infinity enchantment to resolve on datapack load).
        com.ultra.megamod.lib.spellengine.item.SpellEngineItems.register();
        com.ultra.megamod.feature.combat.archers.ArchersMod.init(modEventBus);
        com.ultra.megamod.feature.combat.arsenal.ArsenalMod.init(modEventBus);
        // Activate class-mod SpellEngine-factory registrations for Wizards + Paladins.
        // These register wand/staff/claymore/hammer/mace/holy_wand/holy_staff/kite_shield items
        // with SpellContainer components so right-click casting, passive triggers, and
        // equipment-set MODIFIER spells fire. The matching IDs are removed from
        // {@code ClassWeaponRegistry} below to avoid duplicate registrations.
        com.ultra.megamod.feature.combat.wizards.WizardsMod.registerItems(modEventBus);
        com.ultra.megamod.feature.combat.paladins.PaladinsMod.registerItems(modEventBus);
        com.ultra.megamod.feature.combat.rogues.RoguesMod.registerItems(modEventBus);
        com.ultra.megamod.feature.combat.rogues.RoguesMod.init();
        com.ultra.megamod.feature.combat.spell.SpellItemRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.runes.RuneRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.runes.RuneWorkbenchRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.runes.RuneCrafting.init(modEventBus);
        com.ultra.megamod.feature.combat.items.GemOreRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.village.CombatVillagerRegistry.init(modEventBus);
        com.ultra.megamod.feature.combat.spell.client.particle.SpellParticleRegistry.init(modEventBus);
        modEventBus.addListener(com.ultra.megamod.feature.combat.CombatCreativeTab::onBuildContents);
        // AlchemyNetwork deleted with feature/alchemy/ — Reliquary apothecary replaces it.
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
        // WorldEdit port — admin-gated in-game world editor
        com.ultra.megamod.feature.worldedit.WorldEditRegistry.init(modEventBus);

        // Reliquary port — relic items, pedestals, alkahestry, potion essences, handgun.
        // Uses "reliquary" namespace for IDs so the copied asset/data tree resolves
        // without rewriting 400+ JSON references.
        com.ultra.megamod.reliquary.Reliquary.initCommon(modEventBus, modContainer);

        // Pufferfish Skills framework server-side init. Must run BEFORE SkillTreeMod so
        // that SpellContainerReward / ConditionalAttributeReward can register with SkillsAPI.
        // Registers the SkillsMod singleton, server event listeners (config loading on server
        // start), packet handlers, commands, and argument types.
        new com.ultra.megamod.lib.pufferfish_skills.main.NeoForgeMain(
                modEventBus, net.neoforged.fml.loading.FMLEnvironment.getDist());

        // pufferfish_unofficial_additions port — registers custom experience sources
        // (harvest_crops, fishing, spell_casting [Iron's Spellbooks only]), the "effect"
        // reward type, and the StringCondition operation. Must run after the Pufferfish
        // Skills framework init so SkillsAPI registries exist.
        com.ultra.megamod.lib.pufferfish_additions.PufferfishAdditionsMod.init(modEventBus);

        // Pufferfish Skills / skill_tree_rpgs port — registers the Orb of Oblivion
        // respec item, skill reward node types, skill status effects, and sounds
        // used by the skill tree. Items land under the "skill_tree_rpgs" namespace
        // so the ported recipe/advancement JSONs resolve without rewriting.
        com.ultra.megamod.lib.skilltree.SkillTreeMod.init();
        com.ultra.megamod.lib.skilltree.SkillTreeMod.registerItems(modEventBus);
        com.ultra.megamod.lib.skilltree.SkillTreeMod.registerSounds(modEventBus);
        com.ultra.megamod.lib.skilltree.SkillTreeMod.registerEffects();

        LOGGER.info("MegaMod loading - all systems enabled");
    }
}


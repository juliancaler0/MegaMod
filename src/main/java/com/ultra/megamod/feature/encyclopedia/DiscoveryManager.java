package com.ultra.megamod.feature.encyclopedia;

import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DiscoveryManager {
    private static DiscoveryManager INSTANCE;
    private static final String FILE_NAME = "megamod_discoveries.dat";
    private final Map<UUID, Set<String>> discoveries = new HashMap<>();
    private final Set<UUID> fullyDiscovered = new HashSet<>();
    private boolean dirty = false;

    public static DiscoveryManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new DiscoveryManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void init(ServerLevel level) {
        get(level);
    }

    public static void reset() {
        INSTANCE = null;
    }

    public boolean isDiscovered(UUID player, String entryId) {
        if (fullyDiscovered.contains(player)) return true;
        Set<String> set = discoveries.get(player);
        return set != null && set.contains(entryId);
    }

    public boolean hasDiscoveredAll(UUID player) {
        return fullyDiscovered.contains(player);
    }

    public boolean discover(UUID player, String entryId) {
        Set<String> set = discoveries.computeIfAbsent(player, k -> new HashSet<>());
        if (set.add(entryId)) {
            dirty = true;
            return true;
        }
        return false;
    }

    public Set<String> getDiscoveries(UUID player) {
        return discoveries.getOrDefault(player, Collections.emptySet());
    }

    public void discoverAll(UUID player) {
        fullyDiscovered.add(player);
        Set<String> set = discoveries.computeIfAbsent(player, k -> new HashSet<>());
        // Relics
        set.add("relic_arrow_quiver");
        set.add("relic_elytra_booster");
        set.add("relic_midnight_robe");
        set.add("relic_leather_belt");
        set.add("relic_drowned_belt");
        set.add("relic_hunter_belt");
        set.add("relic_reflection_necklace");
        set.add("relic_jellyfish_necklace");
        set.add("relic_holy_locket");
        set.add("relic_magma_walker");
        set.add("relic_aqua_walker");
        set.add("relic_ice_skates");
        set.add("relic_ice_breaker");
        set.add("relic_roller_skates");
        set.add("relic_amphibian_boot");
        set.add("relic_ender_hand");
        set.add("relic_rage_glove");
        set.add("relic_wool_mitten");
        set.add("relic_bastion_ring");
        set.add("relic_chorus_inhibitor");
        set.add("relic_shadow_glaive");
        set.add("relic_infinity_ham");
        set.add("relic_space_dissector");
        set.add("relic_magic_mirror");
        set.add("relic_horse_flute");
        set.add("relic_spore_sack");
        set.add("relic_blazing_flask");
        // Weapons (legendary "tomes" removed — asset-only items with no Java registration)
        set.add("weapon_lunar_crown");
        set.add("weapon_solar_crown");
        // Mobs (wildlife + aquarium)
        set.add("mob_minecraft:cow"); set.add("mob_minecraft:pig"); set.add("mob_minecraft:sheep");
        set.add("mob_minecraft:chicken"); set.add("mob_minecraft:horse"); set.add("mob_minecraft:donkey");
        set.add("mob_minecraft:mule"); set.add("mob_minecraft:skeleton_horse"); set.add("mob_minecraft:zombie_horse");
        set.add("mob_minecraft:rabbit"); set.add("mob_minecraft:mooshroom"); set.add("mob_minecraft:cat");
        set.add("mob_minecraft:ocelot"); set.add("mob_minecraft:parrot"); set.add("mob_minecraft:villager");
        set.add("mob_minecraft:wandering_trader"); set.add("mob_minecraft:snow_golem"); set.add("mob_minecraft:iron_golem");
        set.add("mob_minecraft:allay"); set.add("mob_minecraft:strider"); set.add("mob_minecraft:camel");
        set.add("mob_minecraft:sniffer"); set.add("mob_minecraft:armadillo"); set.add("mob_minecraft:wolf");
        set.add("mob_minecraft:fox"); set.add("mob_minecraft:panda"); set.add("mob_minecraft:bee");
        set.add("mob_minecraft:llama"); set.add("mob_minecraft:trader_llama"); set.add("mob_minecraft:polar_bear");
        set.add("mob_minecraft:goat"); set.add("mob_minecraft:enderman"); set.add("mob_minecraft:zombified_piglin");
        set.add("mob_minecraft:spider"); set.add("mob_minecraft:cave_spider"); set.add("mob_minecraft:piglin");
        set.add("mob_minecraft:zombie"); set.add("mob_minecraft:zombie_villager"); set.add("mob_minecraft:husk");
        set.add("mob_minecraft:skeleton"); set.add("mob_minecraft:stray"); set.add("mob_minecraft:wither_skeleton");
        set.add("mob_minecraft:bogged"); set.add("mob_minecraft:creeper"); set.add("mob_minecraft:witch");
        set.add("mob_minecraft:slime"); set.add("mob_minecraft:magma_cube"); set.add("mob_minecraft:phantom");
        set.add("mob_minecraft:blaze"); set.add("mob_minecraft:ghast"); set.add("mob_minecraft:piglin_brute");
        set.add("mob_minecraft:hoglin"); set.add("mob_minecraft:zoglin"); set.add("mob_minecraft:ravager");
        set.add("mob_minecraft:vindicator"); set.add("mob_minecraft:evoker"); set.add("mob_minecraft:pillager");
        set.add("mob_minecraft:illusioner"); set.add("mob_minecraft:vex"); set.add("mob_minecraft:shulker");
        set.add("mob_minecraft:endermite"); set.add("mob_minecraft:silverfish"); set.add("mob_minecraft:warden");
        set.add("mob_minecraft:breeze"); set.add("mob_minecraft:ender_dragon"); set.add("mob_minecraft:wither");
        set.add("mob_minecraft:bat");
        set.add("mob_minecraft:cod"); set.add("mob_minecraft:salmon"); set.add("mob_minecraft:tropical_fish");
        set.add("mob_minecraft:pufferfish"); set.add("mob_minecraft:squid"); set.add("mob_minecraft:glow_squid");
        set.add("mob_minecraft:dolphin"); set.add("mob_minecraft:turtle"); set.add("mob_minecraft:axolotl");
        set.add("mob_minecraft:frog"); set.add("mob_minecraft:tadpole"); set.add("mob_minecraft:guardian");
        set.add("mob_minecraft:elder_guardian"); set.add("mob_minecraft:drowned");
        // Dungeons
        set.add("dungeon_normal"); set.add("dungeon_hard"); set.add("dungeon_nightmare"); set.add("dungeon_infernal");
        // Skills
        set.add("skill_combat"); set.add("skill_mining"); set.add("skill_farming"); set.add("skill_arcane"); set.add("skill_survival");
        // Items
        set.add("item_soul_anchor"); set.add("item_void_shard"); set.add("item_boss_trophy");
        set.add("item_dungeon_map"); set.add("item_infernal_essence"); set.add("item_warp_stone");
        set.add("item_cerulean_arrow"); set.add("item_crystal_arrow"); set.add("item_rat_fang");
        set.add("item_fang_on_a_stick"); set.add("item_old_skeleton_bone"); set.add("item_old_skeleton_head");
        set.add("item_mob_net"); set.add("item_dungeon_key_normal"); set.add("item_dungeon_key_hard");
        set.add("item_dungeon_key_nightmare"); set.add("item_dungeon_key_infernal");
        // New dungeon items
        set.add("item_naga_fang_dagger"); set.add("item_wrought_axe"); set.add("item_wrought_helm");
        set.add("item_ice_crystal"); set.add("item_spear"); set.add("item_life_stealer");
        set.add("item_scepter_of_chaos"); set.add("item_sol_visage"); set.add("item_earthrend_gauntlet");
        set.add("item_blowgun"); set.add("item_glowing_jelly"); set.add("item_strange_meat");
        set.add("item_great_experience_bottle"); set.add("item_absorption_orb"); set.add("item_living_divining_rod");
        set.add("item_captured_grottol"); set.add("item_foliaath_seed");
        set.add("item_mask_of_fear"); set.add("item_mask_of_fury"); set.add("item_mask_of_faith");
        set.add("item_mask_of_rage"); set.add("item_mask_of_misery"); set.add("item_mask_of_bliss");
        set.add("item_geomancer_helm"); set.add("item_geomancer_chest");
        set.add("item_geomancer_legs"); set.add("item_geomancer_boots");
        set.add("item_wraith_trophy"); set.add("item_ossukage_trophy"); set.add("item_dungeon_keeper_trophy");
        set.add("item_frostmaw_trophy"); set.add("item_wroughtnaut_trophy"); set.add("item_umvuthi_trophy");
        set.add("item_chaos_spawner_trophy"); set.add("item_sculptor_trophy");
        // New relics (wave 2)
        set.add("relic_wardens_visor"); set.add("relic_verdant_mask"); set.add("relic_frostweave_veil");
        set.add("relic_stormcaller_circlet"); set.add("relic_ashen_diadem"); set.add("relic_wraith_crown");
        set.add("relic_arcane_gauntlet"); set.add("relic_iron_fist"); set.add("relic_plague_grasp");
        set.add("relic_sunforged_bracer"); set.add("relic_stormband"); set.add("relic_gravestone_ring");
        set.add("relic_verdant_signet"); set.add("relic_phoenix_mantle"); set.add("relic_windrunner_cloak");
        set.add("relic_abyssal_cape"); set.add("relic_alchemists_sash"); set.add("relic_guardians_girdle");
        set.add("relic_serpent_belt"); set.add("relic_frostfire_pendant"); set.add("relic_tidekeeper_amulet");
        set.add("relic_bloodstone_choker"); set.add("relic_thornweave_glove"); set.add("relic_chrono_glove");
        set.add("relic_stormstrider_boots"); set.add("relic_sandwalker_treads"); set.add("relic_emberstone_band");
        set.add("relic_void_lantern"); set.add("relic_thunderhorn"); set.add("relic_mending_chalice");
        // New RPG weapons wave 2 (legendary tomes removed — see note above).
        // New unique weapons (wave 2)
        set.add("weapon_unique_whip_1"); set.add("weapon_unique_whip_2"); set.add("weapon_unique_whip_sw");
        set.add("weapon_unique_wand_1"); set.add("weapon_unique_wand_2"); set.add("weapon_unique_wand_sw");
        set.add("weapon_unique_katana_1"); set.add("weapon_unique_katana_2"); set.add("weapon_unique_katana_sw");
        set.add("weapon_unique_greatshield_1"); set.add("weapon_unique_greatshield_2"); set.add("weapon_unique_greatshield_sw");
        set.add("weapon_unique_throwing_axe_1"); set.add("weapon_unique_throwing_axe_2"); set.add("weapon_unique_throwing_axe_sw");
        set.add("weapon_unique_rapier_1"); set.add("weapon_unique_rapier_2"); set.add("weapon_unique_rapier_sw");
        set.add("weapon_unique_longsword_1"); set.add("weapon_unique_longsword_2");
        set.add("weapon_unique_claymore_3"); set.add("weapon_unique_dagger_3"); set.add("weapon_unique_double_axe_3");
        set.add("weapon_unique_glaive_3"); set.add("weapon_unique_hammer_3"); set.add("weapon_unique_mace_3");
        set.add("weapon_unique_sickle_3"); set.add("weapon_unique_spear_3"); set.add("weapon_unique_longbow_3");
        set.add("weapon_unique_heavy_crossbow_3"); set.add("weapon_unique_staff_damage_8");
        set.add("weapon_unique_staff_heal_3"); set.add("weapon_unique_shield_3");
        // Museum entries
        set.add("museum_overview"); set.add("museum_donations"); set.add("museum_mob_net");
        set.add("museum_curator"); set.add("museum_dimension"); set.add("museum_rewards");
        set.add("museum_history");
        // Controls entries
        set.add("controls_overview"); set.add("controls_accessories"); set.add("controls_ability_primary");
        set.add("controls_ability_secondary"); set.add("controls_skill_tree"); set.add("controls_museum");
        // Citizens
        set.add("citizen_farmer"); set.add("citizen_miner"); set.add("citizen_lumberjack");
        set.add("citizen_fisherman"); set.add("citizen_shepherd"); set.add("citizen_cattle_farmer");
        set.add("citizen_chicken_farmer"); set.add("citizen_swineherd"); set.add("citizen_rabbit_farmer");
        set.add("citizen_beekeeper"); set.add("citizen_goat_farmer"); set.add("citizen_merchant");
        set.add("citizen_recruit"); set.add("citizen_shieldman"); set.add("citizen_bowman");
        set.add("citizen_crossbowman"); set.add("citizen_nomad"); set.add("citizen_horseman");
        set.add("citizen_commander"); set.add("citizen_captain"); set.add("citizen_messenger");
        set.add("citizen_scout");
        set.add("citizen_hiring"); set.add("citizen_upkeep"); set.add("citizen_upkeep_chest"); set.add("citizen_patrols");
        set.add("citizen_factions_wiki"); set.add("citizen_territory_wiki");
        set.add("citizen_assassin");
        set.add("citizen_town_app"); set.add("citizen_groups"); set.add("citizen_formations");
        set.add("citizen_combat"); set.add("citizen_tools"); set.add("citizen_mine_patterns");
        set.add("citizen_hunger"); set.add("citizen_commands_wiki"); set.add("citizen_spawn_bonuses");
        // Casino
        set.add("casino_overview"); set.add("casino_slots"); set.add("casino_blackjack");
        set.add("casino_wheel"); set.add("casino_stats");
        // Resource Dimension + Insurance
        set.add("item_resource_dimension_key"); set.add("dungeon_insurance");
        // Marketplace
        set.add("market_overview"); set.add("market_wts"); set.add("market_wtb");
        set.add("market_trading"); set.add("market_terminal"); set.add("market_dimension");
        set.add("market_escrow"); set.add("market_notifications"); set.add("market_my_listings");
        set.add("market_tips");
        // Reliquary (replaces removed MegaMod Alchemy)
        set.add("reliquary_overview"); set.add("reliquary_apothecary_cauldron");
        set.add("reliquary_apothecary_mortar"); set.add("reliquary_brewing_reagents");
        set.add("reliquary_custom_effects"); set.add("reliquary_alkahestry_altar");
        set.add("reliquary_alkahestry_tome_usage"); set.add("reliquary_alkahestry_recipes");
        set.add("reliquary_pedestals"); set.add("reliquary_mob_charm_belt");
        set.add("reliquary_toggles"); set.add("reliquary_vs_megamod_alchemy");
        set.add("reliquary_admin_commands"); set.add("reliquary_handgun");
        // Classes
        set.add("class_overview"); set.add("class_paladin"); set.add("class_warrior");
        set.add("class_wizard"); set.add("class_rogue"); set.add("class_ranger");
        set.add("class_selection"); set.add("class_branches"); set.add("class_restrictions");
        // Class Weapons
        set.add("class_weapon_claymore"); set.add("class_weapon_great_hammer"); set.add("class_weapon_mace");
        set.add("class_weapon_kite_shield"); set.add("class_weapon_holy_wand"); set.add("class_weapon_holy_staff");
        set.add("class_weapon_wand"); set.add("class_weapon_staff");
        set.add("class_weapon_dagger"); set.add("class_weapon_sickle");
        set.add("class_weapon_double_axe"); set.add("class_weapon_glaive");
        set.add("class_weapon_spear"); set.add("class_weapon_longbow"); set.add("class_weapon_crossbow");
        // Class Armor
        set.add("class_armor_paladin"); set.add("class_armor_warrior"); set.add("class_armor_wizard");
        set.add("class_armor_rogue"); set.add("class_armor_ranger"); set.add("class_armor_sets");
        // Spells
        set.add("spell_overview"); set.add("spell_casting"); set.add("spell_schools");
        set.add("spell_cooldowns"); set.add("spell_scrolls"); set.add("spell_books");
        // Wizard Spells
        set.add("spell_arcane_bolt"); set.add("spell_arcane_blast"); set.add("spell_arcane_missile");
        set.add("spell_arcane_beam"); set.add("spell_fireball"); set.add("spell_fire_blast");
        set.add("spell_fire_breath"); set.add("spell_fire_meteor"); set.add("spell_frostbolt");
        set.add("spell_frost_shard"); set.add("spell_frost_nova"); set.add("spell_frost_blizzard");
        // Paladin Spells
        set.add("spell_heal"); set.add("spell_flash_heal"); set.add("spell_holy_shock");
        set.add("spell_holy_beam"); set.add("spell_circle_of_healing"); set.add("spell_judgement");
        set.add("spell_holy_shield"); set.add("spell_consecrate");
        // Rogue Spells
        set.add("spell_backstab"); set.add("spell_vanish"); set.add("spell_shadow_step");
        set.add("spell_eviscerate"); set.add("spell_fan_of_knives"); set.add("spell_poison_cloud");
        // Ranger Spells
        set.add("spell_power_shot"); set.add("spell_barrage"); set.add("spell_entangling_roots");
        set.add("spell_volley");
        // Combat Systems
        set.add("combat_combos"); set.add("combat_dual_wield"); set.add("combat_set_bonuses");
        set.add("combat_arena"); set.add("combat_bounties"); set.add("combat_pvp");
        dirty = true;
    }

    public void clearAll(UUID player) {
        discoveries.remove(player);
        fullyDiscovered.remove(player);
        dirty = true;
    }

    // Auto-discovery hooks
    public static void onRelicEquipped(UUID player, String relicId) {
        if (INSTANCE != null) {
            INSTANCE.discover(player, "relic_" + relicId);
        }
    }

    /** Called when a player successfully casts a spell. */
    public static void onSpellCast(UUID player, String spellId) {
        if (INSTANCE != null) {
            INSTANCE.discover(player, "spell_" + spellId);
        }
    }

    /** Called when a player equips or obtains a class weapon. */
    public static void onClassWeaponEquipped(UUID player, String weaponType) {
        if (INSTANCE != null) {
            INSTANCE.discover(player, "class_weapon_" + weaponType);
        }
    }

    /** Called when a player selects a class. */
    public static void onClassSelected(UUID player, String className) {
        if (INSTANCE != null) {
            INSTANCE.discover(player, "class_" + className.toLowerCase());
            INSTANCE.discover(player, "class_overview");
            INSTANCE.discover(player, "class_selection");
        }
    }

    public static void onCitizenHired(UUID player, String citizenTypeId) {
        if (INSTANCE != null) {
            INSTANCE.discover(player, citizenTypeId);
        }
    }

    public static void onFactionCreated(UUID player) {
        if (INSTANCE != null) {
            INSTANCE.discover(player, "citizen_factions_wiki");
        }
    }

    public static void onSiegeWon(UUID player) {
        if (INSTANCE != null) {
            INSTANCE.discover(player, "citizen_territory_wiki");
        }
    }

    public static void onWeaponUsed(UUID player, String weaponId) {
        if (INSTANCE != null) {
            INSTANCE.discover(player, "weapon_" + weaponId);
        }
    }

    public static void onMobDonated(UUID player, String mobId) {
        if (INSTANCE != null) {
            INSTANCE.discover(player, "mob_" + mobId);
        }
    }

    public static void onDungeonCompleted(UUID player, String tier) {
        if (INSTANCE != null) {
            INSTANCE.discover(player, "dungeon_" + tier.toLowerCase());
        }
    }

    public static void onSkillUnlocked(UUID player, String treeName) {
        if (INSTANCE != null) {
            INSTANCE.discover(player, "skill_" + treeName.toLowerCase());
        }
    }

    public static void onItemObtained(UUID player, String itemId) {
        if (INSTANCE != null) {
            INSTANCE.discover(player, "item_" + itemId);
        }
    }

    public void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (!dataFile.exists()) {
                MegaMod.LOGGER.info("No discovery data file found, starting fresh.");
                return;
            }
            CompoundTag root = NbtIo.readCompressed(dataFile.toPath(), NbtAccounter.unlimitedHeap());
            for (String uuidStr : root.keySet()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(uuidStr);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                CompoundTag playerTag = root.getCompoundOrEmpty(uuidStr);
                Set<String> playerDiscoveries = new HashSet<>();
                ListTag list = playerTag.getListOrEmpty("entries");
                for (int i = 0; i < list.size(); i++) {
                    Tag tag = list.get(i);
                    if (tag instanceof StringTag st) {
                        playerDiscoveries.add(st.value());
                    }
                }
                discoveries.put(uuid, playerDiscoveries);
            }
            MegaMod.LOGGER.info("Discovery data loaded for {} players.", discoveries.size());
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load discovery data!", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            for (Map.Entry<UUID, Set<String>> entry : discoveries.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                ListTag list = new ListTag();
                for (String d : entry.getValue()) {
                    list.add(StringTag.valueOf(d));
                }
                playerTag.put("entries", list);
                root.put(entry.getKey().toString(), playerTag);
            }
            NbtIo.writeCompressed(root, dataFile.toPath());
            dirty = false;
            MegaMod.LOGGER.debug("Discovery data saved for {} players.", discoveries.size());
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save discovery data!", e);
        }
    }
}

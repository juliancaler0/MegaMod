package com.ultra.megamod.feature.combat.spell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bridge between the relic/weapon ability system and the spell system.
 * Maps relic abilities (by "relicName:abilityName") and weapon registry names
 * to spell IDs in SpellRegistry. When a mapping exists, the ability/weapon skill
 * delegates to SpellExecutor instead of the custom executor.
 *
 * Unmapped abilities and weapons continue using their existing handlers unchanged.
 */
public class SpellAbilityBridge {

    // "relicName:abilityName" -> spell ID
    private static final Map<String, String> ABILITY_SPELL_MAP = new HashMap<>();

    // weapon registry name (e.g., "megamod:wand_arcane") -> list of spell IDs (cycleable)
    private static final Map<String, List<String>> WEAPON_SPELL_MAP = new HashMap<>();

    static {
        // ═══════════════════════════════════════════
        // WEAPON -> SPELL MAPPINGS
        // ═══════════════════════════════════════════
        // Use addWeaponSpell() to support multiple spells per weapon.

        // -- Wizard Wands: school-appropriate bolt/shard spells --
        addWeaponSpell("megamod:wand_novice", "arcane_bolt");
        addWeaponSpell("megamod:wand_arcane", "arcane_bolt");
        addWeaponSpell("megamod:wand_fire", "fireball");
        addWeaponSpell("megamod:wand_frost", "frostbolt");
        addWeaponSpell("megamod:wand_netherite_arcane", "arcane_blast");
        addWeaponSpell("megamod:wand_netherite_fire", "fire_blast");
        addWeaponSpell("megamod:wand_netherite_frost", "frost_nova");

        // -- Wizard Staves: higher-tier school spells --
        addWeaponSpell("megamod:staff_wizard", "arcane_bolt");
        addWeaponSpell("megamod:staff_arcane", "arcane_missile");
        addWeaponSpell("megamod:staff_fire", "fire_breath");
        addWeaponSpell("megamod:staff_frost", "frost_shard");
        addWeaponSpell("megamod:staff_netherite_arcane", "arcane_beam");
        addWeaponSpell("megamod:staff_netherite_fire", "fire_meteor");
        addWeaponSpell("megamod:staff_netherite_frost", "frost_blizzard");

        // -- Paladin Healing Wands: heal spells --
        addWeaponSpell("megamod:acolyte_wand", "heal");
        addWeaponSpell("megamod:holy_wand", "heal");
        addWeaponSpell("megamod:diamond_holy_wand", "flash_heal");
        addWeaponSpell("megamod:netherite_holy_wand", "holy_shock");

        // -- Paladin Healing Staves: higher-tier heal/holy spells --
        addWeaponSpell("megamod:holy_staff", "holy_shock");
        addWeaponSpell("megamod:diamond_holy_staff", "circle_of_healing");
        addWeaponSpell("megamod:netherite_holy_staff", "holy_beam");

        // ═══════════════════════════════════════════
        // RELIC ABILITY -> SPELL MAPPINGS
        // ═══════════════════════════════════════════
        // Format: ABILITY_SPELL_MAP.put("RelicName:AbilityName", "spell_id");
        // Only relics with abilities that thematically match registered spells are mapped.

        // Arcane Gauntlet — "Arcane Bolt" ability fires a magic bolt -> arcane_bolt spell
        ABILITY_SPELL_MAP.put("Arcane Gauntlet:Arcane Bolt", "arcane_bolt");

        // Stormcaller Circlet — "Thunder Strike" calls lightning -> no exact lightning spell,
        // but Judgement is the closest AOE burst; skip. "Static Field" zaps mobs -> shock_powder (stun AOE)
        ABILITY_SPELL_MAP.put("Stormcaller Circlet:Thunder Strike", "judgement");

        // Frostweave Veil — frost-themed face relic. Its passive ability doesn't cast,
        // but if it had an active we'd map it. Skip passive-only relics.

        // Frostfire Pendant — "Frostfire Bolt" fires a dual-element bolt -> frostbolt (closest match)
        ABILITY_SPELL_MAP.put("Frostfire Pendant:Frostfire Bolt", "frostbolt");
        // "Elemental Storm" is fire+ice AOE -> frost_nova (closest AOE frost spell)
        ABILITY_SPELL_MAP.put("Frostfire Pendant:Elemental Storm", "frost_nova");

        // Void Lantern — "Dark Beacon" places a gravity well -> shadow_step (teleport/shadow themed)
        ABILITY_SPELL_MAP.put("Void Lantern:Dark Beacon", "shadow_step");
        // "Dimensional Tear" rips a portal for heavy AOE -> arcane_blast
        ABILITY_SPELL_MAP.put("Void Lantern:Dimensional Tear", "arcane_blast");

        // Mending Chalice — "Healing Draught" restores health -> heal spell
        ABILITY_SPELL_MAP.put("Mending Chalice:Healing Draught", "heal");
        // "Sanctified Ground" AOE healing -> circle_of_healing
        ABILITY_SPELL_MAP.put("Mending Chalice:Sanctified Ground", "circle_of_healing");

        // Sunforged Bracer — "Divine Smite" calls a holy beam -> holy_beam
        ABILITY_SPELL_MAP.put("Sunforged Bracer:Divine Smite", "holy_beam");
        // Sunforged Bracer — "Purifying Touch" -> flash_heal (holy cleansing heal)
        ABILITY_SPELL_MAP.put("Sunforged Bracer:Purifying Touch", "flash_heal");

        // ── Head Relics ──

        // Ashen Diadem — "Pyroclasm" -> fire_meteor (devastating fire AOE)
        ABILITY_SPELL_MAP.put("Ashen Diadem:Pyroclasm", "fire_meteor");
        // Ashen Diadem — "Infernal Command" -> fire_breath (cone of fire)
        ABILITY_SPELL_MAP.put("Ashen Diadem:Infernal Command", "fire_breath");

        // Wraith Crown — "Soul Siphon" -> drain (shadow life steal)
        ABILITY_SPELL_MAP.put("Wraith Crown:Soul Siphon", "drain");
        // Wraith Crown — "Phantom Form" -> vanish (become ethereal/invisible)
        ABILITY_SPELL_MAP.put("Wraith Crown:Phantom Form", "vanish");

        // Solar Crown — "Sunfire" -> holy_shock (burst of radiant energy)
        ABILITY_SPELL_MAP.put("Solar Crown:Sunfire", "holy_shock");
        // Lunar Crown — "Moonbeam" -> arcane_beam (focused arcane beam)
        ABILITY_SPELL_MAP.put("Lunar Crown:Moonbeam", "arcane_beam");

        // ── Face Relics ──

        // Frostweave Veil — "Cold Breath" -> frostbolt (frost projectile)
        ABILITY_SPELL_MAP.put("Frostweave Veil:Cold Breath", "frostbolt");
        // Frostweave Veil — "Blizzard Aura" -> frost_blizzard (AOE frost)
        ABILITY_SPELL_MAP.put("Frostweave Veil:Blizzard Aura", "frost_blizzard");
        // Warden's Visor — "Sonic Pulse" -> shockwave (AOE knockback/damage)
        ABILITY_SPELL_MAP.put("Warden's Visor:Sonic Pulse", "shockwave");
        // Verdant Mask — "Blossom Burst" -> entangling_roots (nature AOE)
        ABILITY_SPELL_MAP.put("Verdant Mask:Blossom Burst", "entangling_roots");

        // ── Necklace Relics ──

        // Tidekeeper Amulet — "Tidal Wave" -> tidal_wave (water AOE)
        ABILITY_SPELL_MAP.put("Tidekeeper Amulet:Tidal Wave", "tidal_wave");
        // Tidekeeper Amulet — "Whirlpool" -> frost_nova (closest AOE control)
        ABILITY_SPELL_MAP.put("Tidekeeper Amulet:Whirlpool", "frost_nova");
        // Bloodstone Choker — "Sanguine Feast" -> drain (life steal)
        ABILITY_SPELL_MAP.put("Bloodstone Choker:Sanguine Feast", "drain");

        // ── Back Relics ──

        // Abyssal Cape — "Void Rift" -> arcane_blast (dimensional rip)
        ABILITY_SPELL_MAP.put("Abyssal Cape:Void Rift", "arcane_blast");
        // Abyssal Cape — "Blink" -> shadow_step (short teleport)
        ABILITY_SPELL_MAP.put("Abyssal Cape:Blink", "shadow_step");
        // Phoenix Mantle — "Inferno Wings" -> fire_breath (fiery wing attack)
        ABILITY_SPELL_MAP.put("Phoenix Mantle:Inferno Wings", "fire_breath");
        // Midnight Robe — "Vanish" -> vanish (stealth)
        ABILITY_SPELL_MAP.put("Midnight Robe:Vanish", "vanish");
        // Midnight Robe — "Backstab" -> backstab (stealth break attack)
        ABILITY_SPELL_MAP.put("Midnight Robe:Backstab", "backstab");
        // Midnight Robe — "Shadow Step" -> shadow_step (teleport)
        ABILITY_SPELL_MAP.put("Midnight Robe:Shadow Step", "shadow_step");

        // ── Hand Relics ──

        // Plague Grasp — "Plague Wave" -> poison_cloud (toxic AOE)
        ABILITY_SPELL_MAP.put("Plague Grasp:Plague Wave", "poison_cloud");
        // Plague Grasp — "Wither Grip" -> drain (dark damage + heal)
        ABILITY_SPELL_MAP.put("Plague Grasp:Wither Grip", "drain");
        // Chrono Glove — "Stasis Field" -> frost_nova (freeze in place)
        ABILITY_SPELL_MAP.put("Chrono Glove:Stasis Field", "frost_nova");
        // Thornweave Glove — "Vine Lash" -> entangling_roots (root enemies)
        ABILITY_SPELL_MAP.put("Thornweave Glove:Vine Lash", "entangling_roots");
        // Thornweave Glove — "Entangle" -> entangling_roots (root targets)
        ABILITY_SPELL_MAP.put("Thornweave Glove:Entangle", "entangling_roots");
        // Iron Fist — "Ground Pound" -> shockwave (melee AOE)
        ABILITY_SPELL_MAP.put("Iron Fist:Ground Pound", "shockwave");

        // ── Ring Relics ──

        // Stormband — "Arc Discharge" -> chain_lightning (chain lightning)
        ABILITY_SPELL_MAP.put("Stormband:Arc Discharge", "chain_lightning");
        // Stormband — "Galvanic Surge" -> thunder_bolt (lightning strike)
        ABILITY_SPELL_MAP.put("Stormband:Galvanic Surge", "thunder_bolt");
        // Verdant Signet — "Growth Surge" -> heal (nature healing)
        ABILITY_SPELL_MAP.put("Verdant Signet:Growth Surge", "heal");
        // Verdant Signet — "Bloom Shield" -> circle_of_healing (protective bloom)
        ABILITY_SPELL_MAP.put("Verdant Signet:Bloom Shield", "circle_of_healing");
        // Emberstone Band — "Fire Snap" -> fireball (quick fire burst)
        ABILITY_SPELL_MAP.put("Emberstone Band:Fire Snap", "fireball");
        // Emberstone Band — "Combustion" -> fire_blast (fire AOE)
        ABILITY_SPELL_MAP.put("Emberstone Band:Combustion", "fire_blast");
        // Gravestone Ring — "Life Tap" -> drain (sacrifice health for power)
        ABILITY_SPELL_MAP.put("Gravestone Ring:Life Tap", "drain");

        // ── Belt Relics ──

        // Serpent Belt — "Venom Spit" -> poison_cloud (ranged poison)
        ABILITY_SPELL_MAP.put("Serpent Belt:Venom Spit", "poison_cloud");
        // Guardian's Girdle — "Stalwart Stand" -> holy_shield (defensive barrier)
        ABILITY_SPELL_MAP.put("Guardian's Girdle:Stalwart Stand", "holy_shield");

        // ── Feet Relics ──

        // Stormstrider Boots — "Thunder Leap" -> thunder_bolt (lightning AoE on landing)
        ABILITY_SPELL_MAP.put("Stormstrider Boots:Thunder Leap", "thunder_bolt");
        // Sandwalker Treads — "Sandstorm" -> frost_blizzard (blinding AOE, closest match)
        ABILITY_SPELL_MAP.put("Sandwalker Treads:Sandstorm", "frost_blizzard");

        // ── Usable Relics ──

        // Thunderhorn — "War Cry" -> war_cry (warrior shout/buff)
        ABILITY_SPELL_MAP.put("Thunderhorn:War Cry", "war_cry");
        // Thunderhorn — "Sonic Boom" -> shockwave (AOE blast)
        ABILITY_SPELL_MAP.put("Thunderhorn:Sonic Boom", "shockwave");
    }

    private static void addWeaponSpell(String weaponRegistryName, String spellId) {
        WEAPON_SPELL_MAP.computeIfAbsent(weaponRegistryName, k -> new ArrayList<>()).add(spellId);
    }

    /**
     * Returns the spell ID for a relic ability, or null if not mapped.
     * @param relicName  the relic's name (e.g., "Flame Orb")
     * @param abilityName  the ability's name (e.g., "Fire Burst")
     */
    public static String getSpellForAbility(String relicName, String abilityName) {
        return ABILITY_SPELL_MAP.get(relicName + ":" + abilityName);
    }

    /**
     * Returns the first (or only) spell ID for a weapon, or null if not mapped.
     * @param weaponRegistryName  the weapon's full registry name (e.g., "megamod:wand_arcane")
     */
    public static String getSpellForWeapon(String weaponRegistryName) {
        List<String> spells = WEAPON_SPELL_MAP.get(weaponRegistryName);
        return (spells != null && !spells.isEmpty()) ? spells.get(0) : null;
    }

    /**
     * Returns all spell IDs for a weapon, or empty list if not mapped.
     * Used for cycling through multiple spells on a single weapon.
     */
    public static List<String> getSpellsForWeapon(String weaponRegistryName) {
        List<String> spells = WEAPON_SPELL_MAP.get(weaponRegistryName);
        return spells != null ? Collections.unmodifiableList(spells) : Collections.emptyList();
    }

    /**
     * Returns the spell at a given index for a weapon, or null if out of bounds.
     */
    public static String getSpellForWeaponAtIndex(String weaponRegistryName, int index) {
        List<String> spells = WEAPON_SPELL_MAP.get(weaponRegistryName);
        if (spells == null || index < 0 || index >= spells.size()) return null;
        return spells.get(index);
    }

    /**
     * Registers a relic ability -> spell mapping at runtime.
     */
    public static void registerAbilitySpell(String relicName, String abilityName, String spellId) {
        ABILITY_SPELL_MAP.put(relicName + ":" + abilityName, spellId);
    }

    /**
     * Registers (appends) a weapon -> spell mapping at runtime.
     */
    public static void registerWeaponSpell(String weaponRegistryName, String spellId) {
        WEAPON_SPELL_MAP.computeIfAbsent(weaponRegistryName, k -> new ArrayList<>()).add(spellId);
    }

    /**
     * Removes a relic ability -> spell mapping.
     */
    public static void removeAbilitySpell(String relicName, String abilityName) {
        ABILITY_SPELL_MAP.remove(relicName + ":" + abilityName);
    }

    /**
     * Removes all spell mappings for a weapon.
     */
    public static void removeWeaponSpell(String weaponRegistryName) {
        WEAPON_SPELL_MAP.remove(weaponRegistryName);
    }

    /**
     * Returns true if any spell mapping exists for the given weapon.
     */
    public static boolean hasWeaponSpell(String weaponRegistryName) {
        List<String> spells = WEAPON_SPELL_MAP.get(weaponRegistryName);
        return spells != null && !spells.isEmpty();
    }

    /**
     * Returns true if any spell mapping exists for the given relic ability.
     */
    public static boolean hasAbilitySpell(String relicName, String abilityName) {
        return ABILITY_SPELL_MAP.containsKey(relicName + ":" + abilityName);
    }
}

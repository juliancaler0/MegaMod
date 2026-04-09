package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.feature.combat.spell.SpellDefinition.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.ultra.megamod.feature.combat.spell.SpellDefinition.SpellVisuals.*;

/**
 * Static registry of all spells ported from Wizards, Paladins, Rogues, Archers.
 * Each spell references a SpellSchool and class requirement for skill tree locking.
 */
public class SpellRegistry {

    public static final Map<String, SpellDefinition> ALL_SPELLS = new LinkedHashMap<>();

    private static SpellDefinition register(SpellDefinition spell) {
        ALL_SPELLS.put(spell.id(), spell);
        return spell;
    }

    public static SpellDefinition get(String id) { return ALL_SPELLS.get(id); }

    // ─── Helper shorthand ───

    private static StatusEffectDef eff(String id, int ticks, int amp, boolean harmful) {
        return new StatusEffectDef(id, ticks, amp, harmful);
    }

    private static AreaConfig area(float hRange, float vRange, float angle, boolean caster) {
        return new AreaConfig(hRange, vRange, angle, caster);
    }

    private static ProjectileConfig proj(float vel, float homing, int pierce, int bounce) {
        return new ProjectileConfig(vel, homing, pierce, bounce);
    }

    private static CloudConfig cloud(float radius, float ttl, int interval) {
        return new CloudConfig(radius, ttl, interval);
    }

    // ═══════════════════════════════════════════
    // WIZARD SPELLS — Arcane, Fire, Frost (16)
    // ═══════════════════════════════════════════

    // -- Arcane --
    public static final SpellDefinition ARCANE_BOLT = register(new SpellDefinition(
        "arcane_bolt", "Arcane Bolt", SpellSchool.ARCANE, 0,
        CastMode.CHARGED, 1.0f, DeliveryType.PROJECTILE, TargetType.AIM, 48,
        0.7f, 0, 0, 0.6f, new StatusEffectDef[0],
        null, proj(1.5f, 0, 0, 0), null, null, "WIZARD"
    ).withVisuals(projectile("one_handed_projectile_charge", "one_handed_projectile_release",
        "combat.generic_arcane_casting", "combat.arcane_beam_release", "combat.arcane_missile_impact",
        "spell_projectile/arcane_bolt", 0.5f, 10f)));

    public static final SpellDefinition ARCANE_BLAST = register(new SpellDefinition(
        "arcane_blast", "Arcane Blast", SpellSchool.ARCANE, 1,
        CastMode.CHARGED, 1.5f, DeliveryType.DIRECT, TargetType.AIM, 16,
        0.8f, 0, 0, 0.5f,
        new StatusEffectDef[]{ eff("megamod:arcane_charge", 200, 0, false) },
        null, null, null, null, "WIZARD"
    ).withVisuals(of("one_handed_projectile_charge", "one_handed_projectile_release",
        "combat.generic_arcane_casting", "combat.arcane_blast_release", "combat.arcane_blast_impact")));

    public static final SpellDefinition ARCANE_MISSILE = register(new SpellDefinition(
        "arcane_missile", "Arcane Missile", SpellSchool.ARCANE, 2,
        CastMode.CHANNELED, 4.0f, DeliveryType.PROJECTILE, TargetType.AIM, 64,
        0.8f, 0, 0, 0,
        new StatusEffectDef[0],
        null, proj(1.5f, 30f, 0, 0), null, null, "WIZARD"
    ).withVisuals(projectile("two_handed_channeling", null,
        "combat.generic_arcane_casting", "combat.arcane_missile_release", "combat.arcane_missile_impact",
        "spell_projectile/arcane_missile", 0.6f, 10f)));

    public static final SpellDefinition ARCANE_BEAM = register(new SpellDefinition(
        "arcane_beam", "Arcane Beam", SpellSchool.ARCANE, 3,
        CastMode.CHANNELED, 5.0f, DeliveryType.BEAM, TargetType.AIM, 32,
        1.0f, 0, 10, 0, new StatusEffectDef[0],
        null, null, null, null, "WIZARD"
    ).withExhaust(0.3f).withVisuals(beam("two_handed_channeling",
        "combat.arcane_beam_casting", "combat.arcane_beam_release", "combat.arcane_beam_impact",
        0xFFDD88FFL, 1.5f)));

    public static final SpellDefinition ARCANE_BLINK = register(new SpellDefinition(
        "arcane_blink", "Arcane Blink", SpellSchool.ARCANE, 4,
        CastMode.INSTANT, 0, DeliveryType.TELEPORT, TargetType.AIM, 15,
        0, 0, 12, 0, new StatusEffectDef[0],
        null, null, null, null, "WIZARD"
    ).withExhaust(0.4f).withVisuals(instant("one_handed_area_release", null)));

    // -- Fire --
    public static final SpellDefinition FIREBALL = register(new SpellDefinition(
        "fireball", "Fireball", SpellSchool.FIRE, 0,
        CastMode.CHARGED, 1.5f, DeliveryType.PROJECTILE, TargetType.AIM, 64,
        0.8f, 0, 0, 0.8f,
        new StatusEffectDef[]{ eff("minecraft:fire", 80, 0, true) },
        null, proj(1.5f, 0, 0, 0), null, null, "WIZARD"
    ).withVisuals(projectile("one_handed_projectile_charge", "one_handed_projectile_release",
        "combat.generic_fire_casting", "combat.generic_fire_release", "combat.fireball_impact",
        "spell_projectile/fireball", 0.5f, 0)));

    public static final SpellDefinition FIRE_BLAST = register(new SpellDefinition(
        "fire_blast", "Fire Blast", SpellSchool.FIRE, 1,
        CastMode.CHARGED, 1.5f, DeliveryType.PROJECTILE, TargetType.AIM, 64,
        1.0f, 0, 0, 0.5f,
        new StatusEffectDef[]{ eff("minecraft:fire", 60, 0, true) },
        null, proj(1.5f, 0, 0, 0), null, null, "WIZARD"
    ).withVisuals(projectile("one_handed_projectile_charge", "one_handed_projectile_release",
        "combat.generic_fire_casting", "combat.generic_fire_release", "combat.fire_scorch_impact",
        "spell_projectile/fire_blast", 0.9f, 0)));

    public static final SpellDefinition FIRE_BREATH = register(new SpellDefinition(
        "fire_breath", "Fire Breath", SpellSchool.FIRE, 2,
        CastMode.CHANNELED, 5.0f, DeliveryType.AREA, TargetType.AREA, 10,
        0.9f, 0, 10, 0.9f,
        new StatusEffectDef[]{ eff("minecraft:fire", 60, 0, true) },
        area(10, 0.5f, 40, false), null, null, null, "WIZARD"
    ).withExhaust(0.2f).withVisuals(of("two_handed_channeling", null,
        "combat.fire_breath_casting", "combat.fire_breath_release", "combat.fire_breath_impact")));

    public static final SpellDefinition FIRE_METEOR = register(new SpellDefinition(
        "fire_meteor", "Fire Meteor", SpellSchool.FIRE, 3,
        CastMode.CHARGED, 1.0f, DeliveryType.PROJECTILE, TargetType.AIM, 32,
        1.5f, 0, 10, 1.5f,
        new StatusEffectDef[]{ eff("minecraft:fire", 100, 0, true) },
        area(6, 1.0f, 360, false), proj(0.5f, 0, 0, 0), null, null, "WIZARD"
    ).withExhaust(0.3f).withVisuals(projectile("one_handed_projectile_charge", "one_handed_area_release",
        "combat.generic_fire_casting", "combat.fire_meteor_release", "combat.fire_meteor_impact",
        "spell_projectile/fire_meteor", 0.9f, 0)));

    public static final SpellDefinition FIRE_WALL = register(new SpellDefinition(
        "fire_wall", "Fire Wall", SpellSchool.FIRE, 3,
        CastMode.INSTANT, 0, DeliveryType.CLOUD, TargetType.AIM, 16,
        0.5f, 0, 24, 0,
        new StatusEffectDef[]{ eff("minecraft:fire", 40, 0, true) },
        null, null, cloud(3, 8, 20), null, "WIZARD"
    ).withExhaust(0.4f).withVisuals(SpellDefinition.SpellVisuals.cloud(null, "combat.fire_wall_ignite", "combat.fire_scorch_impact", null)));

    public static final SpellDefinition FIRE_SCORCH = register(new SpellDefinition(
        "fire_scorch", "Fire Scorch", SpellSchool.FIRE, 0,
        CastMode.CHARGED, 1.2f, DeliveryType.DIRECT, TargetType.AIM, 16,
        0.6f, 0, 3, 0,
        new StatusEffectDef[]{ eff("minecraft:fire", 60, 0, true) },
        null, null, null, null, "WIZARD"
    ).withVisuals(of("one_handed_projectile_charge", "one_handed_projectile_release",
        "combat.generic_fire_casting", "combat.generic_fire_release", "combat.fire_scorch_impact")));

    // -- Frost --
    public static final SpellDefinition FROSTBOLT = register(new SpellDefinition(
        "frostbolt", "Frostbolt", SpellSchool.FROST, 1,
        CastMode.CHARGED, 1.1f, DeliveryType.PROJECTILE, TargetType.AIM, 64,
        0.8f, 0, 0, 0,
        new StatusEffectDef[]{ eff("megamod:frost_slowness", 100, 0, true) },
        null, proj(1.5f, 2, 0, 2), null, null, "WIZARD"
    ).withVisuals(projectile("one_handed_projectile_charge", "one_handed_projectile_release",
        "combat.generic_frost_casting", "combat.generic_frost_release", "combat.frost_shard_impact",
        "spell_projectile/frostbolt", 0.8f, 0)));

    public static final SpellDefinition FROST_NOVA = register(new SpellDefinition(
        "frost_nova", "Frost Nova", SpellSchool.FROST, 2,
        CastMode.CHARGED, 0.5f, DeliveryType.AREA, TargetType.AREA, 6,
        0.5f, 0, 10, 0.8f,
        new StatusEffectDef[]{ eff("megamod:frozen", 120, 0, true) },
        area(6, 0.5f, 360, false), null, null, null, "WIZARD"
    ).withExhaust(0.2f).withVisuals(of("one_handed_area_charge", "one_handed_area_release",
        "combat.generic_frost_casting", "combat.frost_nova_release", "combat.frost_nova_damage_impact")));

    public static final SpellDefinition FROST_SHARD = register(new SpellDefinition(
        "frost_shard", "Frost Shard", SpellSchool.FROST, 0,
        CastMode.CHARGED, 1.0f, DeliveryType.PROJECTILE, TargetType.AIM, 48,
        0.6f, 0, 0, 0.3f,
        new StatusEffectDef[]{ eff("megamod:frost_slowness", 60, 0, true) },
        null, proj(1.2f, 0, 0, 0), null, null, "WIZARD"
    ).withVisuals(projectile("one_handed_projectile_charge", "one_handed_projectile_release",
        "combat.generic_frost_casting", "combat.generic_frost_release", "combat.frost_shard_impact",
        "spell_projectile/frost_shard", 0.7f, 0)));

    public static final SpellDefinition FROST_SHIELD = register(SpellDefinition.buff(
        "frost_shield", "Frost Shield", SpellSchool.FROST, 2,
        30, new StatusEffectDef[]{ eff("megamod:frost_shield", 200, 0, false) }, "WIZARD"
    ).withExhaust(0.3f).withVisuals(of("one_handed_area_charge", "one_handed_area_release",
        "combat.generic_frost_casting", "combat.frost_shield_release", "combat.frost_shield_impact")));

    public static final SpellDefinition FROST_BLIZZARD = register(new SpellDefinition(
        "frost_blizzard", "Frost Blizzard", SpellSchool.FROST, 4,
        CastMode.CHANNELED, 8.0f, DeliveryType.CLOUD, TargetType.AIM, 16,
        0.4f, 0, 16, 0,
        new StatusEffectDef[]{ eff("megamod:frozen", 60, 0, true) },
        null, null, cloud(5, 6, 20), null, "WIZARD"
    ).withExhaust(0.4f).withVisuals(SpellDefinition.SpellVisuals.cloud(null, null, null, null)));

    // ═══════════════════════════════════════════
    // PALADIN SPELLS — Healing, Holy (9)
    // ═══════════════════════════════════════════

    public static final SpellDefinition HEAL = register(SpellDefinition.heal(
        "heal", "Heal", 0, CastMode.CHARGED, 1.0f, TargetType.AIM, 16, 0.5f, 4, "PALADIN"
    ).withVisuals(of("one_handed_healing_charge", "one_handed_healing_release",
        "combat.generic_healing_casting", "combat.generic_healing_release", "combat.holy_beam_heal")));

    public static final SpellDefinition FLASH_HEAL = register(SpellDefinition.heal(
        "flash_heal", "Flash Heal", 2, CastMode.CHARGED, 0.5f, TargetType.AIM, 16, 1.2f, 6, "PALADIN"
    ).withExhaust(0.2f).withVisuals(of("one_handed_healing_charge", "one_handed_healing_release",
        "combat.generic_healing_casting", "combat.generic_healing_release", "combat.holy_beam_heal")));

    public static final SpellDefinition HOLY_SHOCK = register(new SpellDefinition(
        "holy_shock", "Holy Shock", SpellSchool.HEALING, 1,
        CastMode.CHARGED, 1.5f, DeliveryType.DIRECT, TargetType.AIM, 16,
        0.8f, 0.4f, 3, 0.5f, new StatusEffectDef[0],
        null, null, null, null, "PALADIN"
    ).withExhaust(0.2f).withVisuals(of("one_handed_projectile_charge", "one_handed_healing_release",
        "combat.generic_healing_casting", "combat.generic_healing_release", "combat.holy_shock_damage")));

    public static final SpellDefinition HOLY_BEAM = register(new SpellDefinition(
        "holy_beam", "Holy Beam", SpellSchool.HEALING, 2,
        CastMode.CHANNELED, 5.0f, DeliveryType.BEAM, TargetType.AIM, 32,
        0.8f, 0.4f, 10, 0.5f, new StatusEffectDef[0],
        null, null, null, null, "PALADIN"
    ).withExhaust(0.2f).withVisuals(beam("two_handed_channeling",
        "combat.holy_beam_casting", "combat.holy_beam_release", "combat.holy_beam_heal",
        0xFFCC66FFL, 1.5f)));

    public static final SpellDefinition DIVINE_PROTECTION = register(SpellDefinition.buff(
        "divine_protection", "Divine Protection", SpellSchool.HEALING, 2,
        30, new StatusEffectDef[]{ eff("megamod:divine_protection", 160, 0, false) }, "PALADIN"
    ).withExhaust(0.3f).withVisuals(instant("one_handed_area_release", "combat.divine_protection_release")));

    public static final SpellDefinition JUDGEMENT = register(new SpellDefinition(
        "judgement", "Judgement", SpellSchool.PHYSICAL_MELEE, 3,
        CastMode.CHARGED, 0.5f, DeliveryType.PROJECTILE, TargetType.AIM, 16,
        0.9f, 0, 15, 1.5f,
        new StatusEffectDef[]{ eff("megamod:judgement_stun", 60, 0, true) },
        area(6, 0.5f, 360, false), proj(0.5f, 0, 0, 0), null, null, "PALADIN"
    ).withVisuals(projectile("one_handed_projectile_charge", "one_handed_projectile_release",
        "combat.generic_healing_casting", "combat.judgement_impact", "combat.judgement_impact",
        null, 1.0f, 0)));

    public static final SpellDefinition CIRCLE_OF_HEALING = register(new SpellDefinition(
        "circle_of_healing", "Circle of Healing", SpellSchool.HEALING, 3,
        CastMode.CHARGED, 0.5f, DeliveryType.AREA, TargetType.AREA, 8,
        0, 0.4f, 10, 0,
        new StatusEffectDef[]{ eff("megamod:priest_absorption", 120, 0, false) },
        area(8, 0.6f, 360, true), null, null, null, "PALADIN"
    ).withExhaust(0.3f).withVisuals(of("one_handed_area_charge", "one_handed_area_release",
        "combat.generic_healing_casting", "combat.generic_healing_release", null)));

    public static final SpellDefinition BATTLE_BANNER = register(new SpellDefinition(
        "battle_banner", "Battle Banner", SpellSchool.HEALING, 4,
        CastMode.INSTANT, 0, DeliveryType.CLOUD, TargetType.SELF, 0,
        0, 0, 45, 0,
        new StatusEffectDef[]{ eff("megamod:battle_banner", 200, 0, false) },
        null, null, cloud(3, 10, 10), null, "PALADIN"
    ).withExhaust(0.3f).withVisuals(SpellDefinition.SpellVisuals.cloud("one_handed_healing_release",
        "combat.battle_banner_release", null, "spell_effect/battle_banner")));

    public static final SpellDefinition BARRIER = register(new SpellDefinition(
        "barrier", "Barrier", SpellSchool.HEALING, 4,
        CastMode.INSTANT, 0, DeliveryType.SPAWN, TargetType.AIM, 4,
        0, 0, 40, 0, new StatusEffectDef[0],
        null, null, null, null, "PALADIN"
    ).withExhaust(0.4f).withVisuals(instant("one_handed_area_release", "combat.holy_barrier_activate")));

    // ═══════════════════════════════════════════
    // ROGUE SPELLS — Physical Melee (7)
    // ═══════════════════════════════════════════

    public static final SpellDefinition SLICE_AND_DICE = register(SpellDefinition.buff(
        "slice_and_dice", "Slice and Dice", SpellSchool.PHYSICAL_MELEE, 2,
        15, new StatusEffectDef[]{ eff("megamod:slice_and_dice", 200, 0, false) }, "ROGUE"
    ).withVisuals(instant("dual_handed_weapon_charge", "combat.slice_and_dice")));

    public static final SpellDefinition SHOCK_POWDER = register(new SpellDefinition(
        "shock_powder", "Shock Powder", SpellSchool.PHYSICAL_MELEE, 2,
        CastMode.INSTANT, 0, DeliveryType.AREA, TargetType.AREA, 5,
        0, 0, 16, 0,
        new StatusEffectDef[]{ eff("megamod:shock", 60, 0, true) },
        area(5, 0.5f, 360, false), null, null, null, "ROGUE"
    ).withExhaust(0.3f).withVisuals(instant("dual_handed_ground_release", "combat.shock_powder_release")));

    public static final SpellDefinition SHADOW_STEP = register(new SpellDefinition(
        "shadow_step", "Shadow Step", SpellSchool.PHYSICAL_MELEE, 3,
        CastMode.INSTANT, 0, DeliveryType.TELEPORT, TargetType.AIM, 15,
        0, 0, 12, 0,
        new StatusEffectDef[]{ eff("megamod:shadow_step_buff", 30, 0, false) },
        null, null, null, null, "ROGUE"
    ).withExhaust(0.4f).withVisuals(instant("one_handed_area_release", "combat.shadow_step_depart")));

    public static final SpellDefinition VANISH = register(SpellDefinition.buff(
        "vanish", "Vanish", SpellSchool.PHYSICAL_MELEE, 4,
        30, new StatusEffectDef[]{ eff("megamod:stealth", 160, 0, false) }, "ROGUE"
    ).withExhaust(0.4f).withVisuals(instant("dual_handed_weapon_cross", "combat.vanish_combined")));

    public static final SpellDefinition SHATTERING_THROW = register(new SpellDefinition(
        "shattering_throw", "Shattering Throw", SpellSchool.PHYSICAL_MELEE, 2,
        CastMode.CHARGED, 0.5f, DeliveryType.PROJECTILE, TargetType.AIM, 24,
        1.0f, 0, 8, 0.5f,
        new StatusEffectDef[]{ eff("megamod:shatter", 160, 0, true) },
        null, proj(0.8f, 2, 0, 1), null, null, "WARRIOR"
    ).withExhaust(0.3f).withVisuals(projectile("one_handed_throw_charge", "one_handed_throw_release_instant",
        null, "combat.throw", "combat.throw_impact",
        null, 1.0f, -36f)));

    public static final SpellDefinition SHOUT = register(new SpellDefinition(
        "shout", "Shout", SpellSchool.PHYSICAL_MELEE, 3,
        CastMode.INSTANT, 0, DeliveryType.AREA, TargetType.AREA, 12,
        0.05f, 0, 12, 0,
        new StatusEffectDef[]{ eff("megamod:demoralize", 160, 0, true) },
        area(12, 0.5f, 360, false), null, null, null, "WARRIOR"
    ).withExhaust(0.3f).withVisuals(instant("one_handed_shout_release", "combat.shout_release")));

    public static final SpellDefinition CHARGE = register(SpellDefinition.buff(
        "charge", "Charge", SpellSchool.PHYSICAL_MELEE, 4,
        12, new StatusEffectDef[]{ eff("megamod:charge_buff", 40, 0, false) }, "WARRIOR"
    ).withExhaust(0.4f).withVisuals(instant("one_handed_area_release", "combat.charge_activate")));

    // ═══════════════════════════════════════════
    // ARCHER/RANGER SPELLS — Physical Ranged (4)
    // ═══════════════════════════════════════════

    public static final SpellDefinition POWER_SHOT = register(SpellDefinition.buff(
        "power_shot", "Power Shot", SpellSchool.PHYSICAL_RANGED, 2,
        8, new StatusEffectDef[]{ eff("megamod:hunters_mark_stash", 240, 0, false) }, "RANGER"
    ).withVisuals(instant(null, "combat.marker_shot")));

    public static final SpellDefinition ENTANGLING_ROOTS = register(new SpellDefinition(
        "entangling_roots", "Entangling Roots", SpellSchool.PHYSICAL_RANGED, 3,
        CastMode.INSTANT, 0, DeliveryType.CLOUD, TargetType.SELF, 0,
        0, 0, 18, 0,
        new StatusEffectDef[]{ eff("megamod:entangling_roots", 20, 0, true) },
        null, null, cloud(3.5f, 8, 15), null, "RANGER"
    ).withExhaust(0.2f).withVisuals(SpellDefinition.SpellVisuals.cloud("one_handed_area_release",
        "combat.entangling_roots", null, "spell_effect/entangling_roots")));

    public static final SpellDefinition BARRAGE = register(new SpellDefinition(
        "barrage", "Barrage", SpellSchool.PHYSICAL_RANGED, 3,
        CastMode.CHARGED, 0.5f, DeliveryType.ARROW, TargetType.AIM, 64,
        0.75f, 0, 10, 0.5f, new StatusEffectDef[0],
        null, proj(3.15f, 0, 0, 0), null, null, "RANGER"
    ).withVisuals(of("archery_pull", "archery_release",
        null, "combat.bow_pull", null)));

    public static final SpellDefinition MAGIC_ARROW = register(new SpellDefinition(
        "magic_arrow", "Magic Arrow", SpellSchool.PHYSICAL_RANGED, 4,
        CastMode.CHARGED, 1.0f, DeliveryType.PROJECTILE, TargetType.AIM, 64,
        1.2f, 0, 8, 2.0f, new StatusEffectDef[0],
        null, proj(1.5f, 2, 99999, 0), null, null, "RANGER"
    ).withVisuals(projectile("archery_pull", "archery_release",
        "combat.generic_wind_charging", "combat.magic_arrow_release", "combat.magic_arrow_impact",
        "spell_projectile/magic_arrow", 1.2f, 0)));

    // ─── Total: 36 spells ───

    static {
        // Verify count on load
        assert ALL_SPELLS.size() == 36 : "Expected 36 spells, got " + ALL_SPELLS.size();
    }
}

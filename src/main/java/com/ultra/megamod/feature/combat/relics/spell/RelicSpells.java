package com.ultra.megamod.feature.combat.relics.spell;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.spellengine.api.datagen.SpellBuilder;
import com.ultra.megamod.lib.spellengine.api.entity.SpellEntityPredicates;
import com.ultra.megamod.lib.spellengine.api.spell.ExternalSpellSchools;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.fx.ParticleBatch;
import com.ultra.megamod.lib.spellengine.api.spell.fx.PlayerAnimation;
import com.ultra.megamod.lib.spellengine.api.spell.fx.Sound;
import com.ultra.megamod.lib.spellengine.client.gui.SpellTooltip;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineParticles;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineSounds;
import com.ultra.megamod.lib.spellengine.internals.target.SpellTarget;
import com.ultra.megamod.lib.spellpower.api.SpellSchool;
import com.ultra.megamod.lib.spellpower.api.SpellSchools;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RelicSpells {
    public record Entry(Identifier id, Spell spell, String title, String description,
                        @Nullable SpellTooltip.DescriptionMutator mutator) {
    }

    public static final List<Entry> entries = new ArrayList<>();

    private static Entry add(Entry entry) {
        entries.add(entry);
        return entry;
    }

    private static final float T1_USE_EFFECT_DURATION = 10;
    private static final float T1_PROC_EFFECT_DURATION = 6;
    private static final float T1_USE_EFFECT_COOLDOWN = 60;
    private static final float T1_PROC_EFFECT_COOLDOWN = 45;
    private static final float T1_PROC_CHANCE = 0.05F;

    private static final float T2_USE_EFFECT_DURATION = 10;
    private static final float T2_PROC_EFFECT_DURATION = 8;
    private static final float T2_USE_EFFECT_COOLDOWN = 60;
    private static final float T2_PROC_EFFECT_COOLDOWN = 45;
    private static final float T2_PROC_CHANCE = 0.06F;

    private static final float T3_TRANCE_CHANCE = 0.1F;
    private static final float T3_TRANCE_DURATION = 10F;
    private static final float T3_TRANCE_COOLDOWN = 45F;
    private static final float T3_PERK_CC_DURATION = 2;
    private static final float T3_PERK_CC_COOLDOWN = 20;

    private static final float T4_USE_EFFECT_DURATION = 15;
    private static final float T4_USE_EFFECT_COOLDOWN = 90;
    private static final float T4_AREA_RANGE = 15;
    private static final float T4_ZONE_RANGE = 3;

    private static Spell activeSpellBase() {
        var spell = new Spell();
        spell.secondary_archetype = Spell.ExtendedArchetype.ANY;
        spell.range = 0;
        spell.tier = 7;

        spell.type = Spell.Type.ACTIVE;
        spell.active = new Spell.Active();

        spell.tooltip = new Spell.Tooltip();
        spell.tooltip.show_header = false;
        spell.tooltip.name = new Spell.Tooltip.LineOptions(false, false);
        spell.tooltip.description.color = ChatFormatting.DARK_GREEN.getName();
        spell.tooltip.description.show_in_compact = true;

        return spell;
    }

    private static Spell passiveSpellBase() {
        var spell = new Spell();
        spell.range = 0;
        spell.tier = 7;

        spell.type = Spell.Type.PASSIVE;
        spell.passive = new Spell.Passive();

        spell.tooltip = new Spell.Tooltip();
        spell.tooltip.show_header = false;
        spell.tooltip.name = new Spell.Tooltip.LineOptions(false, false);
        spell.tooltip.description.color = ChatFormatting.DARK_GREEN.getName();
        spell.tooltip.description.show_in_compact = true;

        return spell;
    }

    private static Spell.Impact createEffectImpact(String effectIdString, float duration) {
        var buff = new Spell.Impact();
        buff.action = new Spell.Impact.Action();
        buff.action.type = Spell.Impact.Action.Type.STATUS_EFFECT;
        buff.action.status_effect = new Spell.Impact.Action.StatusEffect();
        buff.action.status_effect.effect_id = effectIdString;
        buff.action.status_effect.duration = duration;
        return buff;
    }

    private static void configureCooldown(Spell spell, float duration) {
        if (spell.cost == null) {
            spell.cost = new Spell.Cost();
        }
        if (spell.cost.cooldown == null) {
            spell.cost.cooldown = new Spell.Cost.Cooldown();
        }
        spell.cost.cooldown.duration = duration;
    }


    public static Entry lesser_use_damage = add(lesser_use_damage());

    private static @NotNull ParticleBatch lesserActivateParticles(Color color, int count) {
        return lesserActivateParticles(SpellEngineParticles.MagicParticles.get(
                        SpellEngineParticles.MagicParticles.Shape.SPARK,
                        SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                count)
                .color(color.toRGBA());
    }

    private static @NotNull ParticleBatch lesserActivateParticles(String particleId, int count) {
        return new ParticleBatch(
                particleId,
                ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                count, 0.14F, 0.15F);
    }

    private static Entry lesser_use_damage() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "lesser_use_damage");
        var description = "Use: Increases attack damage by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.LESSER_ATTACK_DAMAGE;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = activeSpellBase();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.SHARPEN.id().toString());
        spell.release.particles = new ParticleBatch[]{
                lesserActivateParticles(Color.WHITE, 25)
        };

        var buff = createEffectImpact(effect.id.toString(), T1_USE_EFFECT_DURATION);
        spell.impacts = List.of(buff);

        spell.cost = new Spell.Cost();
        spell.cost.cooldown = new Spell.Cost.Cooldown();
        spell.cost.cooldown.duration = T1_USE_EFFECT_COOLDOWN;

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry lesser_use_dex = add(lesser_use_dex());

    private static Entry lesser_use_dex() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "lesser_use_dex");
        var description = "Use: Increases melee and ranged attack speed by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.LESSER_ATTACKS_SPEED;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = activeSpellBase();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.MEDAL_USE.id().toString());
        spell.release.particles = new ParticleBatch[]{
                lesserActivateParticles(Color.FROST, 25)
        };

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T1_USE_EFFECT_DURATION));
        configureCooldown(spell, T1_USE_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry lesser_use_ranged = add(lesser_use_ranged());

    private static Entry lesser_use_ranged() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "lesser_use_ranged");
        var description = "Use: Increases ranged attack damage by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.LESSER_RANGED_DAMAGE;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = activeSpellBase();
        spell.school = ExternalSpellSchools.PHYSICAL_RANGED;

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.EAGLE_BOOST.id().toString());
        spell.release.particles = new ParticleBatch[]{
                lesserActivateParticles(Color.NATURE, 25)
        };

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T1_USE_EFFECT_DURATION));
        configureCooldown(spell, T1_USE_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry lesser_use_health = add(lesser_use_health());

    private static Entry lesser_use_health() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "lesser_use_health");
        var description = "Use: Heals you for {heal_percent} of your max health.";
        var title = "Sip";
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifiedDescription = args.description();
            var spell = args.spellEntry().value();
            var heal = spell.impacts.get(0).action.heal;
            if (heal != null) {
                modifiedDescription = modifiedDescription.replace("{heal_percent}", SpellTooltip.percent(heal.spell_power_coefficient));
            }
            return modifiedDescription;
        };

        var spell = activeSpellBase();
        spell.school = SpellSchools.HEALING;

        // spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge";
        spell.release.sound = new Sound(RelicSounds.POTION_GENERIC.id().toString());
        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch("spell_engine:magic_nature_impact_decelerate", ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        null, 10, 0.14F, 0.15F, 0.0F, -0.2F)
        };

        var heal = new Spell.Impact();
        heal.attribute = Attributes.MAX_HEALTH.getRegisteredName();
        heal.action = new Spell.Impact.Action();
        heal.action.type = Spell.Impact.Action.Type.HEAL;
        heal.action.heal = new Spell.Impact.Action.Heal();
        heal.action.heal.spell_power_coefficient = 0.2f;

        spell.impacts = List.of(heal);
        configureCooldown(spell, T1_USE_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry lesser_use_spell_power = add(lesser_use_spell_power());

    private static Entry lesser_use_spell_power() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "lesser_use_spell_power");
        var description = "Use: Increases spell power by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.LESSER_SPELL_POWER;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = activeSpellBase();
        spell.school = SpellSchools.ARCANE;

        spell.release.animation = PlayerAnimation.of("spell_engine:one_handed_healing_release");
        spell.release.sound = new Sound(RelicSounds.INTELLECT_BUFF.id().toString());
        spell.release.particles = new ParticleBatch[]{
                lesserActivateParticles(Color.WHITE, 25)
        };

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T1_USE_EFFECT_DURATION));
        configureCooldown(spell, T1_USE_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry lesser_use_spell_haste = add(lesser_use_spell_haste());

    private static Entry lesser_use_spell_haste() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "lesser_use_spell_haste");
        var description = "Use: Increases spell haste by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.LESSER_SPELL_HASTE;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = activeSpellBase();
        spell.school = SpellSchools.ARCANE;

        spell.release.animation = PlayerAnimation.of("spell_engine:one_handed_healing_release");
        spell.release.sound = new Sound(RelicSounds.SPELL_HASTE_ACTIVATE_1.id().toString());
        spell.release.particles = new ParticleBatch[]{
                lesserActivateParticles(Color.HOLY, 25)
        };

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T1_USE_EFFECT_DURATION));
        configureCooldown(spell, T1_USE_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry lesser_proc_spell_crit = add(lesser_proc_spell_crit());

    private static Entry lesser_proc_spell_crit() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "lesser_proc_spell_crit");
        var description = "On spell hit: {trigger_chance} chance to increase spell critical chance by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.LESSER_SPELL_CRIT;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = SpellSchools.ARCANE;

        var trigger = new Spell.Trigger();
        trigger.chance = T1_PROC_CHANCE;
        trigger.type = Spell.Trigger.Type.SPELL_IMPACT_ANY;
        trigger.spell = new Spell.Trigger.SpellCondition();
        trigger.spell.archetype = SpellSchool.Archetype.MAGIC;

        spell.passive.triggers = List.of(trigger);

        spell.release.animation = PlayerAnimation.of("spell_engine:one_handed_healing_release");
        spell.release.sound = new Sound(RelicSounds.INTELLECT_BUFF.id().toString());
        spell.release.particles = new ParticleBatch[]{
                lesserActivateParticles(Color.HOLY, 25)
        };

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T1_PROC_EFFECT_DURATION));
        configureCooldown(spell, T1_PROC_EFFECT_COOLDOWN);


        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry lesser_proc_crit_damage = add(lesser_proc_crit_damage());

    private static Entry lesser_proc_crit_damage() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "lesser_proc_crit_damage");
        var description = "On spell hit: Spell critical strikes have {trigger_chance} chance to increase spell critical damage by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.LESSER_PROC_CRIT_DAMAGE;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = SpellSchools.ARCANE;

        var trigger = new Spell.Trigger();
        trigger.chance = 0.5F;
        trigger.type = Spell.Trigger.Type.SPELL_IMPACT_SPECIFIC;
        trigger.spell = new Spell.Trigger.SpellCondition();
        trigger.spell.archetype = SpellSchool.Archetype.MAGIC;
        trigger.impact = new Spell.Trigger.ImpactCondition();
        trigger.impact.impact_type = Spell.Impact.Action.Type.DAMAGE.toString();

        spell.passive.triggers = List.of(trigger);

        spell.release.animation = PlayerAnimation.of("spell_engine:one_handed_healing_release");
        spell.release.sound = new Sound(RelicSounds.INTELLECT_BUFF.id().toString());
        spell.release.particles = new ParticleBatch[]{
                lesserActivateParticles(Color.HOLY, 25)
        };

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T1_PROC_EFFECT_DURATION));
        configureCooldown(spell, T1_PROC_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry lesser_proc_arcane_fire = add(lesser_proc_arcane_fire());

    private static Entry lesser_proc_arcane_fire() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "lesser_proc_arcane_fire");
        var description = "On spell hit: {trigger_chance} chance to increase arcane and fire spell power by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.LESSER_POWER_ARCANE_FIRE;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = SpellSchools.ARCANE;

        var trigger = new Spell.Trigger();
        trigger.chance = T1_PROC_CHANCE;
        trigger.type = Spell.Trigger.Type.SPELL_IMPACT_ANY;
        trigger.spell = new Spell.Trigger.SpellCondition();
        trigger.spell.archetype = SpellSchool.Archetype.MAGIC;

        spell.passive.triggers = List.of(trigger);

        spell.release.animation = PlayerAnimation.of("spell_engine:one_handed_healing_release");
        spell.release.sound = new Sound(RelicSounds.INTELLECT_BUFF.id().toString());
        spell.release.particles = new ParticleBatch[]{
                lesserActivateParticles(Color.ARCANE, 12),
                lesserActivateParticles(SpellEngineParticles.flame_spark.id().toString(), 12)
        };

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T1_PROC_EFFECT_DURATION));
        configureCooldown(spell, T1_PROC_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry lesser_proc_frost_healing = add(lesser_proc_frost_healing());

    private static Entry lesser_proc_frost_healing() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "lesser_proc_frost_healing");
        var description = "On spell hit: {trigger_chance} chance to increase frost and healing spell power by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.LESSER_POWER_FROST_HEALING;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = SpellSchools.HEALING;

        var trigger = new Spell.Trigger();
        trigger.chance = T1_PROC_CHANCE;

        trigger.type = Spell.Trigger.Type.SPELL_IMPACT_ANY;
        trigger.spell = new Spell.Trigger.SpellCondition();
        trigger.spell.archetype = SpellSchool.Archetype.MAGIC;

        spell.passive.triggers = List.of(trigger);

        spell.release.animation = PlayerAnimation.of("spell_engine:one_handed_healing_release");
        spell.release.sound = new Sound(RelicSounds.INTELLECT_BUFF.id().toString());
        spell.release.particles = new ParticleBatch[]{
                lesserActivateParticles(Color.FROST, 12),
                lesserActivateParticles(Color.HOLY, 12)
        };

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T1_PROC_EFFECT_DURATION));
        configureCooldown(spell, T1_PROC_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    /**
     * SECTION: MEDIUM TIER SPELLS
     */

    private static ParticleBatch[] mediumActivateParticlesSphere(Color color, int count) {
        return mediumActivateParticlesSphere(SpellEngineParticles.MagicParticles.get(
                SpellEngineParticles.MagicParticles.Shape.SPARK,
                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                color,
                count);
    }

    private static ParticleBatch[] mediumActivateParticlesSphere(String particleId, Color color, int count) {
        return new ParticleBatch[]{
                new ParticleBatch(
                        particleId,
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        count, 0.14F, 0.15F)
                        .color(color.toRGBA()),
                new ParticleBatch(
                        particleId,
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        count, 0.24F, 0.25F)
                        .color(color.toRGBA()),
        };
    }

    private static ParticleBatch[] mediumActivateParticlesPipe(Color color, int count) {
        return mediumActivateParticlesPipe(SpellEngineParticles.MagicParticles.get(
                        SpellEngineParticles.MagicParticles.Shape.SPARK,
                        SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                color,
                count);
    }

    private static ParticleBatch[] mediumActivateParticlesPipe(String particleId, Color color, int count) {
        return new ParticleBatch[]{
                new ParticleBatch(
                        particleId,
                        ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.CENTER,
                        count, 0.15F, 0.15F)
                        .color(color.toRGBA()),
                new ParticleBatch(
                        particleId,
                        ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.CENTER,
                        count, 0.3F, 0.3F)
                        .color(color.toRGBA())
        };
    }

    public static Entry medium_proc_attack_damage = add(medium_proc_attack_damage());
    private static Entry medium_proc_attack_damage() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "medium_proc_attack_damage");
        var description = "On melee hit: {trigger_chance} chance to increase attack damage by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.MEDIUM_ATTACK_DAMAGE;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;

        var trigger = new Spell.Trigger();
        trigger.chance = T2_PROC_CHANCE;
        trigger.type = Spell.Trigger.Type.MELEE_IMPACT;
        spell.passive.triggers = List.of(trigger);

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.MELEE_ACTIVATE_1.id());
        spell.release.particles = mediumActivateParticlesSphere(Color.WHITE, 25);

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T2_PROC_EFFECT_DURATION));
        configureCooldown(spell, T2_PROC_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry medium_proc_attack_speed = add(medium_proc_attack_speed());
    private static Entry medium_proc_attack_speed() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "medium_proc_attack_speed");
        var description = "On hit: {trigger_chance_1} chance to increase melee and ranged attack speed by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.MEDIUM_ATTACKS_SPEED;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;

        var melee_trigger = new Spell.Trigger();
        melee_trigger.chance = T2_PROC_CHANCE;
        melee_trigger.type = Spell.Trigger.Type.MELEE_IMPACT;
        var ranged_trigger = new Spell.Trigger();
        ranged_trigger.chance = T2_PROC_CHANCE;
        ranged_trigger.type = Spell.Trigger.Type.ARROW_IMPACT;
        spell.passive.triggers = List.of(melee_trigger, ranged_trigger);

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.BLOODLUST_ACTIVATE.id());
        spell.release.particles = mediumActivateParticlesSphere(Color.FROST, 25);

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T2_PROC_EFFECT_DURATION));
        configureCooldown(spell, T2_PROC_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry medium_proc_ranged_damage = add(medium_proc_ranged_damage());
    private static Entry medium_proc_ranged_damage() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "medium_proc_ranged_damage");
        var description = "On arrow hit: {trigger_chance} chance to increase ranged attack damage by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.MEDIUM_RANGED_DAMAGE;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = ExternalSpellSchools.PHYSICAL_RANGED;

        var trigger = new Spell.Trigger();
        trigger.chance = T2_PROC_CHANCE;
        trigger.type = Spell.Trigger.Type.ARROW_IMPACT;
        spell.passive.triggers = List.of(trigger);

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.BOW_STRING_ACTIVATE.id());
        spell.release.particles = mediumActivateParticlesSphere(Color.NATURE, 25);

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T2_PROC_EFFECT_DURATION));
        configureCooldown(spell, T2_PROC_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry medium_proc_defense = add(medium_proc_defense());
    private static Entry medium_proc_defense() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "medium_proc_defense");
        var description = "On damage taken: {trigger_chance} chance to increase armor toughness by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.MEDIUM_DEFENSE;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;

        var trigger = new Spell.Trigger();
        trigger.chance = T2_PROC_CHANCE * 2;
        trigger.type = Spell.Trigger.Type.DAMAGE_TAKEN;
        spell.passive.triggers = List.of(trigger);

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.DEFENSE_ACTIVATE_1.id().toString());
        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPELL,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        10, 0.15F, 0.15F)
                        .color(Color.RAGE.toRGBA()),
                new ParticleBatch(
                        SpellEngineParticles.shield_small.id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        10, 0.15F, 0.15F),
        };

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T2_PROC_EFFECT_DURATION));
        configureCooldown(spell, T2_PROC_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Color EVASION_COLOR = Color.from(0x9966ff);

    public static Entry medium_proc_evasion = add(medium_proc_evasion());
    private static Entry medium_proc_evasion() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "medium_proc_evasion");
        var description = "On damage taken: {trigger_chance} chance to increase evasion chance by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.MEDIUM_EVASION;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;

        var trigger = SpellBuilder.Triggers.damageTaken();
        trigger.chance = T2_PROC_CHANCE * 2;
        spell.passive.triggers = List.of(trigger);

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.MONKEY_ACTIVATE.id().toString());
        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPELL,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        20, 0.15F, 0.15F)
                        .color(EVASION_COLOR.toRGBA())
        };

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T2_PROC_EFFECT_DURATION));
        configureCooldown(spell, T2_PROC_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry medium_proc_spell_power = add(medium_proc_spell_power());
    private static Entry medium_proc_spell_power() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "medium_proc_spell_power");
        var description = "On spell hit: {trigger_chance} chance to increase spell power by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.MEDIUM_SPELL_POWER;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = SpellSchools.ARCANE;

        var trigger = new Spell.Trigger();
        trigger.chance = T2_PROC_CHANCE;
        trigger.type = Spell.Trigger.Type.SPELL_IMPACT_SPECIFIC;
        trigger.spell = new Spell.Trigger.SpellCondition();
        trigger.spell.archetype = SpellSchool.Archetype.MAGIC;
        trigger.impact = new Spell.Trigger.ImpactCondition();
        trigger.impact.impact_type = Spell.Impact.Action.Type.DAMAGE.toString();

        spell.passive.triggers = List.of(trigger);

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.SPELL_POWER_ACTIVATE_2.id());
        spell.release.particles = mediumActivateParticlesSphere(Color.WHITE, 50);

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T2_PROC_EFFECT_DURATION));
        configureCooldown(spell, T2_PROC_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry medium_proc_spell_haste = add(medium_proc_spell_haste());

    private static Entry medium_proc_spell_haste() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "medium_proc_spell_haste");
        var description = "On spell hit: {trigger_chance} chance to increase spell haste by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.MEDIUM_SPELL_HASTE;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = SpellSchools.ARCANE;

        var trigger = new Spell.Trigger();
        trigger.chance = T2_PROC_CHANCE;
        trigger.type = Spell.Trigger.Type.SPELL_IMPACT_SPECIFIC;
        trigger.spell = new Spell.Trigger.SpellCondition();
        trigger.spell.archetype = SpellSchool.Archetype.MAGIC;
        trigger.impact = new Spell.Trigger.ImpactCondition();
        trigger.impact.impact_type = Spell.Impact.Action.Type.DAMAGE.toString();

        spell.passive.triggers = List.of(trigger);

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.SPELL_HASTE_ACTIVATE_2.id().toString());
        spell.release.particles = mediumActivateParticlesPipe(Color.HOLY, 50);

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T2_PROC_EFFECT_DURATION));
        configureCooldown(spell, T2_PROC_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry medium_use_arcane_power = add(medium_use_arcane_power());

    private static Entry medium_use_arcane_power() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "medium_use_arcane_power");
        var description = "Use: Increases arcane spell power by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.MEDIUM_ARCANE_POWER;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = activeSpellBase();
        spell.school = SpellSchools.ARCANE;

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.INTELLECT_BUFF.id().toString());
        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPELL,
                                SpellEngineParticles.MagicParticles.Motion.ASCEND).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        15, 0.08F, 0.08F)
                        .color(Color.ARCANE.toRGBA()),
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPARK,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25, 0.24F, 0.25F)
                        .color(Color.ARCANE.toRGBA()),
        };

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T2_USE_EFFECT_DURATION));
        configureCooldown(spell, T2_USE_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry medium_use_fire_power = add(medium_use_fire_power());
    private static Entry medium_use_fire_power() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "medium_use_fire_power");
        var description = "Use: Increases fire spell power by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.MEDIUM_FIRE_POWER;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = activeSpellBase();
        spell.school = SpellSchools.FIRE;

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.INTELLECT_BUFF.id().toString());
        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.flame_medium_a.id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25, 0.1F, 0.1F),
                new ParticleBatch(
                        SpellEngineParticles.flame_spark.id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25, 0.12F, 0.12F)
        };

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T2_USE_EFFECT_DURATION));
        configureCooldown(spell, T2_USE_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry medium_use_frost_power = add(medium_use_frost_power());
    private static Entry medium_use_frost_power() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "medium_use_frost_power");
        var description = "Use: Increases frost spell power by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.MEDIUM_FROST_POWER;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = activeSpellBase();
        spell.school = SpellSchools.FROST;

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.INTELLECT_BUFF.id().toString());
        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.snowflake.id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        15, 0.14F, 0.15F),
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPARK,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25, 0.24F, 0.25F)
                        .color(Color.FROST.toRGBA())
        };

        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T2_USE_EFFECT_DURATION));
        configureCooldown(spell, T2_USE_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry medium_use_healing_power = add(medium_use_healing_power());
    private static Entry medium_use_healing_power() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "medium_use_healing_power");
        var description = "Use: Increases healing spell power by {bonus} for {effect_duration} seconds.";
        var effect = RelicEffects.MEDIUM_HEALING_POWER;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = activeSpellBase();
        spell.school = SpellSchools.HEALING;

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.INTELLECT_BUFF.id().toString());
        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPELL,
                                SpellEngineParticles.MagicParticles.Motion.ASCEND).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        15, 0.08F, 0.08F)
                        .color(Color.HOLY.toRGBA()),
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPARK,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25, 0.24F, 0.25F)
                        .color(Color.HOLY.toRGBA()),
        };
        spell.impacts = List.of(createEffectImpact(effect.id.toString(), T2_USE_EFFECT_DURATION));
        configureCooldown(spell, T2_USE_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    /**
     * SECTION: GREATER TIER SPELLS
     */

    public static Entry greater_perk_roll_damage = add(greater_perk_roll_damage());
    private static Entry greater_perk_roll_damage() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "greater_perk_roll_damage");
        var title = "Lightning Roll";
        var description = "Rolling conjures a small discharge of lightning around you, dealing {damage} damage to nearby enemies.";
        var spell = passiveSpellBase();
        spell.school = SpellSchools.LIGHTNING;
        spell.range = 3;

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.ROLL;
        spell.passive.triggers = List.of(trigger);

        spell.target = new Spell.Target();
        spell.target.type = Spell.Target.Type.AREA;
        spell.target.area = new Spell.Target.Area();

        spell.release.sound = new Sound(RelicSounds.LIGHTNING_IMPACT_SMALL.id().toString());
        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch("spell_engine:electric_arc_a", ParticleBatch.Shape.PILLAR, ParticleBatch.Origin.FEET,
                        null, 15, 0.01F, 0.05F, 0.0F, spell.range),
                new ParticleBatch("spell_engine:electric_arc_b", ParticleBatch.Shape.PILLAR, ParticleBatch.Origin.FEET,
                        null, 15, 0.01F, 0.05F, 0.0F, spell.range)
        };

        var damage = new Spell.Impact();
        damage.action = new Spell.Impact.Action();
        damage.action.type = Spell.Impact.Action.Type.DAMAGE;
        damage.action.damage = new Spell.Impact.Action.Damage();
        damage.action.min_power = 6;
        damage.action.max_power = 12;

        spell.impacts = List.of(damage);
        configureCooldown(spell, 10);

        return new Entry(id, spell, title, description, null);
    }

    public static Entry greater_perk_melee_stun = add(greater_perk_melee_stun());
    private static Entry greater_perk_melee_stun() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "greater_perk_melee_stun");
        var title = "Stunning Strikes";
        var description = "On melee hit: {trigger_chance} chance to stun the target and nearby enemies for {effect_duration} seconds.";
        var spell = passiveSpellBase();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.MELEE_IMPACT;
        trigger.chance = 0.1F;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var stun = createEffectImpact(RelicEffects.STUN.id.toString(), T3_PERK_CC_DURATION);
        stun.sound = new Sound(RelicSounds.STUN_GENERIC.id().toString());
        spell.impacts = List.of(stun);
        spell.area_impact = new Spell.AreaImpact();
        spell.area_impact.radius = 2.0F;

        configureCooldown(spell, T3_PERK_CC_COOLDOWN);

        return new Entry(id, spell, title, description, null);
    }

    public static Entry greater_perk_spell_stun = add(greater_perk_spell_stun());
    private static Entry greater_perk_spell_stun() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "greater_perk_spell_stun");
        var title = "Disruption";
        var description = "On spell hit: {trigger_chance} chance to stun the target and nearby enemies for {effect_duration} seconds.";
        var spell = passiveSpellBase();
        spell.school = SpellSchools.ARCANE;

        var trigger = new Spell.Trigger();
        trigger.chance = 0.1F;
        trigger.type = Spell.Trigger.Type.SPELL_IMPACT_SPECIFIC;
        trigger.impact = new Spell.Trigger.ImpactCondition();
        trigger.impact.impact_type = Spell.Impact.Action.Type.DAMAGE.toString();
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var stun = createEffectImpact(RelicEffects.STUN.id.toString(), T3_PERK_CC_DURATION);
        stun.sound = new Sound(RelicSounds.STUN_GENERIC.id().toString());
        spell.impacts = List.of(stun);
        spell.area_impact = new Spell.AreaImpact();
        spell.area_impact.radius = 2.0F;

        configureCooldown(spell, T3_PERK_CC_COOLDOWN);

        return new Entry(id, spell, title, description, null);
    }

    public static Entry greater_perk_ranged_levitate = add(greater_perk_ranged_levitate());
    private static Entry greater_perk_ranged_levitate() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "greater_perk_ranged_levitate");
        var title = "Levitation";
        var description = "On arrow hit: {trigger_chance} chance to levitate the target and nearby enemies for {effect_duration} seconds.";
        var spell = passiveSpellBase();
        spell.school = ExternalSpellSchools.PHYSICAL_RANGED;

        var trigger = new Spell.Trigger();
        trigger.chance = T2_PROC_CHANCE * 2F;
        trigger.type = Spell.Trigger.Type.ARROW_IMPACT;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var levitate = createEffectImpact(MobEffects.LEVITATION.getKey().identifier().toString(), T3_PERK_CC_DURATION);
        levitate.sound = new Sound(RelicSounds.LEVITATE_GENERIC.id().toString());
        levitate.action.status_effect.amplifier = 3;
        levitate.particles = new ParticleBatch[]{
                new ParticleBatch(SpellEngineParticles.MagicParticles.get(
                        SpellEngineParticles.MagicParticles.Shape.SPARK,
                        SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.FEET,
                        50, 0.15F, 0.5F)
                        .color(Color.NATURE.toRGBA())
        };
        spell.impacts = List.of(levitate);
        spell.area_impact = new Spell.AreaImpact();
        spell.area_impact.radius = 2.0F;

        configureCooldown(spell, T3_PERK_CC_COOLDOWN);

        return new Entry(id, spell, title, description, null);
    }

    public static Entry greater_perk_defense_block = add(greater_perk_defense_block());
    private static Entry greater_perk_defense_block() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "greater_perk_defense_block");
        var title = "Wardstone";
        var description = "Blocking with a shield heals you by {heal}.";
        var spell = passiveSpellBase();
        spell.school = SpellSchools.HEALING;

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.SHIELD_BLOCK;
        spell.passive.triggers = List.of(trigger);

        var heal = new Spell.Impact();
        heal.action = new Spell.Impact.Action();
        heal.action.min_power = 8;
        heal.action.type = Spell.Impact.Action.Type.HEAL;
        heal.action.heal = new Spell.Impact.Action.Heal();
        heal.action.heal.spell_power_coefficient = 0.25F;
        heal.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.HOLY,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25, 0.15F, 0.2F)
                        .color(Color.HOLY.toRGBA()),
        };
        heal.sound = new Sound(SpellEngineSounds.GENERIC_HEALING_IMPACT_1.id());
        spell.impacts = List.of(heal);

        configureCooldown(spell, 4);

        return new Entry(id, spell, title, description, null);
    }

    public static Entry greater_perk_evasion_attack = add(greater_perk_evasion_attack());
    private static Entry greater_perk_evasion_attack() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "greater_perk_evasion_attack");
        var title = "Counterattack";
        var description = "Evading an attack has a {trigger_chance_1} chance to make your next attack {bonus} stronger.";
        var effect = RelicEffects.GREATER_EVASION_ATTACK;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;

        spell.release.sound = new Sound(RelicSounds.HOOK_ACTIVATE.id().toString());
        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPARK,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25, 0.3F, 0.3F)
                        .color(Color.from(0xccffff).toRGBA())
                        .preSpawnTravel(4)
                        .followEntity(true)
                        .invert()
        };

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;
        var trigger = SpellBuilder.Triggers.evade();
        trigger.chance = 0.5F;
        trigger.target_override = Spell.Trigger.TargetSelector.CASTER;
        spell.passive.triggers = List.of(trigger);

        SpellBuilder.Deliver.stash(spell, effect.id.toString(), 10,
                SpellBuilder.Triggers.meleeImpact());

        configureCooldown(spell, 10);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry greater_perk_shield_reset = add(greater_perk_shield_reset());
    public static Entry greater_perk_shield_reset() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "greater_perk_shield_reset");
        var title = "Shield Recharge";
        var description = "Rolling resets the cooldown of your currently held shield.";
        var spell = passiveSpellBase();
        spell.school = SpellSchools.HEALING;

        var trigger = SpellBuilder.Triggers.roll();
        trigger.target_override = Spell.Trigger.TargetSelector.CASTER;
        spell.passive.triggers = List.of(trigger);

        var reset = new Spell.Impact();
        reset.action = new Spell.Impact.Action();
        reset.action.type = Spell.Impact.Action.Type.CUSTOM;
        reset.action.custom = new Spell.Impact.Action.Custom();
        reset.action.custom.handler = RelicMechanics.SHIELD_RESET.toString();

        reset.sound = new Sound(RelicSounds.DEFENSE_ACTIVATE_1.id().toString());
        reset.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPARK,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        20, 0.4F, 0.4F)
                        .color(Color.from(0x66ccff).toRGBA()),
        };

        spell.impacts = List.of(reset);

        return new Entry(id, spell, title, description, null);
    }

    public static Entry greater_perk_heal_cleanse = add(greater_perk_heal_cleanse());
    private static Entry greater_perk_heal_cleanse() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "greater_perk_healing_cleanse");
        var title = "Purification";
        var description = "Healing spells have a {trigger_chance} chance to remove a harmful effect from the target.";
        var spell = passiveSpellBase();
        spell.school = SpellSchools.HEALING;

        var trigger = new Spell.Trigger();
        trigger.chance = 0.5F;
        trigger.type = Spell.Trigger.Type.SPELL_IMPACT_SPECIFIC;
        trigger.impact = new Spell.Trigger.ImpactCondition();
        trigger.impact.impact_type = Spell.Impact.Action.Type.HEAL.toString();
        var condition = new Spell.TargetCondition();
        condition.entity_predicate_id = SpellEntityPredicates.HAS_BAD_EFFECT.id().toString();
        trigger.target_conditions = List.of(condition);
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var cleanse = new Spell.Impact();
        cleanse.action = new Spell.Impact.Action();
        cleanse.action.type = Spell.Impact.Action.Type.STATUS_EFFECT;
        cleanse.action.status_effect = new Spell.Impact.Action.StatusEffect();
        cleanse.action.status_effect.apply_mode = Spell.Impact.Action.StatusEffect.ApplyMode.REMOVE;
        cleanse.action.status_effect.remove = new Spell.Impact.Action.StatusEffect.Remove();
        cleanse.action.status_effect.remove.id = "!" + MobEffects.TRIAL_OMEN.getRegisteredName();
        cleanse.action.status_effect.remove.selector = Spell.Impact.Action.StatusEffect.Remove.Selector.RANDOM;
        cleanse.action.status_effect.remove.select_beneficial = false;
        cleanse.sound = new Sound(RelicSounds.HOLY_WATER_IMPACT.id());
        cleanse.particles = new ParticleBatch[]{
                new ParticleBatch(SpellEngineParticles.MagicParticles.get(
                        SpellEngineParticles.MagicParticles.Shape.SPARK,
                        SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        15, 0.15F, 0.15F)
                        .color(Color.FROST.toRGBA()),
                // "dripping_water"
                new ParticleBatch("dripping_water",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        15, 0.15F, 0.25F),
                new ParticleBatch(SpellEngineParticles.MagicParticles.get(
                        SpellEngineParticles.MagicParticles.Shape.HOLY,
                        SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.FEET,
                        15, 0.3F, 0.3F)
                        .color(Color.HOLY.toRGBA())
        };
        spell.impacts = List.of(cleanse);

        configureCooldown(spell, T3_PERK_CC_COOLDOWN);

        return new Entry(id, spell, title, description, null);
    }

    public static Entry greater_proc_physical_trance = add(greater_proc_physical_trance());
    private static Entry greater_proc_physical_trance() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "greater_proc_physical_trance");
        var description = "On hit: {trigger_chance_1} chance to enter battle trance, increasing melee and ranged attack speed by {bonus}. "
                + "Stacking up to {effect_amplifier_cap} times, lasting for {effect_duration} seconds.";
        var effect = RelicEffects.GREATER_PHYSICAL_TRANCE;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;

        var triggerChance = T3_TRANCE_CHANCE;

        var passiveMeleeTrigger = new Spell.Trigger();
        passiveMeleeTrigger.chance = triggerChance;
        passiveMeleeTrigger.type = Spell.Trigger.Type.MELEE_IMPACT;
        var passiveRangedTrigger = new Spell.Trigger();
        passiveRangedTrigger.chance = triggerChance;
        passiveRangedTrigger.type = Spell.Trigger.Type.ARROW_IMPACT;
        spell.passive.triggers = List.of(passiveMeleeTrigger, passiveRangedTrigger);

        spell.deliver.type = Spell.Delivery.Type.STASH_EFFECT;
        spell.deliver.stash_effect = new Spell.Delivery.StashEffect();
        spell.deliver.stash_effect.id = effect.id.toString();
        spell.deliver.stash_effect.consume = 0;
        var stashMeleeTrigger = new Spell.Trigger();
        stashMeleeTrigger.type = Spell.Trigger.Type.MELEE_IMPACT;
        stashMeleeTrigger.target_override = Spell.Trigger.TargetSelector.CASTER;
        var stashRangedTrigger = new Spell.Trigger();
        stashRangedTrigger.type = Spell.Trigger.Type.ARROW_IMPACT;
        stashRangedTrigger.target_override = Spell.Trigger.TargetSelector.CASTER;
        spell.deliver.stash_effect.triggers = List.of(stashMeleeTrigger, stashRangedTrigger);

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.BLOODLUST_ACTIVATE.id().toString());
        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.HOLY,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.FEET,
                        25, 0.15F, 0.15F)
                        .color(Color.RAGE.toRGBA()),
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SKULL,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.FEET,
                        50, 0.3F, 0.3F)
                        .color(Color.RAGE.toRGBA())
        };

        var buff = createEffectImpact(effect.id.toString(), T3_TRANCE_DURATION);
        buff.action.status_effect.apply_mode = Spell.Impact.Action.StatusEffect.ApplyMode.ADD;
        buff.action.status_effect.amplifier = 1;
        buff.action.status_effect.amplifier_cap = 9;
        buff.action.status_effect.refresh_duration = false;
        spell.impacts = List.of(buff);
        configureCooldown(spell, T3_TRANCE_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry greater_proc_spell_trance = add(greater_proc_spell_trance());
    private static Entry greater_proc_spell_trance() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "greater_proc_spell_trance");
        var description = "On spell hit: {trigger_chance_1} chance to enter magic trance, increasing spell haste by {bonus}. "
                + "Stacking up to {effect_amplifier_cap} times, lasting for {effect_duration} seconds.";
        var effect = RelicEffects.GREATER_SPELL_TRANCE;
        var title = effect.title;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = SpellSchools.ARCANE;

        var trigger = new Spell.Trigger();
        trigger.chance = T3_TRANCE_CHANCE;
        trigger.type = Spell.Trigger.Type.SPELL_IMPACT_SPECIFIC;
        trigger.spell = new Spell.Trigger.SpellCondition();
        trigger.spell.archetype = SpellSchool.Archetype.MAGIC;
        trigger.impact = new Spell.Trigger.ImpactCondition();
        trigger.impact.impact_type = Spell.Impact.Action.Type.DAMAGE.toString();
        spell.passive.triggers = List.of(trigger);

        spell.deliver.type = Spell.Delivery.Type.STASH_EFFECT;
        spell.deliver.stash_effect = new Spell.Delivery.StashEffect();
        spell.deliver.stash_effect.id = effect.id.toString().toString();
        spell.deliver.stash_effect.consume = 0;
        var stashTrigger = new Spell.Trigger();
        stashTrigger.type = Spell.Trigger.Type.SPELL_IMPACT_SPECIFIC;
        stashTrigger.spell = new Spell.Trigger.SpellCondition();
        stashTrigger.spell.archetype = SpellSchool.Archetype.MAGIC;
        stashTrigger.impact = new Spell.Trigger.ImpactCondition();
        stashTrigger.impact.impact_type = Spell.Impact.Action.Type.DAMAGE.toString();
        spell.deliver.stash_effect.triggers = List.of(stashTrigger);

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.SPELL_POWER_ACTIVATE_3.id());
        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPELL,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.FEET,
                        50, 0.15F, 0.15F)
                        .color(Color.FROST.toRGBA()),
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPELL,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.FEET,
                        50, 0.3F, 0.3F)
                        .color(Color.FROST.toRGBA())
        };

        var buff = createEffectImpact(effect.id.toString(), T3_TRANCE_DURATION);
        buff.action.apply_to_caster = true;
        buff.action.status_effect.apply_mode = Spell.Impact.Action.StatusEffect.ApplyMode.ADD;
        buff.action.status_effect.amplifier = 1;
        buff.action.status_effect.amplifier_cap = 9;
        buff.action.status_effect.refresh_duration = false;
        spell.impacts = List.of(buff);
        configureCooldown(spell, T3_TRANCE_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry greater_perk_heal_danger = add(greater_perk_heal_danger());
    private static Entry greater_perk_heal_danger() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "greater_perk_heal_danger");
        var title = "Desperation";
        var health_threshold = 0.5F;
        var description = "Healing a target below {health_threshold} health, receives additional {heal} healing.";
        SpellTooltip.DescriptionMutator mutator = (args) -> args.description().replace("{health_threshold}", SpellTooltip.percent(health_threshold));
        var spell = passiveSpellBase();
        spell.school = SpellSchools.HEALING;

        var trigger = new Spell.Trigger();
        var healthCondition = new Spell.TargetCondition();
        healthCondition.health_percent_below = health_threshold;
        trigger.target_conditions = List.of(healthCondition);
        trigger.type = Spell.Trigger.Type.SPELL_IMPACT_SPECIFIC;
        trigger.impact = new Spell.Trigger.ImpactCondition();
        trigger.impact.impact_type = Spell.Impact.Action.Type.HEAL.toString();
        spell.passive.triggers = List.of(trigger);

        var heal = new Spell.Impact();
        heal.action = new Spell.Impact.Action();
        heal.action.min_power = 10;
        heal.action.type = Spell.Impact.Action.Type.HEAL;
        heal.action.heal = new Spell.Impact.Action.Heal();
        heal.action.heal.spell_power_coefficient = 0.3F;
        heal.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPELL,
                                SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25, 0.45F, 0.7F)
                        .color(Color.NATURE.toRGBA()),
        };
        heal.sound = new Sound(SpellEngineSounds.GENERIC_HEALING_IMPACT_3.id());

        spell.impacts = List.of(heal);

        configureCooldown(spell, 5);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry greater_proc_defense_danger = add(greater_proc_defense_danger());
    private static Entry greater_proc_defense_danger() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "greater_perk_defense_danger");
        var effect = RelicEffects.GREATER_DEFENSE_ARMOR;
        var title = effect.title;
        var health_threshold = 0.4F;
        var description = "Taking damage below {health_threshold} health, increases armor by {bonus} for {effect_duration} seconds.";
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description()
                    .replace("{health_threshold}", SpellTooltip.percent(health_threshold))
                    .replace("{bonus}", bonus);
        };

        var spell = passiveSpellBase();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;
        spell.release.sound = new Sound(RelicSounds.DEFENSE_ACTIVATE_2.id().toString());

        var trigger = new Spell.Trigger();
        var healthCondition = new Spell.TargetCondition();
        healthCondition.health_percent_below = health_threshold;
        trigger.caster_conditions = List.of(healthCondition);
        trigger.type = Spell.Trigger.Type.DAMAGE_TAKEN;
        trigger.target_override = Spell.Trigger.TargetSelector.CASTER;
        spell.passive.triggers = List.of(trigger);

        var buff = createEffectImpact(effect.id.toString(), T3_TRANCE_DURATION);
        buff.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPELL,
                                SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25, 0.55F, 0.8F)
                        .color(Color.WHITE.toRGBA()),
                new ParticleBatch(SpellEngineParticles.shield_small.id().toString(),
                        ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.FEET,
                        15, 0.2F, 0.2F)
        };
        spell.impacts = List.of(buff);

        configureCooldown(spell, T3_TRANCE_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    private static ParticleBatch areaSpellCircle(Color color, float speed) {
        return new ParticleBatch(
                SpellEngineParticles.MagicParticles.get(
                        SpellEngineParticles.MagicParticles.Shape.SPELL,
                        SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.CENTER,
                60, speed, speed)
                .color(color.toRGBA());
    }

    public static Entry superior_use_area_attack_damage = add(superior_use_area_attack_damage());
    private static Entry superior_use_area_attack_damage() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "superior_use_area_attack_damage");
        var effect = RelicEffects.SUPERIOR_ATTACK_DAMAGE;
        var title = effect.title;
        var description = "Use: Increases size, melee and ranged attack damage of nearby allies by {bonus} for {effect_duration} seconds.";
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = activeSpellBase();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;

        spell.range = T4_AREA_RANGE;
        spell.target.type = Spell.Target.Type.AREA;
        spell.target.area = new Spell.Target.Area();
        spell.target.area.include_caster = true;

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = Sound.withRandomness(RelicSounds.HORN_ACTIVATE.id(), 0);
        spell.release.particles = new ParticleBatch[]{
                areaSpellCircle(Color.HOLY, 0.8F),
                areaSpellCircle(Color.HOLY, 1.2F)
        };

        var buff = createEffectImpact(effect.id.toString(), T4_USE_EFFECT_DURATION);
        spell.impacts = List.of(buff);

        configureCooldown(spell, T4_USE_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry superior_use_area_defense_health = add(superior_use_area_defense_health());
    private static Entry superior_use_area_defense_health() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "superior_use_area_defense_health");
        var effect = RelicEffects.SUPERIOR_DEFENSE_HEALTH;
        var title = effect.title;
        var description = "Use: Increases maximum health of nearby allies by {bonus} for {effect_duration} seconds.";
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = activeSpellBase();
        spell.school = SpellSchools.HEALING;

        spell.range = T4_AREA_RANGE;
        spell.target.type = Spell.Target.Type.AREA;
        spell.target.area = new Spell.Target.Area();
        spell.target.area.include_caster = true;

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.HEART_OF_BEAST_ACTIVATE.id().toString());
        spell.release.particles = new ParticleBatch[]{
                areaSpellCircle(Color.RAGE, 0.8F),
                areaSpellCircle(Color.RAGE, 1.2F)
        };

        var heal = new Spell.Impact();
        heal.action = new Spell.Impact.Action();
        heal.action.type = Spell.Impact.Action.Type.HEAL;
        heal.action.heal = new Spell.Impact.Action.Heal();
        heal.attribute = Attributes.MAX_HEALTH.getRegisteredName();
        heal.action.heal.spell_power_coefficient = 0.33F;
        heal.sound = new Sound(SpellEngineSounds.GENERIC_HEALING_IMPACT_3.id());

        var buff = createEffectImpact(effect.id.toString(), T4_USE_EFFECT_DURATION);
        buff.particles = new ParticleBatch[]{
                new ParticleBatch("heart", ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        null, 5, 0.75F, 1.5F, 0.0F, 0)
        };
        spell.impacts = List.of(buff, heal);

        configureCooldown(spell, T4_USE_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    private static ParticleBatch[] zoneParticles(Color color) {
        return new ParticleBatch[]{
                new ParticleBatch(SpellEngineParticles.MagicParticles.get(
                        SpellEngineParticles.MagicParticles.Shape.SPARK,
                        SpellEngineParticles.MagicParticles.Motion.FLOAT).id().toString(),
                        ParticleBatch.Shape.PILLAR, ParticleBatch.Origin.FEET,
                        null, 15, 0.05F, 0.1F, 0.0F, 0F)
                        .color(color.toRGBA()),
                new ParticleBatch(SpellEngineParticles.MagicParticles.get(
                        SpellEngineParticles.MagicParticles.Shape.SPELL,
                        SpellEngineParticles.MagicParticles.Motion.ASCEND).id().toString(),
                        ParticleBatch.Shape.PIPE, ParticleBatch.Origin.FEET,
                        null, 5, 0.1F, 0.2F, 0.0F, 0F)
                        .color(color.toRGBA())
        };
    }

    public static Entry superior_use_zone_spell_power = add(superior_use_zone_spell_power());
    private static Entry superior_use_zone_spell_power() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "superior_use_zone_spell_power");
        var effect = RelicEffects.SUPERIOR_SPELL_POWER;
        var title = effect.title;
        var description = "Use: Conjures a powerful circle, lasting {cloud_duration} seconds. While standing in this circle, the caster gains {bonus} spell power.";
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = activeSpellBase();
        spell.school = SpellSchools.ARCANE;
        spell.range = 0;

        spell.deliver.type = Spell.Delivery.Type.CLOUD;

        var cloud = new Spell.Delivery.Cloud();
        cloud.volume.radius = T4_ZONE_RANGE;
        cloud.time_to_live_seconds = T4_USE_EFFECT_DURATION;
        cloud.client_data.particles = zoneParticles(Color.WHITE);
        cloud.presence_sound = new Sound(RelicSounds.MAGIC_ZONE_PRESENCE.id());
        spell.deliver.clouds = List.of(cloud);

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.SPELL_ZONE_ACTIVATE.id().toString());
        spell.release.particles = new ParticleBatch[]{
                areaSpellCircle(Color.WHITE, 0.6F)
        };

        var buff = createEffectImpact(effect.id.toString(), 1);
        spell.impacts = List.of(buff);

        configureCooldown(spell, T4_USE_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }

    public static Entry superior_use_zone_healing_taken = add(superior_use_zone_healing_taken());
    private static Entry superior_use_zone_healing_taken() {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, "superior_use_zone_healing_taken");
        var effect = RelicEffects.SUPERIOR_HEALING_TAKEN;
        var title = effect.title;
        var description = "Use: Conjures a healing circle, {cloud_radius} blocks, lasting {cloud_duration} seconds. While standing in this circle, allies receives {bonus} more healing.";
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{bonus}", bonus);
        };

        var spell = activeSpellBase();
        spell.school = SpellSchools.HEALING;
        spell.range = 0;

        spell.deliver.type = Spell.Delivery.Type.CLOUD;

        var cloud = new Spell.Delivery.Cloud();
        cloud.volume.radius = T4_ZONE_RANGE;
        cloud.time_to_live_seconds = T4_USE_EFFECT_DURATION;
        cloud.client_data.particles = zoneParticles(Color.FROST);
        cloud.presence_sound = new Sound(RelicSounds.MAGIC_ZONE_PRESENCE.id());
        spell.deliver.clouds = List.of(cloud);

        spell.release.animation = PlayerAnimation.of("spell_engine:dual_handed_weapon_charge");
        spell.release.sound = new Sound(RelicSounds.HEALING_ZONE_ACTIVATE.id().toString());
        spell.release.particles = new ParticleBatch[]{
                areaSpellCircle(Color.FROST, 0.6F)
        };

        var buff = createEffectImpact(effect.id.toString(), 1);
        spell.impacts = List.of(buff);

        configureCooldown(spell, T4_USE_EFFECT_COOLDOWN);

        return new Entry(id, spell, title, description, mutator);
    }
}

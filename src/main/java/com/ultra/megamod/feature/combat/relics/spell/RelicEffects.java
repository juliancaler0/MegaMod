package com.ultra.megamod.feature.combat.relics.spell;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.relics.util.SpellSchoolUtil;
import com.ultra.megamod.lib.rangedweapon.api.EntityAttributes_RangedWeapon;
import com.ultra.megamod.lib.spellengine.api.config.AttributeModifier;
import com.ultra.megamod.lib.spellengine.api.config.ConfigFile;
import com.ultra.megamod.lib.spellengine.api.config.EffectConfig;
import com.ultra.megamod.lib.spellengine.api.effect.ActionImpairing;
import com.ultra.megamod.lib.spellengine.api.effect.Effects;
import com.ultra.megamod.lib.spellengine.api.effect.EntityActionsAllowed;
import com.ultra.megamod.lib.spellengine.api.effect.Synchronized;
import com.ultra.megamod.lib.spellengine.api.entity.SpellEngineAttributes;
import com.ultra.megamod.lib.spellpower.api.SpellPowerMechanics;
import com.ultra.megamod.lib.spellpower.api.SpellSchools;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Ported 1:1 from Relics-1.21.1's RelicEffects.
 * Registers all status effects applied by relic spell procs/buffs.
 */
public class RelicEffects {
    public static final List<Effects.Entry> entries = new ArrayList<>();

    private static Effects.Entry add(Effects.Entry entry) {
        entries.add(entry);
        return entry;
    }

    private static final float T1_BUFF_MULTIPLIER = 0.1F;
    private static final float T2_BUFF_MULTIPLIER = 0.2F;

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MegaMod.MODID, path);
    }

    // ═══════════════════════════════════════════════════════════════
    // TIER 1 Effects
    // ═══════════════════════════════════════════════════════════════
    public static final Effects.Entry LESSER_ATTACK_DAMAGE = add(new Effects.Entry(id("lesser_attack_damage"),
            "Sharpness",
            "Increases attack damage.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x880000),
            new EffectConfig(List.of(
                    new AttributeModifier(Attributes.ATTACK_DAMAGE.getRegisteredName(), T1_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static final Effects.Entry LESSER_ATTACKS_SPEED = add(new Effects.Entry(id("lesser_attack_speed"),
            "Valor",
            "Increases attack speed.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x008800),
            new EffectConfig(List.of(
                    new AttributeModifier(Attributes.ATTACK_SPEED.getRegisteredName(), T1_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE),
                    new AttributeModifier(EntityAttributes_RangedWeapon.HASTE.id.toString(), T1_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static final Effects.Entry LESSER_RANGED_DAMAGE = add(new Effects.Entry(id("lesser_ranged_damage"),
            "Eagle Eye",
            "Increases ranged attack damage.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x000088),
            new EffectConfig(List.of(
                    new AttributeModifier(EntityAttributes_RangedWeapon.DAMAGE.id.toString(), T1_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static Effects.Entry LESSER_SPELL_POWER = add(new Effects.Entry(id("lesser_spell_power"),
            "Spell Power",
            "Increases spell power.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888800),
            new EffectConfig(
                    SpellSchoolUtil.allMagicSchools().stream()
                            .map(school -> new AttributeModifier(school.id.toString(), T1_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE))
                            .toList()
            )
    ));

    public static Effects.Entry LESSER_SPELL_HASTE = add(new Effects.Entry(id("lesser_spell_haste"),
            "Spell Haste",
            "Increases spell haste.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x880088),
            new EffectConfig(List.of(
                    new AttributeModifier(SpellPowerMechanics.HASTE.id.toString(), T1_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static Effects.Entry LESSER_SPELL_CRIT = add(new Effects.Entry(id("lesser_spell_crit_chance"),
            "Volatility",
            "Increases spell critical strike chance.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888888),
            new EffectConfig(List.of(
                    new AttributeModifier(SpellPowerMechanics.CRITICAL_CHANCE.id.toString(), 0.15F, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static Effects.Entry LESSER_POWER_ARCANE_FIRE = add(new Effects.Entry(id("lesser_arcane_fire"),
            "Spell Power",
            "Increases spell power.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888800),
            new EffectConfig(
                    List.of(SpellSchools.ARCANE, SpellSchools.FIRE).stream()
                            .map(school -> new AttributeModifier(school.id.toString(), 0.15F, Operation.ADD_MULTIPLIED_BASE))
                            .toList()
            )
    ));

    public static Effects.Entry LESSER_POWER_FROST_HEALING = add(new Effects.Entry(id("lesser_frost_healing"),
            "Spell Power",
            "Increases spell power.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888800),
            new EffectConfig(
                    List.of(SpellSchools.FROST, SpellSchools.HEALING).stream()
                            .map(school -> new AttributeModifier(school.id.toString(), 0.15F, Operation.ADD_MULTIPLIED_BASE))
                            .toList()
            )
    ));

    public static Effects.Entry LESSER_PROC_CRIT_DAMAGE = add(new Effects.Entry(id("lesser_spell_crit_damage"),
            "Amplify Spell",
            "Increases spell critical damage.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888800),
            new EffectConfig(List.of(
                    new AttributeModifier(SpellPowerMechanics.CRITICAL_CHANCE.id.toString(), 0.5F, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    // ═══════════════════════════════════════════════════════════════
    // TIER 2 Effects
    // ═══════════════════════════════════════════════════════════════
    public static Effects.Entry MEDIUM_ATTACK_DAMAGE = add(new Effects.Entry(id("medium_attack_damage"),
            "Strength",
            "Increases attack damage.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x880000),
            new EffectConfig(List.of(
                    new AttributeModifier(Attributes.ATTACK_DAMAGE.getRegisteredName(), T2_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static Effects.Entry MEDIUM_ATTACKS_SPEED = add(new Effects.Entry(id("medium_attack_speed"),
            "Tempo",
            "Increases attack speed.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x008800),
            new EffectConfig(List.of(
                    new AttributeModifier(Attributes.ATTACK_SPEED.getRegisteredName(), T2_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE),
                    new AttributeModifier(EntityAttributes_RangedWeapon.HASTE.id.toString(), T2_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static Effects.Entry MEDIUM_RANGED_DAMAGE = add(new Effects.Entry(id("medium_ranged_damage"),
            "Power",
            "Increases ranged attack damage.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x000088),
            new EffectConfig(List.of(
                    new AttributeModifier(EntityAttributes_RangedWeapon.DAMAGE.id.toString(), T2_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static Effects.Entry MEDIUM_DEFENSE = add(new Effects.Entry(id("medium_defense"),
            "Toughness",
            "Increases armor toughness.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888888),
            new EffectConfig(List.of(
                    new AttributeModifier(Attributes.ARMOR_TOUGHNESS.getRegisteredName(), 4F, Operation.ADD_VALUE)
            ))
    ));

    public static Effects.Entry MEDIUM_EVASION = add(new Effects.Entry(id("medium_evasion"),
            "Monkey's Agility",
            "Increases evasion chance.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888888),
            new EffectConfig(List.of(
                    new AttributeModifier(SpellEngineAttributes.EVASION_CHANCE.id.toString(), T2_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static Effects.Entry MEDIUM_SPELL_POWER = add(new Effects.Entry(id("medium_spell_power"),
            "Spell Power",
            "Increases spell power.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888800),
            new EffectConfig(
                    SpellSchoolUtil.allMagicSchools().stream()
                            .map(school -> new AttributeModifier(school.id.toString(), T2_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE))
                            .toList()
            )
    ));

    public static Effects.Entry MEDIUM_SPELL_HASTE = add(new Effects.Entry(id("medium_spell_haste"),
            "Spell Haste",
            "Increases spell haste.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x880088),
            new EffectConfig(List.of(
                    new AttributeModifier(SpellPowerMechanics.HASTE.id.toString(), T2_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static Effects.Entry MEDIUM_ARCANE_POWER = add(new Effects.Entry(id("medium_arcane_power"),
            "Arcane Power",
            "Increases spell power.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888800),
            new EffectConfig(
                    List.of(SpellSchools.ARCANE).stream()
                            .map(school -> new AttributeModifier(school.id.toString(), T2_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE))
                            .toList()
            )
    ));

    public static Effects.Entry MEDIUM_FIRE_POWER = add(new Effects.Entry(id("medium_fire_power"),
            "Fire Power",
            "Increases spell power.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888800),
            new EffectConfig(
                    List.of(SpellSchools.FIRE).stream()
                            .map(school -> new AttributeModifier(school.id.toString(), T2_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE))
                            .toList()
            )
    ));

    public static Effects.Entry MEDIUM_FROST_POWER = add(new Effects.Entry(id("medium_frost_power"),
            "Frost Power",
            "Increases spell power.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888800),
            new EffectConfig(
                    List.of(SpellSchools.FROST).stream()
                            .map(school -> new AttributeModifier(school.id.toString(), T2_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE))
                            .toList()
            )
    ));

    public static Effects.Entry MEDIUM_HEALING_POWER = add(new Effects.Entry(id("medium_healing_power"),
            "Healing Power",
            "Increases spell power.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888800),
            new EffectConfig(
                    List.of(SpellSchools.HEALING).stream()
                            .map(school -> new AttributeModifier(school.id.toString(), T2_BUFF_MULTIPLIER, Operation.ADD_MULTIPLIED_BASE))
                            .toList()
            )
    ));

    // ═══════════════════════════════════════════════════════════════
    // STUN + Greater
    // ═══════════════════════════════════════════════════════════════
    public static Effects.Entry STUN = add(new Effects.Entry(id("relic_stun"),
            "Stunned",
            "Cannot move or act.",
            new CustomStatusEffect(MobEffectCategory.HARMFUL, 0x888800),
            new EffectConfig(List.of(
                    new AttributeModifier(Attributes.JUMP_STRENGTH.getRegisteredName(), 0, Operation.ADD_MULTIPLIED_TOTAL)
            ))
    ));

    public static Effects.Entry GREATER_EVASION_ATTACK = add(new Effects.Entry(id("greater_evasion_attack"),
            "Evasion",
            "Increases attack damage.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888888),
            new EffectConfig(List.of(
                    new AttributeModifier(Attributes.ATTACK_DAMAGE.getRegisteredName(), 0.5F, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static Effects.Entry GREATER_PHYSICAL_TRANCE = add(new Effects.Entry(id("greater_physical_trance"),
            "Battle Trance",
            "Increases melee and ranged attack speed.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x880000),
            new EffectConfig(List.of(
                    new AttributeModifier(Attributes.ATTACK_SPEED.getRegisteredName(), 0.1F, Operation.ADD_MULTIPLIED_BASE),
                    new AttributeModifier(EntityAttributes_RangedWeapon.HASTE.id.toString(), 0.1F, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static Effects.Entry GREATER_SPELL_TRANCE = add(new Effects.Entry(id("greater_spell_trance"),
            "Spell Trance",
            "Increases spell haste.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888800),
            new EffectConfig(List.of(
                    new AttributeModifier(SpellPowerMechanics.HASTE.id.toString(), 0.1F, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static Effects.Entry GREATER_DEFENSE_ARMOR = add(new Effects.Entry(id("greater_defense_armor"),
            "Fortitude",
            "Increases armor.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888888),
            new EffectConfig(List.of(
                    new AttributeModifier(Attributes.ARMOR.getRegisteredName(), 10F, Operation.ADD_VALUE)
            ))
    ));

    // ═══════════════════════════════════════════════════════════════
    // SUPERIOR Effects
    // ═══════════════════════════════════════════════════════════════
    public static Effects.Entry SUPERIOR_ATTACK_DAMAGE = add(new Effects.Entry(id("superior_attack_damage"),
            "Might",
            "Increases attack damage.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x880000),
            new EffectConfig(List.of(
                    new AttributeModifier(Attributes.ATTACK_DAMAGE.getRegisteredName(), 0.2F, Operation.ADD_MULTIPLIED_BASE),
                    new AttributeModifier(EntityAttributes_RangedWeapon.DAMAGE.id.toString(), 0.2F, Operation.ADD_MULTIPLIED_BASE),
                    new AttributeModifier(Attributes.SCALE.getRegisteredName(), 0.2F, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static Effects.Entry SUPERIOR_SPELL_POWER = add(new Effects.Entry(id("superior_spell_power"),
            "Spell Power",
            "Increases spell power.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x888800),
            new EffectConfig(
                    SpellSchoolUtil.allOffensiveMagicSchools().stream()
                            .map(school -> new AttributeModifier(school.id.toString(), 0.2F, Operation.ADD_MULTIPLIED_BASE))
                            .toList()
            )
    ));

    public static Effects.Entry SUPERIOR_HEALING_TAKEN = add(new Effects.Entry(id("superior_healing_taken"),
            "Divinity",
            "Increases healing taken.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x008800),
            new EffectConfig(List.of(
                    new AttributeModifier(SpellEngineAttributes.HEALING_TAKEN.id.toString(), 0.5F, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static Effects.Entry SUPERIOR_DEFENSE_HEALTH = add(new Effects.Entry(id("superior_defense_health"),
            "Vigor",
            "Increases maximum health.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x008800),
            new EffectConfig(List.of(
                    new AttributeModifier(Attributes.MAX_HEALTH.getRegisteredName(), 0.5F, Operation.ADD_MULTIPLIED_BASE)
            ))
    ));

    public static void register(ConfigFile.Effects config) {
        ActionImpairing.configure(STUN.effect, EntityActionsAllowed.STUN);

        for (var entry : entries) {
            Synchronized.configure(entry.effect, true);
        }

        Effects.register(entries, config.effects);
    }
}

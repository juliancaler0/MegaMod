/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 */
package com.ultra.megamod.feature.attributes;

import com.ultra.megamod.feature.combat.spell.SpellSchool;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class AttributeHelper {
    private AttributeHelper() {
    }

    public static double getValue(LivingEntity entity, Holder<Attribute> attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return 0.0;
        }
        return instance.getValue();
    }

    public static double getBaseValue(LivingEntity entity, Holder<Attribute> attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return 0.0;
        }
        return instance.getBaseValue();
    }

    public static void addModifier(LivingEntity entity, Holder<Attribute> attribute, Identifier modifierId, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (instance.getModifier(modifierId) != null) {
            instance.removeModifier(modifierId);
        }
        instance.addTransientModifier(new AttributeModifier(modifierId, amount, operation));
    }

    public static void removeModifier(LivingEntity entity, Holder<Attribute> attribute, Identifier modifierId) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (instance.getModifier(modifierId) != null) {
            instance.removeModifier(modifierId);
        }
    }

    public static boolean hasModifier(LivingEntity entity, Holder<Attribute> attribute, Identifier modifierId) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return false;
        }
        return instance.getModifier(modifierId) != null;
    }

    public static void setBaseValue(LivingEntity entity, Holder<Attribute> attribute, double value) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.setBaseValue(value);
    }

    // ─── Spell power helpers (from SpellPower port) ───

    /**
     * Returns the spell power value for a given spell school.
     * Maps school name to the appropriate MegaMod attribute.
     */
    public static double getSpellPower(LivingEntity entity, String school) {
        Holder<Attribute> attr = getSchoolAttribute(school);
        return attr != null ? getValue(entity, attr) : 0.0;
    }

    /**
     * Returns the spell critical strike result.
     * @return double[]{finalDamage, wasCritical (1.0 or 0.0)}
     */
    public static double[] calculateSpellDamage(LivingEntity entity, String school, double baseCoefficient) {
        double power = getSpellPower(entity, school);
        double baseDamage = power * baseCoefficient;
        if (baseDamage <= 0) baseDamage = baseCoefficient; // minimum 1x coefficient as base

        double critChance = getValue(entity, MegaModAttributes.CRITICAL_CHANCE) / 100.0;
        double critDamage = 1.5 + (getValue(entity, MegaModAttributes.CRITICAL_DAMAGE) / 100.0);

        boolean isCritical = entity.getRandom().nextFloat() < critChance;
        double finalDamage = isCritical ? baseDamage * critDamage : baseDamage;
        return new double[]{ finalDamage, isCritical ? 1.0 : 0.0 };
    }

    /**
     * Returns the haste multiplier for spell casting speed.
     * 0 = normal speed, 50 = 50% faster, etc.
     */
    public static double getSpellHaste(LivingEntity entity) {
        return 1.0 + (getValue(entity, MegaModAttributes.SPELL_HASTE) / 100.0);
    }

    /**
     * Returns the cooldown reduction multiplier.
     * 0 = no reduction, 75 = 75% reduction (cap).
     */
    public static double getCooldownMultiplier(LivingEntity entity) {
        double cdr = getValue(entity, MegaModAttributes.COOLDOWN_REDUCTION);
        return 1.0 - (Math.min(cdr, 75.0) / 100.0);
    }

    /**
     * Maps a spell school name to its corresponding attribute holder.
     */
    public static Holder<Attribute> getSchoolAttribute(String school) {
        return switch (school.toUpperCase()) {
            case "ARCANE" -> MegaModAttributes.ARCANE_POWER;
            case "FIRE" -> MegaModAttributes.FIRE_DAMAGE_BONUS;
            case "FROST" -> MegaModAttributes.ICE_DAMAGE_BONUS;
            case "HEALING" -> MegaModAttributes.HEALING_POWER;
            case "LIGHTNING" -> MegaModAttributes.LIGHTNING_DAMAGE_BONUS;
            case "SOUL" -> MegaModAttributes.SOUL_POWER;
            case "HOLY" -> MegaModAttributes.HOLY_DAMAGE_BONUS;
            case "POISON" -> MegaModAttributes.POISON_DAMAGE_BONUS;
            case "SHADOW" -> MegaModAttributes.SHADOW_DAMAGE_BONUS;
            default -> MegaModAttributes.ABILITY_POWER;
        };
    }

    // ─── Spell resistance helpers ───

    /**
     * Maps a SpellSchool to its corresponding resistance attribute holder.
     * Returns null for schools with no resistance attribute (ARCANE, HEALING, PHYSICAL).
     */
    public static Holder<Attribute> getResistanceAttribute(SpellSchool school) {
        return switch (school) {
            case FIRE -> MegaModAttributes.FIRE_RESISTANCE_BONUS;
            case FROST -> MegaModAttributes.ICE_RESISTANCE_BONUS;
            case LIGHTNING -> MegaModAttributes.LIGHTNING_RESISTANCE_BONUS;
            case SOUL -> MegaModAttributes.SHADOW_RESISTANCE_BONUS;
            default -> null; // ARCANE, HEALING, PHYSICAL_MELEE, PHYSICAL_RANGED have no resistance attribute
        };
    }

    /**
     * Returns the target's resistance value for a given spell school.
     * The value is a percentage (0-100) read from the target's resistance attribute.
     * Returns 0 for schools that have no associated resistance attribute.
     */
    public static double getTargetResistance(LivingEntity target, SpellSchool school) {
        Holder<Attribute> resistAttr = getResistanceAttribute(school);
        if (resistAttr == null) return 0.0;
        return getValue(target, resistAttr);
    }
}


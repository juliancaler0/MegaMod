package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.feature.attributes.MegaModAttributes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers 28 combat status effects ported from Wizards, Paladins, Rogues, Archers, and Arsenal.
 * Most effects are pure attribute-modifier-based; a few marker effects have no modifiers.
 */
public class SpellEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, "megamod");

    // ========================== Wizards ==========================

    /** Frozen: -90% movement speed, -100% jump strength. Lasts 6s. */
    public static final DeferredHolder<MobEffect, MobEffect> FROZEN = EFFECTS.register("frozen",
            () -> new SimpleSpellEffect(MobEffectCategory.HARMFUL, 0x88CCFF) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.frozen.speed"),
                            -0.9, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.JUMP_STRENGTH,
                            Identifier.fromNamespaceAndPath("megamod", "effect.frozen.jump"),
                            -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Frost Shield: -30% movement speed. Absorbs next hit (handled in event). */
    public static final DeferredHolder<MobEffect, MobEffect> FROST_SHIELD = EFFECTS.register("frost_shield",
            () -> new SimpleSpellEffect(MobEffectCategory.BENEFICIAL, 0xAADDFF) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.frost_shield.speed"),
                            -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Frost Slowness: -50% movement speed. Lasts 5s. */
    public static final DeferredHolder<MobEffect, MobEffect> FROST_SLOWNESS = EFFECTS.register("frost_slowness",
            () -> new SimpleSpellEffect(MobEffectCategory.HARMFUL, 0x7799CC) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.frost_slowness.speed"),
                            -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Arcane Charge: +15% attack damage per amplifier (stacks to 3). */
    public static final DeferredHolder<MobEffect, MobEffect> ARCANE_CHARGE = EFFECTS.register("arcane_charge",
            () -> new SimpleSpellEffect(MobEffectCategory.BENEFICIAL, 0xCC66FF) {}
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                            Identifier.fromNamespaceAndPath("megamod", "effect.arcane_charge.damage"),
                            0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    // ========================== Paladins ==========================

    /** Divine Protection: +6 armor, +2 armor toughness (~30% damage reduction). */
    public static final DeferredHolder<MobEffect, MobEffect> DIVINE_PROTECTION = EFFECTS.register("divine_protection",
            () -> new SimpleSpellEffect(MobEffectCategory.BENEFICIAL, 0xFFDD44) {}
                    .addAttributeModifier(Attributes.ARMOR,
                            Identifier.fromNamespaceAndPath("megamod", "effect.divine_protection.armor"),
                            6.0, AttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(Attributes.ARMOR_TOUGHNESS,
                            Identifier.fromNamespaceAndPath("megamod", "effect.divine_protection.toughness"),
                            2.0, AttributeModifier.Operation.ADD_VALUE));

    /** Battle Banner: +10% attack speed, +50% knockback resistance. */
    public static final DeferredHolder<MobEffect, MobEffect> BATTLE_BANNER = EFFECTS.register("battle_banner",
            () -> new SimpleSpellEffect(MobEffectCategory.BENEFICIAL, 0xFF4444) {}
                    .addAttributeModifier(Attributes.ATTACK_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.battle_banner.attack_speed"),
                            0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,
                            Identifier.fromNamespaceAndPath("megamod", "effect.battle_banner.knockback_res"),
                            0.5, AttributeModifier.Operation.ADD_VALUE));

    /** Judgement Stun: -100% movement speed, -100% attack speed (stun). */
    public static final DeferredHolder<MobEffect, MobEffect> JUDGEMENT_STUN = EFFECTS.register("judgement_stun",
            () -> new SimpleSpellEffect(MobEffectCategory.HARMFUL, 0xFFCC00) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.judgement_stun.speed"),
                            -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.judgement_stun.attack_speed"),
                            -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Priest Absorption: grants absorption hearts (applied via MobEffectInstance amplifier). */
    public static final DeferredHolder<MobEffect, MobEffect> PRIEST_ABSORPTION = EFFECTS.register("priest_absorption",
            () -> new AbsorptionSpellEffect(MobEffectCategory.BENEFICIAL, 0xFFFF44));

    // ========================== Rogues ==========================

    /** Slice and Dice: +10% attack damage per amplifier (stacks to 9). */
    public static final DeferredHolder<MobEffect, MobEffect> SLICE_AND_DICE = EFFECTS.register("slice_and_dice",
            () -> new SimpleSpellEffect(MobEffectCategory.BENEFICIAL, 0xCC3333) {}
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                            Identifier.fromNamespaceAndPath("megamod", "effect.slice_and_dice.damage"),
                            0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Shock: -100% movement speed, -100% attack speed (stun). */
    public static final DeferredHolder<MobEffect, MobEffect> SHOCK = EFFECTS.register("shock",
            () -> new SimpleSpellEffect(MobEffectCategory.HARMFUL, 0xFFFF00) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.shock.speed"),
                            -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.shock.attack_speed"),
                            -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Shadow Step Buff: +20% movement speed. */
    public static final DeferredHolder<MobEffect, MobEffect> SHADOW_STEP_BUFF = EFFECTS.register("shadow_step_buff",
            () -> new SimpleSpellEffect(MobEffectCategory.BENEFICIAL, 0x333366) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.shadow_step_buff.speed"),
                            0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Stealth: invisibility + -50% movement speed. Breaking on attack is handled in event. */
    public static final DeferredHolder<MobEffect, MobEffect> STEALTH = EFFECTS.register("stealth",
            () -> new SimpleSpellEffect(MobEffectCategory.BENEFICIAL, 0x444444) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.stealth.speed"),
                            -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Stealth Speed: +50% movement speed (applied alongside stealth). */
    public static final DeferredHolder<MobEffect, MobEffect> STEALTH_SPEED = EFFECTS.register("stealth_speed",
            () -> new SimpleSpellEffect(MobEffectCategory.BENEFICIAL, 0x555577) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.stealth_speed.speed"),
                            0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Shatter: -30% armor per amplifier (stacks to 5). */
    public static final DeferredHolder<MobEffect, MobEffect> SHATTER = EFFECTS.register("shatter",
            () -> new SimpleSpellEffect(MobEffectCategory.HARMFUL, 0x996633) {}
                    .addAttributeModifier(Attributes.ARMOR,
                            Identifier.fromNamespaceAndPath("megamod", "effect.shatter.armor"),
                            -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Demoralize: -20% attack damage per amplifier (stacks to 5). */
    public static final DeferredHolder<MobEffect, MobEffect> DEMORALIZE = EFFECTS.register("demoralize",
            () -> new SimpleSpellEffect(MobEffectCategory.HARMFUL, 0x666699) {}
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                            Identifier.fromNamespaceAndPath("megamod", "effect.demoralize.damage"),
                            -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Charge Buff: +50% movement speed, +50% knockback resistance. Cleanses movement-impairing effects every tick. */
    public static final DeferredHolder<MobEffect, MobEffect> CHARGE_BUFF = EFFECTS.register("charge_buff",
            () -> new ChargeBuffEffect(MobEffectCategory.BENEFICIAL, 0xFF6600)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.charge_buff.speed"),
                            0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,
                            Identifier.fromNamespaceAndPath("megamod", "effect.charge_buff.knockback_res"),
                            0.5, AttributeModifier.Operation.ADD_VALUE));

    // ========================== Archers ==========================

    /** Hunter's Mark Stash: marks next arrow to apply hunter's mark (marker effect). */
    public static final DeferredHolder<MobEffect, MobEffect> HUNTERS_MARK_STASH = EFFECTS.register("hunters_mark_stash",
            () -> new SimpleSpellEffect(MobEffectCategory.BENEFICIAL, 0x44AA44) {});

    /** Hunter's Mark: target takes more damage via -15% armor (multiplicative). */
    public static final DeferredHolder<MobEffect, MobEffect> HUNTERS_MARK = EFFECTS.register("hunters_mark",
            () -> new SimpleSpellEffect(MobEffectCategory.HARMFUL, 0x33CC33) {}
                    .addAttributeModifier(Attributes.ARMOR,
                            Identifier.fromNamespaceAndPath("megamod", "effect.hunters_mark.armor"),
                            -0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Entangling Roots: -50% movement speed, -50% jump. */
    public static final DeferredHolder<MobEffect, MobEffect> ENTANGLING_ROOTS = EFFECTS.register("entangling_roots",
            () -> new SimpleSpellEffect(MobEffectCategory.HARMFUL, 0x228822) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.entangling_roots.speed"),
                            -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.JUMP_STRENGTH,
                            Identifier.fromNamespaceAndPath("megamod", "effect.entangling_roots.jump"),
                            -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    // ========================== Arsenal ==========================
    // Note: Arsenal's "stun" effect is registered via ArsenalEffects.STUN (id megamod:arsenal_stun).

    /** Frostbite: -30% movement, -20% attack speed. */
    public static final DeferredHolder<MobEffect, MobEffect> FROSTBITE = EFFECTS.register("frostbite",
            () -> new SimpleSpellEffect(MobEffectCategory.HARMFUL, 0x99BBDD) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.frostbite.speed"),
                            -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.ATTACK_SPEED,
                            Identifier.fromNamespaceAndPath("megamod", "effect.frostbite.attack_speed"),
                            -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Guarding: +4 armor, +2 armor toughness. */
    public static final DeferredHolder<MobEffect, MobEffect> GUARDING = EFFECTS.register("guarding",
            () -> new SimpleSpellEffect(MobEffectCategory.BENEFICIAL, 0x8888CC) {}
                    .addAttributeModifier(Attributes.ARMOR,
                            Identifier.fromNamespaceAndPath("megamod", "effect.guarding.armor"),
                            4.0, AttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(Attributes.ARMOR_TOUGHNESS,
                            Identifier.fromNamespaceAndPath("megamod", "effect.guarding.toughness"),
                            2.0, AttributeModifier.Operation.ADD_VALUE));

    /** Sundering: -30% armor. */
    public static final DeferredHolder<MobEffect, MobEffect> SUNDERING = EFFECTS.register("sundering",
            () -> new SimpleSpellEffect(MobEffectCategory.HARMFUL, 0xAA5500) {}
                    .addAttributeModifier(Attributes.ARMOR,
                            Identifier.fromNamespaceAndPath("megamod", "effect.sundering.armor"),
                            -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Rampaging: +20% attack damage (equivalent to 4 stacks of +5%). */
    public static final DeferredHolder<MobEffect, MobEffect> RAMPAGING = EFFECTS.register("rampaging",
            () -> new SimpleSpellEffect(MobEffectCategory.BENEFICIAL, 0xCC2222) {}
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                            Identifier.fromNamespaceAndPath("megamod", "effect.rampaging.damage"),
                            0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Focusing: +20% ranged damage. */
    public static final DeferredHolder<MobEffect, MobEffect> FOCUSING = EFFECTS.register("focusing",
            () -> new SimpleSpellEffect(MobEffectCategory.BENEFICIAL, 0x44CCAA) {}
                    .addAttributeModifier(MegaModAttributes.RANGED_DAMAGE,
                            Identifier.fromNamespaceAndPath("megamod", "effect.focusing.ranged_damage"),
                            0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Unyielding: +3 knockback resistance, +50% armor toughness. */
    public static final DeferredHolder<MobEffect, MobEffect> UNYIELDING = EFFECTS.register("unyielding",
            () -> new SimpleSpellEffect(MobEffectCategory.BENEFICIAL, 0x666666) {}
                    .addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,
                            Identifier.fromNamespaceAndPath("megamod", "effect.unyielding.knockback_res"),
                            3.0, AttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(Attributes.ARMOR_TOUGHNESS,
                            Identifier.fromNamespaceAndPath("megamod", "effect.unyielding.toughness"),
                            0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Surging: +15% spell critical chance. */
    public static final DeferredHolder<MobEffect, MobEffect> SURGING = EFFECTS.register("surging",
            () -> new SimpleSpellEffect(MobEffectCategory.BENEFICIAL, 0x9944FF) {}
                    .addAttributeModifier(MegaModAttributes.CRITICAL_CHANCE,
                            Identifier.fromNamespaceAndPath("megamod", "effect.surging.critical_chance"),
                            0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Spell Absorption: grants 4 absorption hearts. */
    public static final DeferredHolder<MobEffect, MobEffect> SPELL_ABSORPTION = EFFECTS.register("spell_absorption",
            () -> new AbsorptionSpellEffect(MobEffectCategory.BENEFICIAL, 0xAAAA44));

    // ========================== Inner Classes ==========================

    /** Simple concrete MobEffect subclass (MobEffect is abstract in 1.21.11). */
    private static class SimpleSpellEffect extends MobEffect {
        protected SimpleSpellEffect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }

    /**
     * Absorption effect that grants absorption hearts every tick (capped).
     * Each amplifier level grants (amplifier+1) * 4 absorption HP.
     * Uses applyEffectTick to maintain absorption while active.
     */
    private static class AbsorptionSpellEffect extends MobEffect {
        protected AbsorptionSpellEffect(MobEffectCategory category, int color) {
            super(category, color);
        }

        @Override
        public boolean applyEffectTick(net.minecraft.server.level.ServerLevel level,
                                        net.minecraft.world.entity.LivingEntity entity, int amplifier) {
            float target = (float) (amplifier + 1) * 4;
            if (entity.getAbsorptionAmount() < target) {
                entity.setAbsorptionAmount(target);
            }
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            // Apply on first tick and every 20 ticks (once per second) to maintain absorption
            return duration % 20 == 0;
        }
    }

    /**
     * Charge Buff effect that cleanses movement-impairing effects every tick.
     * Removes: Slowness, Entangling Roots, Frost Slowness, Frozen, Shock.
     */
    private static class ChargeBuffEffect extends MobEffect {
        protected ChargeBuffEffect(MobEffectCategory category, int color) {
            super(category, color);
        }

        @Override
        public boolean applyEffectTick(net.minecraft.server.level.ServerLevel level,
                                        LivingEntity entity, int amplifier) {
            // Cleanse vanilla slowness
            entity.removeEffect(MobEffects.SLOWNESS);
            // Cleanse custom movement-impairing spell effects
            entity.removeEffect(ENTANGLING_ROOTS);
            entity.removeEffect(FROST_SLOWNESS);
            entity.removeEffect(FROZEN);
            entity.removeEffect(SHOCK);
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true; // every tick
        }
    }

    // ========================== Init ==========================

    public static void init(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}

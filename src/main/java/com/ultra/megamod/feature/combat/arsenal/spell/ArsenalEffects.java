package com.ultra.megamod.feature.combat.arsenal.spell;

import com.ultra.megamod.feature.combat.arsenal.ArsenalMod;
import com.ultra.megamod.lib.rangedweapon.api.EntityAttributes_RangedWeapon;
import com.ultra.megamod.lib.spellengine.api.config.AttributeModifier;
import com.ultra.megamod.lib.spellengine.api.config.ConfigFile;
import com.ultra.megamod.lib.spellengine.api.config.EffectConfig;
import com.ultra.megamod.lib.spellengine.api.effect.*;
import com.ultra.megamod.lib.spellengine.api.entity.SpellEngineAttributes;
import com.ultra.megamod.lib.spellpower.api.SpellPowerMechanics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.List;

public class ArsenalEffects {
    public static final List<Effects.Entry> entries = new ArrayList<>();
    private static Effects.Entry add(Effects.Entry entry) {
        entries.add(entry);
        return entry;
    }

    public static Effects.Entry STUN = add(new Effects.Entry(Identifier.fromNamespaceAndPath(ArsenalMod.NAMESPACE, "arsenal_stun"),
            "Stunned",
            "Cannot move or act.",
            new CustomStatusEffect(MobEffectCategory.HARMFUL, 0x888800),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            Attributes.JUMP_STRENGTH.getRegisteredName(),
                            0,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            ))
    ));

    public static Effects.Entry FROSTBITE = add(new Effects.Entry(Identifier.fromNamespaceAndPath(ArsenalMod.NAMESPACE, "arsenal_frostbite"),
            "Frostbite",
            "Slower movement and attack speed.",
            new CustomStatusEffect(MobEffectCategory.HARMFUL, 0x99ccff),
            new EffectConfig(
                    List.of(
                            new AttributeModifier(
                                    Attributes.MOVEMENT_SPEED.getRegisteredName(),
                                    -0.25F,
                                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                            ),
                            new AttributeModifier(
                                    Attributes.ATTACK_SPEED.getRegisteredName(),
                                    -0.25F,
                                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                            )
                    )
            )
    ));

    public static Effects.Entry GUARDING = add(new Effects.Entry(Identifier.fromNamespaceAndPath(ArsenalMod.NAMESPACE, "arsenal_guarding"),
            "Guarding",
            "Increased defense.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x00ff00),
            new EffectConfig(
                    List.of(
                            new AttributeModifier(
                                    SpellEngineAttributes.DAMAGE_TAKEN.id.toString(),
                                    -0.3F,
                                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                            )
                    )
            )
    ));

    public static Effects.Entry SUNDERING = add(new Effects.Entry(Identifier.fromNamespaceAndPath(ArsenalMod.NAMESPACE, "arsenal_sundering"),
            "Sundering",
            "Reduced defense.",
            new CustomStatusEffect(MobEffectCategory.HARMFUL, 0xff0000),
            new EffectConfig(
                    List.of(
                            new AttributeModifier(
                                    Attributes.ARMOR.getRegisteredName(),
                                    -0.3F,
                                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                            )
                    )
            )
    ));

    public static Effects.Entry RAMPAGING = add(new Effects.Entry(Identifier.fromNamespaceAndPath(ArsenalMod.NAMESPACE, "arsenal_rampaging"),
            "Rampaging",
            "Increased attack damage.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0xff9900),
            new EffectConfig(
                    List.of(
                            new AttributeModifier(
                                    Attributes.ATTACK_DAMAGE.getRegisteredName(),
                                    0.05F,
                                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                            )
                    )
            )
    ));

    public static Effects.Entry FOCUSING = add(new Effects.Entry(Identifier.fromNamespaceAndPath(ArsenalMod.NAMESPACE, "arsenal_focusing"),
            "Focusing",
            "Increased ranged attack damage.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0xff9900),
            new EffectConfig(
                    List.of(
                            new AttributeModifier(
                                    EntityAttributes_RangedWeapon.DAMAGE.id.toString(),
                                    0.1F,
                                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                            )
                    )
            )
    ));

    public static Effects.Entry UNYIELDING = add(new Effects.Entry(Identifier.fromNamespaceAndPath(ArsenalMod.NAMESPACE, "arsenal_unyielding"),
            "Unyielding",
            "Increased knockback resistance and toughness.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x00ff00),
            new EffectConfig(
                    List.of(
                            new AttributeModifier(
                                    Attributes.KNOCKBACK_RESISTANCE.getRegisteredName(),
                                    3F,
                                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                            ),
                            new AttributeModifier(
                                    Attributes.ARMOR_TOUGHNESS.getRegisteredName(),
                                    0.5F,
                                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                            )
                    )
            )
    ));

    public static Effects.Entry SURGING = add(new Effects.Entry(Identifier.fromNamespaceAndPath(ArsenalMod.NAMESPACE, "arsenal_surging"),
            "Surging",
            "Increased spell critical chance.",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x00ff00),
            new EffectConfig(
                    List.of(
                            new AttributeModifier(
                                    SpellPowerMechanics.CRITICAL_CHANCE.id.toString(),
                                    0.1F,
                                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                            )
                    )
            )
    ));

    public static final Effects.Entry ABSORPTION = add(new Effects.Entry(
            Identifier.fromNamespaceAndPath(ArsenalMod.NAMESPACE, "arsenal_absorption"),
            "Absorption",
            "Increases maximum absorption",
            new AbsorptionStatusEffect(MobEffectCategory.BENEFICIAL, 0xffffcc),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            Attributes.MAX_ABSORPTION.getRegisteredName(),
                            2,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                    )
            ))
    ));

    public static void register(ConfigFile.Effects config) {
        ActionImpairing.configure(STUN.effect, EntityActionsAllowed.STUN);

        for (var entry: entries) {
            Synchronized.configure(entry.effect, true);
        }

        Effects.register(entries, config.effects);
    }
}

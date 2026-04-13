package com.ultra.megamod.feature.combat.paladins.effect;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.paladins.PaladinsMod;
import com.ultra.megamod.feature.combat.paladins.content.PaladinSounds;
import com.ultra.megamod.lib.spellengine.api.config.AttributeModifier;
import com.ultra.megamod.lib.spellengine.api.config.ConfigFile;
import com.ultra.megamod.lib.spellengine.api.config.EffectConfig;
import com.ultra.megamod.lib.spellengine.api.effect.*;
import com.ultra.megamod.lib.spellengine.api.spell.fx.ParticleBatch;
import com.ultra.megamod.lib.spellpower.api.SpellPowerMechanics;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;

public class PaladinEffects {
    public static final List<Effects.Entry> entries = new ArrayList<>();
    private static Effects.Entry add(Effects.Entry entry) {
        entries.add(entry);
        return entry;
    }

    public static final Effects.Entry DIVINE_PROTECTION = add(new Effects.Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "divine_protection"),
            "Divine Protection",
            "Protects you from the incoming attack",
            new DivineProtectionStatusEffect(MobEffectCategory.BENEFICIAL, 0x66ccff)
    ));

    public static final Effects.Entry BATTLE_BANNER = add(new Effects.Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "battle_banner"),
            "Battle Banner",
            "Increases attack speed, spell haste, and knockback resistance",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0x66ccff),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            Attributes.ATTACK_SPEED.getRegisteredName(),
                            0.4F,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ),
                    new AttributeModifier(
                            SpellPowerMechanics.HASTE.id.toString(),
                            0.4F,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ),
                    new AttributeModifier(
                            Attributes.KNOCKBACK_RESISTANCE.getRegisteredName(),
                            0.4F,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ),
                    new AttributeModifier(
                            Identifier.fromNamespaceAndPath("ranged_weapon", "haste").toString(),
                            0.4F,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    )
            ))
    ));

    public static final Effects.Entry JUDGEMENT = add(new Effects.Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "judgement"),
            "Judgement",
            "Prevents movement and actions",
            new JudgementStatusEffect(MobEffectCategory.HARMFUL, 0xffffcc),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            Attributes.JUMP_STRENGTH.getRegisteredName(),
                            0,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
                )
            )
    ));

    public static final Effects.Entry ABSORPTION = add(new Effects.Entry(
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "priest_absorption"),
            "Absorption",
            "Increases maximum absorption",
            new PriestAbsorptionStatusEffect(MobEffectCategory.BENEFICIAL, 0xffffcc),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            Attributes.MAX_ABSORPTION.getRegisteredName(),
                            2,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                    )
            ))
    ));

    public static void init(IEventBus modEventBus) {
        // Configure effect behaviors (safe during construction - no registry access)
        Synchronized.configure(DIVINE_PROTECTION.effect, true);
        Synchronized.configure(JUDGEMENT.effect, true);
        Synchronized.configure(ABSORPTION.effect, true);
        ActionImpairing.configure(JUDGEMENT.effect, EntityActionsAllowed.STUN);
        // NOTE: Do NOT call Effects.register(entries, ...) here — paladin effects are
        // already registered via SpellEffects.EFFECTS DeferredRegister to avoid duplicates.

        // Defer Protection.register to FMLCommonSetupEvent since it accesses DeferredHolder sound events
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) -> {
            event.enqueueWork(() -> {
                // Use the SpellEffects DeferredHolder as the Holder<MobEffect> for Protection
                var dpHolder = (Holder<MobEffect>) (Holder<?>) com.ultra.megamod.feature.combat.spell.SpellEffects.DIVINE_PROTECTION;
                Protection.register(dpHolder, new Protection.Pop(
                        new ParticleBatch[]{ DivineProtectionStatusEffect.particles },
                        PaladinSounds.divine_protection_impact.soundEvent()
                ));
            });
        });
    }
}

package com.ultra.megamod.feature.combat.paladins.client;

import com.ultra.megamod.feature.combat.items.ClassArmorRegistry;
import com.ultra.megamod.feature.combat.paladins.client.armor.PaladinArmorRenderer;
import com.ultra.megamod.feature.combat.paladins.client.armor.PriestArmorRenderer;
import com.ultra.megamod.feature.combat.paladins.client.effect.DivineProtectionRenderer;
import com.ultra.megamod.feature.combat.paladins.client.entity.BannerEntityRenderer;
import com.ultra.megamod.feature.combat.paladins.client.entity.BarrierEntityRenderer;
import com.ultra.megamod.feature.combat.paladins.effect.PaladinEffects;
import com.ultra.megamod.feature.combat.paladins.entity.PaladinEntities;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRenderer;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRendererRegistry;
import com.ultra.megamod.lib.spellengine.api.effect.CustomModelStatusEffect;
import com.ultra.megamod.lib.spellengine.api.effect.CustomParticleStatusEffect;
import com.ultra.megamod.lib.spellengine.api.render.BuffParticleSpawner;
import com.ultra.megamod.lib.spellengine.api.render.StunParticleSpawner;
import com.ultra.megamod.lib.spellengine.api.spell.fx.ParticleBatch;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineParticles;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.function.Supplier;

public class PaladinsClientMod {
    public static void init() {
        CustomModelStatusEffect.register(PaladinEffects.DIVINE_PROTECTION.effect, new DivineProtectionRenderer());
        CustomParticleStatusEffect.register(PaladinEffects.JUDGEMENT.effect, new StunParticleSpawner());
        CustomParticleStatusEffect.register(
                PaladinEffects.ABSORPTION.effect,
                new BuffParticleSpawner(
                        new ParticleBatch(
                                SpellEngineParticles.aura_effect_553.id().toString(),
                                ParticleBatch.Shape.LINE, ParticleBatch.Origin.CENTER,
                                1, 0, 0)
                                .scale(1.4F)
                                .followEntity(true)
                                .color(Color.HOLY.alpha(0.75F).toRGBA())
                ).withFrequency(30).scaleWithAmplifier(false)
        );

        BarrierEntityRenderer.setup();

        // Paladin armor sets
        registerArmorRenderer(
                ClassArmorRegistry.PALADIN_ARMOR_HEAD.get(),
                ClassArmorRegistry.PALADIN_ARMOR_CHEST.get(),
                ClassArmorRegistry.PALADIN_ARMOR_LEGS.get(),
                ClassArmorRegistry.PALADIN_ARMOR_FEET.get(),
                PaladinArmorRenderer::paladin);

        registerArmorRenderer(
                ClassArmorRegistry.CRUSADER_ARMOR_HEAD.get(),
                ClassArmorRegistry.CRUSADER_ARMOR_CHEST.get(),
                ClassArmorRegistry.CRUSADER_ARMOR_LEGS.get(),
                ClassArmorRegistry.CRUSADER_ARMOR_FEET.get(),
                PaladinArmorRenderer::crusader);

        registerArmorRenderer(
                ClassArmorRegistry.NETHERITE_CRUSADER_ARMOR_HEAD.get(),
                ClassArmorRegistry.NETHERITE_CRUSADER_ARMOR_CHEST.get(),
                ClassArmorRegistry.NETHERITE_CRUSADER_ARMOR_LEGS.get(),
                ClassArmorRegistry.NETHERITE_CRUSADER_ARMOR_FEET.get(),
                PaladinArmorRenderer::netheriteCrusader);

        // Priest robe sets
        registerArmorRenderer(
                ClassArmorRegistry.PRIEST_ROBE_HEAD.get(),
                ClassArmorRegistry.PRIEST_ROBE_CHEST.get(),
                ClassArmorRegistry.PRIEST_ROBE_LEGS.get(),
                ClassArmorRegistry.PRIEST_ROBE_FEET.get(),
                PriestArmorRenderer::priest);

        registerArmorRenderer(
                ClassArmorRegistry.PRIOR_ROBE_HEAD.get(),
                ClassArmorRegistry.PRIOR_ROBE_CHEST.get(),
                ClassArmorRegistry.PRIOR_ROBE_LEGS.get(),
                ClassArmorRegistry.PRIOR_ROBE_FEET.get(),
                PriestArmorRenderer::prior);

        registerArmorRenderer(
                ClassArmorRegistry.NETHERITE_PRIOR_ROBE_HEAD.get(),
                ClassArmorRegistry.NETHERITE_PRIOR_ROBE_CHEST.get(),
                ClassArmorRegistry.NETHERITE_PRIOR_ROBE_LEGS.get(),
                ClassArmorRegistry.NETHERITE_PRIOR_ROBE_FEET.get(),
                PriestArmorRenderer::netheritePrior);
    }

    private static void registerArmorRenderer(Item head, Item chest, Item legs, Item feet,
                                              Supplier<AzArmorRenderer> rendererSupplier) {
        AzArmorRendererRegistry.register(rendererSupplier, head, chest, legs, feet);
    }

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PaladinEntities.BARRIER_TYPE.get(), BarrierEntityRenderer::new);
        event.registerEntityRenderer(PaladinEntities.BANNER_TYPE.get(), BannerEntityRenderer::new);
    }
}

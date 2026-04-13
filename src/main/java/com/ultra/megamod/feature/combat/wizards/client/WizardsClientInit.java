package com.ultra.megamod.feature.combat.wizards.client;

import com.ultra.megamod.feature.combat.items.ClassArmorRegistry;
import com.ultra.megamod.feature.combat.spell.SpellEffects;
import com.ultra.megamod.feature.combat.wizards.client.armor.WizardArmorRenderer;
import com.ultra.megamod.feature.combat.wizards.client.effect.ArcaneChargeRenderer;
import com.ultra.megamod.feature.combat.wizards.client.effect.FrostShieldRenderer;
import com.ultra.megamod.feature.combat.wizards.client.effect.FrozenParticles;
import com.ultra.megamod.feature.combat.wizards.client.effect.FrozenRenderer;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRenderer;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRendererRegistry;
import com.ultra.megamod.lib.spellengine.api.effect.CustomModelStatusEffect;
import com.ultra.megamod.lib.spellengine.api.effect.CustomParticleStatusEffect;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * Client-side initialization for Wizard effect renderers and animated armor.
 * Registers custom model renderers for Arcane Charge, Frozen, and Frost Shield effects,
 * plus AzureLib armor renderers for all 7 wizard robe sets.
 *
 * Called from MegaModClient during client init.
 */
public class WizardsClientInit {

    public static void init() {
        // Register custom status effect renderers
        CustomModelStatusEffect.register(SpellEffects.ARCANE_CHARGE.get(), new ArcaneChargeRenderer());
        CustomParticleStatusEffect.register(SpellEffects.FROST_SLOWNESS.get(), new FrozenParticles(1));
        CustomParticleStatusEffect.register(SpellEffects.FROZEN.get(), new FrozenParticles(2));
        CustomModelStatusEffect.register(SpellEffects.FROZEN.get(), new FrozenRenderer());
        CustomModelStatusEffect.register(SpellEffects.FROST_SHIELD.get(), new FrostShieldRenderer());

        // Register animated armor renderers for all wizard robe sets
        registerArmorRenderer(
                ClassArmorRegistry.WIZARD_ROBE_HEAD.get(),
                ClassArmorRegistry.WIZARD_ROBE_CHEST.get(),
                ClassArmorRegistry.WIZARD_ROBE_LEGS.get(),
                ClassArmorRegistry.WIZARD_ROBE_BOOTS.get(),
                WizardArmorRenderer::wizard);

        registerArmorRenderer(
                ClassArmorRegistry.ARCANE_ROBE_HEAD.get(),
                ClassArmorRegistry.ARCANE_ROBE_CHEST.get(),
                ClassArmorRegistry.ARCANE_ROBE_LEGS.get(),
                ClassArmorRegistry.ARCANE_ROBE_BOOTS.get(),
                WizardArmorRenderer::arcane);

        registerArmorRenderer(
                ClassArmorRegistry.FIRE_ROBE_HEAD.get(),
                ClassArmorRegistry.FIRE_ROBE_CHEST.get(),
                ClassArmorRegistry.FIRE_ROBE_LEGS.get(),
                ClassArmorRegistry.FIRE_ROBE_BOOTS.get(),
                WizardArmorRenderer::fire);

        registerArmorRenderer(
                ClassArmorRegistry.FROST_ROBE_HEAD.get(),
                ClassArmorRegistry.FROST_ROBE_CHEST.get(),
                ClassArmorRegistry.FROST_ROBE_LEGS.get(),
                ClassArmorRegistry.FROST_ROBE_BOOTS.get(),
                WizardArmorRenderer::frost);

        registerArmorRenderer(
                ClassArmorRegistry.NETHERITE_ARCANE_ROBE_HEAD.get(),
                ClassArmorRegistry.NETHERITE_ARCANE_ROBE_CHEST.get(),
                ClassArmorRegistry.NETHERITE_ARCANE_ROBE_LEGS.get(),
                ClassArmorRegistry.NETHERITE_ARCANE_ROBE_BOOTS.get(),
                WizardArmorRenderer::netheriteArcane);

        registerArmorRenderer(
                ClassArmorRegistry.NETHERITE_FIRE_ROBE_HEAD.get(),
                ClassArmorRegistry.NETHERITE_FIRE_ROBE_CHEST.get(),
                ClassArmorRegistry.NETHERITE_FIRE_ROBE_LEGS.get(),
                ClassArmorRegistry.NETHERITE_FIRE_ROBE_BOOTS.get(),
                WizardArmorRenderer::netheriteFire);

        registerArmorRenderer(
                ClassArmorRegistry.NETHERITE_FROST_ROBE_HEAD.get(),
                ClassArmorRegistry.NETHERITE_FROST_ROBE_CHEST.get(),
                ClassArmorRegistry.NETHERITE_FROST_ROBE_LEGS.get(),
                ClassArmorRegistry.NETHERITE_FROST_ROBE_BOOTS.get(),
                WizardArmorRenderer::netheriteFrost);
    }

    private static void registerArmorRenderer(Item head, Item chest, Item legs, Item feet,
                                              Supplier<AzArmorRenderer> rendererSupplier) {
        AzArmorRendererRegistry.register(rendererSupplier, head, chest, legs, feet);
    }
}

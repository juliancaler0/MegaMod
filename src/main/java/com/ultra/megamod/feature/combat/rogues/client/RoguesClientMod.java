package com.ultra.megamod.feature.combat.rogues.client;

import com.ultra.megamod.feature.combat.items.ClassArmorRegistry;
import com.ultra.megamod.feature.combat.rogues.client.armor.RogueArmorRenderer;
import com.ultra.megamod.feature.combat.rogues.client.armor.WarriorArmorRenderer;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRenderer;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRendererRegistry;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * Client-side initialization for Rogues & Warriors content.
 * Registers AzureLib armor renderers for all 6 rogue/warrior armor sets.
 *
 * Called from MegaModClient during client init.
 */
public class RoguesClientMod {

    public static void init() {
        // Rogue armor sets
        registerArmorRenderer(
                ClassArmorRegistry.ROGUE_ARMOR_HEAD.get(),
                ClassArmorRegistry.ROGUE_ARMOR_CHEST.get(),
                ClassArmorRegistry.ROGUE_ARMOR_LEGS.get(),
                ClassArmorRegistry.ROGUE_ARMOR_BOOTS.get(),
                RogueArmorRenderer::rogue);

        registerArmorRenderer(
                ClassArmorRegistry.ASSASSIN_ARMOR_HEAD.get(),
                ClassArmorRegistry.ASSASSIN_ARMOR_CHEST.get(),
                ClassArmorRegistry.ASSASSIN_ARMOR_LEGS.get(),
                ClassArmorRegistry.ASSASSIN_ARMOR_BOOTS.get(),
                RogueArmorRenderer::assassin);

        registerArmorRenderer(
                ClassArmorRegistry.NETHERITE_ASSASSIN_ARMOR_HEAD.get(),
                ClassArmorRegistry.NETHERITE_ASSASSIN_ARMOR_CHEST.get(),
                ClassArmorRegistry.NETHERITE_ASSASSIN_ARMOR_LEGS.get(),
                ClassArmorRegistry.NETHERITE_ASSASSIN_ARMOR_BOOTS.get(),
                RogueArmorRenderer::netheriteAssassin);

        // Warrior armor sets
        registerArmorRenderer(
                ClassArmorRegistry.WARRIOR_ARMOR_HEAD.get(),
                ClassArmorRegistry.WARRIOR_ARMOR_CHEST.get(),
                ClassArmorRegistry.WARRIOR_ARMOR_LEGS.get(),
                ClassArmorRegistry.WARRIOR_ARMOR_BOOTS.get(),
                WarriorArmorRenderer::warrior);

        registerArmorRenderer(
                ClassArmorRegistry.BERSERKER_ARMOR_HEAD.get(),
                ClassArmorRegistry.BERSERKER_ARMOR_CHEST.get(),
                ClassArmorRegistry.BERSERKER_ARMOR_LEGS.get(),
                ClassArmorRegistry.BERSERKER_ARMOR_BOOTS.get(),
                WarriorArmorRenderer::berserker);

        registerArmorRenderer(
                ClassArmorRegistry.NETHERITE_BERSERKER_ARMOR_HEAD.get(),
                ClassArmorRegistry.NETHERITE_BERSERKER_ARMOR_CHEST.get(),
                ClassArmorRegistry.NETHERITE_BERSERKER_ARMOR_LEGS.get(),
                ClassArmorRegistry.NETHERITE_BERSERKER_ARMOR_BOOTS.get(),
                WarriorArmorRenderer::netheriteBerserker);
    }

    private static void registerArmorRenderer(Item head, Item chest, Item legs, Item feet,
                                              Supplier<AzArmorRenderer> rendererSupplier) {
        AzArmorRendererRegistry.register(rendererSupplier, head, chest, legs, feet);
    }
}

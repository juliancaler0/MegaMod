package com.ultra.megamod.feature.combat.archers.client;

import com.ultra.megamod.feature.combat.archers.ArchersMod;
import com.ultra.megamod.feature.combat.archers.client.armor.ArcherArmorRenderer;
import com.ultra.megamod.feature.combat.archers.client.effect.HuntersMarkRenderer;
import com.ultra.megamod.feature.combat.archers.client.effect.RootsRenderer;
import com.ultra.megamod.feature.combat.archers.client.util.ArchersTooltip;
import com.ultra.megamod.feature.combat.archers.effect.ArcherEffects;
import com.ultra.megamod.feature.combat.archers.item.Quivers;
import com.ultra.megamod.feature.combat.items.ClassArmorRegistry;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRenderer;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRendererRegistry;
import com.ultra.megamod.lib.spellengine.api.effect.CustomModelStatusEffect;
import com.ultra.megamod.lib.spellengine.api.render.CustomModels;
import com.ultra.megamod.lib.spellengine.client.gui.SpellTooltip;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Supplier;

public class ArchersClientMod {
    public static void init() {
        CustomModelStatusEffect.register(ArcherEffects.HUNTERS_MARK.effect, new HuntersMarkRenderer());
        CustomModelStatusEffect.register(ArcherEffects.ENTANGLING_ROOTS.effect, new RootsRenderer());

        ArchersTooltip.init();

        SpellTooltip.addDescriptionMutator(Identifier.fromNamespaceAndPath(ArchersMod.ID, "power_shot"), (args) -> {
            var description = args.description();
            var huntersMarkPercent = SpellTooltip.percent(ArcherEffects.HUNTERS_MARK.config().firstModifier().value);
            description = description.replace(SpellTooltip.placeholder("damage_taken"), huntersMarkPercent);
            return description;
        });

        registerArmorRenderer(
                ClassArmorRegistry.ARCHER_ARMOR_HEAD.get(),
                ClassArmorRegistry.ARCHER_ARMOR_CHEST.get(),
                ClassArmorRegistry.ARCHER_ARMOR_LEGS.get(),
                ClassArmorRegistry.ARCHER_ARMOR_FEET.get(),
                ArcherArmorRenderer::archer);

        registerArmorRenderer(
                ClassArmorRegistry.RANGER_ARMOR_HEAD.get(),
                ClassArmorRegistry.RANGER_ARMOR_CHEST.get(),
                ClassArmorRegistry.RANGER_ARMOR_LEGS.get(),
                ClassArmorRegistry.RANGER_ARMOR_FEET.get(),
                ArcherArmorRenderer::ranger);

        registerArmorRenderer(
                ClassArmorRegistry.NETHERITE_RANGER_ARMOR_HEAD.get(),
                ClassArmorRegistry.NETHERITE_RANGER_ARMOR_CHEST.get(),
                ClassArmorRegistry.NETHERITE_RANGER_ARMOR_LEGS.get(),
                ClassArmorRegistry.NETHERITE_RANGER_ARMOR_FEET.get(),
                ArcherArmorRenderer::netheriteRanger);

        List<Identifier> quiverModels = Quivers.entries.stream()
                .map(entry -> Identifier.fromNamespaceAndPath(ArchersMod.ID, "item/quiver/" + entry.id().getPath()))
                .toList();
        CustomModels.registerModelIds(quiverModels);
    }

    private static void registerArmorRenderer(Item head, Item chest, Item legs, Item feet,
                                              Supplier<AzArmorRenderer> rendererSupplier) {
        AzArmorRendererRegistry.register(rendererSupplier, head, chest, legs, feet);
    }
}

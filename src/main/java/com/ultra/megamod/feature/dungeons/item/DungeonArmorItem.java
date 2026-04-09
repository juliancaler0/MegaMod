package com.ultra.megamod.feature.dungeons.item;

import com.ultra.megamod.feature.relics.data.ArmorStatRoller;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Rollable dungeon armor — Chainmail, Iron, Diamond, Netherite variants.
 * Stats rolled via ArmorStatRoller on first inventory tick.
 * When generated through DungeonLootGenerator, stats are applied immediately.
 */
public class DungeonArmorItem extends Item {

    public enum Material {
        CHAINMAIL("Chainmail", Rarity.COMMON, ChatFormatting.GRAY,
            new double[]{2.0, 5.0, 4.0, 1.0},
            new double[]{0.0, 0.0, 0.0, 0.0}),
        IRON("Iron", Rarity.COMMON, ChatFormatting.WHITE,
            new double[]{2.0, 6.0, 5.0, 2.0},
            new double[]{0.0, 0.0, 0.0, 0.0}),
        DIAMOND("Diamond", Rarity.UNCOMMON, ChatFormatting.AQUA,
            new double[]{3.0, 8.0, 6.0, 3.0},
            new double[]{2.0, 2.0, 2.0, 2.0}),
        NETHERITE("Netherite", Rarity.RARE, ChatFormatting.DARK_RED,
            new double[]{3.0, 8.0, 6.0, 3.0},
            new double[]{3.0, 3.0, 3.0, 3.0});

        public final String displayName;
        public final Rarity rarity;
        public final ChatFormatting color;
        private final double[] armor;     // [head, chest, legs, feet]
        private final double[] toughness;

        Material(String displayName, Rarity rarity, ChatFormatting color, double[] armor, double[] toughness) {
            this.displayName = displayName;
            this.rarity = rarity;
            this.color = color;
            this.armor = armor;
            this.toughness = toughness;
        }

        public double getArmor(EquipmentSlot slot) {
            return switch (slot) {
                case HEAD -> armor[0];
                case CHEST -> armor[1];
                case LEGS -> armor[2];
                case FEET -> armor[3];
                default -> 0.0;
            };
        }

        public double getToughness(EquipmentSlot slot) {
            return switch (slot) {
                case HEAD -> toughness[0];
                case CHEST -> toughness[1];
                case LEGS -> toughness[2];
                case FEET -> toughness[3];
                default -> 0.0;
            };
        }
    }

    private final Material material;
    private final EquipmentSlot slot;
    private final String pieceName;

    public DungeonArmorItem(Item.Properties props, Material material, EquipmentSlot slot) {
        super(buildProperties(props, material, slot));
        this.material = material;
        this.slot = slot;
        this.pieceName = switch (slot) {
            case HEAD -> "Helmet";
            case CHEST -> "Chestplate";
            case LEGS -> "Leggings";
            case FEET -> "Boots";
            default -> "Armor";
        };
    }

    private static Item.Properties buildProperties(Item.Properties props, Material material, EquipmentSlot slot) {
        // Use the vanilla equipment asset so the armor renders on the player model
        net.minecraft.resources.ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> asset = switch (material) {
            case CHAINMAIL -> net.minecraft.world.item.equipment.EquipmentAssets.CHAINMAIL;
            case IRON -> net.minecraft.world.item.equipment.EquipmentAssets.IRON;
            case DIAMOND -> net.minecraft.world.item.equipment.EquipmentAssets.DIAMOND;
            case NETHERITE -> net.minecraft.world.item.equipment.EquipmentAssets.NETHERITE;
        };
        return props.stacksTo(1).rarity(material.rarity)
                .component(net.minecraft.core.component.DataComponents.EQUIPPABLE,
                        net.minecraft.world.item.equipment.Equippable.builder(slot).setAsset(asset).build());
    }

    public Material getMaterial() { return material; }
    public EquipmentSlot getArmorSlot() { return slot; }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return slot;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot equippedSlot) {
        if (!ArmorStatRoller.isArmorInitialized(stack)) {
            ArmorStatRoller.rollAndApply(stack, material.getArmor(slot), material.getToughness(slot), slot, level.random);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        ArmorStatRoller.appendArmorTooltip(stack, tooltip);
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal(material.displayName + " " + pieceName).withStyle(material.color));
        tooltip.accept(Component.literal("Dungeon Armor").withStyle(ChatFormatting.DARK_RED));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return material == Material.DIAMOND || material == Material.NETHERITE;
    }
}

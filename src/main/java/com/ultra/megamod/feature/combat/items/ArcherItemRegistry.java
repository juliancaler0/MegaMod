package com.ultra.megamod.feature.combat.items;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.attributes.MegaModAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Consumer;

/**
 * Archer-specific utility items: quivers (offhand ranged damage bonus)
 * and the auto-fire hook (auto-shoot loaded crossbows).
 */
public class ArcherItemRegistry {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);

    // ═══════════════════════════════════════════════════════════════
    // QUIVERS — offhand items that boost ranged damage
    // ═══════════════════════════════════════════════════════════════

    public static final DeferredItem<Item> SMALL_QUIVER = ITEMS.registerItem("small_quiver",
        props -> new QuiverItem(props, 5),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildOffhandModifiers("small_quiver", 0.05)
        ));

    public static final DeferredItem<Item> MEDIUM_QUIVER = ITEMS.registerItem("medium_quiver",
        props -> new QuiverItem(props, 10),
        () -> new Item.Properties().stacksTo(1).attributes(
            buildOffhandModifiers("medium_quiver", 0.10)
        ));

    public static final DeferredItem<Item> LARGE_QUIVER = ITEMS.registerItem("large_quiver",
        props -> new QuiverItem(props, 15),
        () -> new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).attributes(
            buildOffhandModifiers("large_quiver", 0.15)
        ));

    // ═══════════════════════════════════════════════════════════════
    // AUTO-FIRE HOOK — offhand item that auto-fires loaded crossbows
    // ═══════════════════════════════════════════════════════════════

    public static final DeferredItem<Item> AUTO_FIRE_HOOK = ITEMS.registerItem("auto_fire_hook",
        AutoFireHookItem::new,
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // REGISTRATION
    // ═══════════════════════════════════════════════════════════════

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    // ─── Loot tier helpers ───

    public static List<Item> getNormalTierItems() {
        return List.of(SMALL_QUIVER.get());
    }

    public static List<Item> getHardTierItems() {
        return List.of(MEDIUM_QUIVER.get(), AUTO_FIRE_HOOK.get());
    }

    public static List<Item> getNightmareTierItems() {
        return List.of(LARGE_QUIVER.get());
    }

    // ═══════════════════════════════════════════════════════════════
    // ATTRIBUTE BUILDER — applies RANGED_DAMAGE bonus in OFFHAND slot
    // ═══════════════════════════════════════════════════════════════

    private static ItemAttributeModifiers buildOffhandModifiers(String name, double amount) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        AttributeModifier modifier = new AttributeModifier(
            Identifier.fromNamespaceAndPath("megamod", "quiver." + name + ".ranged_damage"),
            amount,
            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
        builder.add(MegaModAttributes.RANGED_DAMAGE, modifier, EquipmentSlotGroup.OFFHAND);
        return builder.build();
    }

    // ═══════════════════════════════════════════════════════════════
    // QUIVER ITEM — simple item with tooltip describing its ranged bonus
    // ═══════════════════════════════════════════════════════════════

    public static class QuiverItem extends Item {
        private final int bonusPercent;

        public QuiverItem(Item.Properties props, int bonusPercent) {
            super(props);
            this.bonusPercent = bonusPercent;
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                    Consumer<Component> tooltip, TooltipFlag flag) {
            tooltip.accept(Component.literal("Hold in offhand for +" + bonusPercent + "% Ranged Damage")
                .withStyle(ChatFormatting.GRAY));
            super.appendHoverText(stack, context, display, tooltip, flag);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // AUTO-FIRE HOOK ITEM — tooltip explains auto-fire behavior
    // ═══════════════════════════════════════════════════════════════

    public static class AutoFireHookItem extends Item {

        public AutoFireHookItem(Item.Properties props) {
            super(props);
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                    Consumer<Component> tooltip, TooltipFlag flag) {
            tooltip.accept(Component.literal("Hold in offhand with a crossbow")
                .withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.literal("Auto-fires when crossbow is fully loaded")
                .withStyle(ChatFormatting.GOLD));
            super.appendHoverText(stack, context, display, tooltip, flag);
        }
    }
}

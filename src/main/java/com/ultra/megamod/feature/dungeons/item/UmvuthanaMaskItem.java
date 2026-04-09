package com.ultra.megamod.feature.dungeons.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class UmvuthanaMaskItem extends Item {
    public enum MaskType {
        FEAR("Fear", "Nearby enemies get Weakness I", ChatFormatting.DARK_PURPLE),
        FURY("Fury", "You deal +10% melee damage", ChatFormatting.RED),
        FAITH("Faith", "Slow passive regeneration", ChatFormatting.WHITE),
        RAGE("Rage", "You attack 10% faster", ChatFormatting.DARK_RED),
        MISERY("Misery", "Nearby enemies get Slowness I", ChatFormatting.GRAY),
        BLISS("Bliss", "You take 10% less damage", ChatFormatting.GOLD);

        public final String displayName;
        public final String description;
        public final ChatFormatting color;

        MaskType(String displayName, String description, ChatFormatting color) {
            this.displayName = displayName;
            this.description = description;
            this.color = color;
        }
    }

    private final MaskType maskType;

    public UmvuthanaMaskItem(Item.Properties props, MaskType maskType) {
        super(props.stacksTo(1).rarity(Rarity.RARE));
        this.maskType = maskType;
    }

    public MaskType getMaskType() {
        return this.maskType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Mask of " + maskType.displayName).withStyle(maskType.color));
        tooltip.accept(Component.literal(maskType.description).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Wear in accessory Face slot").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
    }
}

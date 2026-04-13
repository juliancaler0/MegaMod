package com.ultra.megamod.lib.rangedweapon.client;

import com.ibm.icu.text.DecimalFormat;
import com.ultra.megamod.lib.rangedweapon.api.CustomRangedWeapon;
import com.ultra.megamod.lib.rangedweapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TooltipUtil {
    public static void addPullTime(ItemStack itemStack, List<Component> lines) {
        var pullTime = readablePullTime(itemStack);
        if (pullTime > 0) {
            int lastAttributeLine = getLastAttributeLine(lines);

            if (lastAttributeLine > 0) {
                lines.add(lastAttributeLine + 1,
                        Component.literal(" ").append(
                                Component.translatable("item.ranged_weapon.pull_time", formattedNumber(pullTime / 20F))
                                        .withStyle(ChatFormatting.DARK_GREEN)
                        )
                );
            }
        }
    }

    private static int getLastAttributeLine(List<Component> lines) {
        int lastAttributeLine = -1;
        var attributePrefix = "attribute.modifier";
        var handPrefix = "item.modifiers";
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            var content = line.getContents();
            // Is this a line like "+1 Something"
            if (content instanceof TranslatableContents translatableText) {
                var key = translatableText.getKey();
                if (key.startsWith(attributePrefix) || key.startsWith(handPrefix)) {
                    lastAttributeLine = i;
                }
            }
        }
        return lastAttributeLine;
    }

    private static int readablePullTime(ItemStack itemStack) {
        var item = itemStack.getItem();
        double pullTime = 0;
//        if (item instanceof CustomRangedWeapon customBow) {
//            pullTime = customBow.getRangedWeaponConfig().pull_time();
//        }
        // Don't calculate haste for now, it would be inconsistent with showing damage without modifiers
//        var player = Minecraft.getInstance().player;
//        if (player != null && pullTime > 0) {
//            var haste = player.getAttributeValue(EntityAttributes_RangedWeapon.HASTE.entry);
//            pullTime /= EntityAttributes_RangedWeapon.HASTE.asMultiplier(haste);
//        }
        return (int) pullTime;
    }

    private static String formattedNumber(float number) {
        DecimalFormat formatter = new DecimalFormat();
        formatter.setMaximumFractionDigits(1);
        return formatter.format(number);
    }
}

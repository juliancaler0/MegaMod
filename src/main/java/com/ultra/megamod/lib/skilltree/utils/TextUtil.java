package com.ultra.megamod.lib.skilltree.utils;

import net.minecraft.network.chat.*;
import net.minecraft.locale.Language;

import java.util.List;

public class TextUtil {
    public static String convert(List<Component> lines) {
        StringBuilder builder = new StringBuilder();
        for (Component text : lines) {
            // line.getString();

            System.out.println("attribute.name.spell_power.fire translation: " + Language.getInstance().hasTranslation("attribute.name.spell_power.fire")
            + " " + Language.getInstance().get("attribute.name.spell_power.fire"));

            var string = Component.literal("").append(text).getString();
            if (!string.isEmpty()) {
                if (!builder.isEmpty()) {
                    builder.append("\n");
                }
                builder.append(string);
            }
        }
        return builder.toString();
    }
}

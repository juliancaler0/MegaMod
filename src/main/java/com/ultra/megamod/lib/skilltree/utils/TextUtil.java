package com.ultra.megamod.lib.skilltree.utils;

import net.minecraft.network.chat.*;
import net.minecraft.locale.Language;

import java.util.List;

public class TextUtil {
    public static String convert(List<Component> lines) {
        StringBuilder builder = new StringBuilder();
        for (Component text : lines) {
            // line.getString();

            // Debug line removed - Language API changed in 1.21.11

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

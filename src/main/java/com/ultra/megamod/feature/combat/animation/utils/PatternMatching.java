package com.ultra.megamod.feature.combat.animation.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex pattern matching utility for weapon attribute fallback system.
 * Ported 1:1 from BetterCombat (net.bettercombat.utils.PatternMatching).
 */
public class PatternMatching {
    public static boolean matches(String subject, String nullableRegex) {
        if (subject == null) {
            return false;
        }
        if (nullableRegex == null || nullableRegex.isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile(nullableRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(subject);
        return matcher.find();
    }
}

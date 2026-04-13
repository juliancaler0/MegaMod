package com.ultra.megamod.lib.spellengine.utils;

public class ObjectHelper {
    public static <T> T coalesce(T ...items) {
        for (T i : items) if (i != null) return i;
        return null;
    }
}

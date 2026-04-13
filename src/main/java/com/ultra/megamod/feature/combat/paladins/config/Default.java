package com.ultra.megamod.feature.combat.paladins.config;

import com.ultra.megamod.lib.spellengine.api.config.ConfigFile;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Default {
    public static final ConfigFile.Equipment itemConfig;
    static {
        itemConfig = new ConfigFile.Equipment();
    }

    @SafeVarargs
    private static <T> List<T> joinLists(List<T>... lists) {
        return Arrays.stream(lists).flatMap(Collection::stream).collect(Collectors.toList());
    }
}

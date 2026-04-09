/*
 * Decompiled with CFR 0.152.
 */
package com.ultra.megamod.feature.relics.data;

import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;

public record RelicAbility(String name, String description, int requiredLevel, CastType castType, List<RelicStat> stats) {

    public static enum CastType {
        PASSIVE,
        INSTANTANEOUS,
        TOGGLE;

    }
}


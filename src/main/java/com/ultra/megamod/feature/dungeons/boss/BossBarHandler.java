/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.BossEvent$BossBarColor
 */
package com.ultra.megamod.feature.dungeons.boss;

import com.ultra.megamod.feature.dungeons.boss.DungeonBossEntity;
import net.minecraft.world.BossEvent;

public class BossBarHandler {
    private BossBarHandler() {
    }

    public static void updateBar(DungeonBossEntity boss) {
        boss.bossEvent.setColor(switch (boss.getCurrentPhase()) {
            case 2 -> BossEvent.BossBarColor.YELLOW;
            case 3 -> BossEvent.BossBarColor.RED;
            case 4 -> BossEvent.BossBarColor.PURPLE;
            default -> BossEvent.BossBarColor.BLUE;
        });
        boss.bossEvent.setName(boss.getBossDisplayName());
    }
}


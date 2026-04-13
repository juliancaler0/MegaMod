package com.ultra.megamod.lib.spellengine.compat;

import net.minecraft.world.entity.player.Player;
import com.ultra.megamod.lib.spellengine.internals.SpellTriggers;

import java.util.function.Function;

public class CombatRollCompat {
    public static Function<Player, Boolean> isRolling = player -> {
        return false;
    };

    public static void init() {
        // CombatRoll mod not present in MegaMod - stub only
    }
}

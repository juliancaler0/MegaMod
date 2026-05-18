package com.ultra.megamod.lib.spellengine.compat;

import com.ultra.megamod.lib.combatroll.api.event.Event;
import com.ultra.megamod.lib.combatroll.api.event.ServerSideRollEvents;
import com.ultra.megamod.lib.combatroll.internals.RollingEntity;
import com.ultra.megamod.lib.spellengine.internals.SpellTriggers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class CombatRollCompat {
    /**
     * Client-side check: is the local player currently mid-roll?
     * Used by the spell hotbar to cancel any ongoing cast while rolling.
     * Returns false on dedicated-server entities (no RollManager mixin there).
     */
    public static Function<Player, Boolean> isRolling = player -> {
        if (player instanceof LocalPlayer local && local instanceof RollingEntity rolling) {
            return rolling.getRollManager().isRolling();
        }
        return false;
    };

    public static void init() {
        // Bridge combat-roll completion → SpellEngine ROLL trigger so passive
        // spells like fire_tier_2_passive_1 (Flame Trap) actually fire when
        // the player rolls.
        var proxy = (Event.Proxy<ServerSideRollEvents.PlayerStartRolling>) ServerSideRollEvents.PLAYER_START_ROLLING;
        proxy.handlers.add((player, velocity) -> SpellTriggers.onRoll(player));
    }
}

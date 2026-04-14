package com.ultra.megamod.lib.spellengine.compat;

import com.ultra.megamod.feature.combat.animation.api.EntityPlayer_BetterCombat;

import net.minecraft.world.entity.player.Player;
import com.ultra.megamod.lib.spellengine.api.spell.fx.PlayerAnimation;

import java.util.function.Function;

public class MeleeCompat {
    public record Attack(boolean isCombo, boolean isOffhand) {
        public static final Attack EMPTY = new Attack(false, false);
    }
    public static Function<Player, Attack> attackProperties = player -> {
        var isCombo = false; // Default - no combo detection without BetterCombat
        var isOffhand = false;
        return new Attack(isCombo, isOffhand);
    };
    public static void init() {
        // BetterCombat is bundled into MegaMod (not a separate mod), so wire
        // unconditionally rather than gating on ModList.isLoaded("bettercombat").
        attackProperties = (player) -> {
            var attack = ((EntityPlayer_BetterCombat) player).getCurrentAttack();
            if (attack != null) {
                var isCombo = attack.combo().total() == attack.combo().current();
                var isOffhand = attack.isOffHand();
                return new Attack(isCombo, isOffhand);
            } else {
                return Attack.EMPTY;
            }
        };
        // WeaponRegistry/WeaponAttributesHelper not yet ported
        // PlayerAnimation.twoHandedChecker will use default behavior
    }
}

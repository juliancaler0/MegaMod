package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.combat.runes.RuneCrafter;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin that implements the RuneCrafter interface on Player,
 * providing rune crafting sound timing state.
 * Ported 1:1 from the Runes mod's PlayerEntityMixin.
 */
@Mixin(Player.class)
public class RunePlayerMixin implements RuneCrafter {
    private int megamod_lastRuneCrafted = 0;

    @Override
    public void setLastRuneCrafted(int time) {
        megamod_lastRuneCrafted = time;
    }

    @Override
    public int getLastRuneCrafted() {
        return megamod_lastRuneCrafted;
    }
}

package com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Compile-time stub mirroring {@code io.redspace.ironsspellbooks.api.util.Utils}.
 */
public final class Utils {
    private Utils() { }

    /**
     * Stub: when Iron's Spellbooks is absent this returns {@code null}. Upstream returns the
     * spellbook stack the player is currently carrying.
     */
    public static ItemStack getPlayerSpellbookStack(final ServerPlayer player) {
        return null;
    }
}

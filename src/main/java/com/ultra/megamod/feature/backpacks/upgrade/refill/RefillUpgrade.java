package com.ultra.megamod.feature.backpacks.upgrade.refill;

import com.ultra.megamod.feature.backpacks.upgrade.BackpackUpgrade;

/**
 * Refill upgrade — automatically restocks the player's hotbar from the backpack
 * when an item stack is depleted. Currently a marker upgrade; refill logic will
 * be handled by BackpackEvents.
 */
public class RefillUpgrade extends BackpackUpgrade {

    @Override
    public String getId() {
        return "refill";
    }

    @Override
    public String getDisplayName() {
        return "Refill";
    }
}

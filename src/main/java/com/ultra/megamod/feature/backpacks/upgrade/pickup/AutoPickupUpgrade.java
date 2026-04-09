package com.ultra.megamod.feature.backpacks.upgrade.pickup;

import com.ultra.megamod.feature.backpacks.upgrade.BackpackUpgrade;

/**
 * Auto-Pickup upgrade — marker upgrade that BackpackEvents checks for.
 * When active, items picked up by the player are routed into the backpack inventory
 * before the main inventory. The actual pickup logic lives in BackpackEvents.
 */
public class AutoPickupUpgrade extends BackpackUpgrade {

    @Override
    public String getId() {
        return "auto_pickup";
    }

    @Override
    public String getDisplayName() {
        return "Auto-Pickup";
    }
}

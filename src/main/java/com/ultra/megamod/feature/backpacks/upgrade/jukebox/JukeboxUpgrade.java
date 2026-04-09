package com.ultra.megamod.feature.backpacks.upgrade.jukebox;

import com.ultra.megamod.feature.backpacks.upgrade.BackpackUpgrade;

/**
 * Jukebox upgrade — allows music disc playback from the backpack.
 * Currently a marker upgrade; playback logic will be added later.
 */
public class JukeboxUpgrade extends BackpackUpgrade {

    @Override
    public String getId() {
        return "jukebox";
    }

    @Override
    public String getDisplayName() {
        return "Jukebox";
    }
}

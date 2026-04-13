package com.ultra.megamod.lib.combatroll.utils;

import java.util.List;

public class SoundHelper {
    public static List<String> soundKeys = List.of(
        "roll",
        "roll_cooldown_ready"
    );

    // In NeoForge, sounds are registered via DeferredRegister in CombatRollInit
    // or directly in sounds.json. No manual Registry.register needed.
}

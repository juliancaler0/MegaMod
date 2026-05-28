package com.ultra.megamod.feature.combat;

import com.ultra.megamod.MegaMod;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * Hard-abort guard that prevents MegaMod from loading alongside any standalone copy
 * of the RPG Series mods it has consolidated. The four spell-stack mods plus their
 * shared `rpg_series` subnamespace and two animation libraries register identifiers
 * under fixed namespaces ({@code spell_engine:}, {@code spell_power:}, {@code runes:},
 * {@code wizards:}, {@code rpg_series:}, {@code playeranimator}, {@code azurelibarmor}).
 * Loading both this consolidation and any of those upstreams produces duplicate
 * registry entries deep into mod load — by then the JVM crash is cryptic and the
 * stack trace points at vanilla code, not at the actual root cause.
 *
 * Failing fast at construction time with a clear message keeps the diagnostic short.
 */
public final class ModCollisionCheck {

    private ModCollisionCheck() {}

    private static final String[] CONSOLIDATED_MOD_IDS = {
            "spell_engine",
            "spell_power",
            "runes",
            "wizards",
            "rpg_series",
            "playeranimator",
            "playeranim",
            "azurelibarmor"
    };

    /**
     * Scan the loaded mod list for any namespace MegaMod consolidates. If one or more
     * collisions are present, throw {@link IllegalStateException} with a concise
     * remediation message naming every conflicting mod.
     */
    public static void verifyOrThrow() {
        ModList modList = ModList.get();
        if (modList == null) {
            return;
        }
        List<String> conflicts = new ArrayList<>();
        for (String modId : CONSOLIDATED_MOD_IDS) {
            if (modList.isLoaded(modId)) {
                conflicts.add(modId);
            }
        }
        if (conflicts.isEmpty()) {
            return;
        }
        StringBuilder msg = new StringBuilder("MegaMod consolidates the RPG Series mods and cannot load alongside them. Remove from the mods folder: ");
        for (int i = 0; i < conflicts.size(); i++) {
            if (i > 0) msg.append(", ");
            msg.append(conflicts.get(i));
        }
        msg.append(".");
        MegaMod.LOGGER.error(msg.toString());
        throw new IllegalStateException(msg.toString());
    }
}

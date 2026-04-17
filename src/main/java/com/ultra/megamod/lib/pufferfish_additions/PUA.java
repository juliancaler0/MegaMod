package com.ultra.megamod.lib.pufferfish_additions;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

/**
 * Constants holder for the ported pufferfish_unofficial_additions library.
 * Preserves the {@code pufferfish_unofficial_additions} namespace for data/identifier
 * references so the ported data and identifier paths continue to resolve as the
 * upstream mod does.
 */
public final class PUA {
    public static final String MODID = "pufferfish_unofficial_additions";
    public static final Logger LOG = LogUtils.getLogger();

    private PUA() { }

    public static Identifier location(final String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}

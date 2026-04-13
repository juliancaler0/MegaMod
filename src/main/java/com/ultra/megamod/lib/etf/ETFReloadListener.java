package com.ultra.megamod.lib.etf;

import com.ultra.megamod.lib.etf.features.ETFManager;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * Reload listener that resets the {@link ETFManager} singleton on each resource-pack
 * reload. Equivalent to upstream's {@code MixinResourceReload} / {@code MixinMinecraftClient}
 * pair — NeoForge's {@code AddClientReloadListenersEvent} gives us a first-class hook so
 * we don't need a mixin for it.
 * <p>
 * Rebuilding the manager invalidates the variator/texture/directory caches so newly
 * installed textures and properties files are picked up, and clears every per-entity
 * suffix so a visual reload is possible in-game.
 */
public class ETFReloadListener implements ResourceManagerReloadListener {

    public static final ETFReloadListener INSTANCE = new ETFReloadListener();

    private ETFReloadListener() {}

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        ETFUtils2.logMessage("reloading ETF data.");
        ETFRenderContext.reset();
        ETFManager.resetInstance();
    }
}

package com.ultra.megamod.lib.emf;

import com.ultra.megamod.lib.emf.runtime.EmfModelManager;
import com.ultra.megamod.lib.emf.utils.EMFUtils;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * Resource-reload hook for EMF.
 * <p>
 * On every resource pack reload, drops the compiled {@code .jem} cache on
 * {@link EmfModelManager} so the next frame picks up any changes (e.g. a pack
 * was enabled / disabled / edited in-place).
 * <p>
 * Registered via NeoForge's {@code AddClientReloadListenersEvent} from
 * {@link com.ultra.megamod.MegaModClient}. Mirrors the ETF reload pattern.
 */
public class EmfReloadListener implements ResourceManagerReloadListener {

    public static final EmfReloadListener INSTANCE = new EmfReloadListener();

    private EmfReloadListener() {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        EMFUtils.logWarn("reloading EMF data.");
        EmfModelManager.resetAll();
    }
}

package com.ultra.megamod.lib.emf.runtime;

import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import net.minecraft.resources.Identifier;

/**
 * Installs an {@link ETFUtils2#baseTextureRedirector} that overrides the base
 * entity texture with the {@code .jem texture} value when one is present.
 * <p>
 * Upstream EMF expresses this as an in-line check on the identifier the game
 * resolves to; since our port keeps ETF free of EMF dependencies, we install
 * the hook on EMF's init instead and let ETF's central swap point call through.
 * <p>
 * Install flow:
 * <ol>
 *   <li>{@link com.ultra.megamod.MegaModClient} calls {@link #install()} in
 *       {@code FMLClientSetupEvent} after {@code EmfModelManager.getInstance()}
 *       is constructed.</li>
 *   <li>Every render call the redirector is consulted before the ETF variator
 *       picks a variant. If an EMF active model is bound and declares a custom
 *       texture, we return that; otherwise the vanilla identifier is passed
 *       through unchanged.</li>
 * </ol>
 */
public final class EmfTextureRedirect {

    private static boolean installed = false;

    private EmfTextureRedirect() {
    }

    /** Installs the redirector. Idempotent — repeat calls are no-ops. */
    public static synchronized void install() {
        if (installed) return;
        ETFUtils2.baseTextureRedirector = EmfTextureRedirect::redirect;
        installed = true;
    }

    private static Identifier redirect(Identifier vanilla, ETFEntityRenderState state) {
        if (state == null) return vanilla;
        String key = EmfModelBinder.deriveEntityTypeKey(state);
        if (key == null || key.isEmpty()) return vanilla;

        EmfActiveModel active = EmfModelManager.getInstance().bindForEntity(key, state);
        if (active == null) return vanilla;
        if (active.jemTextureOverride == null) return vanilla;
        return active.jemTextureOverride;
    }
}

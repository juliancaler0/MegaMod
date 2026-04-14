package com.ultra.megamod.lib.etf.mixin;

import com.ultra.megamod.lib.etf.ETF;
import com.ultra.megamod.lib.etf.config.ETFConfig;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

/**
 * Allows otherwise-illegal path characters in {@link Identifier}s when the user has
 * opted in via {@code illegalPathSupportMode}. Mirrors upstream ETF's {@code MixinIdentifier}.
 * <p>
 * Gated by config: {@link ETFConfig.IllegalPathMode#None} falls through to vanilla
 * validation, {@code Entity} allows texture-like paths under {@code entity/},
 * {@code optifine/}, {@code etf/}, and {@code emf/}, and {@code All} whitelists
 * everything.
 */
@Mixin(Identifier.class)
public abstract class MixinIdentifier {

    @Unique
    private static final Set<String> etf$EXCUSED_ILLEGAL_PATHS = new HashSet<>();

    @Inject(method = "isValidPath", cancellable = true, at = @At("RETURN"), require = 0)
    private static void etf$illegalPathOverride(String path, CallbackInfoReturnable<Boolean> cir) {
        if (ETF.config().getConfig() == null) return;
        ETFConfig.IllegalPathMode mode = ETF.config().getConfig().illegalPathSupportMode;
        if (mode == ETFConfig.IllegalPathMode.None) return;
        if (Boolean.TRUE.equals(cir.getReturnValue()) || path == null) return;
        if (path.equals("DUMMY") || path.isBlank()) return;

        switch (mode) {
            case Entity -> {
                if ((path.contains("/entity/") || path.contains("optifine/") || path.contains("etf/") || path.contains("emf/"))
                        && (path.endsWith(".png") || path.endsWith(".properties") || path.endsWith(".mcmeta")
                        || path.endsWith(".jem") || path.endsWith(".jpm"))) {
                    etf$allowPathAndLog(path, cir);
                }
            }
            case All -> etf$allowPathAndLog(path, cir);
            default -> { /* None handled above */ }
        }
    }

    @Unique
    private static void etf$allowPathAndLog(final String path, final CallbackInfoReturnable<Boolean> cir) {
        if (!etf$EXCUSED_ILLEGAL_PATHS.contains(path)) {
            etf$EXCUSED_ILLEGAL_PATHS.add(path);
            ETFUtils2.logWarn("Allowing illegal texture path: [" + path + "]");
        }
        cir.setReturnValue(true);
    }
}

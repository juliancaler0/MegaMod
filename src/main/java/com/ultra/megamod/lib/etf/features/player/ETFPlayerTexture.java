package com.ultra.megamod.lib.etf.features.player;

import com.ultra.megamod.lib.etf.ETF;
import com.ultra.megamod.lib.etf.features.ETFManager;
import com.ultra.megamod.lib.etf.features.texture_handlers.ETFTexture;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Per-player skin-feature container. Stripped-down port of upstream's
 * {@code ETFPlayerTexture} — we reuse the standard emissive/enchant companion suffix
 * detection path ({@link ETFTexture}) for player skins rather than duplicating the
 * upstream pixel-based skin-editor feature set.
 * <p>
 * This covers the functional outcome: if a resource pack ships an {@code _e.png} or
 * {@code _enchanted.png} next to a player's skin (by UUID or name), those overlays
 * render through the {@link com.ultra.megamod.lib.etf.features.ETFEmissiveFeatureLayer}.
 */
public class ETFPlayerTexture {

    public static final String SKIN_NAMESPACE = "etf_skin";

    public final UUID playerId;
    private @Nullable ETFTexture skinTexture;

    public ETFPlayerTexture(AbstractClientPlayer player) {
        this.playerId = player.getUUID();
        refresh(player);
    }

    public void refresh(AbstractClientPlayer player) {
        if (!ETF.config().getConfig().skinFeaturesEnabled) {
            this.skinTexture = null;
            return;
        }
        try {
            Object skin = player.getSkin();
            if (skin == null) return;
            // PlayerSkin accessor name varies by version; use reflection to stay portable.
            Identifier skinId = null;
            try {
                skinId = (Identifier) skin.getClass().getMethod("texture").invoke(skin);
            } catch (NoSuchMethodException nsme) {
                try {
                    skinId = (Identifier) skin.getClass().getMethod("textureLocation").invoke(skin);
                } catch (NoSuchMethodException ignored) {}
            }
            if (skinId != null) {
                this.skinTexture = ETFManager.getInstance().getETFTextureNoVariation(skinId);
            }
        } catch (Throwable t) {
            this.skinTexture = null;
        }
    }

    @Nullable
    public ETFTexture getSkinTexture() {
        return skinTexture;
    }

    public boolean hasFeatures() {
        return skinTexture != null && (skinTexture.isEmissive() || skinTexture.isEnchanted());
    }
}

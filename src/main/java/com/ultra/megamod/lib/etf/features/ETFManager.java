package com.ultra.megamod.lib.etf.features;

import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.texture_handlers.ETFDirectory;
import com.ultra.megamod.lib.etf.utils.EntityIntLRU;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * Phase A slim {@code ETFManager}.
 * <p>
 * The upstream manager also owns the {@code ETFTexture} cache, emissive suffix list,
 * player-texture map, and variator map — all render-side machinery that lands in
 * Phases B/C. Phase A only needs the state the parser / selector reads:
 * <ul>
 *   <li>{@link #KNOWN_RESOURCEPACK_ORDER} — drives pack priority in
 *       {@link com.ultra.megamod.lib.etf.utils.ETFUtils2#returnNameOfHighestPackFromTheseTwo(String, String)}</li>
 *   <li>{@link #ETF_DIRECTORY_CACHE} — used by {@link ETFDirectory}</li>
 *   <li>{@link #LAST_SUFFIX_OF_ENTITY} — read by {@code TextureSuffixProperty}</li>
 *   <li>{@link #LAST_RULE_INDEX_OF_ENTITY} — read by {@code TextureRuleIndexProperty}</li>
 *   <li>{@link #grabSpecialProperties(Properties, ETFEntityRenderState)} — invoked by the
 *       parser. In Phase A this is a no-op; the config toggles it would set (entity
 *       light override, suppress particles, render layer override) all drive rendering
 *       and are deferred.</li>
 * </ul>
 */
public class ETFManager {

    private static ETFManager instance;

    public final ArrayList<String> KNOWN_RESOURCEPACK_ORDER = new ArrayList<>();
    public final HashMap<@NotNull Identifier, @NotNull ETFDirectory> ETF_DIRECTORY_CACHE = new HashMap<>();
    public final EntityIntLRU LAST_SUFFIX_OF_ENTITY = new EntityIntLRU();
    public final EntityIntLRU LAST_RULE_INDEX_OF_ENTITY = new EntityIntLRU();

    private ETFManager() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getResourceManager() != null) {
            for (PackResources pack : mc.getResourceManager().listPacks().toList()) {
                KNOWN_RESOURCEPACK_ORDER.add(pack.packId());
            }
        }
    }

    public static ETFManager getInstance() {
        if (instance == null) {
            instance = new ETFManager();
        }
        return instance;
    }

    public static void resetInstance() {
        instance = new ETFManager();
    }

    /**
     * Phase A no-op. Upstream reads {@code vanillaBrightnessOverride}, {@code suppressParticles},
     * and {@code entityRenderLayerOverride} into the config and applies them at render time.
     * Those features land in Phase C when the full config object comes online.
     */
    public void grabSpecialProperties(Properties props, ETFEntityRenderState entity) {
        // intentional no-op for Phase A
    }
}

package com.ultra.megamod.lib.etf;

import com.ultra.megamod.lib.etf.config.ETFConfigHandler;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderStateViaReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Phase A entry point for the ETF port.
 * <p>
 * Exposes the global {@code config()} handle, the render-state factory (used by the
 * property/predicate layer to construct a state from an entity), and a couple of
 * small environment helpers used by the {@code ModLoadedProperty} and
 * {@code BiomeProperty} predicates.
 * <p>
 * Render-side pieces (mod compatibility warnings, config screens, emissive render
 * constants) are deferred to later phases.
 */
public final class ETF {
    public static final String MOD_ID = "entity_texture_features";
    public static final Logger LOGGER = LoggerFactory.getLogger("Entity Texture Features");

    /**
     * Upstream uses this when painting the emissive pass. Kept as a constant so later
     * phases can reuse the exact magic value; not read by Phase A code.
     */
    public static final int EMISSIVE_FEATURE_LIGHT_VALUE = 15728880 + 2;

    /**
     * Swapped out by downstream consumers (upstream: EMF) to wrap the render state with
     * a different cache/strategy. Defaults to the reference-smuggling implementation.
     */
    public static ETFEntityRenderState.ETFRenderStateInit etfRenderStateConstructor =
            ETFEntityRenderStateViaReference::new;

    private static ETFConfigHandler CONFIG = null;

    public static ETFConfigHandler config() {
        if (CONFIG == null) {
            CONFIG = new ETFConfigHandler();
        }
        return CONFIG;
    }

    /**
     * Returns the biome id string for the given position in the form {@code namespace_path}
     * with the minecraft prefix stripped. Ported 1:1 from upstream ETF's {@code getBiomeString}.
     */
    @Nullable
    public static String getBiomeString(Level world, BlockPos pos) {
        if (world == null || pos == null) return null;
        return world.getBiome(pos).unwrapKey().toString().split(" / ")[1].replaceAll("[^\\da-zA-Z_:-]", "");
    }

    public static boolean isThisModLoaded(String modId) {
        try {
            ModList list = ModList.get();
            if (list != null) {
                return list.isLoaded(modId);
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static List<String> modsLoaded() {
        try {
            ModList list = ModList.get();
            if (list != null) {
                return list.getMods().stream().map(mi -> mi.getModId()).toList();
            }
        } catch (Exception ignored) {}
        return List.of();
    }
}

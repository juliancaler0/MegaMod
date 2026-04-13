package com.ultra.megamod.lib.etf.utils;

import com.ultra.megamod.lib.etf.ETF;
import com.ultra.megamod.lib.etf.features.ETFManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * ETF utility helpers.
 * <p>
 * Phase A port: logging, identifier helpers, properties I/O, pack-order helpers,
 * variant-suffix helpers and the OptiFine hash function. Rendering-side helpers
 * (emissive rendering, model submission, native image I/O) are deferred to Phase B+.
 */
public abstract class ETFUtils2 {

    /**
     * Build an identifier from a namespaced string path (e.g. "megamod:foo/bar.properties").
     */
    public static @NotNull Identifier res(String fullPath) {
        return Identifier.parse(fullPath);
    }

    public static @NotNull Identifier res(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    /**
     * OptiFine integer hashing algorithm. Used for weighted seed offsetting in
     * {@link com.ultra.megamod.lib.etf.features.property_reading.RandomPropertyRule}.
     */
    public static int optifineHashing(int x) {
        x ^= 0x3D ^ x >> 16;
        x += x << 3;
        x ^= x >> 4;
        x *= 668265261;
        x ^= x >> 15;
        return x;
    }

    /**
     * Appends a variant integer to an Identifier path. e.g. {@code foo.png -> foo2.png}.
     * Returns null if nothing would change (variant < 2).
     */
    @Nullable
    public static Identifier addVariantNumberSuffix(@NotNull Identifier identifier, int variant) {
        Identifier changed = res(addVariantNumberSuffix(identifier.toString(), variant));
        return identifier.equals(changed) ? null : changed;
    }

    /**
     * Applies a regex/replace pair to the path component of an identifier.
     * Used by the variator when building the .properties companion path for a .png.
     * Ported 1:1 from upstream.
     */
    @Nullable
    public static Identifier replaceIdentifier(@Nullable Identifier id, String regex, String replace) {
        if (id == null) return null;
        try {
            return res(id.getNamespace(), id.getPath().replaceFirst(regex, replace));
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Texture-swap entry point for the {@code RenderTypes.*} static-factory injector.
     * <p>
     * Returns either {@code identifier} unchanged (no matching entity, or render-layer
     * modification is currently disabled) or the resolved variant identifier from the
     * current entity's variator.
     */
    @NotNull
    public static Identifier getETFVariantNotNullForInjector(@NotNull Identifier identifier) {
        com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState current =
                com.ultra.megamod.lib.etf.features.ETFRenderContext.getCurrentEntityState();
        if (current == null
                || !com.ultra.megamod.lib.etf.features.ETFRenderContext.isAllowedToRenderLayerTextureModify()) {
            return identifier;
        }
        com.ultra.megamod.lib.etf.features.texture_handlers.ETFTexture variant =
                com.ultra.megamod.lib.etf.features.ETFManager.getInstance().getETFTextureVariant(identifier, current);
        // Phase C: publish the current ETFTexture so the ModelPart emissive overlay mixin
        // can add emissive/enchant passes after the base render completes.
        com.ultra.megamod.lib.etf.features.ETFRenderContext.setCurrentTexture(variant);
        Identifier modified = variant.getTextureIdentifier(current);
        return modified == null ? identifier : modified;
    }

    @NotNull
    public static String addVariantNumberSuffix(String identifierString, int variant) {
        if (variant < 2) return identifierString;

        String file = identifierString.endsWith(".png")
                ? "png"
                : identifierString.substring(identifierString.lastIndexOf('.') + 1);

        if (identifierString.matches("\\D+\\d+\\." + file)) {
            return identifierString.replace("." + file, "." + variant + "." + file);
        }
        return identifierString.replace("." + file, variant + "." + file);
    }

    /**
     * Given a list of known pack ids, picks the one that sits highest in the resource-pack order.
     */
    @Nullable
    public static String returnNameOfHighestPackFromTheseMultiple(String[] packNameList) {
        ArrayList<String> packNames = new ArrayList<>(Arrays.asList(packNameList));
        final ArrayList<String> knownResourcepackOrder = ETFManager.getInstance().KNOWN_RESOURCEPACK_ORDER;
        while (packNames.size() > 1) {
            packNames.remove(knownResourcepackOrder.indexOf(packNames.get(0)) >= knownResourcepackOrder.indexOf(packNames.get(1)) ? 1 : 0);
        }
        return packNames.isEmpty() ? null : packNames.get(0);
    }

    @Nullable
    public static String returnNameOfHighestPackFromTheseTwo(@Nullable String pack1, @Nullable String pack2) {
        if (pack1 == null) return null;
        if (pack1.equals(pack2) || pack2 == null) return pack1;

        return ETFManager.getInstance().KNOWN_RESOURCEPACK_ORDER.indexOf(pack1)
                >= ETFManager.getInstance().KNOWN_RESOURCEPACK_ORDER.indexOf(pack2)
                ? pack1 : pack2;
    }

    /**
     * Reads a single {@code .properties} resource, returning a {@link Properties} object, or null if
     * the file is missing or unreadable.
     */
    @Nullable
    public static Properties readAndReturnPropertiesElseNull(Identifier path) {
        Properties props = new Properties();
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return null;
        var resourceOpt = mc.getResourceManager().getResource(path);
        if (resourceOpt.isEmpty()) return null;
        try (InputStream in = resourceOpt.get().open()) {
            props.load(in);
            return props;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Reads every layered {@code .properties} file at the given identifier (all resource packs),
     * returning an ordered list or null if none were found.
     */
    @Nullable
    public static List<Properties> readAndReturnAllLayeredPropertiesElseNull(Identifier path) {
        List<Properties> props = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return null;
        try {
            var resources = mc.getResourceManager().getResourceStack(path);
            for (Resource resource : resources) {
                if (resource == null) continue;
                try (InputStream in = resource.open()) {
                    Properties prop = new Properties();
                    prop.load(in);
                    if (!prop.isEmpty()) {
                        props.add(prop);
                    }
                } catch (Exception ignored) {}
            }
            return props.isEmpty() ? null : props;
        } catch (Exception e) {
            return null;
        }
    }

    public static void logMessage(String obj) {
        logMessage(obj, false);
    }

    public static void logMessage(String obj, boolean inChat) {
        ETF.LOGGER.info("[ETF]: {}", obj);
    }

    public static void logWarn(String obj) {
        logWarn(obj, false);
    }

    public static void logWarn(String obj, boolean inChat) {
        ETF.LOGGER.warn("[ETF]: {}", obj);
    }

    public static void logError(String obj) {
        logError(obj, false);
    }

    public static void logError(String obj, boolean inChat) {
        ETF.LOGGER.error("[ETF]: {}", obj);
    }
}

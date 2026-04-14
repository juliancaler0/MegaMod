package com.ultra.megamod.lib.emf.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ultra.megamod.lib.emf.EMF;
import com.ultra.megamod.lib.emf.jem.EmfJemData;
import com.ultra.megamod.lib.emf.jem.EmfPartData;
import com.ultra.megamod.lib.emf.utils.EMFUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Public Phase D entry point for loading OptiFine-format entity models from the
 * active resource pack.
 * <p>
 * Usage (Phase E will wire this into the entity render path):
 * <pre>
 *     EmfJemData jem = EmfModelLoader.loadJem(
 *             Identifier.fromNamespaceAndPath("minecraft", "optifine/cem/creeper.jem"));
 *     if (jem != null) {
 *         // jem.getAllTopLevelAnimationsByVanillaPartName() is ready to drive renders
 *     }
 * </pre>
 * <p>
 * JSON layout matches upstream exactly, so {@code .jem} / {@code .jpm} files shipped
 * for OptiFine / EMF Fabric work here without modification.
 */
public final class EmfModelLoader {

    private EmfModelLoader() {
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Loads, deserialises, and {@link EmfJemData#prepare prepares} a {@code .jem} file.
     * Returns {@code null} if the resource is missing or fails to parse.
     */
    @Nullable
    public static EmfJemData loadJem(Identifier jemPath) {
        return loadJem(jemPath, defaultResourceManager());
    }

    @Nullable
    public static EmfJemData loadJem(Identifier jemPath, ResourceManager resourceManager) {
        try {
            Optional<Resource> res = resourceManager.getResource(jemPath);
            if (res.isEmpty()) {
                if (EMF.logModelCreationData) EMFUtils.log("jem missing: " + jemPath);
                return null;
            }
            EmfJemData jem;
            try (InputStream in = res.get().open();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                jem = GSON.fromJson(reader, EmfJemData.class);
            }
            if (jem == null) {
                EMFUtils.logWarn("jem parsed as null: " + jemPath);
                return null;
            }
            // Wire a resolver that looks sibling .jpm files up inside the same namespace/folder
            Identifier folder = dirOf(jemPath);
            jem.prepare(jemPath.toString(), modelPath -> loadJpm(resolveRelative(folder, modelPath, "jpm"), resourceManager));
            return jem;
        } catch (Exception e) {
            EMFUtils.logError("jem read failed for " + jemPath + ": " + e);
            if (EMF.logModelCreationData) e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads and deserialises a {@code .jpm} file. {@link EmfPartData#prepare} is NOT
     * called automatically; the outer {@code .jem} does so once it copies the part in.
     * Returns {@code null} if the resource is missing or fails to parse.
     */
    @Nullable
    public static EmfPartData loadJpm(Identifier jpmPath) {
        return loadJpm(jpmPath, defaultResourceManager());
    }

    @Nullable
    public static EmfPartData loadJpm(Identifier jpmPath, ResourceManager resourceManager) {
        if (jpmPath == null) return null;
        try {
            Optional<Resource> res = resourceManager.getResource(jpmPath);
            if (res.isEmpty()) {
                if (EMF.logModelCreationData) EMFUtils.log("jpm missing: " + jpmPath);
                return null;
            }
            try (InputStream in = res.get().open();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return GSON.fromJson(reader, EmfPartData.class);
            }
        } catch (Exception e) {
            EMFUtils.logError("jpm read failed for " + jpmPath + ": " + e);
            if (EMF.logModelCreationData) e.printStackTrace();
            return null;
        }
    }

    // ---- helpers ----

    private static ResourceManager defaultResourceManager() {
        return Minecraft.getInstance().getResourceManager();
    }

    /** Returns the directory portion of {@code id} as an {@link Identifier}. */
    private static Identifier dirOf(Identifier id) {
        String path = id.getPath();
        int slash = path.lastIndexOf('/');
        String dir = slash < 0 ? "" : path.substring(0, slash);
        return Identifier.fromNamespaceAndPath(id.getNamespace(), dir);
    }

    /**
     * Resolves an OptiFine-style path. Supported prefixes:
     * <ul>
     *     <li>{@code namespace:folder/file} — absolute</li>
     *     <li>{@code ./file} — relative to the declaring .jem</li>
     *     <li>{@code ~/file} — relative to {@code assets/minecraft/optifine/}</li>
     *     <li>{@code folder/file} — relative to the namespace root</li>
     *     <li>{@code file} (no slash) — same folder as the declaring .jem</li>
     * </ul>
     */
    @Nullable
    private static Identifier resolveRelative(Identifier folder, String raw, String extension) {
        if (raw == null || raw.isBlank()) return null;
        String path = raw.trim();
        if (extension != null && !path.endsWith('.' + extension)) path += '.' + extension;

        if (path.contains(":")) {
            return Identifier.tryParse(path);
        }
        if (path.startsWith("~/")) {
            return Identifier.fromNamespaceAndPath("minecraft", "optifine/" + path.substring(2));
        }
        if (path.startsWith("./")) {
            return Identifier.fromNamespaceAndPath(folder.getNamespace(),
                    folder.getPath().isEmpty() ? path.substring(2) : folder.getPath() + "/" + path.substring(2));
        }
        if (!path.contains("/")) {
            return Identifier.fromNamespaceAndPath(folder.getNamespace(),
                    folder.getPath().isEmpty() ? path : folder.getPath() + "/" + path);
        }
        return Identifier.fromNamespaceAndPath(folder.getNamespace(), path);
    }
}

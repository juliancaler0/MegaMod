package com.ultra.megamod.feature.citizen.blueprint.packs;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.ultra.megamod.feature.citizen.blueprint.Blueprint;
import com.ultra.megamod.feature.citizen.blueprint.BlueprintUtil;
import com.ultra.megamod.feature.citizen.blueprint.BlockInfo;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Central structure pack management singleton.
 *
 * <p>Structure packs are loaded from the {@code blueprints/megamod/} directory.
 * Each subdirectory is a pack containing a {@code pack.json} metadata file and
 * {@code .blueprint} files (compressed NBT).
 *
 * <p>Pack directory structure:
 * <pre>
 *   blueprints/megamod/
 *       medieval/
 *           pack.json
 *           buildings/townhall1.blueprint
 *           buildings/barracks1.blueprint
 *           ...
 *       nordic/
 *           pack.json
 *           ...
 * </pre>
 */
public class StructurePacks {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    /** All loaded packs, keyed by pack ID (directory name). */
    public static final Map<String, StructurePackMeta> loadedPacks = new LinkedHashMap<>();

    /** The currently selected pack ID. */
    public static String selectedPack = "";

    /** Whether packs have already been extracted from the JAR this session. */
    private static boolean packsExtracted = false;

    /**
     * Scans the given packs directory for structure packs.
     * Each subdirectory containing a pack.json is treated as a pack.
     * On first call, automatically extracts bundled packs from the mod JAR
     * into the target directory if they are not already present.
     *
     * @param packsDir the root packs directory (e.g., blueprints/megamod/)
     */
    public static void discoverPacks(Path packsDir) {
        loadedPacks.clear();

        // Extract bundled packs from JAR on first discovery
        if (!packsExtracted) {
            extractPacksFromJar(packsDir);
            packsExtracted = true;
        }

        if (!Files.exists(packsDir)) {
            LOGGER.info("Structure packs directory does not exist: {}", packsDir);
            try {
                Files.createDirectories(packsDir);
            } catch (IOException e) {
                LOGGER.warn("Failed to create packs directory: {}", e.getMessage());
            }
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(packsDir)) {
            for (Path entry : stream) {
                if (!Files.isDirectory(entry)) continue;

                Path packJson = entry.resolve("pack.json");
                if (!Files.exists(packJson)) continue;

                try {
                    String jsonContent = new String(Files.readAllBytes(packJson), StandardCharsets.UTF_8);
                    JsonObject json = GSON.fromJson(jsonContent, JsonObject.class);
                    String id = entry.getFileName().toString();
                    StructurePackMeta meta = StructurePackMeta.fromJson(json, id, entry);
                    loadedPacks.put(id, meta);
                    LOGGER.debug("Loaded structure pack: {} ({})", meta.getName(), id);
                } catch (Exception e) {
                    LOGGER.warn("Failed to load pack.json in {}: {}", entry, e.getMessage());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to scan packs directory: {}", e.getMessage());
        }

        LOGGER.info("Discovered {} structure pack(s)", loadedPacks.size());

        // Auto-select first pack if none selected
        if ((selectedPack.isEmpty() || !loadedPacks.containsKey(selectedPack)) && !loadedPacks.isEmpty()) {
            selectedPack = loadedPacks.keySet().iterator().next();
        }
    }

    /**
     * Loads a full Blueprint object from a pack.
     * Uses {@link BlueprintUtil#readBlueprintFromFile(Path)} for proper deserialization.
     *
     * @param packId        the pack identifier
     * @param blueprintPath the relative path to the blueprint within the pack (e.g., "buildings/townhall1")
     * @return the loaded Blueprint, or null if loading fails
     */
    @Nullable
    public static Blueprint loadBlueprintFull(String packId, String blueprintPath) {
        StructurePackMeta pack = loadedPacks.get(packId);
        if (pack == null) {
            LOGGER.warn("Unknown pack: {}", packId);
            return null;
        }

        String fullPath = blueprintPath.endsWith(".blueprint") ? blueprintPath : blueprintPath + ".blueprint";
        Path file = pack.getPath().resolve(fullPath);

        if (!Files.exists(file)) {
            LOGGER.warn("Blueprint file not found: {}", file);
            return null;
        }

        Blueprint bp = BlueprintUtil.readBlueprintFromFile(file);
        if (bp != null) {
            bp.setPackName(packId);
        }
        return bp;
    }

    /**
     * Loads a blueprint and returns its block info list.
     * Convenience method for callers that only need block placement data.
     *
     * @param packId        the pack identifier
     * @param blueprintPath the relative path to the blueprint within the pack
     * @return list of BlockInfo from the blueprint, or null if loading fails
     */
    @Nullable
    public static List<BlockInfo> loadBlueprint(String packId, String blueprintPath) {
        Blueprint bp = loadBlueprintFull(packId, blueprintPath);
        if (bp == null) return null;
        return bp.getBlockInfoAsList();
    }

    /**
     * Lists all .blueprint files in a pack, returning relative paths.
     *
     * @param packId the pack identifier
     * @return list of relative blueprint file paths within the pack
     */
    public static List<String> getBlueprintList(String packId) {
        List<String> results = new ArrayList<>();
        StructurePackMeta pack = loadedPacks.get(packId);
        if (pack == null) return results;

        try (Stream<Path> walk = Files.walk(pack.getPath())) {
            walk.filter(p -> p.toString().endsWith(".blueprint"))
                .forEach(p -> {
                    String relative = pack.getPath().relativize(p).toString().replace('\\', '/');
                    results.add(relative);
                });
        } catch (IOException e) {
            LOGGER.warn("Failed to list blueprints in pack {}: {}", packId, e.getMessage());
        }

        results.sort(String::compareToIgnoreCase);
        return results;
    }

    /**
     * Returns the metadata for a pack by its ID.
     *
     * @param id the pack identifier
     * @return the pack metadata, or null if not found
     */
    @Nullable
    public static StructurePackMeta getPack(String id) {
        return loadedPacks.get(id);
    }

    /**
     * Returns all loaded pack metadata.
     */
    public static Collection<StructurePackMeta> getAllPacks() {
        return loadedPacks.values();
    }

    /**
     * Returns the currently selected pack ID.
     */
    public static String getSelectedPack() {
        return selectedPack;
    }

    /**
     * Sets the currently selected pack ID.
     */
    public static void setSelectedPack(String packId) {
        if (loadedPacks.containsKey(packId)) {
            selectedPack = packId;
        }
    }

    /**
     * Extracts bundled structure packs from the mod JAR (classpath resource
     * {@code blueprints/megamod/}) into the given target directory on the filesystem.
     * Skips files that already exist so player modifications are preserved.
     *
     * @param targetDir the filesystem directory to extract into (e.g. {@code <gameDir>/blueprints/megamod/})
     */
    private static void extractPacksFromJar(Path targetDir) {
        String resourceRoot = "blueprints/megamod";
        try {
            URL rootUrl = StructurePacks.class.getClassLoader().getResource(resourceRoot);
            if (rootUrl == null) {
                LOGGER.info("No bundled blueprint packs found on classpath at '{}'", resourceRoot);
                return;
            }

            URI rootUri = rootUrl.toURI();

            if ("file".equals(rootUri.getScheme())) {
                // Dev environment -- blueprints are on the filesystem (src/main/resources)
                Path sourcePath = Paths.get(rootUri);
                copyDirectoryTree(sourcePath, targetDir);
            } else if ("jar".equals(rootUri.getScheme()) || rootUri.getScheme() != null) {
                // Production environment -- blueprints are inside the JAR
                // URI format: jar:file:/path/to/mod.jar!/blueprints/megamod
                String jarUriStr = rootUri.toString();
                int bangIdx = jarUriStr.indexOf('!');
                if (bangIdx < 0) {
                    // Fallback: try as a normal path
                    LOGGER.warn("Unable to parse JAR URI for blueprint extraction: {}", jarUriStr);
                    return;
                }
                String jarPart = jarUriStr.substring(0, bangIdx);
                FileSystem jarFs = null;
                boolean weCreatedFs = false;
                try {
                    URI jarFsUri = URI.create(jarPart);
                    try {
                        jarFs = FileSystems.getFileSystem(jarFsUri);
                    } catch (FileSystemNotFoundException e) {
                        jarFs = FileSystems.newFileSystem(jarFsUri, Collections.emptyMap());
                        weCreatedFs = true;
                    }
                    Path jarRoot = jarFs.getPath(resourceRoot);
                    if (Files.exists(jarRoot)) {
                        copyDirectoryTree(jarRoot, targetDir);
                    }
                } finally {
                    if (weCreatedFs && jarFs != null) {
                        try {
                            jarFs.close();
                        } catch (IOException ignored) { }
                    }
                }
            }

            LOGGER.info("Blueprint pack extraction to '{}' complete", targetDir);
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Failed to extract blueprint packs from JAR: {}", e.getMessage(), e);
        }
    }

    /**
     * Recursively copies a directory tree from {@code source} to {@code target}.
     * Only copies files that do not already exist at the destination, preserving
     * any user modifications.
     */
    private static void copyDirectoryTree(Path source, Path target) throws IOException {
        try (Stream<Path> walk = Files.walk(source)) {
            walk.forEach(srcEntry -> {
                try {
                    Path relative = source.relativize(srcEntry);
                    Path destEntry = target.resolve(relative.toString());

                    if (Files.isDirectory(srcEntry)) {
                        if (!Files.exists(destEntry)) {
                            Files.createDirectories(destEntry);
                        }
                    } else {
                        if (!Files.exists(destEntry)) {
                            Files.createDirectories(destEntry.getParent());
                            Files.copy(srcEntry, destEntry);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.warn("Failed to copy blueprint file {}: {}", srcEntry, e.getMessage());
                }
            });
        }
    }
}

package com.ultra.megamod.lib.emf.jem;

import com.ultra.megamod.lib.emf.EMF;
import com.ultra.megamod.lib.emf.EMFException;
import com.ultra.megamod.lib.emf.utils.EMFUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Top-level {@code .jem} file structure — the OptiFine CEM entity model.
 * <p>
 * JSON fields are kept in their upstream names so Gson can deserialise real pack
 * files directly into this class (see {@link EmfModelLoader} for the load entry point).
 * {@link #prepare(String, JpmResolver)} normalises everything post-deserialization:
 * recurses into parts, renames collisions, pre-processes animation expressions.
 */
@SuppressWarnings("CanBeFinal")
public class EmfJemData {

    /** Optional override texture path ({@code ./...} / {@code ~/...} / {@code namespace:path}). */
    public String texture = "";
    /** {@code [u, v]} — atlas size. Defaults to {@code [64, 32]} if absent. */
    public int[] textureSize = null;
    /** Entity shadow size multiplier (post-processed into a root-level animation). */
    public double shadow_size = 1.0;
    /** Top-level parts; each usually overrides a vanilla bone named by {@link EmfPartData#part}. */
    public LinkedList<EmfPartData> models = new LinkedList<>();

    /** True if any part declared a handheld-item attachment (post-prepare only). */
    public transient boolean hasAttachments = false;
    /** File name / id, set by the loader. */
    public transient String displayFileName = "";
    /** Concrete {@code .jpm} resolver provided by the loader. */
    public transient JpmResolver jpmResolver = null;

    /**
     * Per-vanilla-part animation bucket built by {@link #prepare}.
     * <p>
     * Key: vanilla bone name (e.g. {@code head}). Value: list of per-animation-block
     * expression maps, each of which targets one or more top-level keys.
     */
    private final transient LinkedHashMap<String, List<LinkedHashMap<String, String>>>
            allTopLevelAnimationsByVanillaPartName = new LinkedHashMap<>();

    public LinkedHashMap<String, List<LinkedHashMap<String, String>>> getAllTopLevelAnimationsByVanillaPartName() {
        return allTopLevelAnimationsByVanillaPartName;
    }

    /**
     * Finalise the model after deserialization. Must be called before Phase E renders
     * anything. {@code jpmResolver} is consulted for each nested {@code "model": "..."}
     * reference; pass {@code null} if the model declares no .jpm refs.
     */
    public void prepare(String displayFileName, @Nullable JpmResolver jpmResolver) {
        try {
            this.displayFileName = displayFileName;
            this.jpmResolver = jpmResolver;

            if (textureSize == null || textureSize.length != 2) {
                textureSize = new int[]{64, 32};
                EMFUtils.logWarn("No textureSize provided for: " + displayFileName
                        + ". Defaulting to 64x32 texture size for model.");
            }

            // prepare each part (recursively prepares submodels)
            for (EmfPartData partData : models) {
                partData.prepare(textureSize, this);
            }

            if (EMF.logModelCreationData) EMFUtils.log("originalModels #= " + models.size());

            // rename colliding ids
            LinkedHashMap<String, EmfPartData> orderedParts = new LinkedHashMap<>();
            for (EmfPartData partData : models) {
                String newId = EMFUtils.getIdUnique(orderedParts.keySet(), partData.id);
                if (!newId.equals(partData.id)) partData.id = newId;
                orderedParts.put(partData.id, partData);
            }

            // process animations: rewrite `this.`/`part.` prefixes, strip whitespace
            for (EmfPartData part : orderedParts.values()) {
                if (part.animations == null) continue;

                LinkedList<LinkedHashMap<String, String>> animationsList = new LinkedList<>();
                for (LinkedHashMap<String, String> animation : part.animations) {
                    LinkedHashMap<String, String> processedAnimations = new LinkedHashMap<>();
                    animation.forEach((key, anim) -> {
                        key = processAnimAndKeyString(part, key);
                        anim = processAnimAndKeyString(part, anim);
                        if (!key.isBlank() && !anim.isBlank()) {
                            processedAnimations.put(key, anim);
                        }
                    });
                    if (!processedAnimations.isEmpty()) animationsList.add(processedAnimations);
                }
                if (!animationsList.isEmpty()) {
                    allTopLevelAnimationsByVanillaPartName
                            .computeIfAbsent(part.part, k -> new LinkedList<>())
                            .addAll(animationsList);
                }
            }

            // shadow size: emit a synthetic root-level animation
            if (shadow_size != 1.0) {
                shadow_size = Math.max(shadow_size, 0);
                LinkedHashMap<String, String> shadowAnim = new LinkedHashMap<>();
                shadowAnim.put("render.shadow_size", String.valueOf(shadow_size));
                allTopLevelAnimationsByVanillaPartName
                        .computeIfAbsent("root", k -> new LinkedList<>())
                        .add(shadowAnim);
            }
        } catch (Exception e) {
            String message = "Error preparing jem data, for model [" + displayFileName + "]: " + e.getMessage();
            EMFUtils.logError(message);
            throw EMFException.recordException(new RuntimeException(message));
        }
    }

    private static final Pattern WHITESPACE = Pattern.compile("\\s");
    private static final Pattern THIS = Pattern.compile("(?<=\\W|^)this(?=\\W)");
    private static final Pattern PART = Pattern.compile("(?<=\\W|^)part(?=\\W)");

    @NotNull
    private static String processAnimAndKeyString(final EmfPartData part, final String anim) {
        String result = WHITESPACE.matcher(anim.trim()).replaceAll("");
        result = THIS.matcher(result).replaceAll(part.id);
        result = PART.matcher(result).replaceAll(Objects.requireNonNullElse(part.originalPart, part.part));
        return result;
    }

    @Override
    public String toString() {
        return "EmfJemData{texture='" + texture + "', textureSize=" + Arrays.toString(textureSize)
                + ", shadow_size=" + shadow_size + ", models=" + models + '}';
    }

    /**
     * Looks up a {@code .jpm} file by its pack-relative name (the value of a
     * {@code "model": "..."} field inside a {@code .jem} part).
     */
    @FunctionalInterface
    public interface JpmResolver {
        @Nullable
        EmfPartData resolve(String modelPath);
    }
}

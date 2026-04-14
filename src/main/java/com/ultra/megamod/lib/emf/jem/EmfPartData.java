package com.ultra.megamod.lib.emf.jem;

import com.ultra.megamod.lib.emf.EMF;
import com.ultra.megamod.lib.emf.utils.EMFUtils;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

/**
 * One bone in a {@code .jem} hierarchy (or the whole body of a {@code .jpm} file).
 * <p>
 * The JSON fields are preserved in their upstream names so Gson can deserialise real
 * pack files straight into this class. {@link #prepare(int[], EmfJemData)} does the
 * post-deserialization normalisation: converting degrees to radians, applying the
 * {@code invertAxis} string, and (for {@code .jem}) pulling in {@code .jpm} sub-models
 * referenced by name.
 */
@SuppressWarnings("CanBeFinal")
public class EmfPartData {

    // Fields named to match the OptiFine JSON schema.
    public String texture = "";
    public int[] textureSize = null;
    public String invertAxis = "";
    public float[] translate = null;
    public float[] rotate = null;
    public String mirrorTexture = "";
    public EmfBoxData[] boxes = {};

    /** If present, this part definition is loaded from a separate {@code .jpm} file. */
    public String model = "";
    /** ID of the parent part whose values are inherited. */
    public String baseId = "";
    /** The part's own id; other animations may reference it by this name. */
    public String id = "";
    /** Which vanilla entity part this replaces / attaches to (e.g. "head"). */
    public String part = null;
    /** If true, augment the vanilla part instead of replacing it. */
    public boolean attach = false;
    public float scale = 1.0f;

    public EmfPartData submodel = null;
    public LinkedList<EmfPartData> submodels = new LinkedList<>();

    /** Named attachment points (e.g. left_handheld_item offset). */
    public HashMap<String, float[]> attachments = new HashMap<>();

    /**
     * Animation expressions for this bone keyed by target (e.g. {@code head.rx}).
     * Stored as a list so multiple animation blocks targeting the same bone stack.
     */
    public LinkedList<java.util.LinkedHashMap<String, String>> animations = null;

    @Nullable
    public transient String originalPart = null;

    /** The prepared part texture (resolved by the loader from {@link #texture}). */
    public transient String resolvedTexturePath = null;

    /**
     * Copies missing fields from {@code jpmModel} — invoked when this part has a
     * {@code "model": "..."} pointer to an external {@code .jpm} file. Only blank /
     * default fields are overwritten, matching upstream's first-come-first-served rule.
     */
    public void copyFrom(EmfPartData jpmModel) {
        if (EMF.logModelCreationData) EMFUtils.log(">> copying from jpm into part: " + id);
        if (submodels.isEmpty()) this.submodels = jpmModel.submodels;
        if (submodel == null) this.submodel = jpmModel.submodel;
        if (textureSize == null) this.textureSize = jpmModel.textureSize;
        if (texture.isBlank()) this.texture = jpmModel.texture;
        if (invertAxis.isBlank()) this.invertAxis = jpmModel.invertAxis;
        if (translate == null) this.translate = jpmModel.translate;
        if (rotate == null) this.rotate = jpmModel.rotate;
        if (mirrorTexture.isBlank()) this.mirrorTexture = jpmModel.mirrorTexture;
        if (boxes.length == 0) this.boxes = jpmModel.boxes;
        if (scale == 1f) this.scale = jpmModel.scale;
        if (animations == null || animations.isEmpty()) this.animations = jpmModel.animations;
        if (baseId.isBlank()) this.baseId = jpmModel.baseId;
    }

    /**
     * Normalise the part after deserialization.
     * <p>
     * Resolves external {@code .jpm} references (looked up via
     * {@link EmfJemData#jpmResolver}), propagates texture size, converts rotate
     * components from degrees to radians, applies {@code invertAxis}, and recurses
     * into submodels.
     */
    public void prepare(int[] parentTextureSize, EmfJemData jem) {
        try {
            this.id = "EMF_" + (this.id.isBlank() ? Integer.toString(System.identityHashCode(this)) : this.id);

            if (!model.isEmpty() && jem.jpmResolver != null) {
                if (EMF.logModelCreationData) {
                    EMFUtils.log("Loading model part from jpm: " + model + " into part: " + id);
                }
                EmfPartData loaded = jem.jpmResolver.resolve(model);
                if (loaded != null) copyFrom(loaded);
            }

            if (!attachments.isEmpty()
                    && (attachments.containsKey("left_handheld_item") || attachments.containsKey("right_handheld_item"))) {
                jem.hasAttachments = true;
            }

            if (translate == null) translate = new float[]{0, 0, 0};
            if (rotate == null) rotate = new float[]{0, 0, 0};

            if (this.textureSize == null || this.textureSize.length != 2) {
                this.textureSize = parentTextureSize;
            }

            boolean invX = invertAxis.contains("x");
            boolean invY = invertAxis.contains("y");
            boolean invZ = invertAxis.contains("z");

            translate[0] = invX ? -translate[0] : translate[0];
            translate[1] = invY ? -translate[1] : translate[1];
            translate[2] = invZ ? -translate[2] : translate[2];

            rotate[0] = (invX ? -rotate[0] : rotate[0]) * Mth.DEG_TO_RAD;
            rotate[1] = (invY ? -rotate[1] : rotate[1]) * Mth.DEG_TO_RAD;
            rotate[2] = (invZ ? -rotate[2] : rotate[2]) * Mth.DEG_TO_RAD;

            for (EmfBoxData box : boxes) {
                box.prepare(invX, invY, invZ);
            }

            if (submodel != null) {
                submodel.prepare(this.textureSize, jem);
                if (!submodels.contains(submodel)) {
                    submodels.add(submodel);
                    submodel = null;
                }
            }
            for (EmfPartData sub : submodels) {
                sub.prepare(this.textureSize, jem);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error preparing part data for part [" + id + "]: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "EmfPartData{ id='" + id + "', part='" + part
                + "', rawpart='" + originalPart
                + "', submodels=" + submodels.size()
                + ", anims=" + (animations == null ? "0" : Integer.toString(animations.size()))
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmfPartData p)) return false;
        return attach == p.attach
                && Float.compare(p.scale, scale) == 0
                && Objects.equals(id, p.id)
                && Objects.equals(part, p.part)
                && Objects.equals(model, p.model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, part, model, attach, scale);
    }
}

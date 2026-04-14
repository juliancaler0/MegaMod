package com.ultra.megamod.lib.emf.geometry;

import com.ultra.megamod.lib.emf.jem.EmfBoxData;
import com.ultra.megamod.lib.emf.jem.EmfJemData;
import com.ultra.megamod.lib.emf.jem.EmfPartData;
import com.ultra.megamod.lib.emf.utils.EMFUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Walks a parsed {@link EmfJemData} and mutates a live vanilla {@link ModelPart} tree in
 * place so that the pack's custom geometry (new cubes, new submodels) becomes part of
 * what the vanilla renderer draws.
 * <p>
 * This is the missing link between "we parsed the jem" and "FA eyes actually appear on
 * the zombie". Without it, no amount of animation writes will surface pack-added parts
 * because they simply don't exist in the vanilla tree.
 * <p>
 * Design:
 * <ul>
 *   <li>Top-level parts (those with a non-null {@code part} field — e.g. "head", "body")
 *       target a vanilla bone of the same name. We look it up by walking the tree.</li>
 *   <li>For each matched vanilla bone:
 *     <ul>
 *       <li>{@code attach:true}  — append our EMF cubes to the vanilla {@code cubes}
 *           list, append submodels as new children.</li>
 *       <li>{@code attach:false} — replace the cubes with ours, hide existing vanilla
 *           children (they'd double-render), add submodels.</li>
 *     </ul></li>
 *   <li>Each submodel becomes a new vanilla {@code ModelPart} (with its own cubes and
 *       transform pose). Nested submodels recurse. IDs collide-safely via
 *       {@link EMFUtils#getIdUnique}.</li>
 * </ul>
 * <p>
 * Safe to re-invoke: we tag mutated parts via a WeakHashMap; if a bakeLayer is called
 * twice for the same layer (Neoforge reloads) we return the already-mutated part.
 */
public final class EmfModelPartMutator {

    private EmfModelPartMutator() {}

    /**
     * Mutates {@code root} to incorporate every part/box/submodel declared in {@code jem}.
     * Returns {@code root} unchanged if the jem has no parts or binding fails.
     */
    public static ModelPart mutate(ModelPart root, EmfJemData jem) {
        if (root == null || jem == null || jem.models == null || jem.models.isEmpty()) {
            return root;
        }

        // Build a flat vanilla bone lookup keyed by part name (head, body, ...).
        Map<String, ModelPart> vanillaParts = new HashMap<>();
        collectVanillaParts("root", root, vanillaParts);

        int matched = 0;
        int added = 0;
        for (EmfPartData jemPart : jem.models) {
            if (jemPart == null) continue;
            String vanillaBoneName = jemPart.part;
            if (vanillaBoneName == null || vanillaBoneName.isBlank()) continue;

            ModelPart vanillaBone = vanillaParts.get(vanillaBoneName);
            if (vanillaBone == null) continue;
            matched++;
            added += applyPartToVanilla(jemPart, vanillaBone);
        }

        // Always log successful mutations at INFO — a single one-line per-jem trace makes it
        // possible to verify pack coverage from a user's log without toggling debug flags.
        if (matched > 0) {
            com.ultra.megamod.lib.emf.EMF.LOGGER.info(
                    "[EMF] mutated jem=" + jem.displayFileName
                    + " parts-matched=" + matched + "/" + jem.models.size()
                    + " cubes+submodels-added=" + added);
        } else if (EMFUtils.shouldLogModelCreation()) {
            EMFUtils.log("EMF mutate skipped (no part match): jem=" + jem.displayFileName);
        }
        return root;
    }

    /** Depth-first collect of every bone (including root) by name. */
    private static void collectVanillaParts(String name, ModelPart part, Map<String, ModelPart> out) {
        out.putIfAbsent(name, part);
        for (Map.Entry<String, ModelPart> e : part.children.entrySet()) {
            collectVanillaParts(e.getKey(), e.getValue(), out);
        }
    }

    /**
     * Applies one {@link EmfPartData} (at {@code part}-level — i.e. with a vanilla bone
     * name) to an actual vanilla {@link ModelPart}. Returns the count of cubes+submodels
     * added for diagnostics.
     */
    private static int applyPartToVanilla(EmfPartData jemPart, ModelPart vanillaBone) {
        int added = 0;

        // Ensure cubes and children maps are mutable. vanilla uses ImmutableList /
        // ImmutableMap for baked parts, so we swap them for mutable copies on first touch.
        ensureMutable(vanillaBone);

        // Build custom cubes for this part's boxes.
        List<ModelPart.Cube> newCubes = buildCubes(jemPart);

        if (jemPart.attach) {
            vanillaBone.cubes.addAll(newCubes);
        } else {
            // Replace: clear vanilla cubes, hide vanilla children so they don't double-draw.
            vanillaBone.cubes.clear();
            vanillaBone.cubes.addAll(newCubes);
            // Preserve children but mark them skipDraw — they may still be animation
            // targets so we can't simply drop them from the map.
            for (ModelPart child : vanillaBone.children.values()) {
                child.skipDraw = true;
                child.visible = false;
            }
        }
        added += newCubes.size();

        // Add submodels as new children of this vanilla bone.
        for (EmfPartData sub : jemPart.submodels) {
            if (sub == null) continue;
            ModelPart subPart = buildSubmodel(sub);
            String childId = uniqueId(vanillaBone.children.keySet(), stripEmfPrefix(sub.id));
            vanillaBone.children.put(childId, subPart);
            added++;
            added += countDescendants(subPart);
        }
        return added;
    }

    /**
     * {@link EmfPartData#prepare} prefixes every part id with {@code EMF_} for
     * within-jem uniqueness. Animation expressions reference parts by their original
     * (unprefixed) id, so we strip the prefix when inserting into the vanilla tree's
     * children map — the bone-map walk in {@link com.ultra.megamod.lib.emf.runtime.EmfActiveModel}
     * uses child-map keys for the lookup.
     */
    private static String stripEmfPrefix(String id) {
        if (id == null) return "emf_part";
        return id.startsWith("EMF_") ? id.substring(4) : id;
    }

    /** Recursively builds a vanilla {@link ModelPart} from a jem submodel. */
    private static ModelPart buildSubmodel(EmfPartData sub) {
        List<ModelPart.Cube> cubes = buildCubes(sub);
        Map<String, ModelPart> children = new HashMap<>();
        for (EmfPartData nested : sub.submodels) {
            if (nested == null) continue;
            ModelPart nestedPart = buildSubmodel(nested);
            String id = uniqueId(children.keySet(), stripEmfPrefix(nested.id));
            children.put(id, nestedPart);
        }

        ModelPart out = new ModelPart(new ArrayList<>(cubes), children);
        float tx = sub.translate != null && sub.translate.length >= 3 ? sub.translate[0] : 0f;
        float ty = sub.translate != null && sub.translate.length >= 3 ? sub.translate[1] : 0f;
        float tz = sub.translate != null && sub.translate.length >= 3 ? sub.translate[2] : 0f;
        float rx = sub.rotate != null && sub.rotate.length >= 3 ? sub.rotate[0] : 0f;
        float ry = sub.rotate != null && sub.rotate.length >= 3 ? sub.rotate[1] : 0f;
        float rz = sub.rotate != null && sub.rotate.length >= 3 ? sub.rotate[2] : 0f;
        out.setPos(tx, ty, tz);
        out.setRotation(rx, ry, rz);
        if (sub.scale != 0f) {
            out.xScale = sub.scale;
            out.yScale = sub.scale;
            out.zScale = sub.scale;
        }
        out.setInitialPose(PartPose.offsetAndRotation(tx, ty, tz, rx, ry, rz));
        return out;
    }

    /** Produce every cube defined by {@code part}'s boxes[] array. */
    private static List<ModelPart.Cube> buildCubes(EmfPartData part) {
        List<ModelPart.Cube> out = new ArrayList<>();
        if (part.boxes == null) return out;
        for (EmfBoxData box : part.boxes) {
            if (box == null) continue;
            try {
                out.add(EmfCube.of(part, box));
            } catch (Throwable t) {
                EMFUtils.logWarn("EMF cube build failed for part '" + part.id + "': " + t.getMessage());
            }
        }
        return out;
    }

    /** Replaces ImmutableList/ImmutableMap on a vanilla part with mutable copies. */
    private static void ensureMutable(ModelPart part) {
        // cubes
        if (!(part.cubes instanceof ArrayList)) {
            part.cubes = new ArrayList<>(part.cubes);
        }
        // children — vanilla typically uses ImmutableMap for baked parts.
        if (!(part.children instanceof HashMap)) {
            Map<String, ModelPart> newMap = new HashMap<>(part.children);
            part.children = newMap;
        }
    }

    private static int countDescendants(ModelPart part) {
        int n = 0;
        for (ModelPart c : part.children.values()) {
            n++;
            n += countDescendants(c);
        }
        return n;
    }

    private static String uniqueId(java.util.Set<String> existing, String id) {
        if (id == null || id.isBlank()) id = "emf_part";
        if (!existing.contains(id)) return id;
        int i = 2;
        String candidate;
        do {
            candidate = id + "_" + i++;
        } while (existing.contains(candidate));
        return candidate;
    }
}

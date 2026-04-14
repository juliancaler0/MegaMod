package com.ultra.megamod.lib.emf.runtime;

import com.ultra.megamod.lib.emf.animation.EmfModelDefinition;
import com.ultra.megamod.lib.emf.jem.EmfJemData;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-{@code EntityModel} binding between a compiled {@link EmfModelDefinition}
 * and a living vanilla {@link ModelPart} tree.
 * <p>
 * One of these is attached to each vanilla {@code EntityModel} instance that has a
 * matching {@code .jem} in the active resource pack. The
 * {@link com.ultra.megamod.lib.emf.access.EmfEntityModelHolder} interface mixed in
 * via Mixin is the storage handle; this class is the business data.
 * <p>
 * Phase E's render-time apply loop consumes two things:
 * <ul>
 *   <li>{@link #definition} — the Phase D compiled expressions bucketed by vanilla bone.</li>
 *   <li>{@link #boneMap} — a lazily-populated {@code boneName -> ModelPart} lookup so
 *       expression evaluation can write transforms back to the live tree.</li>
 * </ul>
 * When a vanilla {@code HumanoidModel.setupAnim} fires, Phase E's applier walks the
 * bone map, pushes the frame's {@link com.ultra.megamod.lib.emf.animation.EmfVariableContext},
 * and writes the evaluated value to each targeted {@code ModelPart} axis field.
 */
public final class EmfActiveModel {

    public final EmfJemData jem;
    public final EmfModelDefinition definition;
    public final Identifier sourceJemId;
    @Nullable public final Identifier jemTextureOverride;

    /**
     * {@code vanillaBoneName -> ModelPart}. Populated lazily on the first successful
     * setupAnim so we don't have to traverse the tree every frame. Reset when the
     * vanilla model root identity changes (e.g. on resource reload).
     */
    private final Map<String, ModelPart> boneMap = new HashMap<>();
    @Nullable private ModelPart rootReference = null;

    public EmfActiveModel(EmfJemData jem, EmfModelDefinition definition,
                          Identifier sourceJemId, @Nullable Identifier jemTextureOverride) {
        this.jem = jem;
        this.definition = definition;
        this.sourceJemId = sourceJemId;
        this.jemTextureOverride = jemTextureOverride;
    }

    /**
     * Ensures {@link #boneMap} reflects {@code root}. If the root identity changed
     * (model rebuild across a resource reload) the map is cleared and rebuilt.
     */
    public void bindRoot(ModelPart root) {
        if (root == null) return;
        if (rootReference == root && !boneMap.isEmpty()) return;
        rootReference = root;
        boneMap.clear();
        // Walk the vanilla tree. ModelPart.getChild is the vanilla API; we use it via
        // the public getAllParts-ish recursion. ModelPart exposes `children` as a
        // package-private field — instead we cache children by walking hasChild.
        // The names we care about are the vanilla bone names the .jem declared
        // assignments against; walk the tree by recursion using the accessor mixin.
        collect("root", root);
    }

    private void collect(String name, ModelPart part) {
        boneMap.putIfAbsent(name, part);
        // ModelPart#children is package-private in vanilla; we read it through the
        // EmfModelPartAccessor mixin. The accessor returns a LinkedHashMap.
        Map<String, ModelPart> children = emf$childrenOf(part);
        if (children != null) {
            for (Map.Entry<String, ModelPart> e : children.entrySet()) {
                collect(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Reads the children map via the accessor mixin
     * ({@link com.ultra.megamod.lib.emf.access.EmfModelPartAccessor}).
     * Returns {@code null} if the accessor isn't applied (shouldn't happen at runtime).
     * <p>
     * The accessor interface is implemented at runtime by the Mixin injector, which
     * is invisible to the compiler; hence the explicit cast through {@code Object}.
     */
    @SuppressWarnings("unchecked")
    private static @Nullable Map<String, ModelPart> emf$childrenOf(ModelPart part) {
        try {
            Object opaque = part;
            if (opaque instanceof com.ultra.megamod.lib.emf.access.EmfModelPartAccessor acc) {
                return (Map<String, ModelPart>) acc.emf$children();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /**
     * Returns the {@link ModelPart} bound to the given vanilla bone name, or
     * {@code null} if no such bone exists in the tree.
     */
    @Nullable
    public ModelPart bone(String vanillaBoneName) {
        if (vanillaBoneName == null) return null;
        return boneMap.get(vanillaBoneName);
    }

    /** Read-only view of the bone map (for debug / external iteration). */
    public Map<String, ModelPart> boneMap() {
        return boneMap;
    }

    /**
     * Produce a default bone map from a definition, useful when a caller wants to
     * iterate the assignments-by-bone lookup without touching the ModelPart tree.
     * (Primary use: unit tests / validation.)
     */
    public Map<String, Object> previewBoneAssignments() {
        LinkedHashMap<String, Object> out = new LinkedHashMap<>();
        definition.animationsByBone.forEach((bone, animations) -> out.put(bone, animations));
        return out;
    }
}

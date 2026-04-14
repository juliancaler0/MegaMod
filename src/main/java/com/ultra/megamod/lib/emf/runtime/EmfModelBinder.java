package com.ultra.megamod.lib.emf.runtime;

import com.ultra.megamod.lib.emf.animation.EmfVariableContext;
import com.ultra.megamod.lib.emf.access.EmfEntityModelHolder;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

/**
 * Per-frame model-to-entity binder invoked from {@link com.ultra.megamod.lib.emf.mixin.MixinHumanoidModel}
 * and similar model-class mixins.
 * <p>
 * Flow on every {@code setupAnim} call:
 * <ol>
 *   <li>Look up the current entity render state via the ETF render context (the
 *       same handle ETF uses for texture variants).</li>
 *   <li>If the pack has a {@code .jem} for this entity type, ask
 *       {@link EmfModelManager} for (or lazily build) the {@link EmfActiveModel}
 *       and stash it on the {@link EntityModel} instance via
 *       {@link EmfEntityModelHolder}.</li>
 *   <li>If no pack-provided model applies, clear any stale binding so subsequent
 *       frames don't pull the old transforms.</li>
 * </ol>
 * The actual per-frame transform write happens in {@link EmfBoneApplier}, which
 * reads the binding this class planted.
 */
public final class EmfModelBinder {

    private EmfModelBinder() {
    }

    /**
     * Binds (or clears) an EMF active model onto {@code model} based on the ETF
     * render-state currently in scope.
     *
     * @return the active model bound, or {@code null} if none applied.
     */
    @Nullable
    public static EmfActiveModel bindCurrent(EntityModel<?> model) {
        if (model == null) return null;
        EmfEntityModelHolder holder = (EmfEntityModelHolder) model;

        ETFEntityRenderState state = ETFRenderContext.getCurrentEntityState();
        String typeKey = deriveEntityTypeKey(state);
        if (typeKey == null) {
            holder.emf$setActiveModel(null);
            return null;
        }

        EmfActiveModel bound = EmfModelManager.getInstance().bindForEntity(typeKey, state);
        holder.emf$setActiveModel(bound);
        return bound;
    }

    /**
     * Applies all compiled animations to {@code rootPart} using a context built
     * from the currently-rendering entity. Convenience wrapper that combines
     * {@link #bindCurrent(EntityModel)} with {@link EmfBoneApplier#apply}.
     *
     * @return number of writes applied; {@code 0} if no pack model bound.
     */
    public static int applyForCurrent(EntityModel<?> model, ModelPart rootPart) {
        EmfActiveModel active = bindCurrent(model);
        if (active == null) return 0;

        EmfVariableContext ctx = buildContextForCurrent(active);
        if (ctx == null) return 0;
        return EmfBoneApplier.apply(active, rootPart, ctx);
    }

    /** Builds a frame context from the live ETF render state. */
    @Nullable
    public static EmfVariableContext buildContextForCurrent(EmfActiveModel active) {
        ETFEntityRenderState state = ETFRenderContext.getCurrentEntityState();
        if (state == null) return null;

        EntityType<?> type = state.entityType();
        EntityRenderState vanilla = state.vanillaState();
        return new MinecraftRenderStateContext(vanilla, state, state.uuid(), type);
    }

    /**
     * Derives the {@code entityTypeKey} (e.g. {@code "creeper"}) from the ETF state.
     * Returns {@code null} if no entity is currently known.
     * <p>
     * Phase F: respects {@link EmfTransientTypeCapture} — for entities whose
     * renderer-init recorded a transient type override (boats, spectral arrows,
     * etc.) that key wins over the raw entity-type registry path.
     */
    @Nullable
    public static String deriveEntityTypeKey(@Nullable ETFEntityRenderState state) {
        if (state == null) return null;
        EntityType<?> type = state.entityType();
        if (type == null) return null;

        // Transient type override: if the manager recorded one during this renderer's
        // setup, prefer it (matches upstream behaviour).
        String transientKey = EmfModelManager.getInstance().currentSpecifiedModelLoading;
        if (transientKey != null && !transientKey.isEmpty()
                && !transientKey.startsWith("emf$")) {
            return transientKey;
        }
        // Fall back to our helper-derived transient key; for boats and spectral arrows
        // this returns the jem-file-friendly form (e.g. "dark_oak_boat").
        String helperKey = EmfTransientTypeCapture.keyForEntityType(type);
        if (helperKey != null && !helperKey.isEmpty()) {
            return helperKey;
        }

        Identifier id = forgeKeyOf(type);
        if (id == null) return null;
        return id.getPath();
    }

    @Nullable
    private static Identifier forgeKeyOf(EntityType<?> type) {
        try {
            return BuiltInRegistries.ENTITY_TYPE.getKey(type);
        } catch (Throwable ignored) {
            // Fall through to description id as last-ditch key.
        }
        try {
            String desc = type.getDescriptionId();
            if (desc != null && desc.startsWith("entity.minecraft.")) {
                return Identifier.fromNamespaceAndPath(
                        "minecraft", desc.substring("entity.minecraft.".length()));
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}

package com.ultra.megamod.lib.emf.runtime;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

/**
 * Transient entity-type capture table.
 * <p>
 * Some vanilla entity types resolve to a different model key than the one that
 * their renderer-factory invocation suggests. Upstream EMF hard-codes this list
 * inside its {@code MixinEntityRenderers} and {@code Mixin_SpecialModelRenderers}
 * mixins; we extract it into a pure helper so mixin code stays a thin wrapper.
 * <p>
 * The table covers:
 * <ul>
 *   <li>{@code SPECTRAL_ARROW} — shares {@code ArrowModel} with plain arrows,
 *       but {@code .jem} pack lookups want {@code spectral_arrow.jem}.</li>
 *   <li>{@code BREEZE_WIND_CHARGE} — shares {@code WindChargeModel}, wants
 *       {@code breeze_wind_charge.jem}.</li>
 *   <li>Boat / chest-boat / raft / chest-raft variants — all share the base
 *       boat model tree, but each wood type has its own {@code .jem} entry.
 *       Encoded as {@code "emf$boat$<wood>"} so downstream handlers know to
 *       resolve {@code <wood>_boat.jem}, {@code <wood>_raft.jem}, etc.</li>
 *   <li>Husk / skeleton subvariants — resolve to their parent mob's {@code .jem}
 *       when no dedicated variant exists.</li>
 *   <li>Block-entity model key mappings — chest, ender_chest, trapped_chest,
 *       shulker_box, enchanting_book, lectern_book.</li>
 * </ul>
 * <p>
 * Mutates {@link EmfModelManager#currentSpecifiedModelLoading} so the shared
 * renderer-init hook point records the right key. Mirrors upstream 1:1.
 */
public final class EmfTransientTypeCapture {

    private EmfTransientTypeCapture() {
    }

    /**
     * Sets the current transient key on {@link EmfModelManager} based on the
     * entity type. Must be followed by a {@link #reset()} call once the
     * renderer-init callback returns so later invocations don't see stale state.
     */
    public static void captureForEntityType(EntityType<?> entityType) {
        if (entityType == null) return;
        EmfModelManager mgr = EmfModelManager.getInstance();

        if (entityType.equals(EntityType.SPECTRAL_ARROW)) {
            mgr.currentSpecifiedModelLoading = "spectral_arrow";
        } else if (entityType.equals(EntityType.BREEZE_WIND_CHARGE)) {
            mgr.currentSpecifiedModelLoading = "breeze_wind_charge";
        } else if (entityType.is(EntityTypeTags.BOAT)) {
            // e.g. "entity.minecraft.dark_oak_boat" -> "dark_oak"
            String base = entityType.getDescriptionId()
                    .replaceAll("entity\\.minecraft\\.", "")
                    .replaceAll("(_boat|_raft|_chest_boat)$", "");
            mgr.currentSpecifiedModelLoading = "emf$boat$" + base;
        }
    }

    /** Sets an explicit transient key (used by block-entity paths). */
    public static void capture(String key) {
        if (key == null) return;
        EmfModelManager.getInstance().currentSpecifiedModelLoading = key;
    }

    /** Clears the transient key. Must be called at the end of every capture scope. */
    public static void reset() {
        EmfModelManager.getInstance().currentSpecifiedModelLoading = "";
    }

    /**
     * Resolves the entity-type key upstream would have recorded for this vanilla
     * type. Used by the binder when no {@code currentSpecifiedModelLoading} is
     * set but a transient type is in play.
     */
    public static String keyForEntityType(EntityType<?> type) {
        if (type == null) return "";
        if (type.equals(EntityType.SPECTRAL_ARROW)) return "spectral_arrow";
        if (type.equals(EntityType.BREEZE_WIND_CHARGE)) return "breeze_wind_charge";
        if (type.is(EntityTypeTags.BOAT)) {
            String base = type.getDescriptionId()
                    .replaceAll("entity\\.minecraft\\.", "")
                    .replaceAll("(_boat|_raft|_chest_boat)$", "");
            return base + "_boat";
        }
        return "";
    }
}

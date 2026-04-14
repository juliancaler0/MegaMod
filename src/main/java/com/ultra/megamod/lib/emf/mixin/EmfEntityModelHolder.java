package com.ultra.megamod.lib.emf.mixin;

import com.ultra.megamod.lib.emf.runtime.EmfActiveModel;
import org.jetbrains.annotations.Nullable;

/**
 * Duck-type interface mixed into {@code net.minecraft.client.model.EntityModel}
 * so every vanilla entity model can carry an optional
 * {@link EmfActiveModel} binding.
 * <p>
 * Upstream EMF uses {@code IEMFModel} which exposes both a boolean and a root
 * getter; we collapse into one nullable getter because the Phase D definition
 * can be reconstituted from the active model.
 */
public interface EmfEntityModelHolder {

    /** Returns the current active EMF model binding, or {@code null} if the pack has none. */
    @Nullable
    EmfActiveModel emf$getActiveModel();

    /** Installs an active EMF binding. Pass {@code null} to clear. */
    void emf$setActiveModel(@Nullable EmfActiveModel active);

    /** Convenience: returns {@code true} if a binding is currently installed. */
    default boolean emf$hasActiveModel() {
        return emf$getActiveModel() != null;
    }
}

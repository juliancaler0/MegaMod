package com.ultra.megamod.lib.emf.access;

import com.ultra.megamod.lib.emf.runtime.EmfActiveModel;
import org.jetbrains.annotations.Nullable;

/**
 * Duck-type interface mixed into {@code net.minecraft.client.model.EntityModel}
 * so every vanilla entity model can carry an optional
 * {@link EmfActiveModel} binding.
 * <p>
 * Lives outside the {@code .mixin} package because non-mixin runtime code
 * references it directly (the mixin package is declared mixin-only in
 * {@code megamod-emf.mixins.json}; placing a plain interface there causes
 * {@code IllegalClassLoadError} at class-load time).
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

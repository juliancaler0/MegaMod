package com.ultra.megamod.lib.emf.access;

import net.minecraft.client.model.geom.ModelPart;

import java.util.Map;

/**
 * Duck-type interface implemented by {@link ModelPart} via
 * {@code com.ultra.megamod.lib.emf.mixin.MixinModelPart}.
 * <p>
 * Lives outside the {@code .mixin} package because non-mixin runtime code
 * references it directly (the mixin package is declared mixin-only in
 * {@code megamod-emf.mixins.json}; placing a plain interface there causes
 * {@code IllegalClassLoadError} at class-load time).
 */
public interface EmfModelPartAccessor {

    /**
     * Returns the vanilla children map. Exposed as {@code Map<String, ?>} so
     * the interface doesn't drag {@code ModelPart} into every consumer's
     * type signatures.
     */
    Map<String, ?> emf$children();
}

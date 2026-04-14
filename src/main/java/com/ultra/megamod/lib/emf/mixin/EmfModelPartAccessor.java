package com.ultra.megamod.lib.emf.mixin;

import net.minecraft.client.model.geom.ModelPart;

import java.util.Map;

/**
 * Duck-type interface implemented by {@link ModelPart} via {@link MixinModelPart}.
 * <p>
 * Lets EMF read the package-private {@code children} map and stash an
 * {@link com.ultra.megamod.lib.emf.runtime.EmfActiveModel} reference on the root
 * model part without reflection.
 * <p>
 * Upstream EMF does the same thing via its own {@code Accessor} mixin; we mirror
 * the pattern to keep the bridge between compiled-expression values and vanilla
 * bones cheap (single virtual call, no HashMap lookup on the hot path).
 */
public interface EmfModelPartAccessor {

    /**
     * Returns the vanilla children map. The vanilla runtime stores children as a
     * {@code Map<String, ModelPart>}; we expose it as {@code Map<String, ?>} so the
     * interface doesn't accidentally drag {@code ModelPart} into every type
     * signature.
     */
    Map<String, ?> emf$children();
}

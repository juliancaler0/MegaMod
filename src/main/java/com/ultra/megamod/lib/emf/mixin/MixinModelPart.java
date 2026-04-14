package com.ultra.megamod.lib.emf.mixin;

import com.ultra.megamod.lib.emf.access.EmfModelPartAccessor;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

/**
 * Exposes {@link ModelPart#children} through {@link EmfModelPartAccessor}.
 * <p>
 * {@code ModelPart} keeps its children in a package-private map; EMF walks this
 * map to build the {@code vanillaBoneName -> ModelPart} lookup so the per-frame
 * applier can find the right bone by name without reflection.
 * <p>
 * Mirrors the upstream {@code CuboidAccessor} / {@code MixinModelPart} pair. No
 * behaviour change — purely a visibility widening via Mixin's {@code @Shadow}.
 */
@Mixin(ModelPart.class)
public abstract class MixinModelPart implements EmfModelPartAccessor {

    @Shadow @Final private Map<String, ModelPart> children;

    @Override
    public Map<String, ?> emf$children() {
        return children;
    }
}

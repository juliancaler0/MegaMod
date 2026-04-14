package com.ultra.megamod.lib.emf.mixin;

import com.ultra.megamod.lib.emf.runtime.EmfActiveModel;
import net.minecraft.client.model.EntityModel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Attaches an {@link EmfActiveModel} slot to every {@link EntityModel}.
 * <p>
 * Populated lazily by {@link MixinHumanoidModel} /
 * {@link com.ultra.megamod.lib.emf.runtime.EmfModelBinder} on first {@code setupAnim}
 * per frame; read by the applier so Phase E can find the compiled expressions that
 * go with this model instance.
 * <p>
 * Upstream EMF uses the {@code IEMFModel} interface for the same purpose. We keep
 * the pattern identical — one duck-type getter/setter on {@code EntityModel},
 * populated by per-class binders.
 */
@Mixin(EntityModel.class)
public abstract class MixinEntityModel implements EmfEntityModelHolder {

    @Unique
    @Nullable
    private EmfActiveModel emf$activeModel = null;

    @Override
    @Nullable
    public EmfActiveModel emf$getActiveModel() {
        return emf$activeModel;
    }

    @Override
    public void emf$setActiveModel(@Nullable EmfActiveModel active) {
        this.emf$activeModel = active;
    }
}

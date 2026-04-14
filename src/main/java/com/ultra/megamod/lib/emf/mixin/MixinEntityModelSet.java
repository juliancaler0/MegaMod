package com.ultra.megamod.lib.emf.mixin;

import com.ultra.megamod.lib.emf.geometry.EmfModelPartMutator;
import com.ultra.megamod.lib.emf.jem.EmfJemData;
import com.ultra.megamod.lib.emf.runtime.EmfActiveModel;
import com.ultra.megamod.lib.emf.runtime.EmfModelManager;
import com.ultra.megamod.lib.emf.utils.EMFUtils;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects EMF geometry into every model the entity model set bakes.
 * <p>
 * Runs after vanilla has built the {@link ModelPart} tree from the layer definition.
 * If the pack ships an {@code assets/minecraft/optifine/cem/&lt;entity&gt;.jem} matching
 * this layer, {@link EmfModelPartMutator} mutates the tree in place — appending new
 * cubes to {@code attach:true} parts and replacing cubes on {@code attach:false} parts,
 * plus adding new {@link ModelPart} children for every submodel declared in the jem.
 * <p>
 * After this runs, the vanilla render path naturally draws the pack's custom geometry
 * (eyes, face detail, mane, etc.) and the animation applier can target the new bones.
 * <p>
 * Layer filter: we only augment the "main" layer (the primary entity body). Armor and
 * feature layers use their own {@code .jem} files (e.g. {@code armor.jem}) and are
 * handled by dedicated feature-renderer mixins.
 */
@Mixin(EntityModelSet.class)
public abstract class MixinEntityModelSet {

    @Inject(method = "bakeLayer", at = @At("RETURN"), cancellable = true)
    private void emf$injectPackGeometry(ModelLayerLocation layerLoc,
                                        CallbackInfoReturnable<ModelPart> cir) {
        try {
            ModelPart root = cir.getReturnValue();
            if (root == null || layerLoc == null) return;

            String entityKey = emf$entityKeyFor(layerLoc);
            if (entityKey == null) return;

            EmfModelManager manager = EmfModelManager.getInstance();
            EmfActiveModel active = manager.bindFor(entityKey, false);
            if (active == null || active.jem == null) return;

            EmfJemData jem = active.jem;
            EmfModelPartMutator.mutate(root, jem);
            cir.setReturnValue(root);
        } catch (Throwable t) {
            EMFUtils.logWarn("EMF geometry inject failed for " + layerLoc + ": " + t.getMessage());
        }
    }

    /**
     * Derives the OptiFine {@code .jem} base name from a {@link ModelLayerLocation}.
     * <p>
     * We only return non-null for the "main" layer of {@code minecraft:} entities; armor
     * and feature layers use their own .jem files and are mutated elsewhere.
     */
    private static String emf$entityKeyFor(ModelLayerLocation loc) {
        Identifier model = loc.model();
        if (model == null) return null;
        if (!"minecraft".equals(model.getNamespace())) return null;
        String layer = loc.layer();
        if (layer == null || !layer.equals("main")) return null;
        String path = model.getPath();
        if (path == null || path.isEmpty()) return null;
        return path;
    }
}

package com.ultra.megamod.lib.emf.models.parts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import com.ultra.megamod.lib.emf.models.animation.EMFAnimationEntityContext;
import com.ultra.megamod.lib.emf.utils.EMFUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.resources.Identifier;

public abstract class EMFModelPartWithState extends EMFModelPart {

    public final Int2ObjectOpenHashMap<EMFModelState> allKnownStateVariants = new Int2ObjectOpenHashMap<>() {
        @Override
        public EMFModelState get(final int k) {
            if (!containsKey(k)) {
                EMFUtils.logWarn("EMFModelState variant with key " + k + " does not exist in part [" + toStringShort() + "], returning copy of 0");
                put(k, get(0).copy());
            }
            return super.get(k);
        }
    };
    public int currentModelVariant = 0;
    Map<String, ModelPart> vanillaChildren = new HashMap<>();
    Runnable startOfRenderRunnable = null;
    @NotNull Animator animationHolder = new Animator();

    public EMFModelPartWithState(List<Cube> cuboids, Map<String, ModelPart> children) {
        super(cuboids, children);
    }

    void receiveOneTimeRunnable(Runnable run) {
        startOfRenderRunnable = run;
        children.values().forEach((child) -> {
            if (child instanceof EMFModelPartWithState emf) {
                emf.receiveOneTimeRunnable(run);
            }
        });
    }

    @Override
    public void render(PoseStack matrices, VertexConsumer vertices, int light, int overlay,
                       final int k
    ) {
        if (startOfRenderRunnable != null) {
            startOfRenderRunnable.run();
        }
        if (animationHolder != null && !EMFAnimationEntityContext.isEntityAnimPausedWrapped()) {
            animationHolder.run();
        }
        super.render(matrices, vertices, light, overlay,
                k
        );

    }

    EMFModelState getCurrentState() {
        return new EMFModelState(
                getInitialPose(),
                cubes,
                children,
                visible, skipDraw,
                textureOverride, animationHolder
        );
    }

    EMFModelState getStateOf(ModelPart modelPart) {
        if (modelPart instanceof EMFModelPartWithState emf) {
            return new EMFModelState(
                    modelPart.getInitialPose(),
                    modelPart.cubes,
                    modelPart.children,
                    modelPart.visible, modelPart.skipDraw,
                    emf.textureOverride, emf.animationHolder
            );
        }
        return new EMFModelState(
                modelPart.getInitialPose(),
                modelPart.cubes,
                new HashMap<>(),
                modelPart.visible, modelPart.skipDraw,
                null, new Animator()
        );
    }

    void setFromState(EMFModelState newState) {
        setInitialPose(newState.defaultTransform());
        loadPose(getInitialPose());

        cubes = newState.cuboids();
        children = newState.variantChildren();


        visible = newState.visible();
        skipDraw = newState.hidden();
        textureOverride = newState.texture();
        animationHolder = newState.animation();
    }

    protected void resetState(){
        setFromState(allKnownStateVariants.get(currentModelVariant));
    }

    public void setVariantStateTo(int newVariant) {
        if (currentModelVariant != newVariant) {
            setFromState(allKnownStateVariants.get(newVariant));
            currentModelVariant = newVariant;
            for (ModelPart part :
                    children.values()) {
                if (part instanceof EMFModelPartWithState p3)
                    p3.setVariantStateTo(newVariant);
            }
        }
    }

    public void copyVariantTo(int from, int to) {
        allKnownStateVariants.putIfAbsent(to, allKnownStateVariants.get(from).copy());
        for (ModelPart value : children.values()) {
            if (value instanceof EMFModelPartWithState p3)
                p3.copyVariantTo(from, to);
        }
    }

    public record
            EMFModelState(
                    PartPose defaultTransform,
                    // ModelTransform currentTransform,
                    List<Cube> cuboids,
                    Map<String, ModelPart> variantChildren,
                    boolean visible,
                    boolean hidden,
                    Identifier texture,
                    Animator animation
            )

     {
         public EMFModelState copy() {
             return copy(visible());
         }

        public EMFModelState copy(boolean visibleOverride) {
            PartPose trans = defaultTransform();
            Animator animator = new Animator();
            animator.setAnimation(animation().getAnimation());
            return new EMFModelState(
                    new PartPose(trans.x(), trans.y(), trans.z(), trans.xRot(), trans.yRot(), trans.zRot(), trans.xScale(), trans.yScale(), trans.zScale()),
                    new ArrayList<>(cuboids()),
                    new HashMap<>(variantChildren()),

                    visibleOverride,
                    hidden(),
                    texture(),
                    animator
            );
        }
    }
}

package com.ultra.megamod.lib.spellengine.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.entity.SpellProjectile;
import org.jetbrains.annotations.Nullable;

/**
 * Renders {@link SpellProjectile} entities in NeoForge 1.21.11.
 *
 * <p>The old {@code render(state, PoseStack, MultiBufferSource, packedLight)} override is no
 * longer called by the pipeline — 1.21.11 replaced it with
 * {@link #submit(EntityRenderState, PoseStack, SubmitNodeCollector, CameraRenderState)}.
 * An earlier port still used {@code render}, which silently made every JSON-driven spell
 * projectile invisible (the method was dead code with no {@code @Override}). This rewrite
 * overrides {@code submit} and resolves the Blockbench model via
 * {@link net.minecraft.client.renderer.item.ItemModelResolver#updateForTopItem} when the
 * projectile's {@code model_id} maps to a registered dummy item (see
 * {@link com.ultra.megamod.feature.combat.spell.SpellProjectileModelItems}).</p>
 */
public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile, SpellProjectileRenderer.SpellProjectileRenderState> {
    private final float scale;

    public SpellProjectileRenderer(EntityRendererProvider.Context ctx, float scale, boolean lit) {
        super(ctx);
        this.scale = scale;
    }

    public SpellProjectileRenderer(EntityRendererProvider.Context arg) {
        this(arg, 1.0F, false);
    }

    @Override
    public SpellProjectileRenderState createRenderState() {
        return new SpellProjectileRenderState();
    }

    @Override
    public void extractRenderState(SpellProjectile entity, SpellProjectileRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.renderData = entity.renderData();
        state.previousVelocity = entity.previousVelocity;
        state.velocity = entity.getDeltaMovement();
        state.age = entity.tickCount;
        state.partialTick = partialTick;

        // Resolve model by looking up a registered Item whose registry path matches the
        // spell's {@code model_id}. The port registers dummy items at
        // {@code megamod:spell_projectile/<name>} specifically so the new ItemStackRenderState
        // pipeline can render them via {@link ItemStackRenderState#submit}.
        state.modelStack = ItemStack.EMPTY;
        if (entity instanceof SpellProjectile sp && sp.getItemStackModel() != null) {
            state.modelStack = sp.getItemStackModel();
        } else if (state.renderData != null && state.renderData.model_id != null && !state.renderData.model_id.isEmpty()) {
            Identifier id = Identifier.parse(state.renderData.model_id);
            var itemOpt = BuiltInRegistries.ITEM.getOptional(id);
            if (itemOpt.isPresent()) {
                state.modelStack = itemOpt.get().getDefaultInstance();
            }
        }
    }

    @Override
    public void submit(SpellProjectileRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        if (state.renderData == null || state.modelStack.isEmpty()) return;

        poseStack.pushPose();
        poseStack.scale(this.scale, this.scale, this.scale);

        switch (state.renderData.orientation) {
            case TOWARDS_MOTION, ALONG_MOTION -> {
                Vec3 velocity = state.velocity;
                if (state.previousVelocity != null) {
                    velocity = state.previousVelocity.lerp(state.velocity, state.partialTick);
                }
                velocity = velocity.normalize();
                float dirYaw = (float) Math.toDegrees(Math.atan2(velocity.x, velocity.z)) + 180F;
                if (state.renderData.orientation == Spell.ProjectileModel.Orientation.ALONG_MOTION) {
                    dirYaw += 90F;
                }
                float dirPitch = (float) Math.toDegrees(Math.asin(velocity.y));
                poseStack.mulPose(Axis.YP.rotationDegrees(dirYaw));
                poseStack.mulPose(Axis.XP.rotationDegrees(dirPitch));
            }
            default -> { /* TOWARDS_CAMERA — default pose */ }
        }

        // Spin
        float time = state.age + state.partialTick;
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                state.renderData.rotate_degrees_offset + time * state.renderData.rotate_degrees_per_tick
        ));
        poseStack.scale(state.renderData.scale, state.renderData.scale, state.renderData.scale);

        var mc = Minecraft.getInstance();
        var renderState = new ItemStackRenderState();
        mc.getItemModelResolver().updateForTopItem(
                renderState, state.modelStack, ItemDisplayContext.FIXED, mc.level, null, state.age);
        poseStack.translate(-0.5, -0.5, -0.5);
        renderState.submit(poseStack, nodeCollector, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }

    public static class SpellProjectileRenderState extends EntityRenderState {
        public @Nullable Spell.ProjectileModel renderData;
        public @Nullable Vec3 previousVelocity;
        public Vec3 velocity = Vec3.ZERO;
        public int age;
        public float partialTick;
        public ItemStack modelStack = ItemStack.EMPTY;
    }
}

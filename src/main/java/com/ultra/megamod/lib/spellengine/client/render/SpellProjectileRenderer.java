package com.ultra.megamod.lib.spellengine.client.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import com.ultra.megamod.lib.spellengine.api.render.CustomModels;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.entity.SpellProjectile;
import org.jetbrains.annotations.Nullable;


// Mostly copied from: FlyingItemEntityRenderer
public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile, SpellProjectileRenderer.SpellProjectileRenderState> {
    private final float scale;
    private final boolean lit;

    public SpellProjectileRenderer(EntityRendererProvider.Context ctx, float scale, boolean lit) {
        super(ctx);
        this.scale = scale;
        this.lit = lit;
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
        state.entity = entity;
        state.tickDelta = partialTick;
    }

    public void render(SpellProjectileRenderState state, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        var entity = state.entity;
        if (entity == null) return;

        if (entity.renderData() != null) {
            var renderData = entity.renderData();
            render(this.scale, renderData, entity.previousVelocity,
                    entity, 0, state.tickDelta, true, matrices, vertexConsumers, light);
        }
    }

    public static boolean render(float scale, Spell.ProjectileModel renderData,
                                 @Nullable Vec3 previousVelocity, Entity entity, float yaw, float tickDelta, boolean allowSpin,
                                 PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        matrices.pushPose();
        matrices.scale(scale, scale, scale);
        switch (renderData.orientation) {
            case TOWARDS_MOTION, ALONG_MOTION -> {
                var velocity = entity.getDeltaMovement();
                if (previousVelocity != null) {
                    velocity = previousVelocity.lerp(velocity, tickDelta);
                }
                velocity = velocity.normalize();
                var directionBasedYaw = Math.toDegrees(Math.atan2(velocity.x, velocity.z)) + 180F;
                if (renderData.orientation == Spell.ProjectileModel.Orientation.ALONG_MOTION) {
                    directionBasedYaw += 90;
                }
                var directionBasedPitch = Math.toDegrees(Math.asin(velocity.y));
                matrices.mulPose(Axis.YP.rotationDegrees((float) directionBasedYaw));
                matrices.mulPose(Axis.XP.rotationDegrees((float) directionBasedPitch));
            }
            default -> {
                // TOWARDS_CAMERA - use default orientation
            }
        }

        if (allowSpin) {
            matrices.mulPose(Axis.ZP.rotationDegrees(
                    renderData.rotate_degrees_offset +
                    (entity.tickCount + tickDelta) * renderData.rotate_degrees_per_tick)
            );
        }
        matrices.scale(renderData.scale, renderData.scale, renderData.scale);

        Identifier modelId = null;
        if (entity instanceof SpellProjectile spellProjectile && spellProjectile.getItemStackModel() != null) {
            // Use item stack model - render via ItemStackRenderState
            var itemStack = spellProjectile.getItemStackModel();
            CustomModels.renderItemStack(itemStack, matrices, vertexConsumers, light, entity.getId());
        } else if (renderData.model_id != null && !renderData.model_id.isEmpty()) {
            modelId = Identifier.parse(renderData.model_id);
            var layer = SpellModelHelper.LAYERS.get(renderData.light_emission);
            CustomModels.render(layer, null, modelId, matrices, vertexConsumers, light, entity.getId());
        }
        matrices.popPose();
        return true;
    }

    public static class SpellProjectileRenderState extends EntityRenderState {
        public SpellProjectile entity;
        public float tickDelta;
    }
}

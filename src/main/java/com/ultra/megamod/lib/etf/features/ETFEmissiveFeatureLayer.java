package com.ultra.megamod.lib.etf.features;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.lib.etf.ETF;
import com.ultra.megamod.lib.etf.config.ETFConfig;
import com.ultra.megamod.lib.etf.features.texture_handlers.ETFTexture;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Feature layer that renders the emissive and enchant overlays after the base entity
 * model draws.
 * <p>
 * Works by reading the {@link ETFRenderContext#getCurrentTexture() current ETFTexture}
 * (set by the texture-swap path in
 * {@link com.ultra.megamod.lib.etf.utils.ETFUtils2#getETFVariantNotNullForInjector})
 * and, if it has an emissive or enchant companion, submits an additional model draw
 * using the overlay texture.
 * <p>
 * Port of upstream's {@code renderEmissive}/{@code renderEnchanted} pipeline, tailored
 * to NeoForge's {@code RenderLayer} extension point so we don't need the upstream
 * {@code ETFVertexConsumer} duck-typing on BufferBuilder.
 */
public class ETFEmissiveFeatureLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends RenderLayer<S, M> {

    public ETFEmissiveFeatureLayer(RenderLayerParent<S, M> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector,
                       int packedLight, S renderState, float yRot, float xRot) {
        ETFConfig cfg = ETF.config().getConfig();
        ETFTexture tex = ETFRenderContext.getCurrentTexture();

        // Fall back to the current player's stored skin texture if the variator hasn't
        // published one — happens for vanilla skins that aren't listed in any .properties.
        if (tex == null) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level != null) {
                int entityId = com.ultra.megamod.feature.backpacks.client.BackpackRenderContext.getEntityId();
                if (entityId >= 0) {
                    net.minecraft.world.entity.Entity e = mc.level.getEntity(entityId);
                    if (e instanceof net.minecraft.client.player.AbstractClientPlayer acp) {
                        var holder = (com.ultra.megamod.lib.etf.features.player.ETFPlayerSkinHolder) acp;
                        var pt = holder.etf$getETFPlayerTexture();
                        if (pt == null) {
                            pt = new com.ultra.megamod.lib.etf.features.player.ETFPlayerTexture(acp);
                            holder.etf$setETFPlayerTexture(pt);
                        }
                        tex = pt.getSkinTexture();
                    }
                }
            }
        }

        if (tex == null) return;

        EntityModel<? super S> model = getParentModel();
        // setupAnim was already called by the base renderer; we just want to re-submit
        // the same model shape with the overlay texture.

        if (cfg.enableEmissiveTextures && tex.isEmissive()) {
            Identifier emissiveId = tex.getEmissiveIdentifierOfCurrentState();
            if (emissiveId != null) {
                RenderType type;
                if (cfg.getEmissiveRenderMode() == ETFConfig.EmissiveRenderModes.BRIGHT) {
                    type = RenderTypes.beaconBeam(emissiveId, true);
                } else {
                    type = RenderTypes.entityCutoutNoCull(emissiveId);
                }
                collector.submitModel(model, renderState, poseStack, type,
                        ETF.EMISSIVE_FEATURE_LIGHT_VALUE, OverlayTexture.NO_OVERLAY,
                        -1, null, 0, null);
            }
        }

        if (cfg.enableEnchantedTextures && tex.isEnchanted()) {
            Identifier enchantId = tex.getEnchantIdentifierOfCurrentState();
            if (enchantId != null) {
                RenderType type = RenderTypes.entityCutoutNoCull(enchantId);
                collector.submitModel(model, renderState, poseStack, type,
                        ETF.EMISSIVE_FEATURE_LIGHT_VALUE, OverlayTexture.NO_OVERLAY,
                        -1, null, 0, null);
            }
        }
    }

    /**
     * Installs the emissive feature layer on every entity renderer that supports it.
     * Covers both players (via {@code getSkins}/{@code getPlayerRenderer}) and every
     * {@code LivingEntityRenderer} registered for a vanilla {@link net.minecraft.world.entity.EntityType}
     * (via {@code getEntityTypes}/{@code getRenderer}).
     * <p>
     * Needed so resource packs that declare emissive textures for non-player entities
     * (drowned, enderman, blaze, endermite, warden, TRAS's custom entity variants, etc.)
     * actually render the glow pass.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        // Players — both default (steve) and slim (alex) variants.
        for (net.minecraft.world.entity.player.PlayerModelType skin : event.getSkins()) {
            var renderer = event.getPlayerRenderer(skin);
            if (renderer != null) {
                try {
                    renderer.addLayer(new ETFEmissiveFeatureLayer(renderer));
                } catch (Throwable t) {
                    com.ultra.megamod.lib.etf.utils.ETFUtils2.logWarn(
                            "ETF emissive install (player " + skin + ") failed: " + t.getMessage());
                }
            }
        }

        // Every other LivingEntity renderer registered at layer-add time. The event
        // exposes the populated entity-type set via getEntityTypes().
        for (net.minecraft.world.entity.EntityType<?> type : event.getEntityTypes()) {
            try {
                var r = event.getRenderer(type);
                if (r instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer living) {
                    living.addLayer(new ETFEmissiveFeatureLayer(living));
                }
            } catch (Throwable t) {
                // Some renderers (ender dragon, painting) throw here; silent-skip — they
                // have their own dedicated texture pipelines.
            }
        }
    }
}

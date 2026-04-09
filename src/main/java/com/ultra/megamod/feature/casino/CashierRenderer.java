package com.ultra.megamod.feature.casino;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

public class CashierRenderer
extends AgeableMobRenderer<CashierEntity, VillagerRenderState, VillagerModel> {
    private static final Identifier VILLAGER_BASE_SKIN = Identifier.withDefaultNamespace("textures/entity/villager/villager.png");

    public CashierRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new VillagerModel(ctx.bakeLayer(ModelLayers.VILLAGER)), new VillagerModel(ctx.bakeLayer(ModelLayers.VILLAGER_BABY)), 0.5f);
        this.addLayer(new CustomHeadLayer(this, ctx.getModelSet(), ctx.getPlayerSkinRenderCache(), VillagerRenderer.CUSTOM_HEAD_TRANSFORMS));
        this.addLayer(new VillagerProfessionLayer(this, ctx.getResourceManager(), "villager", new VillagerModel(ctx.bakeLayer(ModelLayers.VILLAGER_NO_HAT)), new VillagerModel(ctx.bakeLayer(ModelLayers.VILLAGER_BABY_NO_HAT))));
        this.addLayer(new CrossedArmsItemLayer(this));
    }

    public Identifier getTextureLocation(VillagerRenderState renderState) {
        return VILLAGER_BASE_SKIN;
    }

    public VillagerRenderState createRenderState() {
        return new VillagerRenderState();
    }

    public void extractRenderState(CashierEntity entity, VillagerRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        renderState.villagerData = entity.getVillagerData();
        renderState.isUnhappy = false;
    }
}

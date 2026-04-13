package com.ultra.megamod.feature.combat.archers.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ultra.megamod.feature.combat.archers.ArchersMod;
import com.ultra.megamod.lib.accessories.api.client.AccessoriesRenderStateKeys;
import com.ultra.megamod.lib.accessories.api.client.AccessoryRenderState;
import com.ultra.megamod.lib.accessories.api.client.RenderStateStorage;
import com.ultra.megamod.lib.accessories.api.client.renderers.AccessoryRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AccessoriesQuiverRenderer implements AccessoryRenderer {
    private final Identifier modelId;

    public AccessoriesQuiverRenderer(String modelName) {
        this.modelId = Identifier.fromNamespaceAndPath(ArchersMod.ID, "item/quiver/" + modelName);
    }

    @Override
    public <S extends LivingEntityRenderState> void render(AccessoryRenderState accessoryState,
                                                           S entityState,
                                                           EntityModel<S> entityModel,
                                                           PoseStack matrixStack,
                                                           SubmitNodeCollector collector) {
        var client = Minecraft.getInstance();

        ItemStack stack = accessoryState.getRenderData(AccessoriesRenderStateKeys.ITEM_STACK);
        if (stack == null) stack = ItemStack.EMPTY;

        Integer lightObj = accessoryState.getRenderData(AccessoriesRenderStateKeys.LIGHT);
        int light = lightObj != null ? lightObj : 15728880;

        if (entityModel instanceof PlayerModel playerModel) {
            AccessoryRenderer.transformToModelPart(matrixStack, playerModel.body);
            matrixStack.translate(-1.65F, -0.5F, -1.2F);
            matrixStack.scale(2F, 2F, 2F);

            Quaternionf rotation = new Quaternionf().rotationAxis((float) Math.toRadians(40),
                    new Vector3f(1, 0, 0));
            matrixStack.mulPose(rotation);
        }

        // Render using the new ItemStackRenderState approach
        if (!stack.isEmpty()) {
            var renderState = new ItemStackRenderState();
            client.getItemModelResolver().updateForTopItem(renderState, stack, ItemDisplayContext.FIXED,
                    client.level, null, 0);
            renderState.submit(matrixStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
        }
    }
}

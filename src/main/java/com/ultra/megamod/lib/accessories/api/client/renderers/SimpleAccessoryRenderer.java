package com.ultra.megamod.lib.accessories.api.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ultra.megamod.lib.accessories.api.client.AccessoriesRenderStateKeys;
import com.ultra.megamod.lib.accessories.api.client.AccessoriesRendererRegistry;
import com.ultra.megamod.lib.accessories.api.client.AccessoryRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Simple version of the {@link AccessoryRenderer} with a transformation method to adjust
 * a given item to certain part of the given {@link LivingEntity} then rendering the accessory
 * as an Item at the specified location and scale
 */
public interface SimpleAccessoryRenderer extends AccessoryRenderer {

    @Override
    default <S extends LivingEntityRenderState> void render(AccessoryRenderState accessoryState, S entityState, EntityModel<S> model, PoseStack matrices, SubmitNodeCollector collector) {
        var stackRenderState = accessoryState.getRenderData(AccessoriesRenderStateKeys.ITEM_STACK_STATE);

        if (stackRenderState == null) {
            throw new NullPointerException("Unable to get the required ItemStack render state to render a SimpleAccessoryRenderer: " + AccessoriesRendererRegistry.getRendererId(this));
        }

        align(accessoryState, entityState, model, matrices);

        var light = entityState.getRenderData(AccessoriesRenderStateKeys.LIGHT);
        var stack = accessoryState.getRenderData(AccessoriesRenderStateKeys.ITEM_STACK);

        renderStack(accessoryState, entityState, model, matrices, collector, stack, stackRenderState, light);
    }

    public default <S extends LivingEntityRenderState> void renderStack(AccessoryRenderState accessoryState, S entityState, EntityModel<S> model, PoseStack matrices, SubmitNodeCollector collector, ItemStack stack, ItemStackRenderState stackRenderState, int light) {
        stackRenderState.submit(matrices, collector, entityState.getRenderData(AccessoriesRenderStateKeys.LIGHT), OverlayTexture.NO_OVERLAY, 0);
    }

    @Override
    default boolean shouldCreateStackRenderState() {
        return true;
    }

    /// Method used to align the given matrices to the desired position and scale on the current [LivingEntity]
    /// passed within the [#render] method.
    ///
    /// \[1.21.8 and below-> 1.21.9]
    /// - stack           -> {@link AccessoryRenderState#getRenderData} with {@link AccessoriesRenderStateKeys#ITEM_STACK}
    /// - path            -> {@link AccessoryRenderState#getRenderData} with {@link AccessoriesRenderStateKeys#SLOT_PATH}
    ///
    public <S extends LivingEntityRenderState> void align(AccessoryRenderState accessoryState, S entityState, EntityModel<S> model, PoseStack matrices);

}
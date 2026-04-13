package com.ultra.megamod.lib.spellengine.client.render;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.spellbinding.SpellBindingBlockEntity;

// Copied from EnchantingTableBlockEntityRenderer
public class SpellBindingBlockEntityRenderer implements BlockEntityRenderer<SpellBindingBlockEntity, BlockEntityRenderState> {

    public static final Identifier BOOK_TEXTURE_ID = Identifier.fromNamespaceAndPath("megamod", "entity/spell_binding_book");

    private final BookModel book;

    public SpellBindingBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.book = new BookModel(ctx.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        // TODO: 1.21.11 - BlockEntityRenderer now uses submit() with SubmitNodeCollector
        // Book rendering would need adaptation to the new rendering pipeline.
        // For now, this is a no-op since the enchanting book visual is cosmetic only.
    }
}

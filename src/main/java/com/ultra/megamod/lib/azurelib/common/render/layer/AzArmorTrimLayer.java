package com.ultra.megamod.lib.azurelib.common.render.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import java.util.UUID;
import java.util.function.Function;

import com.ultra.megamod.lib.azurelib.common.model.AzBone;
import com.ultra.megamod.lib.azurelib.common.render.AzRendererPipelineContext;
import com.ultra.megamod.lib.azurelib.common.render.armor.AzArmorRendererPipelineContext;

/**
 * Represents a render layer for applying armor trim textures to an item stack during the rendering process.
 */
public class AzArmorTrimLayer implements AzRenderLayer<UUID, ItemStack> {

    public final Identifier textureBaseLocation;

    public final Function<ArmorTrim, Identifier> texturePermutations;

    public AzArmorTrimLayer(Identifier baseTexture) {
        this(baseTexture, true);
    }

    public AzArmorTrimLayer(Identifier baseTexture, boolean supportPatterns) {
        this(
            baseTexture,
            trim -> {
                var pattern = trim.pattern().value();
                var patternName = pattern.assetId().getPath();
                // In 1.21.11, TrimMaterial API changed — use pattern name only
                return Identifier.fromNamespaceAndPath(
                    baseTexture.getNamespace(),
                    baseTexture.getPath() + "_" + patternName
                );
            }
        );
    }

    public AzArmorTrimLayer(
        Identifier baseTexture,
        Function<ArmorTrim, Identifier> textureLocationPermutations
    ) {
        this.textureBaseLocation = baseTexture;
        this.texturePermutations = textureLocationPermutations;
    }

    @Override
    public void preRender(AzRendererPipelineContext<UUID, ItemStack> context) {}

    @Override
    public void render(AzRendererPipelineContext<UUID, ItemStack> context) {
        var armorPipelineContext = (AzArmorRendererPipelineContext) context;
        var itemstack = armorPipelineContext.currentStack();
        if (itemstack == null) {
            return;
        }
        var armorTrim = itemstack.get(DataComponents.TRIM);
        if (armorTrim == null) {
            return;
        }

        var pattern = armorTrim.pattern().value();

        var renderPipeline = context.rendererPipeline();
        var trimLocation = texturePermutations.apply(armorTrim);

        var renderType = Sheets.armorTrimsSheet(pattern.decal());
        var vertexConsumer = context.multiBufferSource().getBuffer(renderType);

        if (context.renderType() != null) {
            context.setRenderType(renderType);
            context.setVertexConsumer(vertexConsumer);
            renderPipeline.reRender(context);
        }
    }

    @Override
    public void renderForBone(AzRendererPipelineContext<UUID, ItemStack> azRendererPipelineContext, AzBone azBone) {}
}

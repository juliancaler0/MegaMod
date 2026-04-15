package com.ultra.megamod.lib.emf.mixin.mixins.rendering;

import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.ultra.megamod.lib.emf.EMF;
import com.ultra.megamod.lib.emf.config.EMFConfig;
import com.ultra.megamod.lib.emf.EMFManager;
import com.ultra.megamod.lib.emf.utils.EMFUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(SpecialModelRenderers.class)
public class Mixin_SpecialModelRenderers_SetCurrentSpecifiedModel {

    @Unique
    private static final List<String> emf$renderers = new ArrayList<>();

    @Inject(method =
            "createBlockRenderers"
            , at = @At(value = "RETURN"))
    private static void emf$clearMarker(CallbackInfoReturnable<Map<Block, SpecialModelRenderer<?>>> cir) {
        if (EMF.testForForgeLoadingError()) return;
        EMFManager.getInstance().currentSpecifiedModelLoading = "";
        EMFManager.getInstance().currentBlockEntityTypeLoading = null;
        if (EMF.config().getConfig().logModelCreationData || EMF.config().getConfig().modelExportMode != EMFConfig.ModelPrintMode.NONE)
            EMFUtils.log("Identified SPECIAL block entity renderers: " + emf$renderers);
        emf$renderers.clear();
    }

    @ModifyArg(method = "createBlockRenderers", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
    private static BiConsumer<Block, Object> setEmf$Model(BiConsumer<? super Block, Object> action) {
        return (Block type, Object idk) -> {
            setModel(type.defaultBlockState());

            // og code
            action.accept(type, idk);
        };
    }

    @Unique
    private static void setModel(BlockState state) {
        //mark which variant is currently specified for use by otherwise identical block entity renderers
        if (EMF.testForForgeLoadingError()) return;

        // TODO DONT FORGET TO REPLICATE CHANGES IN REGULAR RENDERERS
        if (state.is(Blocks.ENCHANTING_TABLE))
            EMFManager.getInstance().currentSpecifiedModelLoading = "enchanting_book";
        else if (state.is(Blocks.LECTERN))
            EMFManager.getInstance().currentSpecifiedModelLoading = "lectern_book";
        else if (state.is(Blocks.CHEST))
            EMFManager.getInstance().currentSpecifiedModelLoading = "chest";
        else if (state.is(Blocks.ENDER_CHEST))
            EMFManager.getInstance().currentSpecifiedModelLoading = "ender_chest";
        else if (state.is(Blocks.TRAPPED_CHEST))
            EMFManager.getInstance().currentSpecifiedModelLoading = "trapped_chest";
        else {
            try {
                Identifier id = BuiltInRegistries.BLOCK.wrapAsHolder(state.getBlock()).unwrapKey().get().location();

                if (id.getNamespace().equals("minecraft")) {
                    EMFManager.getInstance().currentSpecifiedModelLoading = id.getPath();
                } else {
                    EMFManager.getInstance().currentSpecifiedModelLoading = id.getNamespace() + ":" + id.getPath();
                }
            } catch (Exception e) {
                EMFManager.getInstance().currentSpecifiedModelLoading = state.toString();
            }
        }
        emf$renderers.add(EMFManager.getInstance().currentSpecifiedModelLoading);
        if (EMF.config().getConfig().logModelCreationData)
            EMFUtils.log("Seeing SPECIAL block entity renderer init for: " + EMFManager.getInstance().currentSpecifiedModelLoading);
    }

}

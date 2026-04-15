package com.ultra.megamod.lib.emf.mixin.mixins;


import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

@Mixin(BlockEntityRenderers.class)
public class MixinBlockEntityRendererFactories {

    @Unique
    private static final List<String> emf$renderers = new ArrayList<>();

    @Inject(method = "createEntityRenderers", at = @At(value = "RETURN"))
    private static void emf$clearMarker(final BlockEntityRendererProvider.Context args, final CallbackInfoReturnable<Map<BlockEntityType<?>, BlockEntityRenderer>> cir) {
        if (EMF.testForForgeLoadingError()) return;
        EMFManager.getInstance().currentSpecifiedModelLoading = "";
        EMFManager.getInstance().currentBlockEntityTypeLoading = null;
        if (EMF.config().getConfig().logModelCreationData || EMF.config().getConfig().modelExportMode != EMFConfig.ModelPrintMode.NONE)
            EMFUtils.log("Identified block entity renderers: " + emf$renderers);
        emf$renderers.clear();
    }

    @ModifyArg(method = "createEntityRenderers", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
    private static BiConsumer<BlockEntityType<?>, Object> setEmf$Model(final BiConsumer<BlockEntityType<?>, Object> action) {
        return (BlockEntityType<?> type, Object idk) -> {
                //mark which variant is currently specified for use by otherwise identical block entity renderers
                if (EMF.testForForgeLoadingError()) return;

                EMFManager.getInstance().currentBlockEntityTypeLoading = type;

                // TODO DONT FORGET TO REPLICATE CHANGES IN SPECIAL RENDERERS
                if (BlockEntityType.ENCHANTING_TABLE.equals(type))
                    EMFManager.getInstance().currentSpecifiedModelLoading = "enchanting_book";
                else if (BlockEntityType.LECTERN.equals(type))
                    EMFManager.getInstance().currentSpecifiedModelLoading = "lectern_book";
                else if (BlockEntityType.CHEST.equals(type))
                    EMFManager.getInstance().currentSpecifiedModelLoading = "chest";
                else if (BlockEntityType.ENDER_CHEST.equals(type))
                    EMFManager.getInstance().currentSpecifiedModelLoading = "ender_chest";
                else if (BlockEntityType.TRAPPED_CHEST.equals(type))
                    EMFManager.getInstance().currentSpecifiedModelLoading = "trapped_chest";
                //todo did deprecation start in 1.21.2?
                else if (type.builtInRegistryHolder() != null && type.builtInRegistryHolder().unwrapKey().isPresent()) {
                    Identifier id = type.builtInRegistryHolder().unwrapKey().get().location();
                    if (id.getNamespace().equals("minecraft")) {
                        EMFManager.getInstance().currentSpecifiedModelLoading = id.getPath();
                    } else {
                        EMFManager.getInstance().currentSpecifiedModelLoading = id.getNamespace() + ":" + id.getPath();
                    }
                }
                emf$renderers.add(EMFManager.getInstance().currentSpecifiedModelLoading);
                if (EMF.config().getConfig().logModelCreationData)
                    EMFUtils.log("Seeing block entity renderer init for: " + EMFManager.getInstance().currentSpecifiedModelLoading);

                // og code
                action.accept(type, idk);
            };
    }
}

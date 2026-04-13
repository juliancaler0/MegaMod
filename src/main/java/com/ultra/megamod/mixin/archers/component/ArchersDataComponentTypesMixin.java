package com.ultra.megamod.mixin.archers.component;

import com.ultra.megamod.feature.combat.archers.component.ArcherComponents;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataComponents.class)
public class ArchersDataComponentTypesMixin {

    // Static class init tail inject to force ArcherComponents initialization
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_init_TAIL_Archers(CallbackInfo ci) {
        var asd = ArcherComponents.AUTO_FIRE; // Initialize the component type
    }
}

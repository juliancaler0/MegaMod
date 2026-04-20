package com.ultra.megamod.mixin.accessories.owo.ui.access;

import com.ultra.megamod.lib.accessories.owo.ui.base.BaseOwoContainerScreen;
import com.ultra.megamod.lib.accessories.owo.ui.core.OwoUIAdapter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BaseOwoContainerScreen.class, remap = false)
public interface BaseOwoHandledScreenAccessor {
    @Accessor("uiAdapter")
    OwoUIAdapter<?> owo$getUIAdapter();
}

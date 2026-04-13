package com.ultra.megamod.mixin.spellengine.client.control;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyMapping.class)
public interface KeybindingAccessor {
    @Invoker("release")
    void spellEngine_reset();
    @Accessor("key")
    InputConstants.Key spellEngine_getBoundKey();
}

package com.ultra.megamod.lib.etf.mixin.mixins.mods.iris.old;


import org.spongepowered.asm.mixin.Mixin;
import com.ultra.megamod.lib.etf.mixin.CancelTarget;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(CancelTarget.class)
public class MixinFullyBufferedMultiBufferSource {}

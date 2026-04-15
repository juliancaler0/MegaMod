package com.ultra.megamod.lib.etf.mixin.mixins.mods.imediatelyfast;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import com.ultra.megamod.lib.etf.mixin.CancelTarget;

@Mixin(CancelTarget.class)
public class MixinBatchableBufferSource {}
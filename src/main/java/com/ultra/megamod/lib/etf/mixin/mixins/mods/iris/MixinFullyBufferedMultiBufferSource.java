package com.ultra.megamod.lib.etf.mixin.mixins.mods.iris;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import com.ultra.megamod.lib.etf.mixin.CancelTarget;

@Pseudo
@Mixin(CancelTarget.class)
public class MixinFullyBufferedMultiBufferSource {}
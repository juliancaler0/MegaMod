package com.ultra.megamod.mixin.accessories;

import com.ultra.megamod.lib.accessories.pond.TooltipFlagExtended;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TooltipFlag.class)
public interface TooltipFlagMixin extends TooltipFlagExtended {

}

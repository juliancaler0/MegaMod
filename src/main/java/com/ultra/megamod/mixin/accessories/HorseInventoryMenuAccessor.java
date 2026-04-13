package com.ultra.megamod.mixin.accessories;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractMountInventoryMenu.class)
public interface HorseInventoryMenuAccessor {
    @Accessor("mount")
    LivingEntity accessories$horse();
}

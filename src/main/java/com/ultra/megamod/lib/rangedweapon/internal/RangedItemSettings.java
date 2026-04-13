package com.ultra.megamod.lib.rangedweapon.internal;

import com.ultra.megamod.lib.rangedweapon.api.RangedConfig;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;

public interface RangedItemSettings {
    RangedConfig getRangedAttributes();
    Item.Properties rangedAttributes(RangedConfig config);
    DataComponentMap.Builder rwa_getComponentBuilder();
}

package com.ultra.megamod.mixin.rangedweapon.item;

import com.ultra.megamod.lib.rangedweapon.api.RangedConfig;
import com.ultra.megamod.lib.rangedweapon.internal.RangedItemSettings;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Item.Properties.class)
public class ItemSettingMixin implements RangedItemSettings {
    @Shadow private DataComponentMap.Builder components;
    private RangedConfig rangedConfig;

    @Override
    public RangedConfig getRangedAttributes() {
        return rangedConfig;
    }

    @Override
    public Item.Properties rangedAttributes(RangedConfig config) {
        rangedConfig = config;
        return (Item.Properties) (Object) this;
    }

    @Override
    public DataComponentMap.Builder rwa_getComponentBuilder() {
        return components;
    }
}

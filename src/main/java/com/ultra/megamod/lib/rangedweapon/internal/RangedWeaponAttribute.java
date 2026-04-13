package com.ultra.megamod.lib.rangedweapon.internal;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import org.jetbrains.annotations.Nullable;

public class RangedWeaponAttribute extends RangedAttribute implements IAttributeExtension, NeoAttribute {
    public RangedWeaponAttribute(String translationKey, double fallback, double min, double max) {
        super(translationKey, fallback, min, max);
    }

    private Identifier baseModifierId = null;

    @Override
    public void setBaseModifierId(Identifier id) {
        baseModifierId = id;
    }

    @Nullable
    public Identifier getBaseId() {
        return baseModifierId;
    }
}

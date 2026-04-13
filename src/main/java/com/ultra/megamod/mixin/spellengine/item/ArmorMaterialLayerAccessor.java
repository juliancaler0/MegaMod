package com.ultra.megamod.mixin.spellengine.item;

// TODO: 1.21.11 - ArmorMaterial.Layer may not be accessible via Mixin @Mixin(class) syntax
// The Armor.CustomItem.getFirstLayerId() has been updated to not depend on this accessor.
// Keeping as stub to avoid removing from mixins JSON.

import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ArmorMaterial.class)
public interface ArmorMaterialLayerAccessor {
    // Stubbed - was previously @Mixin(ArmorMaterial.Layer.class) with @Accessor("id")
}

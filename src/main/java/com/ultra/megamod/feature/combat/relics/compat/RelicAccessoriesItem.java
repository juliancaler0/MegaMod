package com.ultra.megamod.feature.combat.relics.compat;

import com.ultra.megamod.lib.accessories.api.SoundEventData;
import com.ultra.megamod.lib.accessories.api.attributes.AccessoryAttributeBuilder;
import com.ultra.megamod.lib.accessories.api.core.AccessoryItem;
import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Ported 1:1 from Relics-1.21.1's net.relics_rpgs.compat.RelicAccessoriesItem.
 * Can't be unequipped while the active ability is cooling down. Bridges vanilla
 * ATTRIBUTE_MODIFIERS into lib/accessories' attribute builder so baked-in
 * modifiers actually apply while in an accessory slot.
 */
public class RelicAccessoriesItem extends AccessoryItem {

    public RelicAccessoriesItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canUnequip(ItemStack stack, SlotReference reference) {
        var entity = reference.entity();
        boolean onCooldown = false;
        if (entity instanceof Player player) {
            onCooldown = !player.isCreative() && player.getCooldowns().isOnCooldown(stack);
        }
        return super.canUnequip(stack, reference) && !onCooldown;
    }

    @Nullable
    public SoundEventData getEquipSound(ItemStack stack, SlotReference reference) {
        return null;
    }

    @Override
    public void getDynamicModifiers(ItemStack stack, SlotReference reference, AccessoryAttributeBuilder builder) {
        super.getDynamicModifiers(stack, reference, builder);
        var mods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (mods == null) return;
        for (var entry : mods.modifiers()) {
            AttributeModifier mod = entry.modifier();
            builder.addStackable(entry.attribute(), mod.id(), mod.amount(), mod.operation());
        }
    }
}

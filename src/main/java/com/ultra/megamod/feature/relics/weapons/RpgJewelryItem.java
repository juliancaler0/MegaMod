package com.ultra.megamod.feature.relics.weapons;

import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.lib.accessories.api.attributes.AccessoryAttributeBuilder;
import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Jewelry accessory item. Extends {@link com.ultra.megamod.lib.accessories.api.core.AccessoryItem}
 * so each instance auto-registers with the lib/accessories system. Slot membership is driven by the
 * {@code megamod:rings}/{@code megamod:necklaces} item tags.
 *
 * <p>Stat rolling has been removed per user directive — items now use whatever
 * {@code ATTRIBUTE_MODIFIERS} component was baked in at registration time (source-parity).
 * Rolled-bonus feature will return later once source item parity is verified.</p>
 */
public class RpgJewelryItem extends com.ultra.megamod.lib.accessories.api.core.AccessoryItem {
    private final AccessorySlotType slotType;

    public RpgJewelryItem(Item.Properties properties, AccessorySlotType slotType) {
        super(properties);
        this.slotType = slotType;
    }

    public AccessorySlotType getSlotType() {
        return slotType;
    }

    /**
     * Bridges vanilla {@link DataComponents#ATTRIBUTE_MODIFIERS} into the lib's
     * accessory attribute system. Without this, rings/necklaces equipped in
     * lib/accessories slots don't apply any of their stats — vanilla only
     * consults ATTRIBUTE_MODIFIERS when an item is in a native equipment slot.
     */
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

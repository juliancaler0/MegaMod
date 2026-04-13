package com.ultra.megamod.lib.accessories.impl;

import com.ultra.megamod.lib.accessories.api.attributes.AccessoryAttributeBuilder;
import com.ultra.megamod.lib.accessories.api.components.AccessoriesDataComponents;
import com.ultra.megamod.lib.accessories.api.components.AccessoryItemAttributeModifiers;
import com.ultra.megamod.lib.accessories.api.core.AccessoryNestUtils;
import com.ultra.megamod.lib.accessories.api.core.AccessoryRegistry;
import com.ultra.megamod.lib.accessories.api.events.AdjustAttributeModifierCallback;
import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class AccessoryAttributeLogic {

    public static AccessoryAttributeBuilder getAttributeModifiers(ItemStack stack, SlotReference slotReference){
        return getAttributeModifiers(stack, slotReference, false);
    }

    public static AccessoryAttributeBuilder getAttributeModifiers(ItemStack stack, SlotReference slotReference, boolean useTooltipCheck){
        return getAttributeModifiers(stack, slotReference.entity(), slotReference.slotName(), slotReference.index(), useTooltipCheck);
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "1.22")
    @Deprecated(forRemoval = true)
    public static AccessoryAttributeBuilder getAttributeModifiers(ItemStack stack, String slotName, int slot){
        return getAttributeModifiers(stack, null, slotName, slot);
    }

    public static AccessoryAttributeBuilder getAttributeModifiers(ItemStack stack, @Nullable LivingEntity entity, String slotName, int slot){
        return getAttributeModifiers(stack, entity, slotName, slot, false);
    }

    /**
     * Attempts to get any at all AttributeModifier's found on the stack either though NBT or the Accessory bound
     * to the {@link ItemStack}'s item
     */
    public static AccessoryAttributeBuilder getAttributeModifiers(ItemStack stack, @Nullable LivingEntity entity, String slotName, int slot, boolean hideTooltipIfDisabled){
        var slotReference = SlotReference.of(entity, slotName, slot);

        var builder = new AccessoryAttributeBuilder(slotReference);

        AccessoryNestUtils.recursivelyConsume(stack, slotReference, (innerStack, innerRef) -> {
            var component = innerStack.getOrDefault(AccessoriesDataComponents.ATTRIBUTES.get(), AccessoryItemAttributeModifiers.EMPTY);

            if (!hideTooltipIfDisabled || component.showInTooltip()) {
                component.gatherAttributes(innerRef, builder);
            }
        });

        if(entity != null) {
            //TODO: Decide if the presence of modifiers prevents the accessory modifiers from existing
            var accessory = AccessoryRegistry.getAccessoryOrDefault(stack);

            if(accessory != null) accessory.getDynamicModifiers(stack, slotReference, builder);

            AdjustAttributeModifierCallback.EVENT.invoker().adjustAttributes(stack, slotReference, builder);
        }

        return builder;
    }
}

package com.ultra.megamod.lib.accessories.api.slot;

import com.ultra.megamod.lib.accessories.api.action.ActionResponse;
import com.ultra.megamod.lib.accessories.api.action.ActionResponseBuffer;
import com.ultra.megamod.lib.accessories.api.slot.validator.EntitySlotValidator;
import com.ultra.megamod.lib.accessories.fabric.TriState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Similar to {@link SlotBasedPredicate} but allows for {@link LivingEntity} if such is required
 */
@Deprecated(forRemoval = true)
public interface EntityBasedPredicate extends SlotBasedPredicate, EntitySlotValidator {

    TriState isValid(Level level, @Nullable LivingEntity entity, SlotType slotType, int index, ItemStack stack);

    @Override
    default TriState isValid(Level level, SlotType slotType, int slot, ItemStack stack) {
        return isValid(level, null, slotType, slot, stack);
    }

    @Override
    default void isValidForSlot(Level level, SlotType slotType, int index, ItemStack stack, ActionResponseBuffer buffer) {
        SlotBasedPredicate.super.isValidForSlot(level, slotType, index, stack, buffer);
    }

    @Override
    default void isValidForSlot(@Nullable LivingEntity entity, Level level, SlotType slotType, int index, ItemStack stack, ActionResponseBuffer buffer) {
        var isValid = isValid(level, entity, slotType, index, stack);

        if (isValid == TriState.DEFAULT) return;

        var message = isValid.isTrue()
            ? Component.literal("Such an item is valid to be equipped!")
            : Component.literal("Such an item is not valid to be equipped!");

        buffer.respondWith(ActionResponse.of(isValid.isTrue(), message));
    }
}

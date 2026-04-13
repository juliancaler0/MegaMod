package com.ultra.megamod.lib.accessories.api.events;

import com.ultra.megamod.lib.accessories.api.action.ActionResponseBuffer;
import com.ultra.megamod.lib.accessories.api.action.ValidationState;
import com.ultra.megamod.lib.accessories.api.core.AccessoryRegistry;
import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import com.ultra.megamod.lib.accessories.impl.core.UnknownResponse;
import com.ultra.megamod.lib.accessories.impl.event.WrappedEvent;
import com.ultra.megamod.lib.accessories.fabric.event.Event;
import com.ultra.megamod.lib.accessories.fabric.TriState;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableObject;

///
/// @deprecated Use [com.ultra.megamod.lib.accessories.api.events.v2.CanUnequipCallback] instead!
/// Event callback used to allow or denied the ability to equip a given accessory for the given referenced slot
/// type and entity.
///
/// Fired in [AccessoryRegistry#canUnequip(ItemStack,SlotReference)]
///
@Deprecated(forRemoval = true)
public interface CanUnequipCallback {

    Event<CanUnequipCallback> EVENT = new WrappedEvent<>(
        com.ultra.megamod.lib.accessories.api.events.v2.CanUnequipCallback.EVENT,
        canEquipCallback -> (stack, reference, callback) -> {
            var result = canEquipCallback.canUnequip(stack, reference);

            if (result != TriState.DEFAULT) callback.respondWith(new UnknownResponse(ValidationState.of(result)));
        },
        canEquipCallbackEvent -> {
            return (stack, reference) -> {
                var buffer = new ActionResponseBuffer(true);

                canEquipCallbackEvent.invoker().canUnequip(stack, reference, buffer);

                return buffer.canPerformAction().toTriState();
            };
        }
    );

    ///
    /// @param stack     The specific stack being evaluated
    /// @param reference The reference to the specific location within the Accessories Inventory
    /// @return If the given stack can be unequipped
    ///
    TriState canUnequip(ItemStack stack, SlotReference reference);
}
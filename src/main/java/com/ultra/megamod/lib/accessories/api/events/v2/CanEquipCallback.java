package com.ultra.megamod.lib.accessories.api.events.v2;

import com.ultra.megamod.lib.accessories.api.action.ActionResponseBuffer;
import com.ultra.megamod.lib.accessories.api.core.AccessoryNestUtils;
import com.ultra.megamod.lib.accessories.api.core.AccessoryRegistry;
import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import com.ultra.megamod.lib.accessories.fabric.event.Event;
import com.ultra.megamod.lib.accessories.fabric.event.EventFactory;
import net.minecraft.world.item.ItemStack;

///
/// Event callback used to allow or denied the ability to equip a given accessory for the given referenced slot
/// type and entity.
///
/// Fired in [AccessoryRegistry#canEquip(ItemStack,SlotReference)]
///
public interface CanEquipCallback {

    Event<CanEquipCallback> EVENT = EventFactory.createArrayBacked(CanEquipCallback.class,
            (invokers) -> (stack, reference, buffer) -> {
                AccessoryNestUtils.recursivelyHandle(stack, reference, (stack1, reference1) -> {
                    for (var invoker : invokers) {
                        invoker.canEquip(stack1, reference1, buffer);

                        if(buffer.shouldReturnEarly()) return true;
                    }

                    return null;
                });
            }
    );

    ///
    /// @param stack     The specific stack being evaluated
    /// @param reference The reference to the specific location within the Accessories Inventory
    /// @param buffer    The buffer to send a response to if the Accessory can or can not be equipped
    ///
    void canEquip(ItemStack stack, SlotReference reference, ActionResponseBuffer buffer);
}

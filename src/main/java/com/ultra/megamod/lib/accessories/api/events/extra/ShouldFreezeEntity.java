package com.ultra.megamod.lib.accessories.api.events.extra;

import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import com.ultra.megamod.lib.accessories.fabric.event.Event;
import com.ultra.megamod.lib.accessories.fabric.event.EventFactory;
import com.ultra.megamod.lib.accessories.fabric.TriState;
import net.minecraft.world.item.ItemStack;

public interface ShouldFreezeEntity {

    Event<ShouldFreezeEntity> EVENT = EventFactory.createArrayBacked(ShouldFreezeEntity.class, invokers -> (stack, reference) -> {
        for (var invoker : invokers) {
            var state = invoker.shouldFreeze(stack, reference);

            if(state != TriState.DEFAULT) return state;
        }

        return TriState.DEFAULT;
    });

    /**
     * @param stack               The specific stack being evaluated
     * @param reference           The reference to the specific location within the Accessories Inventory
     * @return If the given looking entity sees the given entity as disguised
     */
    TriState shouldFreeze(ItemStack stack, SlotReference reference);
}

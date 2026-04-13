package com.ultra.megamod.lib.accessories.api.events.extra;

import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import com.ultra.megamod.lib.accessories.impl.event.WrappedEvent;
import com.ultra.megamod.lib.accessories.fabric.event.Event;
import com.ultra.megamod.lib.accessories.fabric.TriState;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;

/**
 * @deprecated Use {@link IsGazeDisguised#EVENT} and check for Enderman specifically
 */
@Deprecated(forRemoval = true)
public interface EndermanMasked {

    Event<EndermanMasked> EVENT = new WrappedEvent<>(IsGazeDisguised.EVENT, endermanMasked -> {
        return (lookingEntity, stack, reference) -> {
            if (!(lookingEntity instanceof EnderMan enderMan)) return TriState.DEFAULT;

            return endermanMasked.isEndermanMasked(enderMan, stack, reference);
        };
    }, gazeEvent -> (enderMan, stack, reference) -> gazeEvent.invoker().isWearDisguise(enderMan, stack, reference));

    /**
     * @param enderMan  The specific {@link EnderMan} for the given check
     * @param stack     The specific stack being evaluated
     * @param reference The reference to the specific location within the Accessories Inventory
     * @return If the given enderman sees a mask on the given passed referenced entity
     */
    TriState isEndermanMasked(EnderMan enderMan, ItemStack stack, SlotReference reference);
}

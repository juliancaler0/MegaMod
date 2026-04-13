package com.ultra.megamod.lib.accessories.api.events;

import com.ultra.megamod.lib.accessories.api.AccessoriesCapability;
import com.ultra.megamod.lib.accessories.impl.event.AccessoriesEventHandler;
import com.ultra.megamod.lib.accessories.fabric.event.Event;
import com.ultra.megamod.lib.accessories.fabric.event.EventFactory;
import com.ultra.megamod.lib.accessories.fabric.TriState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Event callback used to change if the given entity death will use default accessory behavior to drop
 * the equipped accessories.
 * <p>
 * Fired at {@link AccessoriesEventHandler#onDeath(LivingEntity, DamageSource)}
 */
public interface OnDeathCallback {

    Event<OnDeathCallback> EVENT = EventFactory.createArrayBacked(OnDeathCallback.class,
            (invokers) -> (currentState, entity, capability, damageSource, droppedStacks) -> {
                for (var invoker : invokers) {
                    var returnState = invoker.shouldDrop(currentState, entity, capability, damageSource, droppedStacks);

                    if(returnState != TriState.DEFAULT) currentState = returnState;
                }

                return currentState;
            }
    );

    /**
     * Event used to check if the given default logic for dropping Accessories should be run or not
     *
     * @param currentState Whether someone is attempting to handle the dropping already
     * @param entity The given target entity
     * @param capability The given capability bound to the entity
     * @param damageSource
     * @param droppedStacks
     * @return If default dropping behavior should occur
     */
    TriState shouldDrop(TriState currentState, LivingEntity entity, AccessoriesCapability capability, DamageSource damageSource, List<ItemStack> droppedStacks);
}

package com.ultra.megamod.lib.accessories.api.events;

import com.ultra.megamod.lib.accessories.api.AccessoriesCapability;
import com.ultra.megamod.lib.accessories.api.AccessoriesContainer;
import com.ultra.megamod.lib.accessories.impl.event.AccessoriesEventHandler;
import com.ultra.megamod.lib.accessories.fabric.event.Event;
import com.ultra.megamod.lib.accessories.fabric.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

/**
 * Event callback fired upon detecting changes with a given {@link LivingEntity} {@link AccessoriesContainer}'s
 * at the end of {@link AccessoriesEventHandler#onLivingEntityTick(LivingEntity)}.
 */
public interface ContainersChangeCallback {

    Event<ContainersChangeCallback> EVENT = EventFactory.createArrayBacked(ContainersChangeCallback.class,
            (invokers) -> (livingEntity, capability, changedContainers) -> {
                for (var invoker : invokers) {
                    invoker.onChange(livingEntity, capability, changedContainers);
                }
            }
    );

    /**
     * @param livingEntity      The given entity instance
     * @param capability        The given capability for the entity
     * @param changedContainers a map of changed containers to a boolean indicating whether they were resized
     */
    void onChange(LivingEntity livingEntity, AccessoriesCapability capability, Map<AccessoriesContainer, Boolean> changedContainers);
}

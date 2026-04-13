package com.ultra.megamod.lib.accessories.api.events.v2;

import com.ultra.megamod.lib.accessories.Accessories;
import com.ultra.megamod.lib.accessories.api.action.ActionResponseBuffer;
import com.ultra.megamod.lib.accessories.api.menu.AccessoriesBasedSlot;
import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import com.ultra.megamod.lib.accessories.menu.AccessoriesMenuVariant;
import com.ultra.megamod.lib.accessories.fabric.event.Event;
import com.ultra.megamod.lib.accessories.fabric.event.EventFactory;
import com.ultra.megamod.lib.accessories.fabric.TriState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/// Event callback used to allow or denied the ability to adjust a given entities accessories or prevent the Accessory
/// screen being open by the given player.
///
/// Fired in [AccessoriesBasedSlot#mayPickup]
/// and in [Accessories#openAccessoriesMenu(Player, AccessoriesMenuVariant, LivingEntity,ItemStack)]
///
public interface AllowEntityModificationCallback {

    Event<AllowEntityModificationCallback> EVENT = EventFactory.createArrayBacked(AllowEntityModificationCallback.class,
        (invokers) -> (targetEntity, player, reference, buffer) -> {
            for (var invoker : invokers) {
                invoker.allowModifications(targetEntity, player, reference, buffer);

                if(buffer.shouldReturnEarly()) return;
            }
        }
    );

    ///
    /// @param targetEntity The targeted entity for modification
    /// @param player       The specific player
    /// @param reference    The reference to the specific location within the Accessories Inventory
    /// @param buffer       The buffer to send a response to if the Accessory can or can not be equipped
    ///
    void allowModifications(LivingEntity targetEntity, Player player, @Nullable SlotReference reference, ActionResponseBuffer buffer);
}

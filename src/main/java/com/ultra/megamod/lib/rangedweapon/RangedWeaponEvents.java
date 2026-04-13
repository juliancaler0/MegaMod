package com.ultra.megamod.lib.rangedweapon;

import com.ultra.megamod.lib.rangedweapon.api.EntityAttributes_RangedWeapon;
import com.ultra.megamod.lib.rangedweapon.internal.RangedHasteEntity;
import net.minecraft.world.item.ItemUseAnimation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

/**
 * NeoForge event registrations for RangedWeaponAPI.
 * Call {@link #register()} during mod initialization.
 */
public class RangedWeaponEvents {

    public static void register() {
        RangedWeaponMod.init();

        // Reset happens via mixin
        // `clearActiveItem` => `stopUsingItem`

        NeoForge.EVENT_BUS.addListener(LivingEntityUseItemEvent.Tick.class, (event) -> {
            var entity = event.getEntity();
            var activeItemStack = entity.getUseItem();
            if (entity.isUsingItem())  {
                var useAction = activeItemStack.getUseAnimation();
                if (useAction == ItemUseAnimation.BOW || useAction == ItemUseAnimation.CROSSBOW) {
                    var haste = entity.getAttributeValue(EntityAttributes_RangedWeapon.HASTE.entry);
                    if (haste != EntityAttributes_RangedWeapon.HASTE.baseValue) {
                        // Upon calling this event, NeoForge modifies the itemUseTimeLeft already
                        // by querying it, and than setting it back to itself.
                        // Hence we step back by one partial tick
                        event.setDuration((int) (event.getDuration() + ((RangedHasteEntity)entity).getPartialHasteTick()));

                        var time = entity.getAttributeValue(EntityAttributes_RangedWeapon.PULL_TIME.entry);
                        // var timeTicks = Math.round(time * 20);
                        var bonus = EntityAttributes_RangedWeapon.HASTE.asMultiplier(haste) - 1F;
                        var partialTick = time * bonus;
                        ((RangedHasteEntity)entity).addPartialHasteTick((float) partialTick);
                    }
                }
            }
        });
    }
}

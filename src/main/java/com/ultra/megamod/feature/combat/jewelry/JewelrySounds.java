package com.ultra.megamod.feature.combat.jewelry;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sound events for the jewelry system, ported 1:1 from the Jewelry mod reference.
 * Includes equip sound and workbench sound for the jeweler villager.
 */
public class JewelrySounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MegaMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> JEWELRY_EQUIP =
            SOUND_EVENTS.register("jewelry_equip",
                    () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MegaMod.MODID, "jewelry_equip")));

    public static final DeferredHolder<SoundEvent, SoundEvent> JEWELRY_WORKBENCH =
            SOUND_EVENTS.register("jewelry_workbench",
                    () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MegaMod.MODID, "jewelry_workbench")));

    public static Holder<SoundEvent> getEquipSound() {
        return JEWELRY_EQUIP;
    }
}

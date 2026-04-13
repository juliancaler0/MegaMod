package com.ultra.megamod.lib.accessories.api;

import com.ultra.megamod.lib.accessories.api.core.Accessory;
import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

/**
 * Represents a sound event with volume and pitch data.
 *
 * @see Accessory#getEquipSound(ItemStack, SlotReference)
 */
public record SoundEventData(Holder<SoundEvent> event, float volume, float pitch) { }

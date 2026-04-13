package com.ultra.megamod.lib.spellengine.compat.accessories;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

/**
 * Accessory item for spell hosts.
 * The relic/accessories API is not yet present. This extends Item directly as a stub.
 */
public class SpellHostAccessoryItem extends Item {
    private final Supplier<Holder<SoundEvent>> equipSound;

    public SpellHostAccessoryItem(Item.Properties settings, Supplier<Holder<SoundEvent>> equipSound) {
        super(settings);
        this.equipSound = equipSound;
    }

    // isEnchantable removed in 1.21.11 - enchantability is determined by components
}

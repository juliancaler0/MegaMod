package com.ultra.megamod.lib.spellengine.compat.accessories;

import net.minecraft.world.item.Item;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

/**
 * Universal spell book item for Accessories mod compatibility.
 * Pool ID is derived from the SPELL_CONTAINER component, not stored in the item.
 */
public class SpellBookAccessoryItem extends SpellHostAccessoryItem {
    public SpellBookAccessoryItem(Item.Properties settings, Supplier<Holder<SoundEvent>> equipSound) {
        super(settings, equipSound);
    }
}

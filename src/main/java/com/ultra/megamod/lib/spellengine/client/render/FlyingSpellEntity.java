package com.ultra.megamod.lib.spellengine.client.render;

import net.minecraft.world.entity.projectile.ItemSupplier;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;

public interface FlyingSpellEntity extends ItemSupplier {
    Spell.ProjectileModel renderData();
}

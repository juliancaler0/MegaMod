package com.ultra.megamod.lib.pufferfish_skills.access;

import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface DamageSourceAccess {
	Optional<ItemStack> getWeapon();
}

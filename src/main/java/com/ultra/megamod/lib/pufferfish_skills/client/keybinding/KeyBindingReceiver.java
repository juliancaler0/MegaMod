package com.ultra.megamod.lib.pufferfish_skills.client.keybinding;

import net.minecraft.client.KeyMapping;

public interface KeyBindingReceiver {
	void registerKeyMapping(KeyMapping keyBinding, KeyBindingHandler handler);
}

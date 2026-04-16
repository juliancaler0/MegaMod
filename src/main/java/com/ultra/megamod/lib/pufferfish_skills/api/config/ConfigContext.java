package com.ultra.megamod.lib.pufferfish_skills.api.config;

import net.minecraft.server.MinecraftServer;

public interface ConfigContext {
	MinecraftServer getServer();

	void emitWarning(String message);
}

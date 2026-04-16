package com.ultra.megamod.lib.pufferfish_skills.impl.experience.source;

import net.minecraft.server.MinecraftServer;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSourceDisposeContext;
import com.ultra.megamod.lib.pufferfish_skills.util.DisposeContext;

public record ExperienceSourceDisposeContextImpl(DisposeContext context) implements ExperienceSourceDisposeContext {
	@Override
	public MinecraftServer getServer() {
		return context.server();
	}
}

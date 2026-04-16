package com.ultra.megamod.lib.pufferfish_skills.impl.rewards;

import net.minecraft.server.MinecraftServer;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.RewardDisposeContext;
import com.ultra.megamod.lib.pufferfish_skills.util.DisposeContext;

public record RewardDisposeContextImpl(DisposeContext context) implements RewardDisposeContext {
	@Override
	public MinecraftServer getServer() {
		return context.server();
	}
}

package com.ultra.megamod.lib.pufferfish_skills.api.reward;

import net.minecraft.server.level.ServerPlayer;

public interface RewardUpdateContext {
	ServerPlayer getPlayer();

	int getCount();

	boolean isAction();
}

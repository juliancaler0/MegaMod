package com.ultra.megamod.lib.pufferfish_skills.impl.rewards;

import net.minecraft.server.level.ServerPlayer;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.RewardUpdateContext;

public record RewardUpdateContextImpl(ServerPlayer player, int count, boolean isAction) implements RewardUpdateContext {

	@Override
	public ServerPlayer getPlayer() {
		return player;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public boolean isAction() {
		return isAction;
	}

}

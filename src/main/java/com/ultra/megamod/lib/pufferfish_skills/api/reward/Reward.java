package com.ultra.megamod.lib.pufferfish_skills.api.reward;

public interface Reward {
	void update(RewardUpdateContext context);

	void dispose(RewardDisposeContext context);
}

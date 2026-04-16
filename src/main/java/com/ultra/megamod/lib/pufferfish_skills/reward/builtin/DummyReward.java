package com.ultra.megamod.lib.pufferfish_skills.reward.builtin;

import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.Reward;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.RewardDisposeContext;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.RewardUpdateContext;

public class DummyReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("dummy");

	@Override
	public void update(RewardUpdateContext context) { }

	@Override
	public void dispose(RewardDisposeContext context) { }
}

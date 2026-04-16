package com.ultra.megamod.lib.pufferfish_skills.reward;

import com.ultra.megamod.lib.pufferfish_skills.reward.builtin.AttributeReward;
import com.ultra.megamod.lib.pufferfish_skills.reward.builtin.CommandReward;
import com.ultra.megamod.lib.pufferfish_skills.reward.builtin.PointsReward;
import com.ultra.megamod.lib.pufferfish_skills.reward.builtin.ScoreboardReward;
import com.ultra.megamod.lib.pufferfish_skills.reward.builtin.TagReward;

public class BuiltinRewards {
	public static void register() {
		AttributeReward.register();
		CommandReward.register();
		PointsReward.register();
		ScoreboardReward.register();
		TagReward.register();
	}
}

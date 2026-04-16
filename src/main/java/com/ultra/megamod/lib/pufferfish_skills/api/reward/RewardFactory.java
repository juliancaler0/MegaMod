package com.ultra.megamod.lib.pufferfish_skills.api.reward;

import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;

public interface RewardFactory {
	Result<? extends Reward, Problem> create(RewardConfigContext context);
}

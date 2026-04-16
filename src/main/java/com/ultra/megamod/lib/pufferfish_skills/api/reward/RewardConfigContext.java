package com.ultra.megamod.lib.pufferfish_skills.api.reward;

import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;

public interface RewardConfigContext extends ConfigContext {
	Result<JsonElement, Problem> getData();
}

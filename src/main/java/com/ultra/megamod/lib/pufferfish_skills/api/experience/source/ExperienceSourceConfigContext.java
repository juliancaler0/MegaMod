package com.ultra.megamod.lib.pufferfish_skills.api.experience.source;

import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;

public interface ExperienceSourceConfigContext extends ConfigContext {
	Result<JsonElement, Problem> getData();
}

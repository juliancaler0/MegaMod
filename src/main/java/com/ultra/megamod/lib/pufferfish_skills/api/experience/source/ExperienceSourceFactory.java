package com.ultra.megamod.lib.pufferfish_skills.api.experience.source;

import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;

public interface ExperienceSourceFactory {
	Result<? extends ExperienceSource, Problem> create(ExperienceSourceConfigContext context);
}

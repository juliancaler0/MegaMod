package com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.util;

import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;

public enum TamedActivity {
	EXCLUDE,
	INCLUDE,
	ONLY;

	public static Result<TamedActivity, Problem> parse(JsonElement rootElement) {
		return rootElement.getAsString().andThen(string -> switch (string) {
			case "exclude" -> Result.success(TamedActivity.EXCLUDE);
			case "include" -> Result.success(TamedActivity.INCLUDE);
			case "only" -> Result.success(TamedActivity.ONLY);
			default -> Result.failure(rootElement.getPath().createProblem("Expected a valid tamed option"));
		});
	}
}

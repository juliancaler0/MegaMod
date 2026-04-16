package com.ultra.megamod.lib.pufferfish_skills.config.skill;

import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonArray;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;

public record SkillConnectionsConfig(
		SkillConnectionsGroupConfig normal,
		SkillConnectionsGroupConfig exclusive
) {

	public static Result<SkillConnectionsConfig, Problem> parse(JsonElement rootElement, SkillsConfig skills, ConfigContext context) {
		return rootElement.getAsObject().flatMap(
				LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, skills, context), context),
				problem -> rootElement.getAsArray()
						.andThen(rootArray -> parseLegacy(rootArray, skills))
		);
	}

	private static Result<SkillConnectionsConfig, Problem> parse(JsonObject rootObject, SkillsConfig skills, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var normal = rootObject.get("normal")
				.getSuccess()
				.flatMap(element -> SkillConnectionsGroupConfig.parse(element, skills, context)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(SkillConnectionsGroupConfig::empty);

		var exclusive = rootObject.get("exclusive")
				.getSuccess()
				.flatMap(element -> SkillConnectionsGroupConfig.parse(element, skills, context)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(SkillConnectionsGroupConfig::empty);

		if (problems.isEmpty()) {
			return Result.success(new SkillConnectionsConfig(
					normal,
					exclusive
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	private static Result<SkillConnectionsConfig, Problem> parseLegacy(JsonArray rootArray, SkillsConfig skills) {
		return SkillConnectionsGroupConfig.parseLegacy(rootArray, skills)
				.mapSuccess(normal -> new SkillConnectionsConfig(
						normal,
						SkillConnectionsGroupConfig.empty()
				));
	}

}
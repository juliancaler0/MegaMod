package com.ultra.megamod.lib.pufferfish_skills.config.skill;

import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonArray;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;

import java.util.ArrayList;
import java.util.Optional;

public record SkillConnectionConfig(
		String skillAId,
		String skillBId
) {

	public static Result<Optional<SkillConnectionConfig>, Problem> parse(JsonElement rootElement, SkillsConfig skills) {
		return rootElement.getAsArray()
				.andThen(rootArray -> SkillConnectionConfig.parse(rootArray, skills));
	}

	private static Result<Optional<SkillConnectionConfig>, Problem> parse(JsonArray rootArray, SkillsConfig skills) {
		if (rootArray.getSize() != 2) {
			return Result.failure(rootArray.getPath().createProblem("Expected an array of 2 elements"));
		}

		var problems = new ArrayList<Problem>();

		var optIds = rootArray.getAsList((i, element) -> element.getAsString()
						.andThen(skillId -> {
							if (skills.isValid(skillId)) {
								return Result.success(skillId);
							} else {
								return Result.failure(element.getPath().createProblem("Expected a valid skill"));
							}
						})
				)
				.ifFailure(problems::addAll)
				.getSuccess();

		if (problems.isEmpty()) {
			var ids = optIds.orElseThrow();
			var skillAId = ids.get(0);
			var skillBId = ids.get(1);
			if (skills.isLoaded(skillAId) && skills.isLoaded(skillBId)) {
				return Result.success(Optional.of(new SkillConnectionConfig(skillAId, skillBId)));
			} else {
				return Result.success(Optional.empty());
			}
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

}

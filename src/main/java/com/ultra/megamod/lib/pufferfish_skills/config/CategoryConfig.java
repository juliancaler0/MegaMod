package com.ultra.megamod.lib.pufferfish_skills.config;

import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.config.experience.ExperienceConfig;
import com.ultra.megamod.lib.pufferfish_skills.config.skill.SkillConnectionsConfig;
import com.ultra.megamod.lib.pufferfish_skills.config.skill.SkillDefinitionsConfig;
import com.ultra.megamod.lib.pufferfish_skills.config.skill.SkillsConfig;
import com.ultra.megamod.lib.pufferfish_skills.util.DisposeContext;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

public record CategoryConfig(
		Identifier id,
		GeneralConfig general,
		SkillDefinitionsConfig definitions,
		SkillsConfig skills,
		SkillConnectionsConfig connections,
		Optional<ExperienceConfig> experience
) {

	public static Result<CategoryConfig, Problem> parse(
			Identifier id,
			JsonElement generalElement,
			JsonElement definitionsElement,
			JsonElement skillsElement,
			JsonElement connectionsElement,
			Optional<JsonElement> optExperienceElement,
			ConfigContext context
	) {
		var problems = new ArrayList<Problem>();

		var optGeneral = GeneralConfig.parse(generalElement, context)
				.ifFailure(problems::add)
				.getSuccess();

		var optExperience = optExperienceElement
				.flatMap(experience -> ExperienceConfig.parse(experience, context)
						.ifFailure(problems::add)
						.getSuccess()
						.flatMap(Function.identity())
				);

		var optDefinitions = SkillDefinitionsConfig.parse(definitionsElement, context)
				.ifFailure(problems::add)
				.getSuccess();

		var optSkills = optDefinitions.flatMap(
				definitions -> SkillsConfig.parse(skillsElement, definitions, context)
						.ifFailure(problems::add)
						.getSuccess()
		);

		var optConnections = optSkills.flatMap(
				skills -> SkillConnectionsConfig.parse(connectionsElement, skills, context)
						.ifFailure(problems::add)
						.getSuccess()
		);

		if (problems.isEmpty()) {
			return Result.success(new CategoryConfig(
					id,
					optGeneral.orElseThrow(),
					optDefinitions.orElseThrow(),
					optSkills.orElseThrow(),
					optConnections.orElseThrow(),
					optExperience
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public void dispose(DisposeContext context) {
		definitions.dispose(context);
		experience.ifPresent(experience -> experience.dispose(context));
	}

}

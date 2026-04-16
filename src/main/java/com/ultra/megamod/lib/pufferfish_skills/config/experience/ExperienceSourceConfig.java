package com.ultra.megamod.lib.pufferfish_skills.config.experience;

import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.api.json.BuiltinJson;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonPath;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.ExperienceSourceRegistry;
import com.ultra.megamod.lib.pufferfish_skills.impl.experience.source.ExperienceSourceConfigContextImpl;
import com.ultra.megamod.lib.pufferfish_skills.impl.experience.source.ExperienceSourceDisposeContextImpl;
import com.ultra.megamod.lib.pufferfish_skills.util.DisposeContext;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;

public record ExperienceSourceConfig(
		Identifier type,
		ExperienceSource instance,
		Optional<ExperienceTeamSharingConfig> teamSharing
) {

	public static Result<ExperienceSourceConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context)
		);
	}

	public static Result<ExperienceSourceConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optTypeElement = rootObject.get("type")
				.ifFailure(problems::add)
				.getSuccess();

		var optType = optTypeElement.flatMap(
				typeElement -> BuiltinJson.parseIdentifier(typeElement)
						.ifFailure(problems::add)
						.getSuccess()
		);

		var maybeDataElement = rootObject.get("data");

		var optTeamSharing = rootObject.get("team_sharing")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> ExperienceTeamSharingConfig.parse(element)
						.ifFailure(problems::add)
						.getSuccess()
				);

		if (problems.isEmpty()) {
			return build(
					optType.orElseThrow(),
					maybeDataElement,
					optTypeElement.orElseThrow().getPath(),
					optTeamSharing,
					context
			);
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	private static Result<ExperienceSourceConfig, Problem> build(
			Identifier type,
			Result<JsonElement, Problem> maybeDataElement,
			JsonPath typeElementPath,
			Optional<ExperienceTeamSharingConfig> optTeamSharing,
			ConfigContext context
	) {
		return ExperienceSourceRegistry.getFactory(type)
				.map(factory -> factory.create(new ExperienceSourceConfigContextImpl(context, maybeDataElement))
						.mapSuccess(instance -> new ExperienceSourceConfig(type, instance, optTeamSharing))
				)
				.orElseGet(() -> Result.failure(typeElementPath.createProblem("Expected a valid source type")));
	}

	public void dispose(DisposeContext context) {
		this.instance.dispose(new ExperienceSourceDisposeContextImpl(context));
	}

}

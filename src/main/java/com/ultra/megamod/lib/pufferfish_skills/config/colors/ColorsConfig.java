package com.ultra.megamod.lib.pufferfish_skills.config.colors;

import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;

public record ColorsConfig(
		ConnectionsColorsConfig connections,
		FillStrokeColorsConfig points
) {
	private static final FillStrokeColorsConfig DEFAULT_POINTS = new FillStrokeColorsConfig(
			new ColorConfig(0xff80ff20),
			new ColorConfig(0xff000000)
	);

	public static ColorsConfig createDefault() {
		return new ColorsConfig(
				ConnectionsColorsConfig.createDefault(),
				DEFAULT_POINTS
		);
	}

	public static Result<ColorsConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context)
		);
	}

	private static Result<ColorsConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var connections = rootObject.get("connections")
				.getSuccess()
				.flatMap(element -> ConnectionsColorsConfig.parse(element, context)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(ConnectionsColorsConfig::createDefault);

		var points = rootObject.get("points")
				.getSuccess()
				.flatMap(element -> FillStrokeColorsConfig.parse(element, DEFAULT_POINTS, context)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(DEFAULT_POINTS);

		if (problems.isEmpty()) {
			return Result.success(new ColorsConfig(
					connections,
					points
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

}

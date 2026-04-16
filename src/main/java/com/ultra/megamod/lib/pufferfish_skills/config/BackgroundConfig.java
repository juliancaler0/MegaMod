package com.ultra.megamod.lib.pufferfish_skills.config;

import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.BuiltinJson;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.common.BackgroundPosition;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;

public record BackgroundConfig(
		Identifier texture,
		int width,
		int height,
		BackgroundPosition position
) {

	public static Result<BackgroundConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject().flatMap(
				LegacyUtils.wrapNoUnused(BackgroundConfig::parse, context),
				failure -> BuiltinJson.parseIdentifier(rootElement)
						.mapSuccess(texture -> new BackgroundConfig(texture, 16, 16, BackgroundPosition.TILE))
		);
	}

	public static Result<BackgroundConfig, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optTexture = rootObject.get("texture")
				.andThen(BuiltinJson::parseIdentifier)
				.ifFailure(problems::add)
				.getSuccess();

		var optWidth = rootObject.getInt("width")
				.ifFailure(problems::add)
				.getSuccess();

		var optHeight = rootObject.getInt("height")
				.ifFailure(problems::add)
				.getSuccess();

		var position = rootObject.get("position")
				.getSuccess()
				.flatMap(element -> parseBackgroundPosition(element)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(BackgroundPosition.NONE);

		if (problems.isEmpty()) {
			return Result.success(new BackgroundConfig(
					optTexture.orElseThrow(),
					optWidth.orElseThrow(),
					optHeight.orElseThrow(),
					position
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public static Result<BackgroundPosition, Problem> parseBackgroundPosition(JsonElement rootElement) {
		return rootElement.getAsString().andThen(string -> switch (string) {
			case "none" -> Result.success(BackgroundPosition.NONE);
			case "tile" -> Result.success(BackgroundPosition.TILE);
			case "fill" -> Result.success(BackgroundPosition.FILL);
			case "fill_width" -> Result.success(BackgroundPosition.FILL_WIDTH);
			case "fill_height" -> Result.success(BackgroundPosition.FILL_HEIGHT);
			default -> Result.failure(rootElement.getPath().createProblem("Expected valid background position"));
		});
	}
}

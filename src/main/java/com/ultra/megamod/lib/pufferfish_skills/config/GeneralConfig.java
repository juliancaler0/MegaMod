package com.ultra.megamod.lib.pufferfish_skills.config;

import net.minecraft.network.chat.Component;
import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.BuiltinJson;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.config.colors.ColorsConfig;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;

public record GeneralConfig(
		Component title,
		Component description,
		Component extraDescription,
		IconConfig icon,
		BackgroundConfig background,
		ColorsConfig colors,
		boolean unlockedByDefault,
		int startingPoints,
		boolean exclusiveRoot,
		int spentPointsLimit
) {

	public static Result<GeneralConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject()
				.andThen(LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context));
	}

	public static Result<GeneralConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optTitle = rootObject.get("title")
				.andThen(titleElement -> BuiltinJson.parseText(titleElement, context.getServer().registryAccess()))
				.ifFailure(problems::add)
				.getSuccess();

		var description = rootObject.get("description")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(descriptionElement -> BuiltinJson.parseText(descriptionElement, context.getServer().registryAccess())
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(Component::empty);

		var extraDescription = rootObject.get("extra_description")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(descriptionElement -> BuiltinJson.parseText(descriptionElement, context.getServer().registryAccess())
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(Component::empty);

		var optIcon = rootObject.get("icon")
				.andThen(element -> IconConfig.parse(element, context))
				.ifFailure(problems::add)
				.getSuccess();

		var optBackground = rootObject.get("background")
				.andThen(element -> BackgroundConfig.parse(element, context))
				.ifFailure(problems::add)
				.getSuccess();

		var colors = rootObject.get("colors")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> ColorsConfig.parse(element, context)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(ColorsConfig::createDefault);

		var unlockedByDefault = rootObject.get("unlocked_by_default")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsBoolean()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(true);

		var startingPoints = rootObject.get("starting_points")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(0);

		var exclusiveRoot = rootObject.get("exclusive_root")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsBoolean()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(false);

		var spentPointsLimit = rootObject.get("spent_points_limit")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(Integer.MAX_VALUE);

		if (problems.isEmpty()) {
			return Result.success(new GeneralConfig(
					optTitle.orElseThrow(),
					description,
					extraDescription,
					optIcon.orElseThrow(),
					optBackground.orElseThrow(),
					colors,
					unlockedByDefault,
					startingPoints,
					exclusiveRoot,
					spentPointsLimit
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

}

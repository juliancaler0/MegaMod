package com.ultra.megamod.lib.pufferfish_skills.calculation;

import com.ultra.megamod.lib.pufferfish_skills.api.calculation.Calculation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.Variables;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.Prototype;
import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;
import java.util.List;

public class LegacyCalculation {
	public static <T> Result<Calculation<T>, Problem> parse(
			JsonElement rootElement,
			Prototype<T> prototype,
			ConfigContext context
	) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, prototype, context), context)
		);
	}

	public static <T> Result<Calculation<T>, Problem> parse(
			JsonObject rootObject,
			Prototype<T> prototype,
			ConfigContext context
	) {
		var problems = new ArrayList<Problem>();

		var variablesList = new ArrayList<Variables<T, Double>>();

		for (var keys : LegacyUtils.isRemoved(3, context)
				? List.of("variables")
				: List.of("parameters", "conditions", "variables")
		) {
			rootObject.get(keys)
					.getSuccess() // ignore failure because this property is optional
					.ifPresent(variablesElement -> Variables.parse(variablesElement, prototype, context)
							.ifFailure(problems::add)
							.ifSuccess(variablesList::add)
					);
		}

		var optCalculation = rootObject.get("experience")
				.andThen(experienceElement -> Calculation.parse(
						experienceElement,
						Variables.combine(variablesList), context)
				)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(optCalculation.orElseThrow());
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}
}

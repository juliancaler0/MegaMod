package com.ultra.megamod.lib.pufferfish_skills.api.calculation;

import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.impl.calculation.CalculationImpl;

public interface Calculation<T> {

	static <T> Result<Calculation<T>, Problem> parse(
			JsonElement rootElement,
			Variables<T, Double> variables,
			ConfigContext context
	) {
		return CalculationImpl.create(rootElement, variables, context)
				.mapSuccess(c -> c);
	}

	double evaluate(T t);
}

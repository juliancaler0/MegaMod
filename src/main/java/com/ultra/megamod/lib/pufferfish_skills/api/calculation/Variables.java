package com.ultra.megamod.lib.pufferfish_skills.api.calculation;

import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.Prototype;
import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.impl.calculation.VariablesImpl;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Variables<T, R> {

	static <T> Result<Variables<T, Double>, Problem> parse(
			JsonElement rootElement,
			Prototype<T> prototype,
			ConfigContext context
	) {
		return VariablesImpl.parse(rootElement, prototype, context);
	}

	static <T, R> Variables<T, R> create(
			Map<String, Function<T, R>> operations
	) {
		return VariablesImpl.create(operations);
	}

	static <T, R> Variables<T, R> combine(
			Collection<Variables<T, R>> variables
	) {
		return VariablesImpl.combine(variables);
	}

	@SafeVarargs
	static <T, R> Variables<T, R> combine(
			Variables<T, R>... variables
	) {
		return VariablesImpl.combine(variables);
	}

	Stream<String> streamNames();

	Map<String, R> evaluate(T t);
}

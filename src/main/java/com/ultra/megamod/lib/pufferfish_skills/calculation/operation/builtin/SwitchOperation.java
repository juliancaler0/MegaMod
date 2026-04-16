package com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin;

import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.Operation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.BuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;

public final class SwitchOperation implements Operation<Boolean, Double> {
	private final double trueValue;
	private final double falseValue;

	private SwitchOperation(double trueValue, double falseValue) {
		this.trueValue = trueValue;
		this.falseValue = falseValue;
	}

	public static void register() {
		BuiltinPrototypes.BOOLEAN.registerOperation(
				SkillsMod.createIdentifier("switch"),
				BuiltinPrototypes.NUMBER,
				SwitchOperation::parse
		);
	}

	public static Result<SwitchOperation, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(SwitchOperation::parse, context));
	}

	public static Result<SwitchOperation, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optTrue = rootObject.getDouble("true")
				.ifFailure(problems::add)
				.getSuccess();

		var optFalse = rootObject.getDouble("false")
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new SwitchOperation(
					optTrue.orElseThrow(),
					optFalse.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Double> apply(Boolean value) {
		return Optional.of(value ? trueValue : falseValue);
	}
}

package com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin;

import net.minecraft.world.entity.Entity;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.Operation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.BuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.calculation.LegacyBuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;

public final class ScoreboardOperation implements Operation<Entity, Double> {
	private final String objectiveName;

	private ScoreboardOperation(String objectiveName) {
		this.objectiveName = objectiveName;
	}

	public static void register() {
		BuiltinPrototypes.ENTITY.registerOperation(
				SkillsMod.createIdentifier("get_score"),
				BuiltinPrototypes.NUMBER,
				ScoreboardOperation::parse
		);

		LegacyBuiltinPrototypes.registerAlias(
				BuiltinPrototypes.ENTITY,
				SkillsMod.createIdentifier("scoreboard"),
				SkillsMod.createIdentifier("get_score")
		);
	}

	public static Result<ScoreboardOperation, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(ScoreboardOperation::parse, context));
	}

	public static Result<ScoreboardOperation, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optScoreboard = rootObject.getString("objective")
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new ScoreboardOperation(
					optScoreboard.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Double> apply(Entity entity) {
		var scoreboard = entity.level().getScoreboard();
		return Optional.ofNullable(scoreboard.getNullableObjective(objectiveName))
				.map(objective -> Optional.ofNullable(scoreboard.getScore(entity, objective))
						.map(score -> (double) score.getScore())
						.orElse(0.0));
	}
}

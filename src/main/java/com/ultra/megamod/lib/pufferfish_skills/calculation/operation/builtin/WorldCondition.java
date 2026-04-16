package com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.Operation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.BuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.json.BuiltinJson;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;

import java.util.ArrayList;
import java.util.Optional;

public final class WorldCondition implements Operation<ServerLevel, Boolean> {
	private final Identifier dimension;

	private WorldCondition(Identifier dimension) {
		this.dimension = dimension;
	}

	public static void register() {
		BuiltinPrototypes.WORLD.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				WorldCondition::parse
		);
	}

	public static Result<WorldCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(rootObject -> rootObject.noUnused(WorldCondition::parse));
	}

	public static Result<WorldCondition, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optDimension = rootObject.get("dimension")
				.andThen(BuiltinJson::parseIdentifier)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new WorldCondition(
					optDimension.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(ServerLevel world) {
		return Optional.of(world.dimension().identifier().equals(dimension));
	}
}

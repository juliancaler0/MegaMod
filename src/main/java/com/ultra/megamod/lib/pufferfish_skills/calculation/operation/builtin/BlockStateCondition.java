package com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.HolderSet;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.Operation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.BuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.json.BuiltinJson;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;

public final class BlockStateCondition implements Operation<BlockState, Boolean> {
	private final Optional<HolderSet<Block>> optBlockEntries;
	private final Optional<StatePropertiesPredicate> optState;

	private BlockStateCondition(Optional<HolderSet<Block>> optBlockEntries, Optional<StatePropertiesPredicate> optState) {
		this.optBlockEntries = optBlockEntries;
		this.optState = optState;
	}

	public static void register() {
		BuiltinPrototypes.BLOCK_STATE.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				BlockStateCondition::parse
		);
	}

	public static Result<BlockStateCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(BlockStateCondition::parse, context));
	}

	public static Result<BlockStateCondition, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optBlock = rootObject.get("block")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(idElement -> BuiltinJson.parseBlockOrBlockTag(idElement)
						.ifFailure(problems::add)
						.getSuccess()
				);

		var optState = rootObject.get("state")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(stateElement -> BuiltinJson.parseStatePredicate(stateElement)
						.ifFailure(problems::add)
						.getSuccess()
				);

		if (problems.isEmpty()) {
			return Result.success(new BlockStateCondition(
					optBlock,
					optState
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(BlockState blockState) {
		return Optional.of(
				optBlockEntries.map(blockState::isIn).orElse(true)
						&& optState.map(state -> state.test(blockState)).orElse(true)
		);
	}
}

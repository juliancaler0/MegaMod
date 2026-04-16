package com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.Calculation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationFactory;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.BuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.Prototype;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSourceConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSourceDisposeContext;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.calculation.LegacyBuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.calculation.LegacyCalculation;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.LegacyOperationRegistry;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.AttributeOperation;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.BlockStateCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.EffectOperation;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.ItemStackCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.legacy.LegacyBlockTagCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.legacy.LegacyItemTagCondition;

public record MineBlockExperienceSource(
		Calculation<Data> calculation
) implements ExperienceSource {

	private static final Identifier ID = SkillsMod.createIdentifier("mine_block");
	private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

	static {
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_player"),
				BuiltinPrototypes.PLAYER,
				OperationFactory.create(Data::player)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_mined_block_state"),
				BuiltinPrototypes.BLOCK_STATE,
				OperationFactory.create(Data::blockState)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_tool_item_stack"),
				BuiltinPrototypes.ITEM_STACK,
				OperationFactory.create(Data::tool)
		);
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				MineBlockExperienceSource::parse
		);
	}

	private static Result<MineBlockExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
		return context.getData().andThen(rootElement ->
				LegacyCalculation.parse(rootElement, PROTOTYPE, context)
						.mapSuccess(MineBlockExperienceSource::new)
		);
	}

	public record Data(ServerPlayer player, BlockState blockState, ItemStack tool) { }

	@Override
	public void dispose(ExperienceSourceDisposeContext context) {
		// Nothing to do.
	}

	static {
		// Backwards compatibility.
		var legacy = new LegacyOperationRegistry<>(PROTOTYPE);
		legacy.registerBooleanFunction(
				"block",
				BlockStateCondition::parse,
				Data::blockState
		);
		legacy.registerBooleanFunction(
				"block_state",
				BlockStateCondition::parse,
				Data::blockState
		);
		legacy.registerBooleanFunction(
				"block_tag",
				LegacyBlockTagCondition::parse,
				Data::blockState
		);
		legacy.registerBooleanFunction(
				"tool",
				ItemStackCondition::parse,
				Data::tool
		);
		legacy.registerBooleanFunction(
				"tool_nbt",
				ItemStackCondition::parse,
				Data::tool
		);
		legacy.registerBooleanFunction(
				"tool_tag",
				LegacyItemTagCondition::parse,
				Data::tool
		);
		legacy.registerNumberFunction(
				"player_effect",
				effect -> (double) (effect.getAmplifier() + 1),
				EffectOperation::parse,
				Data::player
		);
		legacy.registerNumberFunction(
				"player_attribute",
				AttributeInstance::getValue,
				AttributeOperation::parse,
				Data::player
		);

		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("player"),
				SkillsMod.createIdentifier("get_player")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("mined_block_state"),
				SkillsMod.createIdentifier("get_mined_block_state")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("tool_item_stack"),
				SkillsMod.createIdentifier("get_tool_item_stack")
		);
	}
}

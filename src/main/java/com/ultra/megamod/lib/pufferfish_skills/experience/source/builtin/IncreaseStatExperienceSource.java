package com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
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
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.EffectOperation;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.StatCondition;

public record IncreaseStatExperienceSource(
		Calculation<Data> calculation
) implements ExperienceSource {

	private static final Identifier ID = SkillsMod.createIdentifier("increase_stat");
	private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

	static {
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_player"),
				BuiltinPrototypes.PLAYER,
				OperationFactory.create(Data::player)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_stat"),
				BuiltinPrototypes.STAT,
				OperationFactory.create(Data::stat)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_increase_amount"),
				BuiltinPrototypes.NUMBER,
				OperationFactory.create(data -> (double) data.amount())
		);
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				IncreaseStatExperienceSource::parse
		);
	}

	private static Result<IncreaseStatExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
		return context.getData().andThen(rootElement ->
				LegacyCalculation.parse(rootElement, PROTOTYPE, context)
						.mapSuccess(IncreaseStatExperienceSource::new)
		);
	}

	public record Data(ServerPlayer player, Stat<?> stat, int amount) { }

	@Override
	public void dispose(ExperienceSourceDisposeContext context) {
		// Nothing to do.
	}

	static {


		// Backwards compatibility.
		var legacy = new LegacyOperationRegistry<>(PROTOTYPE);
		legacy.registerBooleanFunction(
				"stat",
				StatCondition::parse,
				Data::stat
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
		legacy.registerNumberFunction(
				"amount",
				data -> (double) data.amount()
		);

		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("player"),
				SkillsMod.createIdentifier("get_player")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("stat"),
				SkillsMod.createIdentifier("get_stat")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("amount"),
				SkillsMod.createIdentifier("get_increase_amount")
		);
	}
}

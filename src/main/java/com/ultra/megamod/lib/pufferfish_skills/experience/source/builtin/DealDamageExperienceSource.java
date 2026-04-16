package com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.Calculation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.Variables;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationFactory;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.BuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.Prototype;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSourceConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSourceDisposeContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.util.AntiFarmingPerChunk;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.util.AntiFarmingPerEntity;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.util.TamedActivity;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record DealDamageExperienceSource(
		Calculation<Data> calculation,
		Optional<AntiFarmingPerEntity> antiFarmingPerEntity,
		Optional<AntiFarmingPerChunk> antiFarmingPerChunk,
		TamedActivity tamedActivity
) implements ExperienceSource {

	private static final Identifier ID = SkillsMod.createIdentifier("deal_damage");
	private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

	static {
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_player"),
				BuiltinPrototypes.PLAYER,
				OperationFactory.create(Data::player)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_weapon_item_stack"),
				BuiltinPrototypes.ITEM_STACK,
				OperationFactory.create(Data::weapon)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_damaged_living_entity"),
				BuiltinPrototypes.LIVING_ENTITY,
				OperationFactory.create(Data::entity)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_damage_source"),
				BuiltinPrototypes.DAMAGE_SOURCE,
				OperationFactory.create(Data::damageSource)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_dealt_damage"),
				BuiltinPrototypes.NUMBER,
				OperationFactory.create(data -> (double) data.damage())
		);
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				DealDamageExperienceSource::parse
		);
	}

	private static Result<DealDamageExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context));
	}

	private static Result<DealDamageExperienceSource, Problem> parse(JsonObject rootObject, ExperienceSourceConfigContext context) {
		var problems = new ArrayList<Problem>();

		var variables = rootObject.get("variables")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(variablesElement -> Variables.parse(variablesElement, PROTOTYPE, context)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(() -> Variables.create(Map.of()));

		var optCalculation = rootObject.get("experience")
				.andThen(experienceElement -> Calculation.parse(
						experienceElement,
						variables,
						context
				))
				.ifFailure(problems::add)
				.getSuccess();

		var antiFarmingPerEntity = rootObject.get("anti_farming_per_entity")
				.orElse(LegacyUtils.wrapDeprecated(() -> rootObject.get("anti_farming"), 4, context))
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> AntiFarmingPerEntity.parse(element, context)
						.ifFailure(problems::add)
						.getSuccess()
				);

		var antiFarmingPerChunk = rootObject.get("anti_farming_per_chunk")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> AntiFarmingPerChunk.parse(element, context)
						.ifFailure(problems::add)
						.getSuccess()
						.flatMap(Function.identity())
				);

		var tamed = rootObject.get("tamed")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> TamedActivity.parse(element)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(TamedActivity.EXCLUDE);

		if (problems.isEmpty()) {
			return Result.success(new DealDamageExperienceSource(
					optCalculation.orElseThrow(),
					antiFarmingPerEntity,
					antiFarmingPerChunk,
					tamed
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public record Data(
			ServerPlayer player,
			LivingEntity entity,
			ItemStack weapon,
			float damage,
			DamageSource damageSource
	) { }

	@Override
	public void dispose(ExperienceSourceDisposeContext context) {
		// Nothing to do.
	}
}

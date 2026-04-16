package com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.Calculation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationFactory;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.BuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.Prototype;
import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSourceConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSourceDisposeContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.calculation.LegacyBuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.calculation.LegacyCalculation;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.LegacyOperationRegistry;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.AttributeOperation;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.DamageTypeCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.EffectOperation;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.EntityTypeCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.ItemStackCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.legacy.LegacyDamageTypeTagCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.legacy.LegacyEntityTypeTagCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.legacy.LegacyItemTagCondition;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.util.AntiFarmingPerChunk;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.util.TamedActivity;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

public record KillEntityExperienceSource(
		Calculation<Data> calculation,
		Optional<AntiFarmingPerChunk> antiFarmingPerChunk,
		TamedActivity tamedActivity
) implements ExperienceSource {

	private static final Identifier ID = SkillsMod.createIdentifier("kill_entity");
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
				SkillsMod.createIdentifier("get_killed_living_entity"),
				BuiltinPrototypes.LIVING_ENTITY,
				OperationFactory.create(Data::entity)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_damage_source"),
				BuiltinPrototypes.DAMAGE_SOURCE,
				OperationFactory.create(Data::damageSource)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_dropped_experience"),
				BuiltinPrototypes.NUMBER,
				OperationFactory.create(Data::entityDroppedXp)
		);
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				KillEntityExperienceSource::parse
		);
	}

	private static Result<KillEntityExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context));
	}

	private static Result<KillEntityExperienceSource, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optCalculation = LegacyCalculation.parse(rootObject, PROTOTYPE, context)
				.ifFailure(problems::add)
				.getSuccess();

		var antiFarmingPerChunk = rootObject.get("anti_farming_per_chunk")
				.orElse(LegacyUtils.wrapDeprecated(() -> rootObject.get("anti_farming"), 4, context))
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
			return Result.success(new KillEntityExperienceSource(
					optCalculation.orElseThrow(),
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
			DamageSource damageSource,
			double entityDroppedXp
	) { }

	@Override
	public void dispose(ExperienceSourceDisposeContext context) {
		// Nothing to do.
	}

	static {


		// Backwards compatibility.
		var legacy = new LegacyOperationRegistry<>(PROTOTYPE);
		legacy.registerBooleanFunction(
				"entity",
				EntityTypeCondition::parse,
				data -> data.entity().getType()
		);
		legacy.registerBooleanFunction(
				"entity_tag",
				LegacyEntityTypeTagCondition::parse,
				data -> data.entity().getType()
		);
		legacy.registerBooleanFunction(
				"weapon",
				ItemStackCondition::parse,
				Data::weapon
		);
		legacy.registerBooleanFunction(
				"weapon_nbt",
				ItemStackCondition::parse,
				Data::weapon
		);
		legacy.registerBooleanFunction(
				"weapon_tag",
				LegacyItemTagCondition::parse,
				Data::weapon
		);
		legacy.registerBooleanFunction(
				"damage_type",
				DamageTypeCondition::parse,
				data -> data.damageSource().getType()
		);
		legacy.registerBooleanFunction(
				"damage_type_tag",
				LegacyDamageTypeTagCondition::parse,
				data -> data.damageSource().getType()
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
				"entity_dropped_experience",
				Data::entityDroppedXp
		);
		legacy.registerNumberFunction(
				"entity_max_health",
				data -> (double) data.entity().getMaxHealth()
		);

		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("player"),
				SkillsMod.createIdentifier("get_player")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("weapon_item_stack"),
				SkillsMod.createIdentifier("get_weapon_item_stack")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("killed_living_entity"),
				SkillsMod.createIdentifier("get_killed_living_entity")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("damage_source"),
				SkillsMod.createIdentifier("get_damage_source")
		);
		LegacyBuiltinPrototypes.registerAlias(
				PROTOTYPE,
				SkillsMod.createIdentifier("dropped_experience"),
				SkillsMod.createIdentifier("get_dropped_experience")
		);
	}
}

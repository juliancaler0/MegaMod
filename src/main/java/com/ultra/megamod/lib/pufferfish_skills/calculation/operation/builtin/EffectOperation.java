package com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.Operation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.BuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.json.BuiltinJson;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.calculation.LegacyBuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;

public class EffectOperation implements Operation<LivingEntity, MobEffectInstance> {
	private final Holder<MobEffect> effect;

	private EffectOperation(Holder<MobEffect> effect) {
		this.effect = effect;
	}

	public static void register() {
		BuiltinPrototypes.LIVING_ENTITY.registerOperation(
				SkillsMod.createIdentifier("get_effect"),
				BuiltinPrototypes.STATUS_EFFECT_INSTANCE,
				EffectOperation::parse
		);

		LegacyBuiltinPrototypes.registerAlias(
				BuiltinPrototypes.LIVING_ENTITY,
				SkillsMod.createIdentifier("effect"),
				SkillsMod.createIdentifier("get_effect")
		);
	}

	public static Result<EffectOperation, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(EffectOperation::parse, context));
	}

	public static Result<EffectOperation, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optEffect = rootObject.get("effect")
				.andThen(BuiltinJson::parseEffect)
				.ifFailure(problems::add)
				.getSuccess()
				.map(BuiltInRegistries.MOB_EFFECT::wrapAsHolder);

		if (problems.isEmpty()) {
			return Result.success(new EffectOperation(
					optEffect.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<MobEffectInstance> apply(LivingEntity entity) {
		return Optional.ofNullable(entity.getEffect(effect));
	}
}

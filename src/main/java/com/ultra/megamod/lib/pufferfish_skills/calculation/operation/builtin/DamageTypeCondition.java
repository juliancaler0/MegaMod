package com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin;

import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.HolderSet;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.Operation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.BuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.BuiltinJson;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;

public final class DamageTypeCondition implements Operation<DamageType, Boolean> {
	private final HolderSet<DamageType> damageTypeEntries;

	private DamageTypeCondition(HolderSet<DamageType> damageTypeEntries) {
		this.damageTypeEntries = damageTypeEntries;
	}

	public static void register() {
		BuiltinPrototypes.DAMAGE_TYPE.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				DamageTypeCondition::parse
		);
	}

	public static Result<DamageTypeCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context));
	}

	public static Result<DamageTypeCondition, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optDamageType = rootObject.get("damage_type")
				.orElse(LegacyUtils.wrapDeprecated(
						() -> rootObject.get("damage"),
						3,
						context
				))
				.andThen(damageElement -> BuiltinJson.parseDamageTypeOrDamageTypeTag(damageElement, context.getServer().registryAccess()))
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new DamageTypeCondition(
					optDamageType.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(DamageType damageType) {
		return Optional.of(
				damageTypeEntries.stream().anyMatch(entry -> entry.value() == damageType)
		);
	}
}

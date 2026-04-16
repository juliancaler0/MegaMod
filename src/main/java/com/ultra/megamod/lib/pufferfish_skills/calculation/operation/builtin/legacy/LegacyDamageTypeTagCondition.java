package com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.legacy;

import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.HolderSet;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.Operation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.BuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.BuiltinJson;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;

import java.util.ArrayList;
import java.util.Optional;

public final class LegacyDamageTypeTagCondition implements Operation<DamageType, Boolean> {
	private final HolderSet<DamageType> entries;

	private LegacyDamageTypeTagCondition(HolderSet<DamageType> entries) {
		this.entries = entries;
	}

	public static void register() {
		BuiltinPrototypes.DAMAGE_TYPE.registerOperation(
				SkillsMod.createIdentifier("legacy_damage_type_tag"),
				BuiltinPrototypes.BOOLEAN,
				LegacyDamageTypeTagCondition::parse
		);
	}

	public static Result<LegacyDamageTypeTagCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<LegacyDamageTypeTagCondition, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optTag = rootObject.get("tag")
				.andThen(element -> BuiltinJson.parseDamageTypeTag(element, context.getServer().registryAccess()))
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new LegacyDamageTypeTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(DamageType damageType) {
		return Optional.of(entries.stream().anyMatch(entry -> entry.value() == damageType));
	}
}

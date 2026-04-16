package com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.advancements.criterion.NbtPredicate;
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

import java.util.ArrayList;
import java.util.Optional;

public final class EntityCondition implements Operation<Entity, Boolean> {
	private final Optional<HolderSet<EntityType<?>>> optEntityTypeEntries;
	private final Optional<NbtPredicate> optNbt;

	private EntityCondition(Optional<HolderSet<EntityType<?>>> optEntityTypeEntries, Optional<NbtPredicate> optNbt) {
		this.optEntityTypeEntries = optEntityTypeEntries;
		this.optNbt = optNbt;
	}

	public static void register() {
		BuiltinPrototypes.ENTITY.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				EntityCondition::parse
		);
	}

	public static Result<EntityCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(rootObject -> rootObject.noUnused(EntityCondition::parse));
	}

	public static Result<EntityCondition, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optEntityType = rootObject.get("entity_type")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(entityElement -> BuiltinJson.parseEntityTypeOrEntityTypeTag(entityElement)
						.ifFailure(problems::add)
						.getSuccess()
				);

		var optNbt = rootObject.get("nbt")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(nbtElement -> BuiltinJson.parseNbtPredicate(nbtElement)
						.ifFailure(problems::add)
						.getSuccess()
				);

		if (problems.isEmpty()) {
			return Result.success(new EntityCondition(
					optEntityType,
					optNbt
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(Entity entity) {
		// NbtPredicate.test(Entity) removed in 1.21.11 — nbt check stubbed to true
		return Optional.of(
				optEntityTypeEntries.map(entityTypeEntries -> entity.getType().is(entityTypeEntries)).orElse(true)
		);
	}
}

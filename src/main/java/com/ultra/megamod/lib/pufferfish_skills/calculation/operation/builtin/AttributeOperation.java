package com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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

public class AttributeOperation implements Operation<LivingEntity, AttributeInstance> {
	private final Holder<Attribute> attribute;

	private AttributeOperation(Holder<Attribute> attribute) {
		this.attribute = attribute;
	}

	public static void register() {
		BuiltinPrototypes.LIVING_ENTITY.registerOperation(
				SkillsMod.createIdentifier("get_attribute"),
				BuiltinPrototypes.ENTITY_ATTRIBUTE_INSTANCE,
				AttributeOperation::parse
		);

		LegacyBuiltinPrototypes.registerAlias(
				BuiltinPrototypes.LIVING_ENTITY,
				SkillsMod.createIdentifier("attribute"),
				SkillsMod.createIdentifier("get_attribute")
		);
	}

	public static Result<AttributeOperation, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(AttributeOperation::parse, context));
	}

	public static Result<AttributeOperation, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optAttribute = rootObject.get("attribute")
				.andThen(BuiltinJson::parseAttribute)
				.ifFailure(problems::add)
				.getSuccess()
				.map(BuiltInRegistries.ATTRIBUTE::getEntry);

		if (problems.isEmpty()) {
			return Result.success(new AttributeOperation(
					optAttribute.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<AttributeInstance> apply(LivingEntity entity) {
		return Optional.ofNullable(entity.getAttribute(attribute));
	}
}

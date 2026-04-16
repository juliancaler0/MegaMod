package com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin;

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
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Map;

public record FishItemExperienceSource(
		Calculation<Data> calculation
) implements ExperienceSource {

	private static final Identifier ID = SkillsMod.createIdentifier("fish_item");
	private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

	static {
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_player"),
				BuiltinPrototypes.PLAYER,
				OperationFactory.create(Data::player)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_rod_item_stack"),
				BuiltinPrototypes.ITEM_STACK,
				OperationFactory.create(Data::rod)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_fished_item_stack"),
				BuiltinPrototypes.ITEM_STACK,
				OperationFactory.create(Data::fished)
		);
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				FishItemExperienceSource::parse
		);
	}

	private static Result<FishItemExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context));
	}

	private static Result<FishItemExperienceSource, Problem> parse(JsonObject rootObject, ExperienceSourceConfigContext context) {
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

		if (problems.isEmpty()) {
			return Result.success(new FishItemExperienceSource(
					optCalculation.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public record Data(ServerPlayer player, ItemStack rod, ItemStack fished) { }

	@Override
	public void dispose(ExperienceSourceDisposeContext context) {
		// Nothing to do.
	}
}

package com.ultra.megamod.lib.pufferfish_skills.config.skill;

import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.BuiltinJson;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonPath;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.Reward;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.impl.rewards.RewardConfigContextImpl;
import com.ultra.megamod.lib.pufferfish_skills.impl.rewards.RewardDisposeContextImpl;
import com.ultra.megamod.lib.pufferfish_skills.reward.RewardRegistry;
import com.ultra.megamod.lib.pufferfish_skills.reward.builtin.DummyReward;
import com.ultra.megamod.lib.pufferfish_skills.util.DisposeContext;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;

public record SkillRewardConfig(
		Identifier type,
		Reward instance
) {

	public static Result<SkillRewardConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context)
		);
	}

	public static Result<SkillRewardConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optTypeElement = rootObject.get("type")
				.ifFailure(problems::add)
				.getSuccess();

		var optType = optTypeElement.flatMap(
				typeElement -> BuiltinJson.parseIdentifier(typeElement)
						.ifFailure(problems::add)
						.getSuccess()
		);

		var maybeDataElement = rootObject.get("data");

		var required = rootObject.get("required")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsBoolean()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(true);

		if (problems.isEmpty()) {
			return build(
					optType.orElseThrow(),
					maybeDataElement,
					rootObject.getPath().getObject("type"),
					context
			).orElse(problem -> {
				if (required) {
					return Result.failure(problem);
				} else {
					context.emitWarning(problem.toString());
					return Result.success(new SkillRewardConfig(DummyReward.ID, new DummyReward()));
				}
			});
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	private static Result<SkillRewardConfig, Problem> build(Identifier type, Result<JsonElement, Problem> maybeDataElement, JsonPath typePath, ConfigContext context) {
		return RewardRegistry.getFactory(type)
				.map(factory -> factory.create(new RewardConfigContextImpl(context, maybeDataElement))
						.mapSuccess(instance -> new SkillRewardConfig(type, instance))
				)
				.orElseGet(() -> Result.failure(typePath.createProblem("Expected a valid reward type")));
	}

	public void dispose(DisposeContext context) {
		this.instance.dispose(new RewardDisposeContextImpl(context));
	}


}

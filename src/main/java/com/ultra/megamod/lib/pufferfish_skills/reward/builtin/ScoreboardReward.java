package com.ultra.megamod.lib.pufferfish_skills.reward.builtin;

import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.Reward;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.RewardConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.RewardDisposeContext;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.RewardUpdateContext;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;

public class ScoreboardReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("scoreboard");

	private final String objectiveName;

	private ScoreboardReward(String objectiveName) {
		this.objectiveName = objectiveName;
	}

	public static void register() {
		SkillsAPI.registerReward(
				ID,
				ScoreboardReward::parse
		);
	}

	private static Result<ScoreboardReward, Problem> parse(RewardConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(rootObject -> parse(rootObject, context), context));
	}

	private static Result<ScoreboardReward, Problem> parse(JsonObject rootObject, RewardConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optObjective = rootObject.getString("objective")
				.orElse(LegacyUtils.wrapDeprecated(
						() -> rootObject.getString("scoreboard"),
						3,
						context
				))
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new ScoreboardReward(
					optObjective.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public void update(RewardUpdateContext context) {
		var player = context.getPlayer();
		var scoreboard = player.level().getScoreboard();
		var objective = scoreboard.getObjective(objectiveName);
		if (objective != null) {
			scoreboard.getOrCreatePlayerScore((net.minecraft.world.scores.ScoreHolder) player, objective).set(context.getCount());
		}
	}

	@Override
	public void dispose(RewardDisposeContext context) {
		// Nothing to do.
	}
}

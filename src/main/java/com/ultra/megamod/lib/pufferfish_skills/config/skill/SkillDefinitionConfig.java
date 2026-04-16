package com.ultra.megamod.lib.pufferfish_skills.config.skill;

import net.minecraft.network.chat.Component;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.BuiltinJson;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.config.FrameConfig;
import com.ultra.megamod.lib.pufferfish_skills.config.IconConfig;
import com.ultra.megamod.lib.pufferfish_skills.util.DisposeContext;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record SkillDefinitionConfig(
		String id,
		Component title,
		Component description,
		Component extraDescription,
		IconConfig icon,
		FrameConfig frame,
		float size,
		List<SkillRewardConfig> rewards,
		int cost,
		int requiredSkills,
		int requiredPoints,
		int requiredSpentPoints,
		int requiredExclusions
) {

	public static Result<Optional<SkillDefinitionConfig>, Problem> parse(String id, JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(
				LegacyUtils.wrapNoUnused(rootObject -> parse(id, rootObject, context), context)
		);
	}

	public static Result<Optional<SkillDefinitionConfig>, Problem> parse(String id, JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optTitle = rootObject.get("title")
				.andThen(titleElement -> BuiltinJson.parseText(titleElement, context.getServer().registryAccess()))
				.ifFailure(problems::add)
				.getSuccess();

		var description = rootObject.get("description")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(descriptionElement -> BuiltinJson.parseText(descriptionElement, context.getServer().registryAccess())
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(Component::empty);

		var extraDescription = rootObject.get("extra_description")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(descriptionElement -> BuiltinJson.parseText(descriptionElement, context.getServer().registryAccess())
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(Component::empty);

		var optIcon = rootObject.get("icon")
				.andThen(element -> IconConfig.parse(element, context))
				.ifFailure(problems::add)
				.getSuccess();

		var frame = rootObject.get("frame")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> FrameConfig.parse(element, context)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(FrameConfig::createDefault);

		var size = rootObject.get("size")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsFloat()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(1f);

		var rewards = rootObject.getArray("rewards")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(array -> array.getAsList((i, element) -> SkillRewardConfig.parse(element, context)).mapFailure(Problem::combine)
						.ifFailure(problems::add)
						.getSuccess())
				.orElseGet(List::of);

		var cost = rootObject.get("cost")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(1);

		var requiredSkills = rootObject.get("required_skills")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(1);

		var requiredPoints = rootObject.get("required_points")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(0);

		var requiredSpentPoints = rootObject.get("required_spent_points")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(0);

		var requiredExclusions = rootObject.get("required_exclusions")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(1);

		var requiredMods = rootObject.get("required_mods")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsArray()
						.andThen(array -> array.getAsList((i, e) -> e.getAsString()).mapFailure(Problem::combine))
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(List::of);

		// this field is generated by the editor, access it to avoid unused field error
		rootObject.get("metadata");

		// ignore problems because they may be caused by missing mods
		if (!requiredMods.stream().allMatch(SkillsMod.getInstance().getPlatform()::isModLoaded)) {
			return Result.success(Optional.empty());
		}

		if (problems.isEmpty()) {
			return Result.success(Optional.of(new SkillDefinitionConfig(
					id,
					optTitle.orElseThrow(),
					description,
					extraDescription,
					optIcon.orElseThrow(),
					frame,
					size,
					rewards,
					cost,
					requiredSkills,
					requiredPoints,
					requiredSpentPoints,
					requiredExclusions
			)));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public void dispose(DisposeContext context) {
		for (var reward : rewards) {
			reward.dispose(context);
		}
	}

}

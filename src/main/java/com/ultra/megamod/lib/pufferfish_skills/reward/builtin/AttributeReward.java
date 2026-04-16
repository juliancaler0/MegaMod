package com.ultra.megamod.lib.pufferfish_skills.reward.builtin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.pufferfish_skills.api.json.BuiltinJson;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.Reward;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.RewardConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.RewardDisposeContext;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.RewardUpdateContext;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AttributeReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("attribute");

	private final List<Identifier> ids = new ArrayList<>();

	private final Holder<Attribute> attribute;
	private final float value;
	private final AttributeModifier.Operation operation;

	private AttributeReward(Holder<Attribute> attribute, float value, AttributeModifier.Operation operation) {
		this.attribute = attribute;
		this.value = value;
		this.operation = operation;
	}

	public static void register() {
		SkillsAPI.registerReward(
				ID,
				AttributeReward::parse
		);
	}

	private static Result<AttributeReward, Problem> parse(RewardConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(AttributeReward::parse, context));
	}

	private static Result<AttributeReward, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optAttribute = rootObject.get("attribute")
				.andThen(attributeElement -> BuiltinJson.parseAttribute(attributeElement)
						.andThen(attribute -> {
							var attributeEntry = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
							return Result.<Holder<Attribute>, Problem>success(attributeEntry);
						})
				)
				.ifFailure(problems::add)
				.getSuccess();

		var optValue = rootObject.getFloat("value")
				.ifFailure(problems::add)
				.getSuccess();

		var optOperation = rootObject.get("operation")
				.andThen(BuiltinJson::parseAttributeOperation)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new AttributeReward(
					optAttribute.orElseThrow(),
					optValue.orElseThrow(),
					optOperation.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public void update(RewardUpdateContext context) {
		var count = context.getCount();
		var instance = Objects.requireNonNull(context.getPlayer().getAttribute(attribute));

		while (ids.size() < count) {
			ids.add(SkillsMod.createIdentifier(
					RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789")
			));
		}

		for (var i = 0; i < ids.size(); i++) {
			var id = ids.get(i);
			if (instance.getModifier(id) == null) {
				if (i < count) {
					instance.addTransientModifier(new AttributeModifier(
							id,
							value,
							operation
					));
				}
			} else {
				if (i >= count) {
					instance.removeModifier(id);
				}
			}
		}
	}

	@Override
	public void dispose(RewardDisposeContext context) {
		for (var player : context.getServer().getPlayerList().getPlayers()) {
			var instance = Objects.requireNonNull(player.getAttribute(attribute));
			for (var id : ids) {
				instance.removeModifier(id);
			}
		}
		ids.clear();
	}
}

package com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin;

import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
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
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;
import java.util.Optional;

public final class ItemCondition implements Operation<Item, Boolean> {
	private final HolderSet<Item> itemEntries;

	private ItemCondition(HolderSet<Item> itemEntries) {
		this.itemEntries = itemEntries;
	}

	public static void register() {
		BuiltinPrototypes.ITEM.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				ItemCondition::parse
		);
	}

	public static Result<ItemCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(ItemCondition::parse, context));
	}

	public static Result<ItemCondition, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optItem = rootObject.get("item")
				.andThen(BuiltinJson::parseItemOrItemTag)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new ItemCondition(
					optItem.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(Item item) {
		return Optional.of(itemEntries.contains(BuiltInRegistries.ITEM.wrapAsHolder(item)));
	}
}

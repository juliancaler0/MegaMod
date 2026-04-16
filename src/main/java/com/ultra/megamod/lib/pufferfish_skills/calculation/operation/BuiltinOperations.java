package com.ultra.megamod.lib.pufferfish_skills.calculation.operation;

import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.AttributeOperation;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.BlockCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.BlockStateCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.DamageSourceClassification;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.DamageTypeCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.EffectOperation;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.EntityCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.EntityTypeCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.ItemCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.ItemStackCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.ScoreboardOperation;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.StatCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.StatTypeCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.StatValueOperation;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.SwitchOperation;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.TagCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.WorldCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.legacy.LegacyBlockTagCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.legacy.LegacyDamageTypeTagCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.legacy.LegacyEntityTypeTagCondition;
import com.ultra.megamod.lib.pufferfish_skills.calculation.operation.builtin.legacy.LegacyItemTagCondition;

public class BuiltinOperations {
	public static void register() {
		AttributeOperation.register();
		BlockCondition.register();
		BlockStateCondition.register();
		DamageSourceClassification.register();
		DamageTypeCondition.register();
		EffectOperation.register();
		EntityCondition.register();
		EntityTypeCondition.register();
		ItemCondition.register();
		ItemStackCondition.register();
		ScoreboardOperation.register();
		StatCondition.register();
		StatTypeCondition.register();
		StatValueOperation.register();
		SwitchOperation.register();
		TagCondition.register();
		WorldCondition.register();

		LegacyBlockTagCondition.register();
		LegacyDamageTypeTagCondition.register();
		LegacyEntityTypeTagCondition.register();
		LegacyItemTagCondition.register();
	}
}

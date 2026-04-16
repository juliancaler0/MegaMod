package com.ultra.megamod.lib.pufferfish_skills.experience.source;

import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.BreakBlockExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.CraftItemExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.CriterionExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.DealDamageExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.EatFoodExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.EnchantItemExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.FishItemExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.HealExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.IncreaseStatExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.KillEntityExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.MineBlockExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.SharedKillEntityExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.SmeltItemExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.experience.source.builtin.TakeDamageExperienceSource;

public class BuiltinExperienceSources {
	public static void register() {
		BreakBlockExperienceSource.register();
		CraftItemExperienceSource.register();
		CriterionExperienceSource.register();
		DealDamageExperienceSource.register();
		EatFoodExperienceSource.register();
		EnchantItemExperienceSource.register();
		FishItemExperienceSource.register();
		HealExperienceSource.register();
		IncreaseStatExperienceSource.register();
		KillEntityExperienceSource.register();
		MineBlockExperienceSource.register();
		SharedKillEntityExperienceSource.register();
		SmeltItemExperienceSource.register();
		TakeDamageExperienceSource.register();
	}
}

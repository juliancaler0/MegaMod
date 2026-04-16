package com.ultra.megamod.lib.pufferfish_skills.client.config.skill;

import net.minecraft.network.chat.Component;
import com.ultra.megamod.lib.pufferfish_skills.client.config.ClientFrameConfig;
import com.ultra.megamod.lib.pufferfish_skills.client.config.ClientIconConfig;

public record ClientSkillDefinitionConfig(
		String id,
		Component title,
		Component description,
		Component extraDescription,
		ClientIconConfig icon,
		ClientFrameConfig frame,
		float size,
		int cost,
		int requiredSkills,
		int requiredPoints,
		int requiredSpentPoints,
		int requiredExclusions
) { }

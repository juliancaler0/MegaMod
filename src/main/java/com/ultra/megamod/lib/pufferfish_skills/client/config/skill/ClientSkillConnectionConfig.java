package com.ultra.megamod.lib.pufferfish_skills.client.config.skill;

public record ClientSkillConnectionConfig(
		String skillAId,
		String skillBId,
		boolean bidirectional
) { }

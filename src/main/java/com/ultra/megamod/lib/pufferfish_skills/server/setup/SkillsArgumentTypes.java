package com.ultra.megamod.lib.pufferfish_skills.server.setup;

import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.commands.arguments.CategoryArgumentType;
import com.ultra.megamod.lib.pufferfish_skills.commands.arguments.SkillArgumentType;

public class SkillsArgumentTypes {
	public static void register(ServerRegistrar registrar) {
		registrar.registerArgumentType(
				SkillsMod.createIdentifier("category"),
				CategoryArgumentType.class,
				new CategoryArgumentType.Serializer()
		);
		registrar.registerArgumentType(
				SkillsMod.createIdentifier("skill"),
				SkillArgumentType.class,
				new SkillArgumentType.Serializer()
		);
	}
}

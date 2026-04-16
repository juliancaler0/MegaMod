package com.ultra.megamod.lib.pufferfish_skills.server.network.packets.in;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.network.InPacket;

public class SkillClickInPacket implements InPacket {
	private final Identifier categoryId;
	private final String skillId;

	private SkillClickInPacket(Identifier categoryId, String skillId) {
		this.categoryId = categoryId;
		this.skillId = skillId;
	}

	public static SkillClickInPacket read(FriendlyByteBuf buf) {
		return new SkillClickInPacket(
				Identifier.parse(buf.readUtf()),
				buf.readUtf()
		);
	}

	public Identifier getCategoryId() {
		return categoryId;
	}

	public String getSkillId() {
		return skillId;
	}
}

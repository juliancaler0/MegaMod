package com.ultra.megamod.lib.pufferfish_skills.client.network.packets.in;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.network.InPacket;

public class SkillUpdateInPacket implements InPacket {
	private final Identifier categoryId;
	private final String skillId;
	private final boolean unlocked;

	private SkillUpdateInPacket(Identifier categoryId, String skillId, boolean unlocked) {
		this.categoryId = categoryId;
		this.skillId = skillId;
		this.unlocked = unlocked;
	}

	public static SkillUpdateInPacket read(FriendlyByteBuf buf) {
		var categoryId = Identifier.parse(buf.readUtf());
		var skillId = buf.readUtf();
		var unlocked = buf.readBoolean();
		return new SkillUpdateInPacket(
				categoryId,
				skillId,
				unlocked
		);
	}

	public Identifier getCategoryId() {
		return categoryId;
	}

	public String getSkillId() {
		return skillId;
	}

	public boolean isUnlocked() {
		return unlocked;
	}
}

package com.ultra.megamod.lib.pufferfish_skills.client.network.packets.in;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.network.InPacket;

public class ExperienceUpdateInPacket implements InPacket {
	private final Identifier categoryId;
	private final int currentLevel;
	private final int currentExperience;
	private final int requiredExperience;

	private ExperienceUpdateInPacket(Identifier categoryId, int currentLevel, int currentExperience, int requiredExperience) {
		this.categoryId = categoryId;
		this.currentLevel = currentLevel;
		this.currentExperience = currentExperience;
		this.requiredExperience = requiredExperience;
	}

	public static ExperienceUpdateInPacket read(FriendlyByteBuf buf) {
		var categoryId = Identifier.parse(buf.readUtf());
		var currentLevel = buf.readInt();
		var currentExperience = buf.readInt();
		var requiredExperience = buf.readInt();

		return new ExperienceUpdateInPacket(
				categoryId,
				currentLevel,
				currentExperience,
				requiredExperience
		);
	}

	public Identifier getCategoryId() {
		return categoryId;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public int getCurrentExperience() {
		return currentExperience;
	}

	public int getRequiredExperience() {
		return requiredExperience;
	}
}

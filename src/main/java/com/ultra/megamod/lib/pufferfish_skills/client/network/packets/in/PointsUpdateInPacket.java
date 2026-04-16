package com.ultra.megamod.lib.pufferfish_skills.client.network.packets.in;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.network.InPacket;

public class PointsUpdateInPacket implements InPacket {
	private final Identifier categoryId;
	private final int spentPoints;
	private final int earnedPoints;

	private PointsUpdateInPacket(Identifier categoryId, int spentPoints, int earnedPoints) {
		this.categoryId = categoryId;
		this.spentPoints = spentPoints;
		this.earnedPoints = earnedPoints;
	}

	public static PointsUpdateInPacket read(FriendlyByteBuf buf) {
		var categoryId = Identifier.parse(buf.readUtf());
		var spentPoints = buf.readInt();
		var earnedPoints = buf.readInt();

		return new PointsUpdateInPacket(
				categoryId,
				spentPoints,
				earnedPoints
		);
	}

	public Identifier getCategoryId() {
		return categoryId;
	}

	public int getSpentPoints() {
		return spentPoints;
	}

	public int getEarnedPoints() {
		return earnedPoints;
	}
}

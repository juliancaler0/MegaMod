package com.ultra.megamod.lib.pufferfish_skills.client.network.packets.in;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.network.InPacket;

public class NewPointInPacket implements InPacket {
	private final Identifier categoryId;

	private NewPointInPacket(Identifier categoryId) {
		this.categoryId = categoryId;
	}

	public static NewPointInPacket read(FriendlyByteBuf buf) {
		var categoryId = buf.readIdentifier();

		return new NewPointInPacket(
				categoryId
		);
	}

	public Identifier getCategoryId() {
		return categoryId;
	}
}

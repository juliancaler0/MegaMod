package com.ultra.megamod.lib.pufferfish_skills.client.network.packets.in;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.network.InPacket;

import java.util.Optional;

public class OpenScreenInPacket implements InPacket {
	private final Optional<Identifier> categoryId;

	private OpenScreenInPacket(Optional<Identifier> categoryId) {
		this.categoryId = categoryId;
	}

	public static OpenScreenInPacket read(FriendlyByteBuf buf) {
		return new OpenScreenInPacket(buf.readBoolean() ? Optional.of(Identifier.parse(buf.readUtf())) : Optional.empty());
	}

	public Optional<Identifier> getCategoryId() {
		return categoryId;
	}
}

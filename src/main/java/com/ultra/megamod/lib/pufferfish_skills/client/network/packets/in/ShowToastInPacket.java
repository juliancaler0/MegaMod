package com.ultra.megamod.lib.pufferfish_skills.client.network.packets.in;

import net.minecraft.network.FriendlyByteBuf;
import com.ultra.megamod.lib.pufferfish_skills.network.InPacket;
import com.ultra.megamod.lib.pufferfish_skills.util.ToastType;

public class ShowToastInPacket implements InPacket {

	private final ToastType type;

	private ShowToastInPacket(ToastType type) {
		this.type = type;
	}

	public static ShowToastInPacket read(FriendlyByteBuf buf) {
		return new ShowToastInPacket(buf.readEnum(ToastType.class));
	}

	public ToastType getToastType() {
		return type;
	}
}

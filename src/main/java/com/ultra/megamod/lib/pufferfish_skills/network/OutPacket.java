package com.ultra.megamod.lib.pufferfish_skills.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

public interface OutPacket {
	Identifier getId();

	void write(RegistryFriendlyByteBuf buf);
}

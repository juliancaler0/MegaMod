package com.ultra.megamod.lib.pufferfish_skills.client.setup;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.client.network.ClientPacketHandler;
import com.ultra.megamod.lib.pufferfish_skills.network.InPacket;

import java.util.function.Function;

public interface ClientRegistrar {
	<T extends InPacket> void registerInPacket(Identifier id, Function<RegistryFriendlyByteBuf, T> reader, ClientPacketHandler<T> handler);
	void registerOutPacket(Identifier id);
}


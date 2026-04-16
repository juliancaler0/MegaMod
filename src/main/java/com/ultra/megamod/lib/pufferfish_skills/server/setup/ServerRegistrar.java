package com.ultra.megamod.lib.pufferfish_skills.server.setup;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.network.InPacket;
import com.ultra.megamod.lib.pufferfish_skills.server.network.ServerPacketHandler;

import java.util.function.Function;

@SuppressWarnings("unused")
public interface ServerRegistrar {
	<V, T extends V> void register(Registry<V> registry, Identifier id, T entry);
	<A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void registerArgumentType(Identifier id, Class<A> clazz, ArgumentTypeInfo<A, T> serializer);
	<T extends InPacket> void registerInPacket(Identifier id, Function<RegistryFriendlyByteBuf, T> reader, ServerPacketHandler<T> handler);
	void registerOutPacket(Identifier id);
}

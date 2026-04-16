package com.ultra.megamod.lib.pufferfish_skills.server.network;

import net.minecraft.server.level.ServerPlayer;

public interface ServerPacketHandler<T> {
	void handle(ServerPlayer player, T packet);
}

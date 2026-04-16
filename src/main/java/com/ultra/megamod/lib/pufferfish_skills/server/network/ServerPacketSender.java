package com.ultra.megamod.lib.pufferfish_skills.server.network;

import net.minecraft.server.level.ServerPlayer;
import com.ultra.megamod.lib.pufferfish_skills.network.OutPacket;

public interface ServerPacketSender {
	void send(ServerPlayer player, OutPacket packet);
}

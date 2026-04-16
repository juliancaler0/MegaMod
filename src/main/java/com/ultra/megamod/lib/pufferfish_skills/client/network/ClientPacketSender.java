package com.ultra.megamod.lib.pufferfish_skills.client.network;

import com.ultra.megamod.lib.pufferfish_skills.network.OutPacket;

public interface ClientPacketSender {
	void send(OutPacket packet);
}

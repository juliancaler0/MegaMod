package com.ultra.megamod.lib.pufferfish_skills.client.network;

public interface ClientPacketHandler<T> {
	void handle(T packet);
}

package com.ultra.megamod.reliquary.util;

import net.minecraft.server.level.ServerLevel;
import com.ultra.megamod.reliquary.entity.ReliquaryFakePlayer;

public class FakePlayerFactory {
	private FakePlayerFactory() {}

	private static ReliquaryFakePlayer fakePlayer;

	public static ReliquaryFakePlayer get(ServerLevel level) {
		if (fakePlayer == null) {
			fakePlayer = new ReliquaryFakePlayer(level);
		}

		return fakePlayer;
	}

	public static void unloadWorld(ServerLevel level) {
		if (fakePlayer != null && fakePlayer.level() == level) {
			fakePlayer = null;
		}
	}

}

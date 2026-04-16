package com.ultra.megamod.lib.pufferfish_skills.server.setup;

@SuppressWarnings("unused")
public class SkillsGameRules {
	private final boolean announceNewPointsValue;

	public SkillsGameRules(boolean announceNewPoints) {
		this.announceNewPointsValue = announceNewPoints;
	}

	public static SkillsGameRules register(ServerRegistrar registrar) {
		return new SkillsGameRules(true);
	}

	public boolean announceNewPoints() {
		return announceNewPointsValue;
	}
}

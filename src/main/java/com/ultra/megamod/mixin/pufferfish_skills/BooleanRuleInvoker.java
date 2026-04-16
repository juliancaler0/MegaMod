package com.ultra.megamod.mixin.pufferfish_skills;

// GameRules inner classes (BooleanValue, Type) are not accessible in NeoForge 1.21.11
// Custom game rule registration is stubbed out - using simple config boolean instead.

public interface BooleanRuleInvoker {
	// Stubbed - replaced with simple boolean config
	static boolean getDefaultAnnounceNewPoints() {
		return true;
	}
}

package com.ultra.megamod.reliquary.api.client;

/**
 * Enum moved out of the pruned client.gui.hud package so the config
 * layer (which persists HUD placement preferences) still compiles. The
 * actual HUD renderer that consumes these positions is a follow-up port.
 */
public enum HUDPosition {
	BOTTOM_LEFT,
	LEFT,
	TOP_LEFT,
	TOP,
	TOP_RIGHT,
	RIGHT,
	BOTTOM_RIGHT;

	public boolean isLeftSide() {
		return this == BOTTOM_LEFT || this == LEFT || this == TOP_LEFT;
	}
	public boolean isRightSide() {
		return this == BOTTOM_RIGHT || this == RIGHT || this == TOP_RIGHT;
	}
}

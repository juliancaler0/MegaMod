package com.ultra.megamod.lib.pufferfish_skills.client.config.colors;

public record ClientConnectionsColorsConfig(
		ClientFillStrokeColorsConfig locked,
		ClientFillStrokeColorsConfig available,
		ClientFillStrokeColorsConfig affordable,
		ClientFillStrokeColorsConfig unlocked,
		ClientFillStrokeColorsConfig excluded
) { }
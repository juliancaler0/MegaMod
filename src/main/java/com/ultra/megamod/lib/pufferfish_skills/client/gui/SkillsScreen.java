package com.ultra.megamod.lib.pufferfish_skills.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.client.data.ClientSkillScreenData;
import com.ultra.megamod.lib.pufferfish_skills.client.data.ClientCategoryData;

import java.util.Optional;

/**
 * Skills screen - simplified stub for NeoForge 1.21.11.
 * The full rendering implementation needs rewriting for the 1.21.11 Screen/Widget API.
 * TODO: Reimplement the full skill tree GUI with 1.21.11 compatible APIs
 */
public class SkillsScreen extends Screen {
	private final ClientSkillScreenData data;
	private Optional<Identifier> optActiveCategoryId;

	public SkillsScreen(ClientSkillScreenData data, Optional<Identifier> optCategoryId) {
		super(CommonComponents.EMPTY);
		this.data = data;
		optActiveCategoryId = optCategoryId;
	}

	@Override
	protected void init() {
		super.init();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);

		// Render placeholder text
		graphics.drawCenteredString(this.font, Component.literal("Skills Screen - Under Construction"), this.width / 2, this.height / 2, 0xFFFFFF);

		// Categories display placeholder
		graphics.drawCenteredString(this.font, Component.literal("Press ESC to close"), this.width / 2, this.height / 2 + 20, 0xAAAAAA);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}

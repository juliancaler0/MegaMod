package com.ultra.megamod.lib.pufferfish_skills.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;

public class SimpleToast extends SystemToast {

	private SimpleToast(SystemToast.SystemToastId id, Component title, Component description) {
		super(id, title, description);
	}

	public static SimpleToast create(Minecraft client, Component title, Component description) {
		return new SimpleToast(SystemToast.SystemToastId.PACK_LOAD_FAILURE, title, description);
	}
}

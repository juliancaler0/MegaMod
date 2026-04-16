package com.ultra.megamod.lib.skilltree.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import java.util.Optional;

/**
 * Custom ComponentContents for resolvable skill descriptions.
 * Simplified for NeoForge 1.21.11 (ComponentContents.Type not available).
 */
public record ResolvableTextContent(String id) implements ComponentContents {

	public Component getText() {
		return ComponentUtils.formatList(TranslationUtil.resolve(id), Component.literal("\n"));
	}

	@Override
	public <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor) {
		return getText().visit(visitor);
	}

	@Override
	public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> visitor, Style style) {
		return getText().visit(visitor, style);
	}
}

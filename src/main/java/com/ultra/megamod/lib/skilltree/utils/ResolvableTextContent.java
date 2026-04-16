package com.ultra.megamod.lib.skilltree.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import java.util.Optional;

public record ResolvableTextContent(String id) implements ComponentContents {

	private static final MapCodec<ResolvableTextContent> MAP_CODEC =
			RecordCodecBuilder.mapCodec(instance ->
					instance.group(Codec.STRING.fieldOf("id").forGetter(ResolvableTextContent::id))
							.apply(instance, ResolvableTextContent::new));

	public Component getText() {
		return ComponentUtils.formatList(TranslationUtil.resolve(id), Component.literal("\n"));
	}

	@Override
	public MapCodec<ResolvableTextContent> codec() {
		return MAP_CODEC;
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

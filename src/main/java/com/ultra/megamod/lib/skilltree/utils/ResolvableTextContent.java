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

/**
 * Custom {@link ComponentContents} implementation that resolves to a dynamically-generated
 * list of components at render time. Identified in JSON by the presence of a
 * {@code "skill_definition_id"} field.
 *
 * <p>Registered into {@link net.minecraft.network.chat.ComponentSerialization}'s internal
 * {@code LateBoundIdMapper} via {@code TextCodecsMixin} using the type id
 * {@code "megamod:resolvable"}. This allows JSON such as
 * {@code {"skill_definition_id": "arcane_boost"}} to parse as a valid {@link Component}.</p>
 *
 * <p>Because Minecraft's component codec includes a {@code FuzzyCodec} fallback that iterates
 * registered {@link MapCodec}s when no {@code "type"} discriminator is present, our
 * {@link #MAP_CODEC} (which requires the {@code skill_definition_id} field) will be selected
 * for JSON bodies that contain only that field.</p>
 */
public record ResolvableTextContent(String id) implements ComponentContents {

	/** Type identifier used when encoding with the explicit {@code "type":...} discriminator path. */
	public static final String TYPE_ID = "megamod:resolvable";

	/** Field name that, when present in a JSON object, signals this content type. */
	public static final String FIELD = "skill_definition_id";

	public static final MapCodec<ResolvableTextContent> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					Codec.STRING.fieldOf(FIELD).forGetter(ResolvableTextContent::id)
			).apply(instance, ResolvableTextContent::new));

	private Component getText() {
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

package com.ultra.megamod.feature.citizen.blockui.util;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

/**
 * Allows to add customsized spacer into text. Works ONLY in OUR text elements.
 */
public record SpacerTextComponent(int pixelHeight) implements ComponentContents
{
    private static final MapCodec<SpacerTextComponent> CODEC = MapCodec.unit(new SpacerTextComponent(0));

    @Override
    public MapCodec<SpacerTextComponent> codec() {
        return CODEC;
    }

    public static MutableComponent of(final int pixelHeight)
    {
        return MutableComponent.create(new SpacerTextComponent(pixelHeight));
    }

    public FormattedCharSequence getVisualOrderText()
    {
        return new FormattedSpacerComponent(pixelHeight);
    }

    public record FormattedSpacerComponent(int pixelHeight) implements FormattedCharSequence
    {
        @Override
        public boolean accept(final FormattedCharSink p_13732_)
        {
            return true;
        }
    }
}

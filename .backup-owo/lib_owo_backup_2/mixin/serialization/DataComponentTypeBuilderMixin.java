package com.ultra.megamod.lib.owo.mixin.serialization;

import com.ultra.megamod.lib.owo.serialization.OwoDataComponentTypeBuilder;
import net.minecraft.core.component.DataComponentType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DataComponentType.Builder.class)
public abstract class DataComponentTypeBuilderMixin<T> implements OwoDataComponentTypeBuilder<T> {
}

package com.ultra.megamod.reliquary.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class CodecHelper {
	// TODO: 1.21.11 port - ItemStack.ITEM_NON_AIR_CODEC was removed; use the full CODEC which
	// already validates against AIR. This loses the strict oversize stack behaviour (which
	// allowed count > maxStackSize), but vanilla no longer distinguishes.
	public static final Codec<ItemStack> OVERSIZED_ITEM_STACK_CODEC = ItemStack.CODEC;


	public static <T> Codec<Set<T>> setOf(Codec<T> elementCodec) {
		return new SetCodec<>(elementCodec);
	}
}

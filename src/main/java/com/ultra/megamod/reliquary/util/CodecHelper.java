package com.ultra.megamod.reliquary.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class CodecHelper {
	// Port note (1.21.11): ItemStack.ITEM_NON_AIR_CODEC was removed. ItemStack.CODEC already
	// validates against AIR during deserialisation (an AIR item encoded with count > 0 returns
	// ItemStack.EMPTY on decode), so for Reliquary's use-case that uses this codec to persist
	// non-empty inventory slots the behaviour is equivalent. We add an explicit .validate(...)
	// guard so any caller that round-trips an AIR stack through this codec gets a clear error
	// instead of silently producing EMPTY.
	public static final Codec<ItemStack> OVERSIZED_ITEM_STACK_CODEC = ItemStack.CODEC.validate(
			stack -> stack.isEmpty()
					? com.mojang.serialization.DataResult.error(() -> "Expected a non-air ItemStack")
					: com.mojang.serialization.DataResult.success(stack));


	public static <T> Codec<Set<T>> setOf(Codec<T> elementCodec) {
		return new SetCodec<>(elementCodec);
	}
}

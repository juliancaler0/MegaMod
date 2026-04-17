package com.ultra.megamod.mixin.skilltree;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.ultra.megamod.lib.skilltree.utils.ResolvableTextContent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects custom {@link ComponentContents} MapCodecs into Minecraft's component dispatching
 * codec. In 1.21.11 this is done via {@link ExtraCodecs.LateBoundIdMapper} rather than the
 * old {@code TextContent.Type[]} array.
 *
 * <p>We target {@link ComponentSerialization#createCodec(Codec)} and, right after
 * {@code bootstrap(mapper)} finishes populating the vanilla entries (text, translatable,
 * keybind, score, selector, nbt, object, neoforge:inserting), we capture the local
 * {@code lateboundidmapper} variable and append our own entries.</p>
 *
 * <p>This is the same pattern used by owo-lib's {@code ComponentSerializationMixin}. It is
 * the correct approach for 1.21.x NeoForge because {@code ComponentContents.Type} (the
 * registration mechanism used in 1.21.1 Yarn mappings) does not exist in Mojang/Parchment
 * mappings; the vanilla dispatch in those mappings is driven directly by the id mapper.</p>
 */
@Mixin(ComponentSerialization.class)
public abstract class TextCodecsMixin {

	@Inject(
			method = "createCodec",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/chat/ComponentSerialization;bootstrap(Lnet/minecraft/util/ExtraCodecs$LateBoundIdMapper;)V",
					shift = At.Shift.AFTER
			)
	)
	private static void megamod$injectResolvableTextContent(
			Codec<Component> selfCodec,
			CallbackInfoReturnable<Codec<Component>> cir,
			@Local ExtraCodecs.LateBoundIdMapper<String, MapCodec<? extends ComponentContents>> mapper) {
		mapper.put(ResolvableTextContent.TYPE_ID, ResolvableTextContent.MAP_CODEC);
	}
}

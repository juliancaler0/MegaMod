package com.ultra.megamod.lib.accessories.owo.serialization.endec;

import com.mojang.serialization.Codec;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Adapter for io.wispforest.owo.serialization.endec.MinecraftEndecs.
 * Provides Endec wrappers for common Minecraft types.
 */
public final class MinecraftEndecs {

    private MinecraftEndecs() {}

    public static final Endec<Identifier> IDENTIFIER = Endec.ofCodec(Identifier.CODEC);

    public static final Endec<ItemStack> ITEM_STACK = Endec.ofCodec(ItemStack.OPTIONAL_CODEC);

    public static final Endec<Item> ITEM = Endec.ofCodec(BuiltInRegistries.ITEM.byNameCodec());

    public static final Endec<CompoundTag> COMPOUND_TAG = Endec.ofCodec(CompoundTag.CODEC);

    public static final Endec<net.minecraft.network.chat.Component> TEXT = Endec.ofCodec(
        net.minecraft.network.chat.ComponentSerialization.CODEC
    );

    public static final Endec<net.minecraft.world.entity.ai.attributes.AttributeModifier> ATTRIBUTE_MODIFIER =
        Endec.ofCodec(net.minecraft.world.entity.ai.attributes.AttributeModifier.CODEC);
}

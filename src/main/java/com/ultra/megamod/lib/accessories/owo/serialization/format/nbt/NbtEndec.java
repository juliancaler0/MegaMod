package com.ultra.megamod.lib.accessories.owo.serialization.format.nbt;

import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Adapter for io.wispforest.owo.serialization.format.nbt.NbtEndec.
 * Provides Endec wrappers for NBT types.
 */
public final class NbtEndec {

    private NbtEndec() {}

    public static final Endec<CompoundTag> COMPOUND = Endec.ofCodec(CompoundTag.CODEC);

    public static final Endec<Tag> ELEMENT = COMPOUND.xmap(tag -> tag, tag -> (CompoundTag) tag);
}

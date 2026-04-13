package com.ultra.megamod.lib.accessories.endec;

import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.KeyedEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.util.MapCarrier;
import com.ultra.megamod.lib.accessories.owo.serialization.format.nbt.NbtEndec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

public record NbtMapCarrier(CompoundTag compoundTag) implements MapCarrier {

    public static final Endec<NbtMapCarrier> ENDEC = NbtEndec.COMPOUND.xmap(NbtMapCarrier::new, NbtMapCarrier::compoundTag);

    public static NbtMapCarrier of() {
        return new NbtMapCarrier(new CompoundTag());
    }

    @Override
    public <T> T getWithErrors(SerializationContext ctx, @NotNull KeyedEndec<T> key) {
        if (!this.has(key)) return key.defaultValue();
        Tag tag = this.compoundTag().get(key.key());
        if (tag == null) return key.defaultValue();
        return key.endec().codec().parse(NbtOps.INSTANCE, tag)
            .result().orElse(key.defaultValue());
    }

    @Override
    public <T> void put(SerializationContext ctx, @NotNull KeyedEndec<T> key, @NotNull T value) {
        key.endec().codec().encodeStart(NbtOps.INSTANCE, value)
            .result().ifPresent(tag -> this.compoundTag().put(key.key(), tag));
    }

    @Override
    public <T> void delete(@NotNull KeyedEndec<T> key) {
        this.compoundTag().remove(key.key());
    }

    @Override
    public <T> boolean has(@NotNull KeyedEndec<T> key) {
        return this.compoundTag().contains(key.key());
    }
}

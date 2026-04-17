package com.ultra.megamod.lib.owo.mixin.serialization;

import com.mojang.serialization.Codec;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.KeyedEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.util.MapCarrierEncodable;
import com.ultra.megamod.lib.owo.serialization.CodecUtils;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ValueOutput.class)
public interface ValueOutputMixin extends MapCarrierEncodable {

    @Shadow <T> void store(String key, Codec<T> codec, T value);

    @Shadow void discard(String key);

    @Override
    default <T> void put(SerializationContext ctx, @NotNull KeyedEndec<T> key, @NotNull T value) {
        this.store(key.key(), CodecUtils.toCodec(key.endec(), ctx), value);
    }

    @Override
    default <T> void delete(@NotNull KeyedEndec<T> key) {
        this.discard(key.key());
    }
}

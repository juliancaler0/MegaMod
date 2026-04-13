package com.ultra.megamod.lib.accessories.owo.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Adapter for io.wispforest.owo.serialization.CodecUtils.
 * Bridges between Endec and Codec/StreamCodec systems.
 */
public final class CodecUtils {

    private CodecUtils() {}

    public static <T> Codec<T> toCodec(Endec<T> endec) {
        return endec.codec();
    }

    public static <T> Codec<T> toCodec(Endec<T> endec, SerializationContext ctx) {
        return endec.codec();
    }

    /**
     * Creates an Endec that can decode from either of two formats.
     * Returns Either<L, R> wrapped in an Endec.
     */
    public static <L, R> Endec<com.mojang.datafixers.util.Either<L, R>> eitherEndec(Endec<L> left, Endec<R> right) {
        return Endec.ofCodec(Codec.either(left.codec(), right.codec()));
    }

    public static <T> Endec<T> ofCodec(Codec<T> codec) {
        return Endec.ofCodec(codec);
    }

    public static <T> Endec<T> toEndec(Codec<T> codec) {
        return Endec.ofCodec(codec);
    }

    public static <T> Endec<T> toEndecWithRegistries(Codec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return Endec.ofCodec(codec);
    }

    public static <T> StructEndec<T> toStructEndec(MapCodec<T> mapCodec) {
        return new StructEndec<>() {
            @Override
            public Codec<T> codec() {
                return mapCodec.codec();
            }
        };
    }

    public static <T> StructEndec<T> toStructEndec(Codec<T> codec) {
        return new StructEndec<>() {
            @Override
            public Codec<T> codec() {
                return codec;
            }
        };
    }

    public static <T> StreamCodec<FriendlyByteBuf, T> toPacketCodec(Endec<T> endec) {
        return StreamCodec.of(
            (buf, value) -> {
                // Use codec to encode to NBT, then write NBT to buf
                var nbt = endec.codec().encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, value).getOrThrow();
                buf.writeNbt(nbt);
            },
            buf -> {
                var nbt = buf.readNbt();
                return endec.codec().parse(net.minecraft.nbt.NbtOps.INSTANCE, nbt).getOrThrow();
            }
        );
    }

    @SuppressWarnings("unchecked")
    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> toRegistryPacketCodec(Endec<T> endec) {
        return StreamCodec.of(
            (buf, value) -> {
                var nbt = endec.codec().encodeStart(buf.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), value).getOrThrow();
                buf.writeNbt(nbt);
            },
            buf -> {
                var nbt = buf.readNbt();
                return endec.codec().parse(buf.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), nbt).getOrThrow();
            }
        );
    }
}

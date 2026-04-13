package com.ultra.megamod.lib.accessories.networking.holder;

import com.ultra.megamod.lib.accessories.client.gui.AccessoriesScreenBase;
import com.ultra.megamod.lib.accessories.impl.option.PlayerOption;
import com.ultra.megamod.lib.accessories.networking.AccessoriesNetworking;
import com.ultra.megamod.lib.accessories.endec.adapter.*;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.function.Function;

public record SyncOptionChange(PlayerOption<?> option, Object data) {

    public SyncOptionChange {
        Objects.requireNonNull(option.endec(), "Unable to sync change as this PlayerOption is not designed to be persistent! [Name: " + option.name() + "]");
    }

    public static final StructEndec<SyncOptionChange> ENDEC = new StructEndec<>() {
        @Override
        public com.mojang.serialization.Codec<SyncOptionChange> codec() {
            // Stubbed codec - SyncOptionChange is network-only, not persisted
            throw new UnsupportedOperationException("SyncOptionChange codec not supported");
        }

        @Override
        public void encodeStruct(SerializationContext ctx, Serializer<?> serializer, Serializer.Struct struct, SyncOptionChange value) {
            // Network-only encoding handled by StreamCodec
        }

        @Override
        public SyncOptionChange decodeStruct(SerializationContext ctx, Deserializer<?> deserializer, Deserializer.Struct struct) {
            // Network-only decoding handled by StreamCodec
            return null;
        }
    };

    public static <T> SyncOptionChange of(PlayerOption<T> property, T data) {
        return new SyncOptionChange(property, data);
    }

    public static <T> SyncOptionChange of(PlayerOption<T> property, Player player, Function<T, T> operation) {
        return new SyncOptionChange(property, operation.apply(property.getDataOrDefault(player)));
    }

    public static void handlePacket(SyncOptionChange packet, Player player) {
        packet.option().setData(player, packet.data());

        if(player.level().isClientSide()) {
            handleClient(packet, player);
        } else {
            AccessoriesNetworking.sendToPlayer((ServerPlayer) player, SyncOptionChange.of((PlayerOption<Object>) packet.option(),packet.option().getDataOrDefault(player)));
        }
    }

    //@Environment(EnvType.CLIENT)
    public static void handleClient(SyncOptionChange packet, Player player) {
        if(Minecraft.getInstance().screen instanceof AccessoriesScreenBase accessoriesScreen) {
            accessoriesScreen.onHolderChange(packet.option());
        }
    }
}
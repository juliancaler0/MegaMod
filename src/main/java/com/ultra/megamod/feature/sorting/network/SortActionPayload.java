package com.ultra.megamod.feature.sorting.network;

import com.ultra.megamod.feature.sorting.SortAlgorithm;
import com.ultra.megamod.feature.sorting.SortingManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SortActionPayload(String sortType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SortActionPayload> TYPE =
        new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("megamod", "sort_action"));

    public static final StreamCodec<FriendlyByteBuf, SortActionPayload> STREAM_CODEC =
        new StreamCodec<FriendlyByteBuf, SortActionPayload>() {
            public SortActionPayload decode(FriendlyByteBuf buf) {
                return new SortActionPayload(buf.readUtf());
            }

            public void encode(FriendlyByteBuf buf, SortActionPayload payload) {
                buf.writeUtf(payload.sortType());
            }
        };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(SortActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            SortAlgorithm algorithm = SortAlgorithm.fromString(payload.sortType());
            SortingManager.sortContainer(player, algorithm);
        });
    }
}

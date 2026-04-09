package com.ultra.megamod.feature.dungeons.insurance.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.ultra.megamod.feature.dungeons.insurance.InsuranceManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Client → Server: player has selected insurance slots and is ready (or cancelled).
 */
public record InsuranceReadyPayload(String jsonSelectedSlots, boolean cancelled) implements CustomPacketPayload {

    private static final Gson GSON = new Gson();

    public static final CustomPacketPayload.Type<InsuranceReadyPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "insurance_ready"));

    public static final StreamCodec<FriendlyByteBuf, InsuranceReadyPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public InsuranceReadyPayload decode(FriendlyByteBuf buf) {
            return new InsuranceReadyPayload(buf.readUtf(), buf.readBoolean());
        }

        @Override
        public void encode(FriendlyByteBuf buf, InsuranceReadyPayload payload) {
            buf.writeUtf(payload.jsonSelectedSlots());
            buf.writeBoolean(payload.cancelled());
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(InsuranceReadyPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            if (payload.cancelled()) {
                InsuranceManager.markReady(serverPlayer, Set.of(), true);
                return;
            }

            // Parse selected slots from JSON
            Set<String> selectedSlots = new HashSet<>();
            try {
                JsonArray arr = GSON.fromJson(payload.jsonSelectedSlots(), JsonArray.class);
                for (int i = 0; i < arr.size(); i++) {
                    selectedSlots.add(arr.get(i).getAsString());
                }
            } catch (Exception e) {
                // If parse fails, treat as no selection
            }

            InsuranceManager.markReady(serverPlayer, selectedSlots, false);
        });
    }
}

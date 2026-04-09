package com.ultra.megamod.feature.citizen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client-to-server payload: colony research actions.
 *
 * <p>Supported actions:
 * <ul>
 *   <li>{@code "start"} - begin researching. jsonData: {"branch":"civilian","researchId":"megamod:faster_farming"}</li>
 *   <li>{@code "cancel"} - cancel in-progress research. jsonData: {"branch":"civilian","researchId":"megamod:faster_farming"}</li>
 *   <li>{@code "reset"} - reset completed research. jsonData: {"branch":"civilian","researchId":"megamod:faster_farming"}</li>
 *   <li>{@code "request"} - request full research tree sync from server. jsonData: {}</li>
 * </ul>
 *
 * <p>Server responds with {@link ResearchSyncPayload}.
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code colonyId} - the faction/colony UUID string</li>
 *   <li>{@code action} - the research action type</li>
 *   <li>{@code jsonData} - action-specific parameters as JSON</li>
 * </ul>
 */
public record ResearchPayload(String colonyId, String action, String jsonData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ResearchPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "colony_research"));

    public static final StreamCodec<FriendlyByteBuf, ResearchPayload> STREAM_CODEC =
        StreamCodec.of(ResearchPayload::write, ResearchPayload::read);

    private static void write(FriendlyByteBuf buf, ResearchPayload payload) {
        buf.writeUtf(payload.colonyId, 256);
        buf.writeUtf(payload.action, 256);
        buf.writeUtf(payload.jsonData, 131072);
    }

    private static ResearchPayload read(FriendlyByteBuf buf) {
        return new ResearchPayload(buf.readUtf(256), buf.readUtf(256), buf.readUtf(131072));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

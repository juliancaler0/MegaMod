package com.ultra.megamod.feature.citizen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server-to-client payload: research tree state sync.
 * Sent in response to {@link ResearchPayload} requests or when research state changes.
 *
 * <p>Actions:
 * <ul>
 *   <li>{@code "sync"} - full research tree. jsonData: {"branches":{branchId:{researches:{researchId:{status,progress,level}}}},"effects":{effectId:level}}</li>
 *   <li>{@code "progress"} - single research update. jsonData: {"branch":"civilian","researchId":"...","progress":0.75,"status":"IN_PROGRESS"}</li>
 *   <li>{@code "complete"} - research completed. jsonData: {"branch":"civilian","researchId":"...","effectId":"...","level":1}</li>
 * </ul>
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code colonyId} - the faction/colony UUID string</li>
 *   <li>{@code action} - the sync action type</li>
 *   <li>{@code jsonData} - research data as JSON</li>
 * </ul>
 */
public record ResearchSyncPayload(String colonyId, String action, String jsonData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ResearchSyncPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "research_sync"));

    public static final StreamCodec<FriendlyByteBuf, ResearchSyncPayload> STREAM_CODEC =
        StreamCodec.of(ResearchSyncPayload::write, ResearchSyncPayload::read);

    /** Client-side last received research sync for screen polling. */
    public static volatile ResearchSyncPayload lastResponse = null;

    private static void write(FriendlyByteBuf buf, ResearchSyncPayload payload) {
        buf.writeUtf(payload.colonyId, 256);
        buf.writeUtf(payload.action, 256);
        buf.writeUtf(payload.jsonData, 131072);
    }

    private static ResearchSyncPayload read(FriendlyByteBuf buf) {
        return new ResearchSyncPayload(buf.readUtf(256), buf.readUtf(256), buf.readUtf(131072));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

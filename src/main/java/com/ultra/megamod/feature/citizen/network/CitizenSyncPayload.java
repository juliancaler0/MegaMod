package com.ultra.megamod.feature.citizen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server-to-client payload: detailed citizen data sync.
 * Sent when a player opens a citizen's info screen or interacts with them,
 * and periodically to keep the client view up to date.
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code colonyId} - the faction/colony UUID string</li>
 *   <li>{@code citizenId} - the citizen entity UUID string</li>
 *   <li>{@code jsonData} - JSON object with citizen detail state:
 *       name, job, level, skills{skillName:{level,xp}}, happiness{overall,modifiers{}},
 *       health, maxHealth, saturation, position{x,y,z}, status, bedPos, workPos,
 *       assignedBuildingPos, inventory[], requests[], isChild, gender, textureId</li>
 * </ul>
 */
public record CitizenSyncPayload(String colonyId, String citizenId, String jsonData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CitizenSyncPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "citizen_sync"));

    public static final StreamCodec<FriendlyByteBuf, CitizenSyncPayload> STREAM_CODEC =
        StreamCodec.of(CitizenSyncPayload::write, CitizenSyncPayload::read);

    /** Client-side last received citizen sync for screen polling. */
    public static volatile CitizenSyncPayload lastResponse = null;

    private static void write(FriendlyByteBuf buf, CitizenSyncPayload payload) {
        buf.writeUtf(payload.colonyId, 256);
        buf.writeUtf(payload.citizenId, 256);
        buf.writeUtf(payload.jsonData, 131072);
    }

    private static CitizenSyncPayload read(FriendlyByteBuf buf) {
        return new CitizenSyncPayload(buf.readUtf(256), buf.readUtf(256), buf.readUtf(131072));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package com.ultra.megamod.feature.backpacks.network;

import com.ultra.megamod.feature.backpacks.BackpackWearableManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-to-client payload that syncs a player's equipped backpack state.
 * Sent when a player equips/unequips a backpack, and when a new player joins.
 *
 * @param entityId       The entity ID of the player whose backpack state changed
 * @param backpackItemId The registry name of the backpack item, or empty string if unequipped
 */
public record BackpackSyncPayload(int entityId, String backpackItemId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BackpackSyncPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "backpack_sync"));

    public static final StreamCodec<FriendlyByteBuf, BackpackSyncPayload> STREAM_CODEC =
        new StreamCodec<FriendlyByteBuf, BackpackSyncPayload>() {
            @Override
            public BackpackSyncPayload decode(FriendlyByteBuf buf) {
                return new BackpackSyncPayload(buf.readVarInt(), buf.readUtf());
            }

            @Override
            public void encode(FriendlyByteBuf buf, BackpackSyncPayload payload) {
                buf.writeVarInt(payload.entityId());
                buf.writeUtf(payload.backpackItemId());
            }
        };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handle on the client: update the client-side equipped backpack cache.
     */
    public static void handleOnClient(BackpackSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            BackpackWearableManager.setClientEquipped(payload.entityId(), payload.backpackItemId());
        });
    }
}

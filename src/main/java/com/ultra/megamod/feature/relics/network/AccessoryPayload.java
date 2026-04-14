package com.ultra.megamod.feature.relics.network;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Legacy-compat client sync payload for MegaMod HUD overlays (ability bar,
 * equipment stats, insurance screen, etc.). The lib/accessories library owns the
 * equip/unequip UI now; this payload just mirrors slot→item-id strings to the
 * client so the MegaMod renderers — which predate the lib port — can keep working.
 */
public class AccessoryPayload {

    public record AccessorySyncPayload(CompoundTag tagData) implements CustomPacketPayload {
        public static volatile Map<String, String> clientEquipped = new HashMap<>();
        /** Incremented each time clientEquipped is replaced, so renderers can detect changes. */
        public static volatile long syncVersion = 0;

        public static final CustomPacketPayload.Type<AccessorySyncPayload> TYPE =
                new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "accessory_sync"));

        public static final StreamCodec<FriendlyByteBuf, AccessorySyncPayload> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public AccessorySyncPayload decode(FriendlyByteBuf buf) {
                CompoundTag tag = buf.readNbt();
                return new AccessorySyncPayload(tag != null ? tag : new CompoundTag());
            }

            @Override
            public void encode(FriendlyByteBuf buf, AccessorySyncPayload payload) {
                buf.writeNbt(payload.tagData());
            }
        };

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handleOnClient(AccessorySyncPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                HashMap<String, String> parsed = new HashMap<>();
                CompoundTag tag = payload.tagData();
                for (String key : tag.keySet()) {
                    String value = tag.getStringOr(key, "");
                    if (!value.isEmpty()) {
                        parsed.put(key, value);
                    }
                }
                clientEquipped = parsed;
                syncVersion++;
            });
        }
    }
}

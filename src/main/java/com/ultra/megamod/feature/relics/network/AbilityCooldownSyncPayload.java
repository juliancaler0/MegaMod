package com.ultra.megamod.feature.relics.network;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AbilityCooldownSyncPayload(CompoundTag tagData) implements CustomPacketPayload
{
    public static volatile Map<String, Integer> clientCooldowns = new HashMap<String, Integer>();
    public static final CustomPacketPayload.Type<AbilityCooldownSyncPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"ability_cooldown_sync"));
    public static final StreamCodec<FriendlyByteBuf, AbilityCooldownSyncPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, AbilityCooldownSyncPayload>(){

        public AbilityCooldownSyncPayload decode(FriendlyByteBuf buf) {
            CompoundTag tag = buf.readNbt();
            return new AbilityCooldownSyncPayload(tag != null ? tag : new CompoundTag());
        }

        public void encode(FriendlyByteBuf buf, AbilityCooldownSyncPayload payload) {
            buf.writeNbt(payload.tagData());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(AbilityCooldownSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            HashMap<String, Integer> parsed = new HashMap<>();
            CompoundTag tag = payload.tagData();
            for (String key : tag.keySet()) {
                int ticks = tag.getIntOr(key, 0);
                if (ticks > 0) {
                    parsed.put(key, ticks);
                }
            }
            clientCooldowns = parsed;
        });
    }
}

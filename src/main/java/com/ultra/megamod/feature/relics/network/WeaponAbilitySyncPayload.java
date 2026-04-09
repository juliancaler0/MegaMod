package com.ultra.megamod.feature.relics.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Server -> Client: syncs RPG weapon cooldowns for the held weapon.
 * Sent every 5 ticks when the player holds an RPG weapon.
 */
public record WeaponAbilitySyncPayload(CompoundTag tagData) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WeaponAbilitySyncPayload> TYPE =
            new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("megamod", "weapon_ability_sync"));

    public static final StreamCodec<FriendlyByteBuf, WeaponAbilitySyncPayload> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, WeaponAbilitySyncPayload>() {
                public WeaponAbilitySyncPayload decode(FriendlyByteBuf buf) {
                    CompoundTag tag = buf.readNbt();
                    return new WeaponAbilitySyncPayload(tag != null ? tag : new CompoundTag());
                }
                public void encode(FriendlyByteBuf buf, WeaponAbilitySyncPayload payload) {
                    buf.writeNbt(payload.tagData());
                }
            };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    /** Client-side cooldown map for RPG weapon skills (skill name -> ticks remaining). */
    public static volatile Map<String, Integer> clientWeaponCooldowns = new HashMap<>();

    public static void handleOnClient(WeaponAbilitySyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            HashMap<String, Integer> parsed = new HashMap<>();
            CompoundTag tag = payload.tagData();
            for (String key : tag.keySet()) {
                int ticks = tag.getIntOr(key, 0);
                parsed.put(key, Math.max(0, ticks));
            }
            clientWeaponCooldowns = parsed;
        });
    }
}

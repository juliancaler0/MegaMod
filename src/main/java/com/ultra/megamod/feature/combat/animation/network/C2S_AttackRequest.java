package com.ultra.megamod.feature.combat.animation.network;

import com.ultra.megamod.MegaMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;

/**
 * Client → Server: Attack request with target entity IDs from client-side hitbox detection.
 * Ported 1:1 from BetterCombat's C2S_AttackRequest packet.
 *
 * The client finds targets via TargetFinder and sends their IDs to the server.
 * The server validates range and executes the attack.
 */
public record C2S_AttackRequest(
        int comboCount,
        boolean isSneaking,
        int selectedSlot,
        int cursorTargetId,  // -1 if no cursor target
        int[] entityIds      // Target entity IDs from client hitbox detection
) implements CustomPacketPayload {

    public static final Type<C2S_AttackRequest> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MegaMod.MODID, "bc_attack_request"));

    public static final StreamCodec<FriendlyByteBuf, C2S_AttackRequest> STREAM_CODEC =
            StreamCodec.of(C2S_AttackRequest::write, C2S_AttackRequest::read);

    private static void write(FriendlyByteBuf buf, C2S_AttackRequest p) {
        buf.writeVarInt(p.comboCount);
        buf.writeBoolean(p.isSneaking);
        buf.writeVarInt(p.selectedSlot);
        buf.writeVarInt(p.cursorTargetId);
        buf.writeVarInt(p.entityIds.length);
        for (int id : p.entityIds) buf.writeVarInt(id);
    }

    private static C2S_AttackRequest read(FriendlyByteBuf buf) {
        int combo = buf.readVarInt();
        boolean sneak = buf.readBoolean();
        int slot = buf.readVarInt();
        int cursor = buf.readVarInt();
        int count = buf.readVarInt();
        int[] ids = new int[Math.min(count, 64)]; // Cap to prevent abuse
        for (int i = 0; i < ids.length; i++) ids[i] = buf.readVarInt();
        return new C2S_AttackRequest(combo, sneak, slot, cursor, ids);
    }

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() { return TYPE; }
}

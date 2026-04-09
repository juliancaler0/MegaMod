package com.ultra.megamod.feature.relics.network;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> Server: request to cast an ability.
 * slotName: "MAINHAND" for weapons, or "BACK"/"BELT"/etc. for accessories.
 * abilityName: "__byindex__" for index-based resolution, or explicit ability name.
 * abilityIndex: which non-PASSIVE ability to cast (0, 1, 2...).
 */
public record AbilityCastPayload(String slotName, String abilityName, int abilityIndex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AbilityCastPayload> TYPE =
            new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("megamod", "ability_cast"));

    public static final StreamCodec<FriendlyByteBuf, AbilityCastPayload> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, AbilityCastPayload>() {
                public AbilityCastPayload decode(FriendlyByteBuf buf) {
                    String slotName = buf.readUtf();
                    String abilityName = buf.readUtf();
                    int abilityIndex = buf.readVarInt();
                    return new AbilityCastPayload(slotName, abilityName, abilityIndex);
                }
                public void encode(FriendlyByteBuf buf, AbilityCastPayload payload) {
                    buf.writeUtf(payload.slotName());
                    buf.writeUtf(payload.abilityName());
                    buf.writeVarInt(payload.abilityIndex());
                }
            };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(AbilityCastPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player p = context.player();
            if (!(p instanceof ServerPlayer player)) return;
            AbilityCastHandler.handleCast(player, payload.slotName(), payload.abilityName(), payload.abilityIndex());
        });
    }
}

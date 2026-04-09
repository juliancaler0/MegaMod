package com.ultra.megamod.feature.combat.spell;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Server-to-client payload that tells nearby players about another player's casting state.
 * Rendered as a small school-colored indicator above the caster's head.
 */
public record NearbyPlayerCastPayload(UUID casterId, boolean casting, String spellName, float progress, int schoolColor)
    implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<NearbyPlayerCastPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "nearby_player_cast"));

    public static final StreamCodec<FriendlyByteBuf, NearbyPlayerCastPayload> STREAM_CODEC =
        new StreamCodec<FriendlyByteBuf, NearbyPlayerCastPayload>() {

            @Override
            public NearbyPlayerCastPayload decode(FriendlyByteBuf buf) {
                UUID casterId = buf.readUUID();
                boolean casting = buf.readBoolean();
                String spellName = buf.readUtf(256);
                float progress = buf.readFloat();
                int schoolColor = buf.readInt();
                return new NearbyPlayerCastPayload(casterId, casting, spellName, progress, schoolColor);
            }

            @Override
            public void encode(FriendlyByteBuf buf, NearbyPlayerCastPayload payload) {
                buf.writeUUID(payload.casterId());
                buf.writeBoolean(payload.casting());
                buf.writeUtf(payload.spellName(), 256);
                buf.writeFloat(payload.progress());
                buf.writeInt(payload.schoolColor());
            }
        };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(NearbyPlayerCastPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            NearbyPlayerCastTracker.updateCasterState(
                payload.casterId(), payload.casting(), payload.spellName(),
                payload.progress(), payload.schoolColor()
            );
        });
    }
}

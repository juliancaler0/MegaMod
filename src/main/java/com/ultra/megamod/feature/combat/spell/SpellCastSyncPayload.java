package com.ultra.megamod.feature.combat.spell;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-to-client payload that syncs spell cast bar state for the HUD overlay.
 * Sent every tick while a player is casting, and once when casting stops (to clear the bar).
 */
public record SpellCastSyncPayload(boolean casting, String spellName, float progress, int schoolColor)
    implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SpellCastSyncPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "spell_cast_sync"));

    public static final StreamCodec<FriendlyByteBuf, SpellCastSyncPayload> STREAM_CODEC =
        new StreamCodec<FriendlyByteBuf, SpellCastSyncPayload>() {

            @Override
            public SpellCastSyncPayload decode(FriendlyByteBuf buf) {
                boolean casting = buf.readBoolean();
                String spellName = buf.readUtf(256);
                float progress = buf.readFloat();
                int schoolColor = buf.readInt();
                return new SpellCastSyncPayload(casting, spellName, progress, schoolColor);
            }

            @Override
            public void encode(FriendlyByteBuf buf, SpellCastSyncPayload payload) {
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

    public static void handleOnClient(SpellCastSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.casting()) {
                SpellCastOverlay.castingSpellId = payload.spellName();
                SpellCastOverlay.castingSpellName = payload.spellName();
                SpellCastOverlay.castProgress = payload.progress();
                SpellCastOverlay.castingSchoolColor = payload.schoolColor();
            } else {
                // Casting stopped — clear the overlay
                SpellCastOverlay.castingSpellId = null;
                SpellCastOverlay.castingSpellName = "";
                SpellCastOverlay.castProgress = 0f;
                SpellCastOverlay.castingSchoolColor = 0xFFFFFFFF;
            }
        });
    }
}

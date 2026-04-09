package com.ultra.megamod.feature.combat.animation.network;

import com.ultra.megamod.MegaMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

/**
 * Server → Client: Play a weapon swing sound at a position.
 * Ported from BetterCombat's attack sound packet.
 */
public record S2C_AttackSound(
        double x, double y, double z,
        String soundId,
        float volume, float pitch, long seed
) implements CustomPacketPayload {

    public static final Type<S2C_AttackSound> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MegaMod.MODID, "bc_attack_sound"));

    public static final StreamCodec<FriendlyByteBuf, S2C_AttackSound> STREAM_CODEC =
            StreamCodec.of(S2C_AttackSound::write, S2C_AttackSound::read);

    private static void write(FriendlyByteBuf buf, S2C_AttackSound p) {
        buf.writeDouble(p.x); buf.writeDouble(p.y); buf.writeDouble(p.z);
        buf.writeUtf(p.soundId, 256);
        buf.writeFloat(p.volume); buf.writeFloat(p.pitch);
        buf.writeLong(p.seed);
    }

    private static S2C_AttackSound read(FriendlyByteBuf buf) {
        return new S2C_AttackSound(
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readUtf(256), buf.readFloat(), buf.readFloat(), buf.readLong());
    }

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleClient(S2C_AttackSound payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            var soundId = Identifier.fromNamespaceAndPath("megamod", payload.soundId.replace(".", "/"));
            var soundEvent = SoundEvent.createVariableRangeEvent(soundId);
            mc.level.playLocalSound(payload.x, payload.y, payload.z,
                    soundEvent, SoundSource.PLAYERS, payload.volume, payload.pitch, false);
        });
    }
}

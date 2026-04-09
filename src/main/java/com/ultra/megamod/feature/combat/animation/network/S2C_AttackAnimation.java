package com.ultra.megamod.feature.combat.animation.network;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.animation.client.AttackAnimationStack;
import com.ultra.megamod.feature.combat.animation.client.PlayerAttackAnimatable;
import com.ultra.megamod.feature.combat.animation.client.SwingAnimationState;
import com.ultra.megamod.feature.combat.animation.WeaponAttributes;
import com.ultra.megamod.feature.combat.animation.logic.AnimatedHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

/**
 * Server → Client: Attack animation broadcast to all tracking players.
 * Ported 1:1 from BetterCombat's S2C AttackAnimation packet.
 *
 * Carries the PlayerAnimator animation name so clients play the proper
 * keyframe animation on the attacking player.
 */
public record S2C_AttackAnimation(
        int playerId,
        int animatedHand,     // AnimatedHand ordinal
        String animationName, // PlayerAnimator animation ID
        float length,         // Attack cooldown in ticks
        float upswing         // Upswing ratio (0.0-1.0)
) implements CustomPacketPayload {

    /** Sentinel animation name to signal "stop animation". */
    public static final String STOP = "";

    public static final Type<S2C_AttackAnimation> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MegaMod.MODID, "bc_attack_anim"));

    public static final StreamCodec<FriendlyByteBuf, S2C_AttackAnimation> STREAM_CODEC =
            StreamCodec.of(S2C_AttackAnimation::write, S2C_AttackAnimation::read);

    private static void write(FriendlyByteBuf buf, S2C_AttackAnimation p) {
        buf.writeVarInt(p.playerId);
        buf.writeVarInt(p.animatedHand);
        buf.writeUtf(p.animationName, 256);
        buf.writeFloat(p.length);
        buf.writeFloat(p.upswing);
    }

    private static S2C_AttackAnimation read(FriendlyByteBuf buf) {
        return new S2C_AttackAnimation(
                buf.readVarInt(), buf.readVarInt(), buf.readUtf(256),
                buf.readFloat(), buf.readFloat());
    }

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() { return TYPE; }

    /**
     * Client-side handler — plays the attack animation on the target player.
     */
    public static void handleClient(S2C_AttackAnimation payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Entity entity = mc.level.getEntity(payload.playerId);
            if (!(entity instanceof AbstractClientPlayer player)) return;

            // Don't override local player's animation (they already played it locally)
            if (player == mc.player) return;

            if (payload.animationName.equals(STOP)) {
                if (player instanceof PlayerAttackAnimatable animatable) {
                    animatable.stopAttackAnimation(5);
                }
                return;
            }

            AnimatedHand hand = AnimatedHand.values()[
                    Math.min(payload.animatedHand, AnimatedHand.values().length - 1)];

            if (player instanceof PlayerAttackAnimatable animatable) {
                animatable.playAttackAnimation(
                        payload.animationName, hand, payload.length, payload.upswing);
            }

            // Also track swing state for particle system
            WeaponAttributes.SwingDirection dir = WeaponAttributes.SwingDirection.SLASH_RIGHT; // Default
            SwingAnimationState.startSwing(
                    payload.playerId, dir, hand.isOffHand(), hand == AnimatedHand.TWO_HANDED, 0);
        });
    }
}

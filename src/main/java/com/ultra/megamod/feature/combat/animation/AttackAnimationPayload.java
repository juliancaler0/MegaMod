package com.ultra.megamod.feature.combat.animation;

import com.ultra.megamod.feature.combat.animation.client.SpellAnimationManager;
import com.ultra.megamod.feature.combat.animation.client.SwingAnimationState;
import com.ultra.megamod.lib.playeranim.minecraft.animation.PlayerAnimResources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-to-client payload that broadcasts a combat animation event.
 * Includes the PlayerAnimationLib animation name so the client plays
 * the actual keyframe animation instead of manual arm rotation.
 */
public record AttackAnimationPayload(
        int entityId,
        int comboIndex,
        int swingDirection,
        boolean isOffHand,
        boolean twoHanded,
        String animationName  // PlayerAnimationLib animation ID (e.g., "one_handed_slash_horizontal_right")
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AttackAnimationPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "attack_animation"));

    public static final StreamCodec<FriendlyByteBuf, AttackAnimationPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public AttackAnimationPayload decode(FriendlyByteBuf buf) {
                    int entityId = buf.readVarInt();
                    int comboIndex = buf.readVarInt();
                    int swingDirection = buf.readVarInt();
                    boolean isOffHand = buf.readBoolean();
                    boolean twoHanded = buf.readBoolean();
                    String animName = buf.readUtf(256);
                    return new AttackAnimationPayload(entityId, comboIndex, swingDirection, isOffHand, twoHanded, animName);
                }

                @Override
                public void encode(FriendlyByteBuf buf, AttackAnimationPayload payload) {
                    buf.writeVarInt(payload.entityId);
                    buf.writeVarInt(payload.comboIndex);
                    buf.writeVarInt(payload.swingDirection);
                    buf.writeBoolean(payload.isOffHand);
                    buf.writeBoolean(payload.twoHanded);
                    buf.writeUtf(payload.animationName != null ? payload.animationName : "", 256);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    /** Per-player animation data for the current frame. */
    public static final Map<Integer, AnimationData> CLIENT_ANIMATIONS = new ConcurrentHashMap<>();

    public record AnimationData(int comboIndex, int swingDirection, boolean isOffHand,
                                boolean twoHanded, long receivedTime) {
        private static final long EXPIRY_MS = 1000;
        public boolean isExpired() {
            return System.currentTimeMillis() - receivedTime > EXPIRY_MS;
        }
    }

    /**
     * Client-side handler. Plays the keyframe animation via PlayerAnimationLib
     * AND tracks swing state for particle/hitbox systems.
     */
    public static void handleOnClient(AttackAnimationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            CLIENT_ANIMATIONS.put(payload.entityId(),
                    new AnimationData(payload.comboIndex(), payload.swingDirection(),
                            payload.isOffHand(), payload.twoHanded(), System.currentTimeMillis()));

            // Bridge to swing state for particles
            WeaponAttributes.SwingDirection dir = WeaponAttributes.SwingDirection.values()[
                    Math.min(payload.swingDirection(), WeaponAttributes.SwingDirection.values().length - 1)];
            SwingAnimationState.startSwing(
                    payload.entityId(), dir, payload.isOffHand(), payload.twoHanded(), payload.comboIndex());

            // Play the actual PlayerAnimationLib keyframe animation on the player
            if (payload.animationName() != null && !payload.animationName().isEmpty()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    Entity entity = mc.level.getEntity(payload.entityId());
                    if (entity instanceof AbstractClientPlayer player) {
                        playAttackAnimation(player, payload.animationName(),
                                payload.isOffHand(), payload.twoHanded());
                    }
                }
            }
        });
    }

    /**
     * Play a BetterCombat-style attack animation on a player using PlayerAnimationLib.
     * Uses the MISC layer (priority 200) from SpellAnimationManager with quick fade-in.
     */
    private static void playAttackAnimation(AbstractClientPlayer player, String animName,
                                             boolean isOffHand, boolean twoHanded) {
        Identifier animId = Identifier.fromNamespaceAndPath("megamod", animName);
        if (!PlayerAnimResources.hasAnimation(animId)) return;

        // Use SpellAnimationManager's MISC layer for combat animations
        // This gives priority 200 which is below spell casting (900) but above idle
        SpellAnimationManager.playAnimation(player, SpellAnimationManager.AnimationType.MISC,
                animId, 1.0f, isOffHand);
    }
}

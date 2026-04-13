package com.ultra.megamod.feature.combat.animation;

import com.ultra.megamod.feature.combat.animation.client.PlayerAttackAnimatable;
import com.ultra.megamod.feature.combat.animation.client.SwingAnimationState;
import com.ultra.megamod.feature.combat.animation.logic.AnimatedHand;
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
 *
 * <p>Carries {@code cooldownTicks} and {@code upswing} so remote clients
 * can scale the animation to match the attacker's weapon speed, matching
 * BetterCombat's speed formula {@code speed = endTick / cooldownTicks}.</p>
 */
public record AttackAnimationPayload(
        int entityId,
        int comboIndex,
        int swingDirection,
        boolean isOffHand,
        boolean twoHanded,
        String animationName,   // PlayerAnimationLib animation ID (e.g., "one_handed_slash_horizontal_right")
        float cooldownTicks,    // Weapon attack cooldown in ticks (20 / attack_speed)
        float upswing           // Upswing ratio from weapon_attributes (typically 0.5)
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
                    float cooldown = buf.readFloat();
                    float upswing = buf.readFloat();
                    return new AttackAnimationPayload(entityId, comboIndex, swingDirection, isOffHand, twoHanded, animName, cooldown, upswing);
                }

                @Override
                public void encode(FriendlyByteBuf buf, AttackAnimationPayload payload) {
                    buf.writeVarInt(payload.entityId);
                    buf.writeVarInt(payload.comboIndex);
                    buf.writeVarInt(payload.swingDirection);
                    buf.writeBoolean(payload.isOffHand);
                    buf.writeBoolean(payload.twoHanded);
                    buf.writeUtf(payload.animationName != null ? payload.animationName : "", 256);
                    buf.writeFloat(payload.cooldownTicks);
                    buf.writeFloat(payload.upswing);
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
     *
     * <p>Local player is skipped — their animation is already driven by
     * {@code AbstractClientPlayerMixin.playAttackAnimation} from the local click.
     * Remote players get the animation routed through the same
     * {@link PlayerAttackAnimatable} path so speed scaling (endTick/cooldown)
     * matches BetterCombat's reference formula.</p>
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

            if (payload.animationName() == null || payload.animationName().isEmpty()) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Entity entity = mc.level.getEntity(payload.entityId());
            if (!(entity instanceof AbstractClientPlayer player)) return;

            // Skip local player — their attack animation was already triggered locally by the mixin
            // using the true weapon cooldown. Re-triggering here would double-play at wrong speed.
            if (player == mc.player) return;

            if (!(player instanceof PlayerAttackAnimatable animatable)) return;

            AnimatedHand hand = payload.twoHanded() ? AnimatedHand.TWO_HANDED
                    : (payload.isOffHand() ? AnimatedHand.OFF_HAND : AnimatedHand.MAIN_HAND);

            // Fall back to sensible defaults if server didn't supply speed info
            float cooldown = payload.cooldownTicks() > 0 ? payload.cooldownTicks() : 20f;
            float upswing = payload.upswing() > 0 ? payload.upswing() : 0.5f;

            animatable.playAttackAnimation(payload.animationName(), hand, cooldown, upswing);
        });
    }
}

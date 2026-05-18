package com.ultra.megamod.feature.combat.animation;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.animation.client.SpellAnimationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;

/**
 * S2C payload to sync spell animation playback to all nearby clients.
 * Sent when a player starts casting, releases a spell, or dodges.
 */
public record SpellAnimationPayload(
        int playerId,
        int animType, // 0=CASTING, 1=RELEASE, 2=MISC, 3=STOP_CASTING, 4=STOP_ALL
        String animationId,
        float speed,
        boolean mirror
) implements CustomPacketPayload {

    public static final Type<SpellAnimationPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(MegaMod.MODID, "spell_animation"));

    public static final StreamCodec<FriendlyByteBuf, SpellAnimationPayload> STREAM_CODEC =
            StreamCodec.of(SpellAnimationPayload::write, SpellAnimationPayload::read);

    private static void write(FriendlyByteBuf buf, SpellAnimationPayload payload) {
        buf.writeVarInt(payload.playerId);
        buf.writeByte(payload.animType);
        buf.writeUtf(payload.animationId);
        buf.writeFloat(payload.speed);
        buf.writeBoolean(payload.mirror);
    }

    private static SpellAnimationPayload read(FriendlyByteBuf buf) {
        return new SpellAnimationPayload(
                buf.readVarInt(),
                buf.readByte(),
                buf.readUtf(),
                buf.readFloat(),
                buf.readBoolean()
        );
    }

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() { return TYPE; }

    /**
     * Handle on client side — play/stop the animation on the target player.
     * <p>
     * Unlike {@link AttackAnimationPayload} (which skips the caster because the
     * attack animation is already triggered by {@code MinecraftMixin.megamod$startUpswing}
     * on the local click), spell animations are NOT pre-triggered locally —
     * the caster needs the broadcast too, otherwise they see no cast pose
     * while everyone else does. {@link SpellAnimationManager#playAnimation} has
     * a (animId, speed, mirror) dedup guard, so an echo from a parallel local
     * driver wouldn't cause the fade-stack shimmy that used to plague the
     * attack path.
     */
    public static void handleClient(SpellAnimationPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Entity entity = mc.level.getEntity(payload.playerId);
            if (!(entity instanceof AbstractClientPlayer player)) return;

            if (payload.animType == 4) {
                SpellAnimationManager.stopAll(player);
                return;
            }
            if (payload.animType == 3) {
                SpellAnimationManager.stopAnimation(player, SpellAnimationManager.AnimationType.CASTING);
                return;
            }

            SpellAnimationManager.AnimationType type = switch (payload.animType) {
                case 1 -> SpellAnimationManager.AnimationType.RELEASE;
                case 2 -> SpellAnimationManager.AnimationType.MISC;
                default -> SpellAnimationManager.AnimationType.CASTING;
            };

            // Resolve animation id. Accept either bare path ("one_handed_projectile_charge")
            // or fully-namespaced ("megamod:one_handed_projectile_charge") so callers can
            // pass either format without a transform step.
            String name = payload.animationId;
            Identifier animId = name.contains(":")
                    ? Identifier.parse(name)
                    : Identifier.fromNamespaceAndPath("megamod", name);
            SpellAnimationManager.playAnimation(player, type, animId, payload.speed, payload.mirror);
        });
    }
}

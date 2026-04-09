package com.ultra.megamod.feature.combat.animation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-to-server payload sent when the player left-clicks with a BetterCombat weapon.
 * <p>
 * The client-side mixin intercepts {@code Minecraft.startAttack()}, cancels vanilla's
 * attack/swing, and sends this payload instead. The server then runs the full combo
 * attack logic (target detection, damage, sweep, animation broadcast) via
 * {@link BetterCombatHandler#handleClientAttackRequest(ServerPlayer)}.
 * <p>
 * This payload carries no data because all necessary context (held item, look direction,
 * nearby entities) is read from the server-side player state when the packet arrives.
 */
public record BetterCombatAttackPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BetterCombatAttackPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "better_combat_attack"));

    public static final StreamCodec<FriendlyByteBuf, BetterCombatAttackPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BetterCombatAttackPayload decode(FriendlyByteBuf buf) {
                    return new BetterCombatAttackPayload();
                }

                @Override
                public void encode(FriendlyByteBuf buf, BetterCombatAttackPayload payload) {
                    // No data to encode — all context is server-side
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Server-side handler. Validates the player state and delegates to the combo attack system.
     */
    public static void handleOnServer(BetterCombatAttackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // Validate: player must be holding a BetterCombat weapon
            if (!WeaponAttributeRegistry.hasAttributes(player.getMainHandItem())) return;

            // Validate: basic anti-spam — respect attack cooldown
            if (player.getAttackStrengthScale(0.0f) < 0.1f) return;

            // Delegate to the handler
            BetterCombatHandler.handleClientAttackRequest(player);
        });
    }
}

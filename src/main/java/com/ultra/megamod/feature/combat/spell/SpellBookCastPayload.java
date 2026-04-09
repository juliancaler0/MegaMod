package com.ultra.megamod.feature.combat.spell;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-to-server payload sent when a player presses R to cast a spell from their
 * offhand SpellBookItem. The server validates that the player holds the correct book,
 * the spell belongs to that book's school, and the player is allowed to cast it
 * (via SpellUnlockManager, which already checks for spell book bypass).
 */
public record SpellBookCastPayload(String spellId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SpellBookCastPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "spell_book_cast"));

    public static final StreamCodec<FriendlyByteBuf, SpellBookCastPayload> STREAM_CODEC =
        new StreamCodec<FriendlyByteBuf, SpellBookCastPayload>() {

            @Override
            public SpellBookCastPayload decode(FriendlyByteBuf buf) {
                return new SpellBookCastPayload(buf.readUtf(256));
            }

            @Override
            public void encode(FriendlyByteBuf buf, SpellBookCastPayload payload) {
                buf.writeUtf(payload.spellId(), 256);
            }
        };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Server-side handler. Validates the spell book, spell, and permissions, then casts.
     */
    public static void handleOnServer(SpellBookCastPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            String spellId = payload.spellId();
            if (spellId == null || spellId.isEmpty()) return;

            // Verify offhand holds a spell book
            ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
            if (offhand.isEmpty() || !(offhand.getItem() instanceof SpellBookItem book)) {
                return;
            }

            // Verify the spell belongs to this book's school
            if (!book.getSpellIds().contains(spellId)) {
                return;
            }

            // Verify the spell exists
            SpellDefinition spell = SpellRegistry.get(spellId);
            if (spell == null) return;

            // SpellUnlockManager.canCastSpell already checks for spell book bypass,
            // but we call it anyway for consistency (admin bypass, skill node checks, etc.)
            ServerLevel level = (ServerLevel) player.level();
            if (!SpellUnlockManager.canCastSpell(player.getUUID(), spellId, level)) {
                return;
            }

            // Cast the spell via the cast manager (handles instant/charged/channeled)
            SpellCastManager.startCast(player, spellId);
        });
    }
}

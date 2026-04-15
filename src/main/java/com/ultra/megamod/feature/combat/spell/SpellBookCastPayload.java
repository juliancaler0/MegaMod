package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCast;
import com.ultra.megamod.lib.spellengine.internals.target.SpellTarget;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

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

            // Look up the spell via the datapack-aware registry (contains all 278
            // JSON-loaded spells plus the Java-registered subset). The hotbar cast
            // path in lib/spellengine/network/ServerNetwork uses this same registry.
            Identifier spellIdentifier;
            try {
                spellIdentifier = Identifier.parse(spellId);
            } catch (Exception e) {
                MegaMod.LOGGER.warn("SpellBookCastPayload: invalid spell id '{}'", spellId);
                return;
            }

            ServerLevel world = (ServerLevel) player.level();
            var spellEntry = com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry
                .from(world)
                .get(spellIdentifier);
            if (spellEntry.isEmpty()) {
                MegaMod.LOGGER.warn("SpellBookCastPayload: spell '{}' not present in datapack SpellRegistry", spellId);
                return;
            }

            // Perform the spell through the shared SpellHelper path — this is what
            // the hotbar path already does. TRIGGER action with full progress matches
            // a book-style instant cast from an equipped spell book.
            var target = new SpellTarget.SearchResult(List.of(), null);
            SpellHelper.performSpell(world, player, spellEntry.get(), target, SpellCast.Action.RELEASE, 1.0f);

            // Preserve the existing animation/cooldown dispatch for the 36 hardcoded
            // spells — it is a safe fall-through (no-ops for ids outside its map).
            SpellCastManager.startCast(player, spellId);
        });
    }
}

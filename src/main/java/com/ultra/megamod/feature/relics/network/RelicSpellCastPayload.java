package com.ultra.megamod.feature.relics.network;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.internals.container.SpellContainerSource;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCast;
import com.ultra.megamod.lib.spellengine.internals.target.SpellTarget;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/**
 * Phase G.1 client-to-server relay for relic accessory casts.
 *
 * <p>The R keybind in {@code AbilityKeybind} calls
 * {@link com.ultra.megamod.feature.relics.client.UnifiedAbilityBar#getSelectedAccessory()} and - if
 * the chosen ability maps to a SpellEngine spell id via the relic's
 * {@code SpellContainer} - sends this payload with the spell id. The server
 * handler resolves the spell, validates that it is reachable from the player's
 * merged SpellContainer sources (accessories + held), and drives an immediate
 * cast through {@link SpellHelper#performSpell}. Level-gating runs inside
 * {@link SpellHelper#attemptCasting} via the {@code CASTING_ATTEMPT} predicate
 * registered by {@link com.ultra.megamod.feature.relics.RelicLevelGate}.
 */
public record RelicSpellCastPayload(String spellId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RelicSpellCastPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "relic_spell_cast"));

    public static final StreamCodec<FriendlyByteBuf, RelicSpellCastPayload> STREAM_CODEC =
            new StreamCodec<>() {
                public RelicSpellCastPayload decode(FriendlyByteBuf buf) {
                    return new RelicSpellCastPayload(buf.readUtf());
                }
                public void encode(FriendlyByteBuf buf, RelicSpellCastPayload payload) {
                    buf.writeUtf(payload.spellId());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(RelicSpellCastPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player p = context.player();
            if (!(p instanceof ServerPlayer player)) return;
            try {
                Identifier id = Identifier.tryParse(payload.spellId());
                if (id == null) return;
                ServerLevel level = (ServerLevel) player.level();
                var entry = SpellRegistry.from(level).get(id).orElse(null);
                if (entry == null) {
                    MegaMod.LOGGER.debug("[RelicSpellCast] unknown spell id {}", id);
                    return;
                }
                // Ensure the player actually has this spell available (relic equipped / held).
                boolean hasSpell = false;
                for (var holder : SpellContainerSource.activeSpellsOf(player)) {
                    var key = holder.getKey();
                    if (key != null && key.identifier().equals(id)) {
                        hasSpell = true;
                        break;
                    }
                }
                if (!hasSpell) {
                    // Fallback: check passives too (some relic passives may want manual trigger)
                    for (var holder : SpellContainerSource.passiveSpellsOf(player)) {
                        var key = holder.getKey();
                        if (key != null && key.identifier().equals(id)) {
                            hasSpell = true;
                            break;
                        }
                    }
                }
                if (!hasSpell) {
                    MegaMod.LOGGER.debug("[RelicSpellCast] {} not available to {}", id, player.getGameProfile().name());
                    return;
                }
                // Fire immediate cast: start + release in one call.
                var target = new SpellTarget.SearchResult(List.of(), null);
                SpellHelper.performSpell(level, player, entry, target, SpellCast.Action.RELEASE, 1.0f);
            } catch (Throwable t) {
                MegaMod.LOGGER.debug("[RelicSpellCast] failed", t);
            }
        });
    }
}

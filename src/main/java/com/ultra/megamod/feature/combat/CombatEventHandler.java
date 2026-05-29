package com.ultra.megamod.feature.combat;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.animation.BetterCombatHandler;
import com.ultra.megamod.feature.combat.animation.PlayerComboTracker;
import com.ultra.megamod.feature.combat.items.EquipmentSetManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

/**
 * Server-side event handler for the combat overhaul.
 *
 * Ticks {@link EquipmentSetManager} (armor set bonuses) once per second and cleans up
 * per-player combat state on logout. Spell casting is handled entirely by the faithful
 * SpellEngine library ({@code lib/spellengine}) — there is no custom cast loop here.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class CombatEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        // Tick equipment set bonuses (every 20 ticks = 1 second)
        if (player.tickCount % 20 == 0) {
            try {
                EquipmentSetManager.tickPlayer(player);
            } catch (Exception ignored) {
                // EquipmentSetManager may not be initialized yet
            }
        }
    }

    // ─── Player disconnect cleanup ─────────────────────────────────────

    /**
     * Clean up all per-player combat state when a player disconnects.
     * Prevents memory leaks from static maps retaining data for departed players.
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID id = player.getUUID();

        // Combo tracker: remove combo state
        PlayerComboTracker.resetCombo(id);

        // Equipment set bonuses: remove tracked tiers
        EquipmentSetManager.onPlayerLogout(player);

        // Melee swing slow tracking
        BetterCombatHandler.removeSwingSlow(id);

        // Combat attack context (should already be consumed, but clean up just in case)
        BetterCombatHandler.CombatAttackContext.clear(id);
    }

    // ─── Static state cleanup ──────────────────────────────────────────

    /**
     * Clear all combat static state. Called on server shutdown to prevent
     * stale data persisting across singleplayer world reloads.
     */
    public static void clearAll() {
        // No custom-casting state to clear; spell casting lives in lib/spellengine.
    }
}

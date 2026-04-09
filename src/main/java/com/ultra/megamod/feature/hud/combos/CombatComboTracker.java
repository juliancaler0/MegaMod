package com.ultra.megamod.feature.hud.combos;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Tracks abilities cast by each player within an 8-second window.
 * When a combo match is found, applies the effect and notifies the client.
 */
public class CombatComboTracker {

    private record TimestampedAbility(String name, CombatComboRegistry.AbilityTag tag, long tick) {}

    private static final Map<UUID, List<TimestampedAbility>> RECENT_ABILITIES = new HashMap<>();
    private static final long COMBO_WINDOW_TICKS = 160L; // 8 seconds

    /**
     * Called from AbilityCastHandler after an ability is successfully cast.
     */
    public static void onAbilityCast(ServerPlayer player, String abilityName) {
        CombatComboRegistry.AbilityTag tag = CombatComboRegistry.getTag(abilityName);
        if (tag == null) return; // Unknown ability, no tag

        UUID uuid = player.getUUID();
        long currentTick = player.level().getGameTime();

        List<TimestampedAbility> recent = RECENT_ABILITIES.computeIfAbsent(uuid, k -> new ArrayList<>());

        // Clean expired entries
        recent.removeIf(a -> currentTick - a.tick() > COMBO_WINDOW_TICKS);

        // Check if this new ability forms a combo with any recent one
        for (TimestampedAbility prev : recent) {
            CombatComboRegistry.ComboDefinition combo = CombatComboRegistry.findCombo(prev.tag(), tag);
            if (combo != null) {
                // Combo found! Apply effects and notify client
                CombatComboEffects.applyCombo(player, combo);
                PacketDistributor.sendToPlayer(player, new CombatComboPayload(combo.displayName(), combo.color()));
                recent.clear(); // Reset after combo
                return;
            }
        }

        // Add to recent
        recent.add(new TimestampedAbility(abilityName, tag, currentTick));
        // Keep only last 3
        while (recent.size() > 3) recent.remove(0);
    }

    public static void clearPlayer(UUID uuid) {
        RECENT_ABILITIES.remove(uuid);
    }
}

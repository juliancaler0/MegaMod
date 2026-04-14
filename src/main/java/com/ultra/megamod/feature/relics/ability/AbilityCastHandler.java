package com.ultra.megamod.feature.relics.ability;

import com.ultra.megamod.feature.relics.data.RelicAbility;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Registry of per-relic passive/toggle ability executors.
 *
 * <p>Phase H: the legacy right-click / R-tap active cast path has been removed —
 * all instantaneous relic/weapon casting now routes through SpellEngine via
 * {@code SpellContainer} data components and {@link com.ultra.megamod.feature.relics.network.RelicSpellCastPayload}.
 * This class is retained only as the executor map looked up by
 * {@link AbilitySystem#tickPassiveAbility} / {@link AbilitySystem#tickToggleAbility}
 * so that {@link RelicAbility.CastType#PASSIVE} and {@link RelicAbility.CastType#TOGGLE}
 * abilities keep running each server tick.
 */
public class AbilityCastHandler {
    private static final Map<String, AbilityExecutor> EXECUTORS = new HashMap<>();

    public static void registerAbility(String relicName, String abilityName, AbilityExecutor executor) {
        String key = relicName + ":" + abilityName;
        EXECUTORS.put(key, executor);
    }

    public static AbilityExecutor getExecutor(String key) {
        return EXECUTORS.get(key);
    }

    @FunctionalInterface
    public interface AbilityExecutor {
        void execute(ServerPlayer var1, ItemStack var2, RelicAbility var3, double[] var4);
    }
}

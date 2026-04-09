package com.ultra.megamod.feature.hud;

import com.ultra.megamod.feature.computer.network.handlers.SettingsHandler;
import com.ultra.megamod.feature.hud.network.ComboPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side kill combo tracking. Increments combo on rapid kills (within 4 seconds).
 */
@EventBusSubscriber(modid = "megamod")
public class KillComboTracker {

    private static final int COMBO_WINDOW_TICKS = 80; // 4 seconds
    private static final Map<UUID, ComboState> COMBOS = new HashMap<>();

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
        // Don't count player kills for combo
        if (event.getEntity() instanceof ServerPlayer) return;
        if (!SettingsHandler.isEnabled(killer.getUUID(), "hud_kill_combo")) return;

        int currentTick = killer.level().getServer().getTickCount();
        UUID uuid = killer.getUUID();

        ComboState state = COMBOS.get(uuid);
        if (state == null || currentTick - state.lastKillTick > COMBO_WINDOW_TICKS) {
            state = new ComboState(1, currentTick);
        } else {
            state.count++;
            state.lastKillTick = currentTick;
        }
        COMBOS.put(uuid, state);

        PacketDistributor.sendToPlayer(killer, new ComboPayload(state.count, COMBO_WINDOW_TICKS));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        int currentTick = event.getServer().getTickCount();
        if (currentTick % 10 != 0) return; // Check every 0.5 seconds

        Iterator<Map.Entry<UUID, ComboState>> it = COMBOS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, ComboState> entry = it.next();
            if (currentTick - entry.getValue().lastKillTick > COMBO_WINDOW_TICKS) {
                // Combo expired — send reset
                ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
                if (player != null) {
                    PacketDistributor.sendToPlayer(player, new ComboPayload(0, 0));
                }
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        COMBOS.remove(event.getEntity().getUUID());
    }

    private static class ComboState {
        int count;
        int lastKillTick;

        ComboState(int count, int lastKillTick) {
            this.count = count;
            this.lastKillTick = lastKillTick;
        }
    }
}

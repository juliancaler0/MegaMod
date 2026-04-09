package com.ultra.megamod.feature.backpacks;

import com.ultra.megamod.feature.backpacks.upgrade.UpgradeManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Server-side events for the backpack wearable system.
 * Handles syncing equipped backpacks on join, cleanup on disconnect,
 * ticking upgrades on worn backpacks, and variant abilities.
 */
@EventBusSubscriber(modid = "megamod")
public class BackpackEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) return;
        BackpackWearableManager.syncAllToPlayer(sp);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) return;
        BackpackWearableManager.clearOnDisconnect(sp);
    }

    /**
     * Tick upgrades on worn backpacks + variant abilities.
     * Runs every tick but individual upgrades control their own tick rate.
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 20 != 0) return; // Only check every 20 ticks (once/sec)
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!BackpackWearableManager.isWearing(player.getUUID())) continue;
            ItemStack backpackStack = BackpackWearableManager.getEquipped(player.getUUID());
            if (backpackStack == null || backpackStack.isEmpty()) continue;

            ServerLevel level = (ServerLevel) player.level();

            // Tick upgrades
            {
                if (backpackStack.getItem() instanceof BackpackItem bpItem) {
                    BackpackTier tier = bpItem.getTier(backpackStack);
                    UpgradeManager mgr = new UpgradeManager(tier);
                    mgr.initializeFromStack(backpackStack);
                    if (mgr.hasTickingUpgrade()) {
                        mgr.tickAll(player, level);
                        mgr.saveToStack(backpackStack);
                    }
                }
            }

            // Tick variant abilities (every 40 ticks = every 2nd call since we run every 20 ticks)
            if (player.tickCount % 40 < 20 && backpackStack.getItem() instanceof BackpackItem bpItem) {
                String variantName = bpItem.getVariant().name();
                BackpackAbilities.tickAbility(player, variantName, level);
            }
        }
    }
}

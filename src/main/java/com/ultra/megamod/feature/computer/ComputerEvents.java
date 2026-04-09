package com.ultra.megamod.feature.computer;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.CombatEventHandler;
import com.ultra.megamod.feature.combat.animation.BetterCombatHandler;
import com.ultra.megamod.feature.combat.animation.PlayerComboTracker;
import com.ultra.megamod.feature.combat.spell.SpellBarrierManager;
import com.ultra.megamod.feature.combat.spell.SpellCastManager;
import com.ultra.megamod.feature.combat.spell.SpellExecutor;
import com.ultra.megamod.feature.computer.network.handlers.BountyBoardHandler;
import com.ultra.megamod.feature.computer.network.handlers.FriendsHandler;
import com.ultra.megamod.feature.computer.network.handlers.PartyHandler;
import com.ultra.megamod.feature.computer.network.handlers.TradeHandler;
import com.ultra.megamod.feature.corruption.PurgeManager;
import com.ultra.megamod.feature.marketplace.MarketplaceManager;
import com.ultra.megamod.feature.moderation.ModerationManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = MegaMod.MODID)
public class ComputerEvents {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = (ServerLevel) player.level();

        FriendsHandler.onPlayerDisconnect(player, level);
        PartyHandler.onPlayerDisconnect(player, level);
        TradeHandler.onPlayerDisconnect(player);
        MarketplaceManager.get(level).onPlayerDisconnect(player, level);
        // Purge system: remove disconnecting player from purge participants
        PurgeManager.get(level.getServer().overworld()).removeParticipant(player.getUUID());
        // Citizen system cleanup (citizens persist in world, no cleanup needed for now)
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel level = event.getServer().overworld();
        if (level == null) return;

        // Save all persistent systems that were missing server-stop saves
        MarketplaceManager.get(level).forceSave(level);
        ModerationManager.get(level).saveToDisk(level);
        BountyBoardHandler.forceSave(level);
        BountyBoardHandler.reset();
        FriendsHandler.forceSave(level);
        FriendsHandler.reset();

        // Save and reset all handlers with static loaded flags
        // so they reload from disk on next server start (critical for singleplayer)
        com.ultra.megamod.feature.computer.network.handlers.MapHandler.saveToDisk(level);
        com.ultra.megamod.feature.computer.network.handlers.MapHandler.reset();
        com.ultra.megamod.feature.computer.network.handlers.MapDrawingManager.saveToDisk(level);
        com.ultra.megamod.feature.computer.network.handlers.MapDrawingManager.reset();
        com.ultra.megamod.feature.computer.network.handlers.NotesHandler.saveToDisk(level);
        com.ultra.megamod.feature.computer.network.handlers.NotesHandler.reset();
        com.ultra.megamod.feature.computer.network.handlers.MailHandler.saveToDisk(level);
        com.ultra.megamod.feature.computer.network.handlers.MailHandler.reset();
        com.ultra.megamod.feature.computer.network.handlers.SettingsHandler.saveToDisk(level);
        com.ultra.megamod.feature.computer.network.handlers.SettingsHandler.reset();

        // Quest progress
        com.ultra.megamod.feature.quests.QuestProgressManager.get(level).saveToDisk(level);
        com.ultra.megamod.feature.quests.QuestProgressManager.reset();

        // Reset singleton managers so they reload from disk on next server start
        com.ultra.megamod.feature.economy.EconomyManager.get(level).saveToDisk(level);
        com.ultra.megamod.feature.economy.EconomyManager.reset();
        com.ultra.megamod.feature.computer.network.handlers.SystemHealthHandler.reset();

        // Player class system — save and reset
        com.ultra.megamod.feature.combat.PlayerClassManager.get(level).saveToDisk(level);
        com.ultra.megamod.feature.combat.PlayerClassManager.reset();
        com.ultra.megamod.feature.combat.ClassEventHandler.clearAll();

        // Combat system static state cleanup — prevents stale data across singleplayer world reloads
        SpellBarrierManager.clearAll();
        SpellCastManager.clearAll();
        SpellExecutor.clearAllCooldowns();
        PlayerComboTracker.clearAll();
        CombatEventHandler.clearAll();
        BetterCombatHandler.clearAll();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            // Save dirty persistent data when a level unloads to prevent data loss
            ServerLevel overworld = serverLevel.getServer().overworld();
            MarketplaceManager.get(overworld).forceSave(overworld);
            ModerationManager.get(overworld).saveToDisk(overworld);
            com.ultra.megamod.feature.computer.network.handlers.MapHandler.saveToDisk(overworld);
            com.ultra.megamod.feature.computer.network.handlers.MapDrawingManager.saveToDisk(overworld);
            com.ultra.megamod.feature.computer.network.handlers.NotesHandler.saveToDisk(overworld);
            com.ultra.megamod.feature.computer.network.handlers.MailHandler.saveToDisk(overworld);
            com.ultra.megamod.feature.computer.network.handlers.SettingsHandler.saveToDisk(overworld);
            com.ultra.megamod.feature.quests.QuestProgressManager.get(overworld).saveToDisk(overworld);
            com.ultra.megamod.feature.combat.PlayerClassManager.get(overworld).saveToDisk(overworld);
        }
    }
}

package com.ultra.megamod.feature.citizen;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.data.*;
import com.ultra.megamod.feature.citizen.data.CitizenLifecycleManager;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.encyclopedia.DiscoveryManager;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = MegaMod.MODID)
public class CitizenEvents {

    private static long lastUpkeepCheck = 0;
    private static long lastSaveTick = 0;
    private static long lastNotificationCheck = 0;
    private static final long SAVE_INTERVAL = 6000; // auto-save every 5 min
    private static final long NOTIFICATION_INTERVAL = 2400; // check every 2 minutes

    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        // Register citizen name data loader so names are loaded from data/megamod/citizennames/
        event.addListener(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("megamod", "citizen_names"),
                com.ultra.megamod.feature.citizen.data.listener.CitizenNameListener.INSTANCE);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        CitizenManager.get(level);
        FactionManager.get(level);
        GroupManager.get(level);
        ClaimManager.get(level);
        DiplomacyManager.get(level);
        TreatyManager.get(level);
        CitizenConfigManager.get(level);
        com.ultra.megamod.feature.citizen.quest.ColonyQuestManager.get(level);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel level = event.getServer().overworld();
        if (!FeatureToggleManager.get(level).isEnabled("citizens")) return;

        long tick = level.getServer().getTickCount();

        // Upkeep check every interval
        if (tick - lastUpkeepCheck >= CitizenConfig.UPKEEP_INTERVAL_TICKS) {
            lastUpkeepCheck = tick;
            processUpkeep(level);
        }

        // Treaty expiry check every 1200 ticks (1 minute)
        if (tick % 1200 == 0) {
            TreatyManager.get(level).removeExpired(tick);
        }

        // Colony quest trigger evaluation (every 200 ticks, handled internally)
        com.ultra.megamod.feature.citizen.quest.ColonyQuestManager.get(level).tick(level);

        // Citizen needs notifications
        if (tick - lastNotificationCheck >= NOTIFICATION_INTERVAL) {
            lastNotificationCheck = tick;
            checkCitizenNeeds(level);
        }

        // Periodic save (async, staggered across 3 phases to reduce tick spikes)
        if (tick - lastSaveTick >= SAVE_INTERVAL && event.getServer().isRunning()) {
            lastSaveTick = tick;
            final ServerLevel saveLevel = level;
            com.ultra.megamod.util.AsyncSaveHelper.saveAsync(() -> saveAllStaggered(saveLevel, false));
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof MCEntityCitizen citizen)) return;
        ServerLevel level = (ServerLevel) citizen.level();
        if (!FeatureToggleManager.get(level).isEnabled("citizens")) return;

        // Unregister citizen from manager
        CitizenManager.get(level).unregisterCitizen(citizen.getUUID());
        // Remove from any group
        GroupData group = GroupManager.get(level).getGroupForEntity(citizen.getUUID());
        if (group != null) {
            group.removeMember(citizen.getUUID());
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel level = event.getServer().overworld();
        saveAll(level);
        // Reset singletons for clean world reload
        CitizenManager.reset();
        FactionManager.reset();
        GroupManager.reset();
        ClaimManager.reset();
        com.ultra.megamod.feature.citizen.data.SupplyPlacedTracker.reset();
        DiplomacyManager.reset();
        TreatyManager.reset();
        CitizenConfigManager.reset();
        com.ultra.megamod.feature.citizen.data.FactionStatsManager.reset();
        com.ultra.megamod.feature.citizen.equipment.CitizenEquipmentManager.reset();
        com.ultra.megamod.feature.prestige.MasteryMarkManager.reset();
        com.ultra.megamod.feature.prestige.PrestigeRewardManager.reset();
        // Colony quest system
        com.ultra.megamod.feature.citizen.quest.ColonyQuestManager.reset();
        // MegaColonies integration managers
        com.ultra.megamod.feature.citizen.research.ResearchManager.reset();
        com.ultra.megamod.feature.citizen.building.workorder.WorkManager.reset();
        com.ultra.megamod.feature.citizen.data.ColonyStatisticsManager.resetAll();
        // Visitor system
        com.ultra.megamod.feature.citizen.visitor.VisitorManager.resetAll();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            // Save dirty citizen data when a level unloads to prevent data loss
            ServerLevel overworld = serverLevel.getServer().overworld();
            saveAll(overworld);
        }
    }

    /**
     * Staggered save: saves 3-4 managers per call across 3 phases to reduce tick spikes.
     * savePhase cycles 0->1->2->0. For shutdown/unload contexts, pass forceAll=true.
     */
    private static int savePhase = 0;

    private static void saveAll(ServerLevel level) {
        saveAllStaggered(level, true);
    }

    private static void saveAllStaggered(ServerLevel level, boolean forceAll) {
        if (forceAll) {
            // Shutdown / level unload: save everything immediately
            CitizenManager.get(level).saveToDisk(level);
            FactionManager.get(level).saveToDisk(level);
            GroupManager.get(level).saveToDisk(level);
            ClaimManager.get(level).saveToDisk(level);
            DiplomacyManager.get(level).saveToDisk(level);
            TreatyManager.get(level).saveToDisk(level);
            // HeraldQuestTracker removed during MCEntityCitizen transition
            com.ultra.megamod.feature.citizen.data.FactionStatsManager.get(level).saveToDisk(level);
            com.ultra.megamod.feature.citizen.equipment.CitizenEquipmentManager.get(level).saveToDisk(level);
            com.ultra.megamod.feature.prestige.MasteryMarkManager.get(level).saveToDisk(level);
            com.ultra.megamod.feature.prestige.PrestigeRewardManager.get(level).saveToDisk(level);
            // Colony quest system
            com.ultra.megamod.feature.citizen.quest.ColonyQuestManager.get(level).saveToDisk(level);
            // MegaColonies integration managers
            com.ultra.megamod.feature.citizen.research.ResearchManager.saveAll(level);
            com.ultra.megamod.feature.citizen.building.workorder.WorkManager.get(level).saveToDisk(level);
            com.ultra.megamod.feature.citizen.data.ColonyStatisticsManager.saveAll(level);
            // Visitor system
            com.ultra.megamod.feature.citizen.visitor.VisitorManager.saveAll(level);
            return;
        }
        // Staggered: save ~4 managers per phase
        switch (savePhase) {
            case 0 -> {
                CitizenManager.get(level).saveToDisk(level);
                FactionManager.get(level).saveToDisk(level);
                GroupManager.get(level).saveToDisk(level);
                ClaimManager.get(level).saveToDisk(level);
                com.ultra.megamod.feature.citizen.research.ResearchManager.saveAll(level);
            }
            case 1 -> {
                DiplomacyManager.get(level).saveToDisk(level);
                TreatyManager.get(level).saveToDisk(level);
                // HeraldQuestTracker removed during MCEntityCitizen transition
                com.ultra.megamod.feature.citizen.data.FactionStatsManager.get(level).saveToDisk(level);
                com.ultra.megamod.feature.citizen.building.workorder.WorkManager.get(level).saveToDisk(level);
            }
            case 2 -> {
                com.ultra.megamod.feature.citizen.equipment.CitizenEquipmentManager.get(level).saveToDisk(level);
                com.ultra.megamod.feature.prestige.MasteryMarkManager.get(level).saveToDisk(level);
                com.ultra.megamod.feature.prestige.PrestigeRewardManager.get(level).saveToDisk(level);
                com.ultra.megamod.feature.citizen.data.ColonyStatisticsManager.saveAll(level);
                com.ultra.megamod.feature.citizen.visitor.VisitorManager.saveAll(level);
                com.ultra.megamod.feature.citizen.quest.ColonyQuestManager.get(level).saveToDisk(level);
            }
        }
        savePhase = (savePhase + 1) % 3;
    }

    private static void processUpkeep(ServerLevel level) {
        CitizenManager cm = CitizenManager.get(level);
        EconomyManager eco = EconomyManager.get(level);
        var upkeep = cm.getUpkeepTracker();

        if (!upkeep.isGlobalUpkeepEnabled()) return;

        for (var entry : cm.getAllCitizens().entrySet()) {
            UUID ownerUuid = entry.getKey();
            if (upkeep.isExempt(ownerUuid)) continue;

            int totalCost = 0;
            double adminMult = upkeep.getMultiplier(ownerUuid);
            // Arcane tree (mana_weaver branch) reduces upkeep costs
            double arcaneMult = CitizenSkillBonuses.getUpkeepMultiplier(level, ownerUuid);
            double combinedMult = adminMult * arcaneMult;
            for (var record : entry.getValue()) {
                totalCost += (int) (CitizenConfig.getUpkeepCost(record.job()) * combinedMult);
            }

            if (totalCost <= 0) continue;

            int bank = eco.getBank(ownerUuid);
            if (bank >= totalCost) {
                eco.setBank(ownerUuid, bank - totalCost);
                // Payment successful - reset missed counter
                upkeep.resetMissedPayments(ownerUuid);
            } else {
                // Insufficient funds - escalating consequences
                upkeep.incrementMissedPayments(ownerUuid);
                int missed = upkeep.getMissedPayments(ownerUuid);
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(ownerUuid);

                if (missed == 1) {
                    // First miss: citizens work at 50% speed (checked via getWorkEfficiency)
                    if (player != null) {
                        player.sendSystemMessage(Component.literal(
                            "\u00A7e\u00A7l\u26A0 Upkeep missed! \u00A77Your citizens are working at \u00A7e50% speed\u00A77 due to unpaid wages.")
                            .withStyle(ChatFormatting.YELLOW));
                    }
                } else if (missed == 2) {
                    // Second miss: citizens stop working entirely
                    if (player != null) {
                        player.sendSystemMessage(Component.literal(
                            "\u00A7c\u00A7l\u26A0 STRIKE! \u00A77Your citizens have \u00A7cstopped working\u00A77! Pay upkeep immediately!")
                            .withStyle(ChatFormatting.RED));
                    }
                } else if (missed >= 3 && missed < 5) {
                    // 3-4 misses: 1 random citizen deserts per cycle
                    desertRandomCitizen(level, cm, ownerUuid, player);
                    if (player != null) {
                        player.sendSystemMessage(Component.literal(
                            "\u00A74\u00A7l\u2620 CRITICAL: \u00A77A citizen has \u00A74deserted\u00A77 your colony! ("
                            + missed + " payments missed)")
                            .withStyle(ChatFormatting.DARK_RED));
                    }
                } else if (missed >= 5) {
                    // 5+ misses: ALL citizens desert, colony dissolved
                    desertAllCitizens(level, cm, ownerUuid, player);
                    if (player != null) {
                        player.sendSystemMessage(Component.literal(
                            "\u00A74\u00A7l\u2620\u2620\u2620 COLONY DISSOLVED! \u00A77All citizens have \u00A74abandoned\u00A77 your colony due to prolonged unpaid upkeep!")
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
                    }
                }
            }
        }
    }

    private static void desertRandomCitizen(ServerLevel level, CitizenManager cm, UUID ownerUuid, ServerPlayer player) {
        List<CitizenManager.CitizenRecord> citizens = new ArrayList<>(cm.getCitizensForOwner(ownerUuid));
        if (citizens.isEmpty()) return;
        Collections.shuffle(citizens);
        CitizenManager.CitizenRecord victim = citizens.get(0);

        // Find and despawn the entity
        despawnCitizenEntity(level, victim.entityId());

        // Unregister from manager
        cm.unregisterCitizen(victim.entityId());

        // Remove from any group
        GroupData group = GroupManager.get(level).getGroupForEntity(victim.entityId());
        if (group != null) {
            group.removeMember(victim.entityId());
        }

        MegaMod.LOGGER.info("Citizen {} ({}) deserted from {}'s colony due to missed upkeep",
            victim.name(), victim.job(), ownerUuid);
    }

    private static void desertAllCitizens(ServerLevel level, CitizenManager cm, UUID ownerUuid, ServerPlayer player) {
        List<CitizenManager.CitizenRecord> citizens = new ArrayList<>(cm.getCitizensForOwner(ownerUuid));
        int count = citizens.size();
        for (CitizenManager.CitizenRecord citizen : citizens) {
            // Find and despawn the entity
            despawnCitizenEntity(level, citizen.entityId());

            // Unregister from manager
            cm.unregisterCitizen(citizen.entityId());

            // Remove from any group
            GroupData group = GroupManager.get(level).getGroupForEntity(citizen.entityId());
            if (group != null) {
                group.removeMember(citizen.entityId());
            }
        }

        MegaMod.LOGGER.warn("All {} citizens deserted from {}'s colony due to 5+ missed upkeep payments",
            count, ownerUuid);
    }

    private static void despawnCitizenEntity(ServerLevel level, UUID entityId) {
        // Direct entity lookup instead of world-wide AABB scan
        net.minecraft.world.entity.Entity entity = level.getEntity(entityId);
        if (entity instanceof MCEntityCitizen citizen) {
            citizen.discard();
        }
    }

    private static void checkCitizenNeeds(ServerLevel level) {
        // Gather per-owner hunger/needs stats
        Map<UUID, Integer> hungryCount = new HashMap<>();
        Map<UUID, Integer> noChestCount = new HashMap<>();

        // Use CitizenManager registry + entity lookup instead of world-wide AABB scan
        CitizenManager cm = CitizenManager.get(level);
        for (var ownerEntry : cm.getAllCitizens().entrySet()) {
            UUID owner = ownerEntry.getKey();
            for (var record : ownerEntry.getValue()) {
                net.minecraft.world.entity.Entity entity = level.getEntity(record.entityId());
                if (!(entity instanceof MCEntityCitizen citizen)) continue;

                // TODO: MCEntityCitizen food handler check — skipped for now
            }
        }

        // Send notifications to online owners
        for (var entry : hungryCount.entrySet()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) continue;

            int hungry = entry.getValue();
            int noChest = noChestCount.getOrDefault(entry.getKey(), 0);

            if (noChest > 0) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "\u00A7e\u00A7l\u26A0 \u00A76" + noChest + " citizen" + (noChest > 1 ? "s are" : " is")
                    + " \u00A7chungy \u00A76and ha" + (noChest > 1 ? "ve" : "s")
                    + " \u00A7cno upkeep chest\u00A76! Assign one via interaction screen."));
            } else if (hungry > 0) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "\u00A7e\u00A7l\u26A0 \u00A76" + hungry + " citizen" + (hungry > 1 ? "s are" : " is")
                    + " \u00A7chungy\u00A76. Make sure upkeep chests have food!"));
            }
        }

        // Also check upkeep payment status
        EconomyManager eco = EconomyManager.get(level);
        var upkeep = cm.getUpkeepTracker();
        if (upkeep.isGlobalUpkeepEnabled()) {
            for (var ownerEntry : cm.getAllCitizens().entrySet()) {
                UUID ownerUuid = ownerEntry.getKey();
                if (upkeep.isExempt(ownerUuid)) continue;
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(ownerUuid);
                if (player == null) continue;

                int totalCost = 0;
                double adminMult = upkeep.getMultiplier(ownerUuid);
                double arcaneMult = CitizenSkillBonuses.getUpkeepMultiplier(level, ownerUuid);
                for (var record : ownerEntry.getValue()) {
                    totalCost += (int) (CitizenConfig.getUpkeepCost(record.job()) * adminMult * arcaneMult);
                }
                int bank = eco.getBank(ownerUuid);
                if (totalCost > 0 && bank < totalCost) {
                    int daysLeft = totalCost > 0 ? bank / totalCost : 0;
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "\u00A7c\u00A7l\u26A0 \u00A76Low upkeep funds! \u00A77Bank: \u00A7a" + bank + " MC \u00A77| Need: \u00A7c"
                        + totalCost + " MC/day \u00A77| ~\u00A7e" + daysLeft + " day" + (daysLeft != 1 ? "s" : "") + " \u00A77left."));
                }
            }
        }
    }

    public static void onCitizenHired(ServerLevel level, ServerPlayer player, CitizenJob job) {
        try {
            DiscoveryManager.get(level).discover(player.getUUID(), "citizen_" + job.name().toLowerCase());
        } catch (Exception ignored) {}
    }
}

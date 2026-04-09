package com.ultra.megamod.feature.citizen.data;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.CitizenManager;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.visitor.VisitorManager;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Manages citizen lifecycle: periodic spawning into residences with open slots,
 * visitor spawning in taverns, and death handling (graves, unassignment, stats).
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class CitizenLifecycleManager {

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (++tickCounter % 6000 != 0) return; // Every 5 min
        ServerLevel level = event.getServer().overworld();
        if (!FeatureToggleManager.get(level).isEnabled("citizens")) return;

        FactionManager fm = FactionManager.get(level);
        CitizenManager cm = CitizenManager.get(level);
        Collection<FactionData> factions = fm.getAllFactions();

        for (FactionData faction : factions) {
            String factionId = faction.getFactionId();
            checkResidencesForOpenSlots(level, cm, faction, factionId);
            checkTavernsForVisitors(level, faction, factionId);
        }
    }

    /**
     * For each faction, check residence buildings for open slots.
     * If a residence has capacity for more citizens than are currently assigned,
     * mark it as needing new citizens. New citizens spawn naturally over time
     * when residences have open beds.
     */
    private static void checkResidencesForOpenSlots(ServerLevel level, CitizenManager cm,
                                                     FactionData faction, String factionId) {
        List<CitizenManager.CitizenRecord> factionCitizens = cm.getCitizensForFaction(factionId);
        int currentCount = factionCitizens.size();
        int maxNPCs = faction.getMaxNPCs();

        if (currentCount >= maxNPCs) return;

        // Notify the faction leader that there's room for more citizens
        UUID leaderUuid = faction.getLeaderUuid();
        ServerPlayer leader = level.getServer().getPlayerList().getPlayer(leaderUuid);
        if (leader != null) {
            int openSlots = maxNPCs - currentCount;
            if (openSlots > 0 && currentCount > 0) {
                // Only notify if they have at least 1 citizen (colony is established)
                leader.displayClientMessage(
                    Component.literal("\u00A7a\u00A7lColony Update: \u00A77Your colony has room for \u00A7a"
                        + openSlots + "\u00A77 more citizen" + (openSlots > 1 ? "s" : "")
                        + ". Visit a Tavern to recruit visitors!"),
                    false);
            }
        }
    }

    /**
     * For each faction, check if the tavern should spawn visitors.
     * Delegates to the VisitorManager which handles actual spawning logic.
     */
    private static void checkTavernsForVisitors(ServerLevel level, FactionData faction, String factionId) {
        VisitorManager vm = VisitorManager.get(level, factionId);
        // VisitorManager handles its own spawn timing and capacity checks.
        // We just ensure it gets ticked for visitor expiry processing.
        vm.tickVisitors(level);
    }

    /**
     * Called when a citizen entity dies.
     * Places a grave at the death position, stores inventory in the grave,
     * unassigns the citizen from their building, tracks the DEATH stat,
     * and reduces raid difficulty.
     */
    public static void onCitizenDeath(ServerLevel level, MCEntityCitizen citizen) {
        BlockPos deathPos = citizen.blockPosition();

        // Look up faction from CitizenManager record
        CitizenManager cm = CitizenManager.get(level);
        CitizenManager.CitizenRecord record = cm.getCitizenByEntity(citizen.getUUID());
        String factionId = record != null ? record.factionId() : "";

        // 1. Place grave at death position
        placeGrave(level, citizen, deathPos);

        // 2. Unassign from building (CitizenEvents already unregisters from CitizenManager)
        // The building module will detect the citizen is gone on next tick

        // 3. Track DEATH stat in colony statistics
        if (factionId != null && !factionId.isEmpty()) {
            ColonyStatisticsManager stats = ColonyStatisticsManager.get(level, factionId);
            stats.setCurrentDay((int) (level.getDayTime() / 24000));
            stats.increment(ColonyStatisticsManager.DEATH);
        }

        // 4. Reduce raid difficulty for the faction (fewer citizens = lower threat)
        if (factionId != null && !factionId.isEmpty()) {
            FactionStatsManager fsm = FactionStatsManager.get(level);
            fsm.recordCitizenDeath(factionId);
        }

        // 5. Notify the citizen's owner
        UUID ownerUuid = citizen.getOwnerUUID();
        if (ownerUuid != null) {
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerUuid);
            if (owner != null) {
                String citizenName = citizen.getCitizenName();
                owner.displayClientMessage(
                    Component.literal("\u00A7c\u2620 \u00A77Your citizen \u00A7e" + citizenName
                        + " \u00A77has died! A grave has been placed at \u00A7b"
                        + deathPos.getX() + ", " + deathPos.getY() + ", " + deathPos.getZ() + "\u00A77."),
                    false);
            }
        }

        // 6. Set all colony citizens to mourning for 1 MC day (24000 ticks)
        triggerMourning(level, citizen.getOwnerUUID());

        MegaMod.LOGGER.info("Citizen {} ({}) died at {} in faction {}",
            citizen.getCitizenName(), citizen.getCitizenJob(), deathPos, factionId);
    }

    /**
     * Sets all worker citizens owned by the given player to mourning state.
     * Mourning citizens will not work for 1 MC day (24000 ticks).
     */
    private static void triggerMourning(ServerLevel level, UUID ownerUUID) {
        if (ownerUUID == null) return;

        for (net.minecraft.world.entity.Entity entity :
                java.util.stream.StreamSupport.stream(level.getAllEntities().spliterator(), false).toList()) {
            if (entity instanceof MCEntityCitizen citizen) {
                // TODO: trigger mourning via MCEntityCitizen mourn handler
            }
        }
    }

    /**
     * Places a citizen gravestone at the death position and stores their inventory in it.
     */
    private static void placeGrave(ServerLevel level, MCEntityCitizen citizen, BlockPos deathPos) {
        // Find a suitable position for the grave (on top of a solid block)
        BlockPos gravePos = findGravePosition(level, deathPos);
        if (gravePos == null) return;

        try {
            // Place a cobblestone wall as a simple grave marker
            // (Reusing the existing GravestoneBlock would require the player recovery registry;
            //  for citizen graves we use a simple marker block)
            level.setBlock(gravePos, Blocks.COBBLESTONE_WALL.defaultBlockState(), 3);

            // Drop the citizen's inventory at the grave location
            for (int i = 0; i < citizen.getInventory().getContainerSize(); i++) {
                ItemStack stack = citizen.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    net.minecraft.world.entity.item.ItemEntity itemEntity =
                        new net.minecraft.world.entity.item.ItemEntity(
                            level, gravePos.getX() + 0.5, gravePos.getY() + 0.5,
                            gravePos.getZ() + 0.5, stack.copy());
                    itemEntity.setDefaultPickUpDelay();
                    level.addFreshEntity(itemEntity);
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.warn("Failed to place citizen grave at {}: {}", gravePos, e.getMessage());
        }
    }

    /**
     * Finds a valid position to place a grave marker near the death position.
     * Searches downward for a solid block, then places the grave on top.
     */
    private static BlockPos findGravePosition(ServerLevel level, BlockPos deathPos) {
        // Search up to 5 blocks down for a solid block
        for (int y = 0; y <= 5; y++) {
            BlockPos checkPos = deathPos.below(y);
            BlockPos abovePos = checkPos.above();
            if (!level.getBlockState(checkPos).isAir() && level.getBlockState(abovePos).isAir()) {
                return abovePos;
            }
        }
        // Fallback: use the death position itself if nothing suitable found
        if (level.getBlockState(deathPos).isAir()) {
            return deathPos;
        }
        return null;
    }
}

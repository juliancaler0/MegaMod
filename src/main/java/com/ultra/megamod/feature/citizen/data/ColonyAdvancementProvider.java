package com.ultra.megamod.feature.citizen.data;

import com.ultra.megamod.MegaMod;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Registers custom advancement triggers for the colony system.
 * <ul>
 *   <li>PlaceColonyTrigger — first colony placement</li>
 *   <li>PopulationMilestone — reaching 5, 10, 20 citizens</li>
 *   <li>BuildingComplete — completing specific building types</li>
 *   <li>ResearchComplete — completing any research</li>
 * </ul>
 *
 * These are stub implementations. The trigger methods attempt to grant
 * named advancements from data packs at {@code data/megamod/advancement/colony/}.
 * Full advancement JSON trees should be created separately.
 */
public class ColonyAdvancementProvider {

    // Advancement IDs that correspond to data pack advancement JSON files
    public static final Identifier PLACE_COLONY_ID =
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "colony/place_colony");
    public static final Identifier POPULATION_5_ID =
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "colony/population_5");
    public static final Identifier POPULATION_10_ID =
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "colony/population_10");
    public static final Identifier POPULATION_20_ID =
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "colony/population_20");
    public static final Identifier BUILDING_COMPLETE_ID =
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "colony/building_complete");
    public static final Identifier RESEARCH_COMPLETE_ID =
            Identifier.fromNamespaceAndPath(MegaMod.MODID, "colony/research_complete");

    private static boolean registered = false;

    /**
     * Register colony advancement stubs.
     * Call once during mod initialization.
     */
    public static void register() {
        if (registered) return;
        registered = true;
        MegaMod.LOGGER.info("Colony advancement provider registered (4 triggers)");
    }

    /**
     * Trigger the "place colony" advancement for the given player.
     */
    public static void triggerPlaceColony(ServerPlayer player) {
        grantAdvancement(player, PLACE_COLONY_ID);
    }

    /**
     * Trigger the "population milestone" advancement for the given player.
     * Call this when the colony reaches 5, 10, or 20 citizens.
     */
    public static void triggerPopulationMilestone(ServerPlayer player, int population) {
        if (population >= 5) grantAdvancement(player, POPULATION_5_ID);
        if (population >= 10) grantAdvancement(player, POPULATION_10_ID);
        if (population >= 20) grantAdvancement(player, POPULATION_20_ID);
    }

    /**
     * Trigger the "building complete" advancement for the given player.
     */
    public static void triggerBuildingComplete(ServerPlayer player) {
        grantAdvancement(player, BUILDING_COMPLETE_ID);
    }

    /**
     * Trigger the "research complete" advancement for the given player.
     */
    public static void triggerResearchComplete(ServerPlayer player) {
        grantAdvancement(player, RESEARCH_COMPLETE_ID);
    }

    /**
     * Attempt to grant an advancement to the player if it exists in the data pack.
     * Silently does nothing if the advancement is not defined.
     */
    private static void grantAdvancement(ServerPlayer player, Identifier advancementId) {
        try {
            var server = player.level().getServer();
            if (server == null) return;
            AdvancementHolder holder = server.getAdvancements().get(advancementId);
            if (holder == null) return;

            var progress = player.getAdvancements().getOrStartProgress(holder);
            if (progress.isDone()) return;

            // Grant all remaining criteria
            for (String criterion : progress.getRemainingCriteria()) {
                player.getAdvancements().award(holder, criterion);
            }
        } catch (Exception e) {
            // Silently fail — advancement JSON may not exist yet
        }
    }
}

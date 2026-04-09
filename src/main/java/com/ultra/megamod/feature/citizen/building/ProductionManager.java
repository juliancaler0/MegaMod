package com.ultra.megamod.feature.citizen.building;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.building.module.WorkerBuildingModule;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.citizen.request.BuildingRequester;
import com.ultra.megamod.feature.citizen.request.RequestManager;
import com.ultra.megamod.feature.citizen.request.types.DeliveryRequest;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

/**
 * Server-side tick handler that processes simplified production for all active colony buildings.
 * <p>
 * Real worker AI provides animation and movement, but ProductionManager ensures output is
 * generated even when AI has pathfinding issues or the worker is idle. This acts as a fallback
 * production system that runs every 5 seconds.
 * <p>
 * For each building with a WorkerBuildingModule:
 * <ul>
 *   <li>Checks if the assigned citizen entity exists and is alive</li>
 *   <li>If the building has crafting recipes, checks ingredients in nearby racks</li>
 *   <li>Consumes ingredients and produces output, depositing to racks</li>
 *   <li>Increments citizen XP and tracks statistics</li>
 * </ul>
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class ProductionManager {

    private static int tickCounter = 0;

    /** How often to process production (every 5 seconds = 100 ticks). */
    private static final int TICK_INTERVAL = 100;

    /** Maximum number of buildings to process per tick cycle to avoid lag spikes. */
    private static final int MAX_BUILDINGS_PER_TICK = 20;

    /** Radius to search for colony buildings around each known position. */
    private static final int BUILDING_SEARCH_RADIUS = 16;

    /** Tracked building positions that have been discovered. */
    private static final Set<Long> knownBuildingPositions = new HashSet<>();

    /** Statistics: total items produced by the production manager. */
    private static int totalItemsProduced = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (++tickCounter % TICK_INTERVAL != 0) return;

        ServerLevel level = event.getServer().overworld();
        if (!FeatureToggleManager.get(level).isEnabled("citizens")) return;

        // Process production for all known colony buildings
        processAllBuildings(level);
    }

    /**
     * Scans and processes all colony buildings in the overworld.
     * Uses a tracked set of known positions plus citizen-based discovery.
     */
    private static void processAllBuildings(ServerLevel level) {
        int processed = 0;

        // Discover buildings near active workers
        discoverBuildingsFromWorkers(level);

        // Process known building positions
        List<Long> positionsToRemove = new ArrayList<>();
        for (Long posLong : knownBuildingPositions) {
            if (processed >= MAX_BUILDINGS_PER_TICK) break;

            BlockPos pos = BlockPos.of(posLong);

            // Verify the building still exists
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof TileEntityColonyBuilding tile)) {
                positionsToRemove.add(posLong);
                continue;
            }

            processProductionBuilding(level, tile);
            processed++;
        }

        // Clean up invalid positions
        for (Long pos : positionsToRemove) {
            knownBuildingPositions.remove(pos);
        }
    }

    /**
     * Discovers colony buildings by scanning the area around active worker citizens.
     * This is how the production manager learns about new buildings without needing
     * to scan the entire world.
     */
    private static void discoverBuildingsFromWorkers(ServerLevel level) {
        // Find all citizen entities in loaded chunks
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen && citizen.isAlive()) {
                BlockPos startPos = citizen.blockPosition();
                // Scan around the citizen's position for colony buildings
                for (int x = -BUILDING_SEARCH_RADIUS; x <= BUILDING_SEARCH_RADIUS; x += 4) {
                    for (int z = -BUILDING_SEARCH_RADIUS; z <= BUILDING_SEARCH_RADIUS; z += 4) {
                        for (int y = -4; y <= 4; y += 2) {
                            BlockPos searchPos = startPos.offset(x, y, z);
                            if (knownBuildingPositions.contains(searchPos.asLong())) continue;
                            BlockEntity be = level.getBlockEntity(searchPos);
                            if (be instanceof TileEntityColonyBuilding) {
                                knownBuildingPositions.add(searchPos.asLong());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Processes a single production building. Looks up its building type,
     * checks for assigned workers, and runs one production cycle if ingredients
     * are available in nearby racks.
     *
     * @param level the server level
     * @param tile  the colony building tile entity
     */
    private static void processProductionBuilding(ServerLevel level, TileEntityColonyBuilding tile) {
        String buildingId = tile.getBuildingId();
        if (buildingId == null || buildingId.isEmpty()) return;

        BuildingEntry entry = BuildingRegistry.get(buildingId);
        if (entry == null) return;

        // Create a temporary building instance to access its modules
        AbstractBuilding building = entry.buildingFactory().get();
        building.registerModulesPublic();

        // Check if this building has a worker module
        Optional<WorkerBuildingModule> workerModule = building.getModule(WorkerBuildingModule.class);
        if (workerModule.isEmpty()) return;

        // Check if there's an assigned citizen nearby and alive
        BlockPos buildingPos = tile.getBlockPos();
        MCEntityCitizen assignedCitizen = findAssignedCitizenNear(level, buildingPos, workerModule.get());
        if (assignedCitizen == null) return;

        // Only produce if the citizen is alive
        if (!assignedCitizen.isAlive()) return;

        // Run a simplified production cycle based on building type
        runSimpleProduction(level, tile, building, assignedCitizen);
    }

    /**
     * Finds an assigned citizen near the building position.
     */
    private static MCEntityCitizen findAssignedCitizenNear(
            ServerLevel level, BlockPos buildingPos, WorkerBuildingModule workerModule) {

        AABB searchArea = new AABB(buildingPos).inflate(64);
        List<MCEntityCitizen> citizens = level.getEntitiesOfClass(
                MCEntityCitizen.class, searchArea);

        for (MCEntityCitizen citizen : citizens) {
            if (!citizen.isAlive()) continue;
            // Match by job type
            var jobHandler = citizen.getCitizenJobHandler();
            if (jobHandler.getColonyJob() == workerModule.getJobType()) {
                // Check if citizen is near this building
                if (citizen.blockPosition().distSqr(buildingPos) <= 256) { // 16 blocks
                    return citizen;
                }
            }
        }
        return null;
    }

    /**
     * Runs a simplified production cycle for a building.
     * Uses hardcoded production rules based on building type.
     * Checks for ingredients in nearby racks and produces output.
     * This is a fallback -- the real AI handles the animation/movement.
     */
    private static void runSimpleProduction(
            ServerLevel level, TileEntityColonyBuilding tile,
            AbstractBuilding building, MCEntityCitizen citizen) {

        BlockPos buildingPos = tile.getBlockPos();
        String buildingId = building.getBuildingId();
        processDefaultProduction(level, buildingPos, buildingId, citizen);
    }

    /**
     * Production rules for common building types.
     * Each building type has simple input->output conversions that model
     * the real crafting the worker AI would perform.
     * <p>
     * When ingredients are missing from nearby racks, auto-creates delivery
     * requests via the RequestManager so the colony's delivery system can
     * fulfill them.
     */
    private static void processDefaultProduction(
            ServerLevel level, BlockPos buildingPos,
            String buildingId, MCEntityCitizen citizen) {

        switch (buildingId) {
            case "baker" -> {
                // Baker: wheat -> bread
                if (BuildingWorkHelper.hasItem(level, buildingPos, Items.WHEAT, 3)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.WHEAT, 3);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.BREAD));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else {
                    requestIfMissing(level, buildingPos, Items.WHEAT, 3);
                }
            }
            case "smeltery" -> {
                // Smelter: raw iron -> iron ingot
                if (BuildingWorkHelper.hasItem(level, buildingPos, Items.RAW_IRON, 1)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.RAW_IRON, 1);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.IRON_INGOT));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else if (BuildingWorkHelper.hasItem(level, buildingPos, Items.RAW_GOLD, 1)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.RAW_GOLD, 1);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.GOLD_INGOT));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else if (BuildingWorkHelper.hasItem(level, buildingPos, Items.RAW_COPPER, 1)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.RAW_COPPER, 1);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.COPPER_INGOT));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else {
                    requestIfMissing(level, buildingPos, Items.RAW_IRON, 1);
                }
            }
            case "stone_smeltery" -> {
                // Stone smelter: cobblestone -> stone
                if (BuildingWorkHelper.hasItem(level, buildingPos, Items.COBBLESTONE, 1)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.COBBLESTONE, 1);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.STONE));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else {
                    requestIfMissing(level, buildingPos, Items.COBBLESTONE, 1);
                }
            }
            case "blacksmith" -> {
                // Blacksmith: 2 iron ingots -> iron sword
                if (BuildingWorkHelper.hasItem(level, buildingPos, Items.IRON_INGOT, 2)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.IRON_INGOT, 2);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.IRON_SWORD));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else {
                    requestIfMissing(level, buildingPos, Items.IRON_INGOT, 2);
                }
            }
            case "sawmill" -> {
                // Sawmill: logs -> planks
                if (BuildingWorkHelper.hasItem(level, buildingPos, Items.OAK_LOG, 1)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.OAK_LOG, 1);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.OAK_PLANKS, 4));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else {
                    requestIfMissing(level, buildingPos, Items.OAK_LOG, 1);
                }
            }
            case "stonemason" -> {
                // Stonemason: stone -> stone bricks
                if (BuildingWorkHelper.hasItem(level, buildingPos, Items.STONE, 4)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.STONE, 4);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.STONE_BRICKS, 4));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else {
                    requestIfMissing(level, buildingPos, Items.STONE, 4);
                }
            }
            case "crusher" -> {
                // Crusher: cobblestone -> gravel
                if (BuildingWorkHelper.hasItem(level, buildingPos, Items.COBBLESTONE, 1)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.COBBLESTONE, 1);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.GRAVEL));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else {
                    requestIfMissing(level, buildingPos, Items.COBBLESTONE, 1);
                }
            }
            case "cook", "kitchen" -> {
                // Cook/Kitchen: raw beef -> cooked beef
                if (BuildingWorkHelper.hasItem(level, buildingPos, Items.BEEF, 1)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.BEEF, 1);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.COOKED_BEEF));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else if (BuildingWorkHelper.hasItem(level, buildingPos, Items.PORKCHOP, 1)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.PORKCHOP, 1);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.COOKED_PORKCHOP));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else if (BuildingWorkHelper.hasItem(level, buildingPos, Items.CHICKEN, 1)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.CHICKEN, 1);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.COOKED_CHICKEN));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else {
                    requestIfMissing(level, buildingPos, Items.BEEF, 1);
                }
            }
            case "dyer" -> {
                // Dyer: white wool + any dye -> colored wool (simplified)
                if (BuildingWorkHelper.hasItem(level, buildingPos, Items.WHITE_WOOL, 1)
                        && BuildingWorkHelper.hasItem(level, buildingPos, Items.RED_DYE, 1)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.WHITE_WOOL, 1);
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.RED_DYE, 1);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.RED_WOOL));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else {
                    if (!BuildingWorkHelper.hasItem(level, buildingPos, Items.WHITE_WOOL, 1)) {
                        requestIfMissing(level, buildingPos, Items.WHITE_WOOL, 1);
                    }
                    if (!BuildingWorkHelper.hasItem(level, buildingPos, Items.RED_DYE, 1)) {
                        requestIfMissing(level, buildingPos, Items.RED_DYE, 1);
                    }
                }
            }
            case "fletcher" -> {
                // Fletcher: sticks + feathers + flint -> arrows
                if (BuildingWorkHelper.hasItem(level, buildingPos, Items.STICK, 1)
                        && BuildingWorkHelper.hasItem(level, buildingPos, Items.FEATHER, 1)
                        && BuildingWorkHelper.hasItem(level, buildingPos, Items.FLINT, 1)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.STICK, 1);
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.FEATHER, 1);
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.FLINT, 1);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.ARROW, 4));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else {
                    if (!BuildingWorkHelper.hasItem(level, buildingPos, Items.STICK, 1)) {
                        requestIfMissing(level, buildingPos, Items.STICK, 1);
                    }
                    if (!BuildingWorkHelper.hasItem(level, buildingPos, Items.FEATHER, 1)) {
                        requestIfMissing(level, buildingPos, Items.FEATHER, 1);
                    }
                    if (!BuildingWorkHelper.hasItem(level, buildingPos, Items.FLINT, 1)) {
                        requestIfMissing(level, buildingPos, Items.FLINT, 1);
                    }
                }
            }
            case "glassblower" -> {
                // Glassblower: sand -> glass
                if (BuildingWorkHelper.hasItem(level, buildingPos, Items.SAND, 1)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.SAND, 1);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.GLASS));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else {
                    requestIfMissing(level, buildingPos, Items.SAND, 1);
                }
            }
            case "concrete_mixer" -> {
                // Concrete mixer: concrete powder -> concrete (simplified, needs water in real life)
                if (BuildingWorkHelper.hasItem(level, buildingPos, Items.WHITE_CONCRETE_POWDER, 1)) {
                    BuildingWorkHelper.takeItem(level, buildingPos, Items.WHITE_CONCRETE_POWDER, 1);
                    BuildingWorkHelper.depositItem(level, buildingPos, new ItemStack(Items.WHITE_CONCRETE));
                    // TODO: increment citizen production stats via MCEntityCitizen handler
                    totalItemsProduced++;
                } else {
                    requestIfMissing(level, buildingPos, Items.WHITE_CONCRETE_POWDER, 1);
                }
            }
            // Other building types (farmer, miner, lumberjack, etc.) are handled by their
            // specific AI goals and don't need fallback production since they work on
            // world blocks rather than inventory-to-inventory crafting.
            default -> {
                // No default production for unknown building types
            }
        }
    }

    /**
     * Creates a delivery request for a missing ingredient at a building position.
     * Only creates a request if there isn't already an active request for the
     * same item at this building to avoid request spam.
     *
     * @param level       the server level
     * @param buildingPos the building block position
     * @param item        the item needed
     * @param count       the count needed
     */
    private static void requestIfMissing(ServerLevel level, BlockPos buildingPos, Item item, int count) {
        BlockEntity be = level.getBlockEntity(buildingPos);
        if (!(be instanceof TileEntityColonyBuilding tile)) return;

        // Check for existing active requests for the same item at this building to avoid duplicates
        RequestManager requestManager = RequestManager.get(level);
        ItemStack neededStack = new ItemStack(item, count);
        for (var req : requestManager.getAllActiveRequests()) {
            if (req.getRequester().getRequesterPosition().equals(buildingPos)
                    && req.getRequestable().matchesItem(neededStack)) {
                // Already has an active request for this item at this building, skip
                return;
            }
        }

        BuildingRequester requester = new BuildingRequester(tile);
        DeliveryRequest deliveryRequest = new DeliveryRequest(neededStack, count);
        requestManager.createRequest(requester, deliveryRequest);
    }

    /**
     * Returns the total number of items produced by the production manager since server start.
     * Useful for admin panel statistics.
     *
     * @return total items produced
     */
    public static int getTotalItemsProduced() {
        return totalItemsProduced;
    }

    /**
     * Returns the number of tracked building positions.
     *
     * @return known building count
     */
    public static int getKnownBuildingCount() {
        return knownBuildingPositions.size();
    }

    /**
     * Resets all tracking data. Called on server stop.
     */
    public static void reset() {
        tickCounter = 0;
        totalItemsProduced = 0;
        knownBuildingPositions.clear();
    }
}

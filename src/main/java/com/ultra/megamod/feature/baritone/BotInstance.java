package com.ultra.megamod.feature.baritone;

import com.ultra.megamod.feature.baritone.behavior.InventoryBehavior;
import com.ultra.megamod.feature.baritone.behavior.LookBehavior;
import com.ultra.megamod.feature.baritone.goals.*;
import com.ultra.megamod.feature.baritone.pathfinding.*;
import com.ultra.megamod.feature.baritone.process.*;
import com.ultra.megamod.feature.baritone.safety.InventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.LevelData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Per-player bot instance. Holds pathfinding state, processes, and settings.
 */
public class BotInstance {
    private final ServerPlayer player;
    private final BotSettings settings = new BotSettings();
    private final ProcessManager processManager = new ProcessManager();
    private final List<String> actionLog = Collections.synchronizedList(new ArrayList<>());

    // Behaviors
    private final LookBehavior lookBehavior;
    private final InventoryBehavior inventoryBehavior;

    // Pathfinding state
    private PathExecutor currentExecutor;
    private CompletableFuture<ServerPath> pathFuture;
    private Goal currentGoal;
    private boolean paused = false;
    private int ticksSinceStart = 0;
    private int pathRecalcCooldown = 0;
    private ServerPath previousPath; // For favoring system

    // Statistics
    private int blocksMined = 0;
    private int cropsHarvested = 0;
    private int blocksPlaced = 0;
    private int kills = 0;

    // Processes
    private final GoToProcess goToProcess = new GoToProcess();
    private final MineProcess mineProcess = new MineProcess();
    private final FollowProcess followProcess = new FollowProcess();
    private final FarmProcess farmProcess = new FarmProcess();
    private final ExploreProcess exploreProcess = new ExploreProcess();
    private final BuildProcess buildProcess = new BuildProcess();
    private final PatrolProcess patrolProcess = new PatrolProcess();
    private final GetToBlockProcess getToBlockProcess = new GetToBlockProcess();
    private final TunnelProcess tunnelProcess = new TunnelProcess();
    private final QuarryProcess quarryProcess = new QuarryProcess();
    private final CombatProcess combatProcess = new CombatProcess();
    private final ChestProcess chestProcess = new ChestProcess();
    private final BackfillProcess backfillProcess = new BackfillProcess();
    private final ElytraProcess elytraProcess = new ElytraProcess();

    public BotInstance(ServerPlayer player) {
        this.player = player;
        this.lookBehavior = new LookBehavior(player);
        this.inventoryBehavior = new InventoryBehavior(player);
        processManager.register(goToProcess);
        processManager.register(mineProcess);
        processManager.register(followProcess);
        processManager.register(farmProcess);
        processManager.register(exploreProcess);
        processManager.register(buildProcess);
        processManager.register(patrolProcess);
        processManager.register(getToBlockProcess);
        processManager.register(tunnelProcess);
        processManager.register(quarryProcess);
        processManager.register(combatProcess);
        processManager.register(chestProcess);
        processManager.register(backfillProcess);
        processManager.register(elytraProcess);
        // Wire block break listeners so tunnel/quarry notify backfill
        tunnelProcess.setBlockBreakListener(backfillProcess::recordBrokenBlock);
        quarryProcess.setBlockBreakListener(backfillProcess::recordBrokenBlock);
        log("Bot initialized for " + player.getGameProfile().name());
    }

    /**
     * Tick the bot — called every server tick.
     */
    public void tick() {
        if (paused || player.isRemoved()) return;
        ticksSinceStart++;
        if (pathRecalcCooldown > 0) pathRecalcCooldown--;

        ServerLevel level = (ServerLevel) player.level();

        // Update process state
        mineProcess.updatePlayerPos(player.blockPosition());
        mineProcess.updateLevel(level);
        farmProcess.updateState(level, player.blockPosition());
        buildProcess.updateLevel(level);
        getToBlockProcess.updatePlayerPos(player.blockPosition());
        getToBlockProcess.updateLevel(level);
        quarryProcess.updateLevel(level);
        combatProcess.updateState(player, level);
        chestProcess.updateState(player, level);
        backfillProcess.updateState(player, level);
        elytraProcess.updateState(player, level);

        // Get current command from process manager
        boolean calcFailed = currentExecutor != null && currentExecutor.getState() == PathExecutor.State.FAILED;
        boolean safeToCancel = currentExecutor == null || currentExecutor.getState() != PathExecutor.State.RUNNING;
        BotProcess.PathingCommand command = processManager.tick(calcFailed, safeToCancel);

        if (command == null) {
            // No active process — stop movement and zero velocity
            if (currentExecutor != null) {
                currentExecutor = null;
                currentGoal = null;
                player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                player.hurtMarked = true;
            }
            return;
        }

        // Handle command type
        switch (command.type()) {
            case CANCEL -> {
                currentExecutor = null;
                currentGoal = null;
                if (pathFuture != null) pathFuture.cancel(true);
            }
            case REQUEST_PAUSE -> {
                // Do nothing — let current path continue
            }
            case SET_GOAL_AND_PATH, FORCE_REVALIDATE -> {
                Goal newGoal = command.goal();
                if (newGoal == null) return;
                boolean needsRepath = command.type() == BotProcess.PathingCommand.CommandType.FORCE_REVALIDATE
                    || currentGoal != newGoal
                    || currentExecutor == null
                    || currentExecutor.getState() != PathExecutor.State.RUNNING;

                currentGoal = newGoal;

                if (needsRepath && pathRecalcCooldown <= 0) {
                    startPathfinding(level, newGoal);
                    pathRecalcCooldown = 20; // Don't recalc more than once per second
                }
            }
        }

        // Check if pathfinding completed
        if (pathFuture != null && pathFuture.isDone()) {
            try {
                ServerPath path = pathFuture.join();
                if (path != null) {
                    previousPath = path; // Store for favoring
                    currentExecutor = new PathExecutor(path);
                    log("Path found: " + path.length() + " nodes (" + path.getNodesExplored() + " explored)");
                } else {
                    log("No path found!");
                }
            } catch (Exception e) {
                log("Pathfinding error: " + e.getMessage());
            }
            pathFuture = null;
        }

        // Have the bot look at its next waypoint for natural head movement
        if (currentExecutor != null && currentExecutor.getState() == PathExecutor.State.RUNNING) {
            BetterBlockPos nextTarget = currentExecutor.getCurrentTarget();
            if (nextTarget != null) {
                lookBehavior.lookAt(new BlockPos(nextTarget.x, nextTarget.y, nextTarget.z));
            }
        }

        // Tick look behavior (smooth head rotation)
        lookBehavior.tick();

        // Execute current path
        if (currentExecutor != null) {
            PathExecutor.State result = currentExecutor.tick(player, level);
            if (result == PathExecutor.State.FINISHED) {
                handlePathComplete(level);
            } else if (result == PathExecutor.State.FAILED) {
                log("Path execution failed, replanning...");
                currentExecutor = null;
            }
        }
    }

    private void handlePathComplete(ServerLevel level) {
        // Check which process was active and handle completion
        BotProcess active = processManager.getActiveProcess();
        if (active == goToProcess) {
            if (currentGoal != null && currentGoal.isInGoal(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ())) {
                goToProcess.markComplete();
                log("Arrived at destination!");
            }
        } else if (active == mineProcess) {
            // Try to break the target block adjacent to us
            BlockPos bp = player.blockPosition();
            Block target = mineProcess.getTargetBlock();
            boolean mined = false;
            if (target != null) {
                for (int dx = -1; dx <= 1 && !mined; dx++) {
                    for (int dy = -1; dy <= 1 && !mined; dy++) {
                        for (int dz = -1; dz <= 1 && !mined; dz++) {
                            BlockPos check = bp.offset(dx, dy, dz);
                            if (level.getBlockState(check).getBlock() == target) {
                                // Select the best tool for this block and look at it
                                inventoryBehavior.selectBestToolFor(level.getBlockState(check));
                                lookBehavior.lookAt(check);
                                level.destroyBlock(check, true, player);
                                mineProcess.onBlockMined();
                                backfillProcess.recordBrokenBlock(check);
                                blocksMined++;
                                log("Mined block at " + check.getX() + "," + check.getY() + "," + check.getZ());
                                mined = true;
                            }
                        }
                    }
                }
            }
        } else if (active == farmProcess) {
            // Harvest crop adjacent to us
            BlockPos bp = player.blockPosition();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos check = bp.offset(dx, 0, dz);
                    if (isCrop(level, check)) {
                        level.destroyBlock(check, true, player);
                        farmProcess.onCropHarvested();
                        cropsHarvested++;
                        log("Harvested crop at " + check.getX() + "," + check.getY() + "," + check.getZ());
                        // Try to replant
                        farmProcess.tryReplant(level, check, player);
                    }
                }
            }
        } else if (active == buildProcess) {
            // Place block adjacent to us
            BlockPos bp = player.blockPosition();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos check = bp.offset(dx, dy, dz);
                        if (level.getBlockState(check).isAir() && isInBuildArea(check)) {
                            level.setBlockAndUpdate(check, buildProcess.getMaterial().defaultBlockState());
                            buildProcess.onBlockPlaced();
                            blocksPlaced++;
                            log("Placed block at " + check.getX() + "," + check.getY() + "," + check.getZ());
                            break;
                        }
                    }
                }
            }
        } else if (active == patrolProcess) {
            BlockPos target = patrolProcess.getCurrentTarget();
            if (target != null) {
                double dist = player.blockPosition().distSqr(target);
                if (dist <= 4) {
                    patrolProcess.onWaypointReached();
                    log("Reached waypoint");
                }
            }
        } else if (active == exploreProcess) {
            int cx = player.blockPosition().getX() >> 4;
            int cz = player.blockPosition().getZ() >> 4;
            exploreProcess.markChunkVisited(cx, cz);
        } else if (active == getToBlockProcess) {
            if (currentGoal != null && currentGoal.isInGoal(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ())) {
                getToBlockProcess.markComplete();
                log("Arrived at target block!");
            }
        } else if (active == quarryProcess) {
            // Quarry handles its own block breaking in onTick, just log progress
            log("Quarry progress: " + quarryProcess.getStatus());
        } else if (active == combatProcess) {
            // Combat handles its own attacking in onTick
            log("Combat: " + combatProcess.getStatus());
        } else if (active == chestProcess) {
            // Chest process handles transfers in onTick when adjacent
            log("Chest: " + chestProcess.getStatus());
        }

        currentExecutor = null;
    }

    private boolean isCrop(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.CropBlock;
    }

    private boolean isInBuildArea(BlockPos pos) {
        BlockPos origin = buildProcess.getOrigin();
        if (origin == null) return false;
        int dx = pos.getX() - origin.getX();
        int dy = pos.getY() - origin.getY();
        int dz = pos.getZ() - origin.getZ();
        return dx >= 0 && dx < buildProcess.getWidth()
            && dy >= 0 && dy < buildProcess.getHeight()
            && dz >= 0 && dz < buildProcess.getDepth();
    }

    private void startPathfinding(ServerLevel level, Goal goal) {
        if (pathFuture != null && !pathFuture.isDone()) {
            pathFuture.cancel(true);
        }
        BetterBlockPos start = new BetterBlockPos(player.blockPosition());
        Avoidance avoidance = settings.avoidMobs
            ? Avoidance.create(level, player.blockPosition(), settings.mobAvoidRadius * 2)
            : Avoidance.empty();
        CalculationContext ctx = new CalculationContext(level, player, settings, avoidance);
        // Snapshot block states on the server thread BEFORE going async
        ctx.blockAccess.snapshotAround(player.blockPosition());
        // Create favoring from previous path for smoother paths
        Favoring favoring = previousPath != null ? new Favoring(previousPath, 30) : new Favoring();
        pathFuture = CompletableFuture.supplyAsync(() -> {
            AStarPathFinder finder = new AStarPathFinder(start, goal, ctx, favoring);
            return finder.calculate();
        });
    }

    // === Command handling ===

    /** Reset pathfinding state when starting a new command to prevent stale paths */
    private void resetPathfindingState() {
        if (pathFuture != null && !pathFuture.isDone()) {
            pathFuture.cancel(true);
        }
        pathFuture = null;
        currentExecutor = null;
        currentGoal = null;
        previousPath = null;
        pathRecalcCooldown = 0;
    }

    public String executeCommand(String cmd, ServerLevel level) {
        String[] parts = cmd.trim().split("\\s+");
        if (parts.length == 0) return "Empty command";

        switch (parts[0].toLowerCase()) {
            case "goto" -> {
                if (parts.length == 4) {
                    try {
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        int z = Integer.parseInt(parts[3]);
                        processManager.cancelAll();
                        resetPathfindingState();
                        goToProcess.setGoal(x, y, z);
                        log("GoTo: " + x + ", " + y + ", " + z);
                        return "Going to " + x + ", " + y + ", " + z;
                    } catch (NumberFormatException e) {
                        return "Invalid coordinates";
                    }
                } else if (parts.length == 3) {
                    try {
                        int x = Integer.parseInt(parts[1]);
                        int z = Integer.parseInt(parts[2]);
                        processManager.cancelAll();
                        resetPathfindingState();
                        goToProcess.setGoalXZ(x, z);
                        log("GoTo XZ: " + x + ", " + z);
                        return "Going to " + x + ", " + z;
                    } catch (NumberFormatException e) {
                        return "Invalid coordinates";
                    }
                }
                return "Usage: goto <x> <y> <z> or goto <x> <z>";
            }
            case "mine" -> {
                try {
                    String block = parts.length > 1 ? parts[1] : "diamond_ore";
                    int count = parts.length > 2 ? Integer.parseInt(parts[2]) : 64;
                    processManager.cancelAll();
                    resetPathfindingState();
                    mineProcess.start(block, count, level, player.blockPosition(), settings.mineRadius, settings.mineScanInterval);
                    log("Mining: " + block + " x" + count);
                    return "Mining " + block + " (up to " + count + ")";
                } catch (NumberFormatException e) { return "Invalid count number"; }
            }
            case "follow" -> {
                if (parts.length < 2) return "Usage: follow <player>";
                String targetName = parts[1];
                ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
                if (target == null) return "Player not found: " + targetName;
                processManager.cancelAll();
                resetPathfindingState();
                followProcess.start(target, settings.followDistance, settings.followSprint);
                log("Following: " + targetName);
                return "Following " + targetName;
            }
            case "farm" -> {
                try {
                    int radius = parts.length > 1 ? Integer.parseInt(parts[1]) : settings.farmRadius;
                    processManager.cancelAll();
                    resetPathfindingState();
                    farmProcess.start(radius, level, player.blockPosition(), settings.autoReplant, settings.useBoneMeal, settings.farmScanInterval);
                    log("Farming radius: " + radius);
                    return "Farming in radius " + radius;
                } catch (NumberFormatException e) { return "Invalid radius number"; }
            }
            case "explore" -> {
                try {
                    int ox = parts.length > 1 ? Integer.parseInt(parts[1]) : player.blockPosition().getX();
                    int oz = parts.length > 2 ? Integer.parseInt(parts[2]) : player.blockPosition().getZ();
                    processManager.cancelAll();
                    resetPathfindingState();
                    exploreProcess.start(ox, oz, settings.exploreChunkRadius, settings.spiralExpansion);
                    log("Exploring from: " + ox + ", " + oz);
                    return "Exploring from " + ox + ", " + oz;
                } catch (NumberFormatException e) { return "Invalid coordinates"; }
            }
            case "build" -> {
                if (parts.length < 5) return "Usage: build <w> <h> <d> <block>";
                try {
                    int w = Math.min(Math.max(Integer.parseInt(parts[1]), 1), settings.maxBuildRadius);
                    int h = Math.min(Math.max(Integer.parseInt(parts[2]), 1), settings.maxBuildRadius);
                    int d = Math.min(Math.max(Integer.parseInt(parts[3]), 1), settings.maxBuildRadius);
                    String block = parts[4];
                    processManager.cancelAll();
                    resetPathfindingState();
                    buildProcess.start(player.blockPosition(), w, h, d, block, level);
                    buildProcess.configure(settings.buildInLayers, settings.buildBottomUp, settings.skipMatchingBlocks);
                    log("Building: " + w + "x" + h + "x" + d + " " + block);
                    return "Building " + w + "x" + h + "x" + d + " with " + block;
                } catch (NumberFormatException e) { return "Invalid dimensions"; }
            }
            case "patrol" -> {
                if (parts.length < 7) return "Usage: patrol <x1> <y1> <z1> <x2> <y2> <z2> [...]";
                try {
                    List<BlockPos> waypoints = new ArrayList<>();
                    for (int i = 1; i + 2 < parts.length; i += 3) {
                        waypoints.add(new BlockPos(
                            Integer.parseInt(parts[i]),
                            Integer.parseInt(parts[i + 1]),
                            Integer.parseInt(parts[i + 2])
                        ));
                    }
                    processManager.cancelAll();
                    resetPathfindingState();
                    patrolProcess.start(waypoints, settings.waypointPauseTicks, settings.loopPatrol, false);
                    log("Patrolling " + waypoints.size() + " waypoints");
                    return "Patrolling " + waypoints.size() + " waypoints";
                } catch (NumberFormatException e) { return "Invalid waypoint coordinates"; }
            }
            case "gotoblock" -> {
                if (parts.length < 2) return "Usage: gotoblock <blocktype> [radius]";
                String blockType = parts[1];
                int radius = parts.length > 2 ? Integer.parseInt(parts[2]) : 64;
                processManager.cancelAll();
                resetPathfindingState();
                getToBlockProcess.start(blockType, level, player.blockPosition(), radius);
                log("GoToBlock: " + blockType);
                return "Searching for " + blockType;
            }
            case "tunnel" -> {
                if (parts.length < 3) return "Usage: tunnel <direction> <length> [width] [height]";
                try {
                    Direction dir = parseDirection(parts[1]);
                    if (dir == null) return "Invalid direction: " + parts[1] + " (use: north/south/east/west)";
                    int length = Integer.parseInt(parts[2]);
                    int w = parts.length > 3 ? Integer.parseInt(parts[3]) : 1;
                    int h = parts.length > 4 ? Integer.parseInt(parts[4]) : 2;
                    processManager.cancelAll();
                    resetPathfindingState();
                    tunnelProcess.start(dir, length, w, h, player.blockPosition(), level);
                    log("Tunnel: " + dir.getName() + " " + length + "m");
                    return "Tunneling " + dir.getName() + " for " + length + " blocks (" + w + "x" + h + ")";
                } catch (NumberFormatException e) { return "Invalid number"; }
            }
            case "waypoint" -> {
                if (parts.length < 2) return "Usage: waypoint save/goto/list/delete <name>";
                String subCmd = parts[1].toLowerCase();
                switch (subCmd) {
                    case "save" -> {
                        if (parts.length < 3) return "Usage: waypoint save <name>";
                        String name = parts[2];
                        WaypointManager.save(name, player.getUUID(), player.blockPosition());
                        log("Waypoint saved: " + name);
                        return "Saved waypoint '" + name + "' at " + player.blockPosition().getX() + "," + player.blockPosition().getY() + "," + player.blockPosition().getZ();
                    }
                    case "goto" -> {
                        if (parts.length < 3) return "Usage: waypoint goto <name>";
                        String name = parts[2];
                        BlockPos wp = WaypointManager.get(name, player.getUUID());
                        if (wp == null) return "Waypoint not found: " + name;
                        processManager.cancelAll();
                        resetPathfindingState();
                        goToProcess.setGoal(wp.getX(), wp.getY(), wp.getZ());
                        log("GoTo waypoint: " + name);
                        return "Going to waypoint '" + name + "' at " + wp.getX() + "," + wp.getY() + "," + wp.getZ();
                    }
                    case "list" -> {
                        Map<String, BlockPos> wps = WaypointManager.list(player.getUUID());
                        if (wps.isEmpty()) return "No waypoints saved";
                        StringBuilder sb = new StringBuilder("Waypoints: ");
                        wps.forEach((n, p) -> sb.append(n).append("(").append(p.getX()).append(",").append(p.getY()).append(",").append(p.getZ()).append(") "));
                        return sb.toString();
                    }
                    case "delete" -> {
                        if (parts.length < 3) return "Usage: waypoint delete <name>";
                        String name = parts[2];
                        if (WaypointManager.delete(name, player.getUUID())) {
                            log("Waypoint deleted: " + name);
                            return "Deleted waypoint '" + name + "'";
                        }
                        return "Waypoint not found: " + name;
                    }
                    default -> {
                        return "Usage: waypoint save/goto/list/delete <name>";
                    }
                }
            }
            case "scan" -> {
                if (parts.length < 2) return "Usage: scan <blocktype> [radius]";
                String blockType = parts[1];
                int radius = parts.length > 2 ? Integer.parseInt(parts[2]) : 32;
                net.minecraft.resources.Identifier loc = net.minecraft.resources.Identifier.parse(
                    blockType.contains(":") ? blockType : "minecraft:" + blockType);
                net.minecraft.world.level.block.Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(loc);
                if (block == null || block == net.minecraft.world.level.block.Blocks.AIR) return "Unknown block: " + blockType;
                WorldScanner scanner = new WorldScanner(level);
                List<BlockPos> found = scanner.scan(player.blockPosition(), block, radius, 10);
                if (found.isEmpty()) return "No " + blockType + " found within " + radius + " blocks";
                StringBuilder sb = new StringBuilder("Found " + found.size() + " " + blockType + ": ");
                for (BlockPos p : found) {
                    sb.append("(").append(p.getX()).append(",").append(p.getY()).append(",").append(p.getZ()).append(") ");
                }
                return sb.toString();
            }
            case "inventory" -> {
                return InventoryHelper.getInventorySummary(player);
            }
            case "come" -> {
                // Navigate to the admin who sent this command — find first admin online
                for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
                    if (com.ultra.megamod.feature.computer.admin.AdminSystem.isAdmin(p) && p != player) {
                        processManager.cancelAll();
                        resetPathfindingState();
                        goToProcess.setGoal(p.blockPosition().getX(), p.blockPosition().getY(), p.blockPosition().getZ());
                        log("Coming to admin: " + p.getGameProfile().name());
                        return "Coming to " + p.getGameProfile().name();
                    }
                }
                return "No admin found to come to";
            }
            case "surface" -> {
                // Find highest solid block above player
                int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                    player.blockPosition().getX(), player.blockPosition().getZ());
                processManager.cancelAll();
                resetPathfindingState();
                goToProcess.setGoal(player.blockPosition().getX(), surfaceY, player.blockPosition().getZ());
                log("Going to surface: Y=" + surfaceY);
                return "Going to surface (Y=" + surfaceY + ")";
            }
            case "home" -> {
                // Go to bed/respawn location, or world spawn as fallback
                BlockPos spawn = null;
                ServerPlayer.RespawnConfig respawnConfig = player.getRespawnConfig();
                if (respawnConfig != null) {
                    spawn = respawnConfig.respawnData().pos();
                }
                if (spawn == null) {
                    // Fallback to world spawn
                    ServerLevel overworld = level.getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);
                    if (overworld != null) {
                        spawn = overworld.getRespawnData().pos();
                    }
                }
                if (spawn == null) {
                    return "No spawn/home location found";
                }
                processManager.cancelAll();
                resetPathfindingState();
                goToProcess.setGoal(spawn.getX(), spawn.getY(), spawn.getZ());
                log("Going home: " + spawn.getX() + "," + spawn.getY() + "," + spawn.getZ());
                return "Going home to " + spawn.getX() + "," + spawn.getY() + "," + spawn.getZ();
            }
            case "eta" -> {
                if (currentExecutor == null) return "No active path";
                int ticksRemaining = currentExecutor.getEstimatedTicksRemaining();
                int seconds = ticksRemaining / 20;
                return "ETA: ~" + seconds + "s (" + currentExecutor.getRemainingMovements() + " movements remaining)";
            }
            case "stop", "cancel" -> {
                processManager.cancelAll();
                resetPathfindingState();
                player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                player.hurtMarked = true;
                player.setSprinting(false);
                log("Stopped");
                return "Stopped all processes";
            }
            case "pause" -> {
                paused = true;
                player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                player.hurtMarked = true;
                log("Paused");
                return "Bot paused";
            }
            case "resume" -> {
                paused = false;
                log("Resumed");
                return "Bot resumed";
            }
            case "status" -> {
                return getStatusText();
            }
            case "settings" -> {
                if (parts.length < 3) return "Usage: settings <key> <value>";
                settings.set(parts[1], parts[2]);
                log("Setting: " + parts[1] + " = " + parts[2]);
                return "Set " + parts[1] + " = " + parts[2];
            }
            case "quarry" -> {
                if (parts.length < 7) return "Usage: quarry <x1> <y1> <z1> <x2> <y2> <z2>";
                try {
                    int x1 = Integer.parseInt(parts[1]);
                    int y1 = Integer.parseInt(parts[2]);
                    int z1 = Integer.parseInt(parts[3]);
                    int x2 = Integer.parseInt(parts[4]);
                    int y2 = Integer.parseInt(parts[5]);
                    int z2 = Integer.parseInt(parts[6]);
                    processManager.cancelAll();
                    resetPathfindingState();
                    quarryProcess.start(x1, y1, z1, x2, y2, z2, level);
                    int w = Math.abs(x2 - x1) + 1;
                    int h = Math.abs(y2 - y1) + 1;
                    int d = Math.abs(z2 - z1) + 1;
                    log("Quarry: " + w + "x" + h + "x" + d + " from (" + x1 + "," + y1 + "," + z1 + ") to (" + x2 + "," + y2 + "," + z2 + ")");
                    return "Quarrying " + w + "x" + h + "x" + d + " area (" + (w * h * d) + " blocks max)";
                } catch (NumberFormatException e) { return "Invalid coordinates"; }
            }
            case "combat" -> {
                try {
                    int radius = parts.length > 1 ? Integer.parseInt(parts[1]) : 16;
                    processManager.cancelAll();
                    resetPathfindingState();
                    combatProcess.start(player, level, radius);
                    log("Combat mode: radius " + radius);
                    return "Combat mode engaged (radius " + radius + ")";
                } catch (NumberFormatException e) { return "Invalid radius"; }
            }
            case "deposit" -> {
                processManager.cancelAll();
                resetPathfindingState();
                chestProcess.startDeposit(player, level);
                log("Deposit: searching for chest");
                return "Depositing items into nearest chest";
            }
            case "withdraw" -> {
                if (parts.length < 2) return "Usage: withdraw <item> [count]";
                String itemName = parts[1];
                int count = parts.length > 2 ? Integer.parseInt(parts[2]) : 64;
                processManager.cancelAll();
                resetPathfindingState();
                chestProcess.startWithdraw(player, level, itemName, count);
                log("Withdraw: " + itemName + " x" + count);
                return "Withdrawing " + itemName + " (up to " + count + ")";
            }
            case "chest" -> {
                if (parts.length < 2) return "Usage: chest sort";
                if (parts[1].equalsIgnoreCase("sort")) {
                    processManager.cancelAll();
                    resetPathfindingState();
                    chestProcess.startSort(player, level);
                    log("Chest sort: organizing nearby chests");
                    return "Sorting nearby chests";
                }
                return "Usage: chest sort";
            }
            case "backfill" -> {
                backfillProcess.toggle(player, level);
                boolean enabled = backfillProcess.isEnabled();
                log("Backfill " + (enabled ? "enabled" : "disabled"));
                return "Backfill " + (enabled ? "enabled" : "disabled") + " (" + backfillProcess.getTrackedCount() + " positions tracked)";
            }
            case "elytra" -> {
                if (parts.length < 4) return "Usage: elytra <x> <y> <z> [altitude]";
                try {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);
                    int altitude = parts.length > 4 ? Integer.parseInt(parts[4]) : 200;
                    processManager.cancelAll();
                    resetPathfindingState();
                    elytraProcess.start(x, y, z, player, level, altitude);
                    log("Elytra flight: " + x + ", " + y + ", " + z + " (alt " + altitude + ")");
                    return "Elytra flight to " + x + ", " + y + ", " + z + " (cruise altitude " + altitude + ")";
                } catch (NumberFormatException e) { return "Invalid coordinates"; }
            }
            default -> {
                return "Unknown command: " + parts[0] + ". Commands: goto, mine, follow, farm, explore, build, patrol, gotoblock, tunnel, quarry, combat, deposit, withdraw, chest, backfill, elytra, waypoint, scan, inventory, come, surface, home, eta, stop, pause, resume, status, settings";
            }
        }
    }

    private Direction parseDirection(String dir) {
        return switch (dir.toLowerCase()) {
            case "north", "n" -> Direction.NORTH;
            case "south", "s" -> Direction.SOUTH;
            case "east", "e" -> Direction.EAST;
            case "west", "w" -> Direction.WEST;
            default -> null;
        };
    }

    // === Getters ===

    public ServerPlayer getPlayer() { return player; }
    public BotSettings getSettings() { return settings; }
    public ProcessManager getProcessManager() { return processManager; }
    public LookBehavior getLookBehavior() { return lookBehavior; }
    public InventoryBehavior getInventoryBehavior() { return inventoryBehavior; }
    public boolean isPaused() { return paused; }
    public List<String> getActionLog() {
        synchronized (actionLog) {
            return new ArrayList<>(actionLog);
        }
    }
    public int getBlocksMined() { return blocksMined; }
    public int getCropsHarvested() { return cropsHarvested; }
    public int getBlocksPlaced() { return blocksPlaced; }
    public int getKills() { return kills; }
    public void addKill() { kills++; }

    /** Get ETA in seconds, or -1 if no active path */
    public int getEtaSeconds() {
        return currentExecutor != null ? currentExecutor.getEstimatedTicksRemaining() / 20 : -1;
    }

    /** Get current goal coordinates as [x, y, z], or null if no goal */
    public int[] getGoalCoords() {
        if (currentGoal instanceof GoalBlock gb) {
            return new int[]{gb.x, gb.y, gb.z};
        } else if (currentGoal instanceof GoalXZ gxz) {
            return new int[]{gxz.x, 64, gxz.z};
        }
        return null;
    }

    public String getStatusText() {
        String processStatus = processManager.getStatus();
        String pathStatus = currentExecutor != null
            ? "Path: " + currentExecutor.getCurrentIndex() + "/" + currentExecutor.getPath().length() + " (" + currentExecutor.getState() + ")"
            : "No path";
        BlockPos pos = player.blockPosition();
        return processStatus + " | " + pathStatus + " | Pos: " + pos.getX() + "," + pos.getY() + "," + pos.getZ()
             + (paused ? " | PAUSED" : "");
    }

    public String toStatusJson() {
        BlockPos pos = player.blockPosition();
        String name = player.getGameProfile().name();
        BotProcess active = processManager.getActiveProcess();
        int eta = currentExecutor != null ? currentExecutor.getEstimatedTicksRemaining() / 20 : -1;
        return "{\"player\":\"" + name + "\""
             + ",\"x\":" + pos.getX() + ",\"y\":" + pos.getY() + ",\"z\":" + pos.getZ()
             + ",\"process\":\"" + (active != null ? active.name() : "none") + "\""
             + ",\"status\":\"" + escapeJson(processManager.getStatus()) + "\""
             + ",\"paused\":" + paused
             + ",\"pathProgress\":\"" + (currentExecutor != null ? currentExecutor.getCurrentIndex() + "/" + currentExecutor.getPath().length() : "none") + "\""
             + ",\"eta\":" + eta
             + ",\"blocksMined\":" + blocksMined
             + ",\"cropsHarvested\":" + cropsHarvested
             + ",\"blocksPlaced\":" + blocksPlaced
             + ",\"settings\":" + settings.toJson()
             + "}";
    }

    /** Get path positions as JSON for client-side visualization */
    public String getPathJson() {
        StringBuilder sb = new StringBuilder("{\"path\":[");
        if (currentExecutor != null) {
            var positions = currentExecutor.getPath().getPositions();
            int start = currentExecutor.getCurrentIndex();
            int count = 0;
            for (int i = start; i < positions.size() && count < 200; i++, count++) {
                if (count > 0) sb.append(",");
                var p = positions.get(i);
                sb.append("{\"x\":").append(p.x).append(",\"y\":").append(p.y).append(",\"z\":").append(p.z).append("}");
            }
        }
        sb.append("],\"status\":\"").append(escapeJson(processManager.getStatus())).append("\"");

        // Add goal position if available
        if (currentGoal instanceof GoalBlock gb) {
            sb.append(",\"goal\":{\"x\":").append(gb.x).append(",\"y\":").append(gb.y).append(",\"z\":").append(gb.z).append("}");
        } else if (currentGoal instanceof GoalXZ gxz) {
            sb.append(",\"goal\":{\"x\":").append(gxz.x).append(",\"z\":").append(gxz.z).append("}");
        }

        // Add stats
        sb.append(",\"eta\":").append(currentExecutor != null ? currentExecutor.getEstimatedTicksRemaining() / 20 : -1);
        sb.append(",\"blocksMined\":").append(blocksMined);
        sb.append(",\"cropsHarvested\":").append(cropsHarvested);
        sb.append(",\"blocksPlaced\":").append(blocksPlaced);

        // Add target blocks (ores, crops being targeted)
        sb.append(",\"targetBlocks\":[");
        appendTargetBlocks(sb);
        sb.append("]");

        // Add calculating state
        boolean calculating = isPathCalculating();
        sb.append(",\"calculating\":").append(calculating);
        sb.append(",\"nodesExplored\":").append(getNodesExplored());

        // Add partial calculating path if actively computing
        if (calculating && previousPath != null) {
            sb.append(",\"calculatingPath\":[");
            var prevPositions = previousPath.getPositions();
            int count = 0;
            for (int i = 0; i < prevPositions.size() && count < 100; i++, count++) {
                if (count > 0) sb.append(",");
                var p = prevPositions.get(i);
                sb.append("{\"x\":").append(p.x).append(",\"y\":").append(p.y).append(",\"z\":").append(p.z).append("}");
            }
            sb.append("]");
        }

        // Add selection region (quarry/build bounds)
        appendSelectionBounds(sb);

        // Add waypoint markers (patrol waypoints)
        appendWaypointMarkers(sb);

        sb.append("}");
        return sb.toString();
    }

    /** Append target block positions from active processes to the JSON builder */
    private void appendTargetBlocks(StringBuilder sb) {
        BotProcess active = processManager.getActiveProcess();
        int count = 0;

        if (active == mineProcess && mineProcess.isActive()) {
            // Scan for target ore blocks near the player and color them based on ore type
            ServerLevel level = (ServerLevel) player.level();
            Block target = mineProcess.getTargetBlock();
            if (target != null && level != null) {
                BlockPos bp = player.blockPosition();
                float[] color = getOreColor(target);
                for (int dx = -16; dx <= 16 && count < 40; dx++) {
                    for (int dy = -8; dy <= 8 && count < 40; dy++) {
                        for (int dz = -16; dz <= 16 && count < 40; dz++) {
                            BlockPos check = bp.offset(dx, dy, dz);
                            if (level.isLoaded(check) && level.getBlockState(check).getBlock() == target) {
                                if (count > 0) sb.append(",");
                                sb.append("{\"x\":").append(check.getX())
                                  .append(",\"y\":").append(check.getY())
                                  .append(",\"z\":").append(check.getZ())
                                  .append(",\"r\":").append(color[0])
                                  .append(",\"g\":").append(color[1])
                                  .append(",\"b\":").append(color[2]).append("}");
                                count++;
                            }
                        }
                    }
                }
            }
        } else if (active == farmProcess && farmProcess.isActive()) {
            // Highlight harvestable crops in green/yellow
            ServerLevel level = (ServerLevel) player.level();
            BlockPos bp = player.blockPosition();
            for (int dx = -12; dx <= 12 && count < 30; dx++) {
                for (int dz = -12; dz <= 12 && count < 30; dz++) {
                    for (int dy = -4; dy <= 4 && count < 30; dy++) {
                        BlockPos check = bp.offset(dx, dy, dz);
                        if (level.isLoaded(check) && isCrop(level, check)) {
                            if (count > 0) sb.append(",");
                            sb.append("{\"x\":").append(check.getX())
                              .append(",\"y\":").append(check.getY())
                              .append(",\"z\":").append(check.getZ())
                              .append(",\"r\":0.2,\"g\":0.9,\"b\":0.3}");
                            count++;
                        }
                    }
                }
            }
        }
    }

    /** Get a color for a given ore/target block type */
    private float[] getOreColor(Block block) {
        String name = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();
        if (name.contains("diamond")) return new float[]{0.2f, 0.9f, 0.95f};
        if (name.contains("emerald")) return new float[]{0.1f, 0.95f, 0.3f};
        if (name.contains("gold")) return new float[]{1.0f, 0.85f, 0.1f};
        if (name.contains("lapis")) return new float[]{0.2f, 0.3f, 0.95f};
        if (name.contains("redstone")) return new float[]{0.95f, 0.15f, 0.15f};
        if (name.contains("iron")) return new float[]{0.85f, 0.7f, 0.6f};
        if (name.contains("copper")) return new float[]{0.9f, 0.55f, 0.3f};
        if (name.contains("coal")) return new float[]{0.3f, 0.3f, 0.3f};
        if (name.contains("ancient_debris") || name.contains("netherite")) return new float[]{0.5f, 0.3f, 0.2f};
        // Default: cyan-ish
        return new float[]{0.4f, 0.8f, 0.9f};
    }

    /** Returns true if pathFuture is currently running (A* calculation in progress) */
    public boolean isPathCalculating() {
        return pathFuture != null && !pathFuture.isDone();
    }

    /** Returns the number of nodes explored in the most recent completed path, or 0 if none */
    public int getNodesExplored() {
        if (currentExecutor != null) {
            return currentExecutor.getPath().getNodesExplored();
        }
        if (previousPath != null) {
            return previousPath.getNodesExplored();
        }
        return 0;
    }

    /** Append selection bounds (quarry/build area) to the JSON builder */
    private void appendSelectionBounds(StringBuilder sb) {
        BotProcess active = processManager.getActiveProcess();
        if (active == quarryProcess && quarryProcess.isActive()) {
            // Quarry has min/max bounds stored internally - expose them
            int[] bounds = getQuarryBounds();
            if (bounds != null) {
                sb.append(",\"selection\":{")
                  .append("\"minX\":").append(bounds[0])
                  .append(",\"minY\":").append(bounds[1])
                  .append(",\"minZ\":").append(bounds[2])
                  .append(",\"maxX\":").append(bounds[3] + 1)
                  .append(",\"maxY\":").append(bounds[4] + 1)
                  .append(",\"maxZ\":").append(bounds[5] + 1)
                  .append("}");
            }
        } else if (active == buildProcess && buildProcess.isActive()) {
            BlockPos origin = buildProcess.getOrigin();
            if (origin != null) {
                sb.append(",\"selection\":{")
                  .append("\"minX\":").append(origin.getX())
                  .append(",\"minY\":").append(origin.getY())
                  .append(",\"minZ\":").append(origin.getZ())
                  .append(",\"maxX\":").append(origin.getX() + buildProcess.getWidth())
                  .append(",\"maxY\":").append(origin.getY() + buildProcess.getHeight())
                  .append(",\"maxZ\":").append(origin.getZ() + buildProcess.getDepth())
                  .append("}");
            }
        }
    }

    /** Append patrol waypoint markers to the JSON builder */
    private void appendWaypointMarkers(StringBuilder sb) {
        BotProcess active = processManager.getActiveProcess();
        if (active == patrolProcess && patrolProcess.isActive()) {
            List<BlockPos> waypoints = patrolProcess.getWaypoints();
            if (!waypoints.isEmpty()) {
                sb.append(",\"waypoints\":[");
                for (int i = 0; i < waypoints.size(); i++) {
                    if (i > 0) sb.append(",");
                    BlockPos wp = waypoints.get(i);
                    sb.append("{\"x\":").append(wp.getX())
                      .append(",\"y\":").append(wp.getY())
                      .append(",\"z\":").append(wp.getZ()).append("}");
                }
                sb.append("]");
            }
        }
    }

    /** Get quarry bounds as [minX, minY, minZ, maxX, maxY, maxZ], or null if not quarrying */
    public int[] getQuarryBounds() {
        if (!quarryProcess.isActive()) return null;
        return new int[] {
            quarryProcess.getMinX(), quarryProcess.getMinY(), quarryProcess.getMinZ(),
            quarryProcess.getMaxX(), quarryProcess.getMaxY(), quarryProcess.getMaxZ()
        };
    }

    private void log(String msg) {
        String entry = "[" + ticksSinceStart + "] " + msg;
        actionLog.add(entry);
        if (actionLog.size() > 1000) actionLog.remove(0);
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}

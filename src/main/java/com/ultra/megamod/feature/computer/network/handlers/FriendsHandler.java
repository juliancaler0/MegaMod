package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.MegaMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.dimensions.DimensionHelper;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dimensions.PocketManager;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.museum.dimension.MuseumDimensionManager;
import com.ultra.megamod.feature.museum.dimension.MuseumDisplayManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FriendsHandler {

    // Per-player friend lists (bidirectional: both sides stored)
    private static final Map<UUID, Set<UUID>> friendships = new ConcurrentHashMap<>();
    // Pending friend requests: target -> set of requesters
    private static final Map<UUID, Set<UUID>> pendingRequests = new ConcurrentHashMap<>();
    // TP requests: target -> (requester -> timestamp)
    private static final Map<UUID, Map<UUID, Long>> tpRequests = new ConcurrentHashMap<>();
    // Last seen timestamps: player UUID -> epoch millis
    private static final Map<UUID, Long> lastSeenTimes = new ConcurrentHashMap<>();
    // UUID -> name cache for offline players
    private static final Map<UUID, String> nameCache = new ConcurrentHashMap<>();

    private static boolean loaded = false;
    private static boolean dirty = false;
    private static final String FILE_NAME = "megamod_friends.dat";
    private static final long TP_REQUEST_TIMEOUT_MS = 60_000; // 60 seconds

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "friends_request": {
                ensureLoaded(level);
                cleanExpiredTpRequests();
                sendFriendsData(player, level, eco);
                return true;
            }
            case "friends_add": {
                ensureLoaded(level);
                handleAdd(player, jsonData, level, eco);
                return true;
            }
            case "friends_remove": {
                ensureLoaded(level);
                handleRemove(player, jsonData, level, eco);
                return true;
            }
            case "friends_accept": {
                ensureLoaded(level);
                handleAcceptFriend(player, jsonData, level, eco);
                return true;
            }
            case "friends_decline": {
                ensureLoaded(level);
                handleDeclineFriend(player, jsonData, level, eco);
                return true;
            }
            case "friends_tp_request": {
                ensureLoaded(level);
                handleTpRequest(player, jsonData, level, eco);
                return true;
            }
            case "friends_tp_accept": {
                ensureLoaded(level);
                handleTpAccept(player, jsonData, level, eco);
                return true;
            }
            case "friends_tp_decline": {
                ensureLoaded(level);
                handleTpDecline(player, jsonData, level, eco);
                return true;
            }
            case "friends_message": {
                ensureLoaded(level);
                handleMessage(player, jsonData, level, eco);
                return true;
            }
            case "friends_visit_museum": {
                handleVisitMuseum(player, jsonData, level, eco);
                return true;
            }
            default:
                return false;
        }
    }

    // --- Action handlers ---

    private static void handleVisitMuseum(ServerPlayer player, String targetUuidStr, ServerLevel level, EconomyManager eco) {
        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(targetUuidStr.trim());
        } catch (IllegalArgumentException e) {
            sendResult(player, false, "Invalid player.", eco);
            return;
        }

        if (targetUuid.equals(player.getUUID())) {
            sendResult(player, false, "Use the Museum Door to enter your own museum!", eco);
            return;
        }

        // Must be friends
        Set<UUID> playerFriends = friendships.get(player.getUUID());
        if (playerFriends == null || !playerFriends.contains(targetUuid)) {
            sendResult(player, false, "You can only visit friends' museums.", eco);
            return;
        }

        ServerLevel overworld = level.getServer().overworld();
        PocketManager pockets = PocketManager.get(overworld);
        MuseumDimensionManager dimManager = MuseumDimensionManager.get(overworld);

        if (!dimManager.isMuseumInitialized(targetUuid)) {
            sendResult(player, false, "That player hasn't built their museum yet!", eco);
            return;
        }

        BlockPos origin = pockets.getMuseumPocket(targetUuid);
        if (origin == null) {
            sendResult(player, false, "Could not find that player's museum!", eco);
            return;
        }

        ServerLevel museumLevel = level.getServer().getLevel(MegaModDimensions.MUSEUM);
        if (museumLevel == null) {
            sendResult(player, false, "Museum dimension is not available!", eco);
            return;
        }

        // Close the screen before teleporting
        player.closeContainer();

        BlockPos spawnPos = origin.offset(10, 1, 4);
        DimensionHelper.teleportToDimension(player, MegaModDimensions.MUSEUM, spawnPos, 0.0f, 0.0f);
        MuseumDisplayManager.rebuildWings(museumLevel, origin, targetUuid, null);

        String targetName = getPlayerName(targetUuid, level);
        player.sendSystemMessage(Component.literal("Visiting " + targetName + "'s Museum!").withStyle(ChatFormatting.GOLD));

        // Notify target if online
        ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetUuid);
        if (target != null) {
            target.sendSystemMessage(Component.literal(player.getGameProfile().name() + " is visiting your museum!").withStyle(ChatFormatting.AQUA));
        }
    }

    private static void handleAdd(ServerPlayer player, String targetName, ServerLevel level, EconomyManager eco) {
        targetName = targetName.trim();
        if (targetName.isEmpty()) {
            sendResult(player, false, "Enter a player name.", eco);
            return;
        }

        // Cannot add yourself
        if (targetName.equalsIgnoreCase(player.getGameProfile().name())) {
            sendResult(player, false, "Cannot add yourself.", eco);
            return;
        }

        // Look up target by name (online check)
        ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
        UUID targetUuid = null;
        if (target != null) {
            targetUuid = target.getUUID();
        } else {
            // Check name cache for offline players
            for (Map.Entry<UUID, String> entry : nameCache.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(targetName)) {
                    targetUuid = entry.getKey();
                    break;
                }
            }
        }

        if (targetUuid == null) {
            sendResult(player, false, "Player not found: " + targetName, eco);
            return;
        }

        UUID playerUuid = player.getUUID();

        // Check if already friends
        Set<UUID> playerFriends = friendships.computeIfAbsent(playerUuid, k -> new HashSet<>());
        if (playerFriends.contains(targetUuid)) {
            sendResult(player, false, targetName + " is already your friend.", eco);
            return;
        }

        // Check if we already sent a request
        Set<UUID> targetPending = pendingRequests.computeIfAbsent(targetUuid, k -> new HashSet<>());
        if (targetPending.contains(playerUuid)) {
            sendResult(player, false, "Request already sent to " + targetName + ".", eco);
            return;
        }

        // Check if target already sent us a request (auto-accept / mutual)
        Set<UUID> ourPending = pendingRequests.computeIfAbsent(playerUuid, k -> new HashSet<>());
        if (ourPending.contains(targetUuid)) {
            // Auto-accept: they sent us a request, we're adding them => become friends
            ourPending.remove(targetUuid);
            playerFriends.add(targetUuid);
            friendships.computeIfAbsent(targetUuid, k -> new HashSet<>()).add(playerUuid);
            updateNameCache(player);
            if (target != null) {
                updateNameCache(target);
            }
            dirty = true;
            saveToDisk(level);
            sendResult(player, true, "You and " + targetName + " are now friends!", eco);

            // Notify target if online
            if (target != null) {
                target.sendSystemMessage(Component.literal(player.getGameProfile().name() + " accepted your friend request!").withStyle(ChatFormatting.GREEN));
            }
            return;
        }

        // Add pending request
        targetPending.add(playerUuid);
        updateNameCache(player);
        dirty = true;
        saveToDisk(level);
        sendResult(player, true, "Friend request sent to " + targetName + "!", eco);

        // Notify target if online
        if (target != null) {
            target.sendSystemMessage(Component.literal(player.getGameProfile().name() + " sent you a friend request! Use the Computer to respond.").withStyle(ChatFormatting.AQUA));
        }
    }

    private static void handleRemove(ServerPlayer player, String targetUuidStr, ServerLevel level, EconomyManager eco) {
        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(targetUuidStr.trim());
        } catch (IllegalArgumentException e) {
            sendResult(player, false, "Invalid player.", eco);
            return;
        }

        UUID playerUuid = player.getUUID();
        Set<UUID> playerFriends = friendships.get(playerUuid);
        if (playerFriends == null || !playerFriends.remove(targetUuid)) {
            sendResult(player, false, "That player is not your friend.", eco);
            return;
        }

        // Remove from other side too
        Set<UUID> targetFriends = friendships.get(targetUuid);
        if (targetFriends != null) {
            targetFriends.remove(playerUuid);
        }

        dirty = true;
        saveToDisk(level);

        String targetName = getPlayerName(targetUuid, level);
        sendResult(player, true, "Removed " + targetName + " from friends.", eco);

        // Notify target if online
        ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetUuid);
        if (target != null) {
            target.sendSystemMessage(Component.literal(player.getGameProfile().name() + " removed you from their friends list.").withStyle(ChatFormatting.YELLOW));
        }
    }

    private static void handleAcceptFriend(ServerPlayer player, String requesterUuidStr, ServerLevel level, EconomyManager eco) {
        UUID requesterUuid;
        try {
            requesterUuid = UUID.fromString(requesterUuidStr.trim());
        } catch (IllegalArgumentException e) {
            sendResult(player, false, "Invalid player.", eco);
            return;
        }

        UUID playerUuid = player.getUUID();
        Set<UUID> ourPending = pendingRequests.get(playerUuid);
        if (ourPending == null || !ourPending.remove(requesterUuid)) {
            sendResult(player, false, "No pending request from that player.", eco);
            return;
        }

        // Create friendship (both directions)
        friendships.computeIfAbsent(playerUuid, k -> new HashSet<>()).add(requesterUuid);
        friendships.computeIfAbsent(requesterUuid, k -> new HashSet<>()).add(playerUuid);
        updateNameCache(player);

        dirty = true;
        saveToDisk(level);

        String requesterName = getPlayerName(requesterUuid, level);
        sendResult(player, true, "You and " + requesterName + " are now friends!", eco);

        // Notify requester if online
        ServerPlayer requester = level.getServer().getPlayerList().getPlayer(requesterUuid);
        if (requester != null) {
            updateNameCache(requester);
            requester.sendSystemMessage(Component.literal(player.getGameProfile().name() + " accepted your friend request!").withStyle(ChatFormatting.GREEN));
        }
    }

    private static void handleDeclineFriend(ServerPlayer player, String requesterUuidStr, ServerLevel level, EconomyManager eco) {
        UUID requesterUuid;
        try {
            requesterUuid = UUID.fromString(requesterUuidStr.trim());
        } catch (IllegalArgumentException e) {
            sendResult(player, false, "Invalid player.", eco);
            return;
        }

        UUID playerUuid = player.getUUID();
        Set<UUID> ourPending = pendingRequests.get(playerUuid);
        if (ourPending == null || !ourPending.remove(requesterUuid)) {
            sendResult(player, false, "No pending request from that player.", eco);
            return;
        }

        dirty = true;
        saveToDisk(level);

        String requesterName = getPlayerName(requesterUuid, level);
        sendResult(player, true, "Declined friend request from " + requesterName + ".", eco);
    }

    private static void handleTpRequest(ServerPlayer player, String targetUuidStr, ServerLevel level, EconomyManager eco) {
        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(targetUuidStr.trim());
        } catch (IllegalArgumentException e) {
            sendResult(player, false, "Invalid player.", eco);
            return;
        }

        UUID playerUuid = player.getUUID();

        // Must be friends
        Set<UUID> playerFriends = friendships.get(playerUuid);
        if (playerFriends == null || !playerFriends.contains(targetUuid)) {
            sendResult(player, false, "You can only TP to friends.", eco);
            return;
        }

        // Target must be online
        ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetUuid);
        if (target == null) {
            sendResult(player, false, "That player is offline.", eco);
            return;
        }

        // Add TP request
        Map<UUID, Long> targetTpReqs = tpRequests.computeIfAbsent(targetUuid, k -> new HashMap<>());
        targetTpReqs.put(playerUuid, System.currentTimeMillis());

        sendResult(player, true, "TP request sent to " + target.getGameProfile().name() + "!", eco);

        // Notify target
        target.sendSystemMessage(Component.literal(player.getGameProfile().name() + " wants to teleport to you! Use the Computer to respond.").withStyle(ChatFormatting.AQUA));
    }

    private static void handleTpAccept(ServerPlayer player, String requesterUuidStr, ServerLevel level, EconomyManager eco) {
        UUID requesterUuid;
        try {
            requesterUuid = UUID.fromString(requesterUuidStr.trim());
        } catch (IllegalArgumentException e) {
            sendResult(player, false, "Invalid player.", eco);
            return;
        }

        UUID playerUuid = player.getUUID();
        Map<UUID, Long> ourTpReqs = tpRequests.get(playerUuid);
        if (ourTpReqs == null || !ourTpReqs.containsKey(requesterUuid)) {
            sendResult(player, false, "No TP request from that player.", eco);
            return;
        }

        long timestamp = ourTpReqs.get(requesterUuid);
        long elapsed = System.currentTimeMillis() - timestamp;
        if (elapsed > TP_REQUEST_TIMEOUT_MS) {
            ourTpReqs.remove(requesterUuid);
            sendResult(player, false, "TP request has expired.", eco);
            return;
        }

        ourTpReqs.remove(requesterUuid);

        // Requester must be online
        ServerPlayer requester = level.getServer().getPlayerList().getPlayer(requesterUuid);
        if (requester == null) {
            sendResult(player, false, "That player is no longer online.", eco);
            return;
        }

        // Teleport requester to target (player who accepted)
        if (requester.level() != player.level()) {
            // Cross-dimension teleport
            ServerLevel targetLevel = (ServerLevel) player.level();
            requester.teleportTo(targetLevel, player.getX(), player.getY(), player.getZ(), java.util.Set.of(), player.getYRot(), player.getXRot(), false);
        } else {
            requester.teleportTo(player.getX(), player.getY(), player.getZ());
        }

        sendResult(player, true, requester.getGameProfile().name() + " has been teleported to you!", eco);
        requester.sendSystemMessage(Component.literal("Teleported to " + player.getGameProfile().name() + "!").withStyle(ChatFormatting.GREEN));
    }

    private static void handleTpDecline(ServerPlayer player, String requesterUuidStr, ServerLevel level, EconomyManager eco) {
        UUID requesterUuid;
        try {
            requesterUuid = UUID.fromString(requesterUuidStr.trim());
        } catch (IllegalArgumentException e) {
            sendResult(player, false, "Invalid player.", eco);
            return;
        }

        UUID playerUuid = player.getUUID();
        Map<UUID, Long> ourTpReqs = tpRequests.get(playerUuid);
        if (ourTpReqs != null) {
            ourTpReqs.remove(requesterUuid);
        }

        sendResult(player, true, "TP request declined.", eco);

        // Notify requester if online
        ServerPlayer requester = level.getServer().getPlayerList().getPlayer(requesterUuid);
        if (requester != null) {
            requester.sendSystemMessage(Component.literal(player.getGameProfile().name() + " declined your TP request.").withStyle(ChatFormatting.RED));
        }
    }

    private static void handleMessage(ServerPlayer player, String data, ServerLevel level, EconomyManager eco) {
        // Format: "targetUUID:message text"
        int colonIdx = data.indexOf(':');
        if (colonIdx < 0) {
            sendResult(player, false, "Invalid message format.", eco);
            return;
        }

        String targetUuidStr = data.substring(0, colonIdx).trim();
        String message = data.substring(colonIdx + 1).trim();

        if (message.isEmpty()) {
            sendResult(player, false, "Message cannot be empty.", eco);
            return;
        }

        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(targetUuidStr);
        } catch (IllegalArgumentException e) {
            sendResult(player, false, "Invalid player.", eco);
            return;
        }

        // Must be friends
        UUID playerUuid = player.getUUID();
        Set<UUID> playerFriends = friendships.get(playerUuid);
        if (playerFriends == null || !playerFriends.contains(targetUuid)) {
            sendResult(player, false, "You can only message friends.", eco);
            return;
        }

        // Target must be online
        ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetUuid);
        if (target == null) {
            sendResult(player, false, "That player is offline.", eco);
            return;
        }

        // Send the message
        String senderName = player.getGameProfile().name();
        target.sendSystemMessage(
            Component.literal("[Friend] " + senderName + ": " + message).withStyle(ChatFormatting.LIGHT_PURPLE)
        );
        sendResult(player, true, "Message sent!", eco);
    }

    // --- Data response ---

    private static void sendFriendsData(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerUuid = player.getUUID();

        // Update name cache and last seen for this player
        updateNameCache(player);

        JsonObject root = new JsonObject();

        // Friends array
        JsonArray friendsArr = new JsonArray();
        Set<UUID> playerFriends = friendships.getOrDefault(playerUuid, new HashSet<>());
        for (UUID friendUuid : playerFriends) {
            JsonObject f = new JsonObject();
            String name = getPlayerName(friendUuid, level);
            boolean online = level.getServer().getPlayerList().getPlayer(friendUuid) != null;
            f.addProperty("name", name);
            f.addProperty("uuid", friendUuid.toString());
            f.addProperty("online", online);
            f.addProperty("lastSeen", online ? "" : formatLastSeen(friendUuid));
            friendsArr.add(f);
        }
        root.add("friends", friendsArr);

        // Pending friend requests (incoming to this player)
        JsonArray friendReqArr = new JsonArray();
        Set<UUID> ourPending = pendingRequests.getOrDefault(playerUuid, new HashSet<>());
        for (UUID requesterUuid : ourPending) {
            JsonObject r = new JsonObject();
            r.addProperty("name", getPlayerName(requesterUuid, level));
            r.addProperty("uuid", requesterUuid.toString());
            r.addProperty("timestamp", System.currentTimeMillis());
            friendReqArr.add(r);
        }
        root.add("friendRequests", friendReqArr);

        // Pending TP requests (incoming to this player)
        JsonArray tpReqArr = new JsonArray();
        Map<UUID, Long> ourTpReqs = tpRequests.getOrDefault(playerUuid, new HashMap<>());
        for (Map.Entry<UUID, Long> entry : ourTpReqs.entrySet()) {
            // Skip expired
            long elapsed = System.currentTimeMillis() - entry.getValue();
            if (elapsed > TP_REQUEST_TIMEOUT_MS) continue;

            JsonObject r = new JsonObject();
            r.addProperty("name", getPlayerName(entry.getKey(), level));
            r.addProperty("uuid", entry.getKey().toString());
            r.addProperty("timestamp", entry.getValue());
            tpReqArr.add(r);
        }
        root.add("tpRequests", tpReqArr);

        sendResponse(player, "friends_data", root.toString(), eco);
    }

    private static void sendResult(ServerPlayer player, boolean success, String message, EconomyManager eco) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", success);
        obj.addProperty("message", message);
        sendResponse(player, "friends_result", obj.toString(), eco);
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }

    // --- Helpers ---

    private static void updateNameCache(ServerPlayer player) {
        nameCache.put(player.getUUID(), player.getGameProfile().name());
    }

    /**
     * Called when a player disconnects to record their last seen time.
     */
    public static void onPlayerDisconnect(ServerPlayer player, ServerLevel level) {
        if (!loaded) return;
        lastSeenTimes.put(player.getUUID(), System.currentTimeMillis());
        updateNameCache(player);
        dirty = true;
        saveToDisk(level);
    }

    private static String getPlayerName(UUID uuid, ServerLevel level) {
        // Try online player first
        ServerPlayer online = level.getServer().getPlayerList().getPlayer(uuid);
        if (online != null) {
            String name = online.getGameProfile().name();
            nameCache.put(uuid, name);
            return name;
        }
        // Fall back to cache
        return nameCache.getOrDefault(uuid, "Unknown");
    }

    private static String formatLastSeen(UUID uuid) {
        Long time = lastSeenTimes.get(uuid);
        if (time == null) return "";

        long elapsed = System.currentTimeMillis() - time;
        long seconds = elapsed / 1000;
        if (seconds < 60) return seconds + "s ago";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        return days + "d ago";
    }

    private static void cleanExpiredTpRequests() {
        long now = System.currentTimeMillis();
        for (Map<UUID, Long> reqs : tpRequests.values()) {
            reqs.entrySet().removeIf(e -> (now - e.getValue()) > TP_REQUEST_TIMEOUT_MS);
        }
    }

    // --- NbtIo Persistence ---

    private static void ensureLoaded(ServerLevel level) {
        if (!loaded) {
            loadFromDisk(level);
            loaded = true;
        }
    }

    public static void loadFromDisk(ServerLevel level) {
        friendships.clear();
        pendingRequests.clear();
        tpRequests.clear();
        lastSeenTimes.clear();
        nameCache.clear();
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());

                // Friendships
                CompoundTag friendshipsTag = root.getCompoundOrEmpty("friendships");
                for (String key : friendshipsTag.keySet()) {
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(key);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                    ListTag list = friendshipsTag.getListOrEmpty(key);
                    Set<UUID> friends = new HashSet<>();
                    for (int i = 0; i < list.size(); i++) {
                        CompoundTag entry = list.getCompoundOrEmpty(i);
                        String friendStr = entry.getStringOr("uuid", "");
                        if (!friendStr.isEmpty()) {
                            try {
                                friends.add(UUID.fromString(friendStr));
                            } catch (IllegalArgumentException e) {
                                // skip
                            }
                        }
                    }
                    friendships.put(uuid, friends);
                }

                // Pending requests
                CompoundTag pendingTag = root.getCompoundOrEmpty("pendingRequests");
                for (String key : pendingTag.keySet()) {
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(key);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                    ListTag list = pendingTag.getListOrEmpty(key);
                    Set<UUID> requesters = new HashSet<>();
                    for (int i = 0; i < list.size(); i++) {
                        CompoundTag entry = list.getCompoundOrEmpty(i);
                        String reqStr = entry.getStringOr("uuid", "");
                        if (!reqStr.isEmpty()) {
                            try {
                                requesters.add(UUID.fromString(reqStr));
                            } catch (IllegalArgumentException e) {
                                // skip
                            }
                        }
                    }
                    pendingRequests.put(uuid, requesters);
                }

                // Last seen times
                CompoundTag lastSeenTag = root.getCompoundOrEmpty("lastSeen");
                for (String key : lastSeenTag.keySet()) {
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(key);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                    long time = lastSeenTag.getLongOr(key, 0L);
                    if (time > 0) {
                        lastSeenTimes.put(uuid, time);
                    }
                }

                // Name cache
                CompoundTag nameCacheTag = root.getCompoundOrEmpty("nameCache");
                for (String key : nameCacheTag.keySet()) {
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(key);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                    String name = nameCacheTag.getStringOr(key, "");
                    if (!name.isEmpty()) {
                        nameCache.put(uuid, name);
                    }
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load friends data", e);
        }
        dirty = false;
    }

    public static void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();

            // Friendships
            CompoundTag friendshipsTag = new CompoundTag();
            for (Map.Entry<UUID, Set<UUID>> entry : friendships.entrySet()) {
                ListTag list = new ListTag();
                for (UUID friendUuid : entry.getValue()) {
                    CompoundTag friendTag = new CompoundTag();
                    friendTag.putString("uuid", friendUuid.toString());
                    list.add((Tag) friendTag);
                }
                friendshipsTag.put(entry.getKey().toString(), (Tag) list);
            }
            root.put("friendships", (Tag) friendshipsTag);

            // Pending requests
            CompoundTag pendingTag = new CompoundTag();
            for (Map.Entry<UUID, Set<UUID>> entry : pendingRequests.entrySet()) {
                if (entry.getValue().isEmpty()) continue;
                ListTag list = new ListTag();
                for (UUID reqUuid : entry.getValue()) {
                    CompoundTag reqTag = new CompoundTag();
                    reqTag.putString("uuid", reqUuid.toString());
                    list.add((Tag) reqTag);
                }
                pendingTag.put(entry.getKey().toString(), (Tag) list);
            }
            root.put("pendingRequests", (Tag) pendingTag);

            // Last seen times
            CompoundTag lastSeenTag = new CompoundTag();
            for (Map.Entry<UUID, Long> entry : lastSeenTimes.entrySet()) {
                lastSeenTag.putLong(entry.getKey().toString(), entry.getValue());
            }
            root.put("lastSeen", (Tag) lastSeenTag);

            // Name cache
            CompoundTag nameCacheTag = new CompoundTag();
            for (Map.Entry<UUID, String> entry : nameCache.entrySet()) {
                nameCacheTag.putString(entry.getKey().toString(), entry.getValue());
            }
            root.put("nameCache", (Tag) nameCacheTag);

            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save friends data", e);
        }
    }

    /**
     * Force save regardless of dirty flag. Called on server stop.
     */
    /**
     * Check if two players are friends. Used by museum visit command and other systems.
     */
    public static boolean areFriends(UUID player1, UUID player2) {
        Set<UUID> friends = friendships.get(player1);
        return friends != null && friends.contains(player2);
    }

    public static void forceSave(ServerLevel level) {
        if (!loaded) return;
        dirty = true;
        saveToDisk(level);
    }

    public static void reset() {
        friendships.clear();
        pendingRequests.clear();
        tpRequests.clear();
        lastSeenTimes.clear();
        nameCache.clear();
        dirty = false;
        loaded = false;
    }
}

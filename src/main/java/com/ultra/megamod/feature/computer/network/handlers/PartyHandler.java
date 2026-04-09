package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PartyHandler {

    // All active parties, keyed by leader UUID
    private static final Map<UUID, Party> parties = new ConcurrentHashMap<>();
    // Reverse lookup: player UUID -> leader UUID (for quick "which party am I in?")
    private static final Map<UUID, UUID> playerToParty = new ConcurrentHashMap<>();
    // Pending invites: target UUID -> inviter (leader) UUID
    private static final Map<UUID, UUID> pendingInvites = new ConcurrentHashMap<>();
    // UUID -> name cache for display (bounded to avoid unbounded growth)
    private static final Map<UUID, String> nameCache = new ConcurrentHashMap<>();
    private static final int MAX_NAME_CACHE_SIZE = 500;

    private static final int MAX_PARTY_SIZE = 4;

    public record Party(UUID leader, Set<UUID> members, Set<UUID> pendingInvites) {
        public Party(UUID leader) {
            this(leader, ConcurrentHashMap.newKeySet(), ConcurrentHashMap.newKeySet());
            this.members.add(leader);
        }
    }

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "party_request": {
                updateNameCache(player);
                sendPartyData(player, level, eco);
                return true;
            }
            case "party_create": {
                updateNameCache(player);
                handleCreate(player, level, eco);
                return true;
            }
            case "party_invite": {
                updateNameCache(player);
                handleInvite(player, jsonData, level, eco);
                return true;
            }
            case "party_accept": {
                updateNameCache(player);
                handleAccept(player, level, eco);
                return true;
            }
            case "party_decline": {
                updateNameCache(player);
                handleDecline(player, level, eco);
                return true;
            }
            case "party_kick": {
                updateNameCache(player);
                handleKick(player, jsonData, level, eco);
                return true;
            }
            case "party_leave": {
                updateNameCache(player);
                handleLeave(player, level, eco);
                return true;
            }
            case "party_disband": {
                updateNameCache(player);
                handleDisband(player, level, eco);
                return true;
            }
            default:
                return false;
        }
    }

    // --- Action handlers ---

    private static void handleCreate(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerUuid = player.getUUID();

        if (playerToParty.containsKey(playerUuid)) {
            sendResult(player, false, "You are already in a party.", eco);
            return;
        }

        Party party = new Party(playerUuid);
        parties.put(playerUuid, party);
        playerToParty.put(playerUuid, playerUuid);

        sendResult(player, true, "Party created! Invite players to join.", eco);
    }

    private static void handleInvite(ServerPlayer player, String targetName, ServerLevel level, EconomyManager eco) {
        targetName = targetName.trim();
        if (targetName.isEmpty()) {
            sendResult(player, false, "Enter a player name.", eco);
            return;
        }

        UUID playerUuid = player.getUUID();

        // Must be in a party
        UUID leaderUuid = playerToParty.get(playerUuid);
        if (leaderUuid == null) {
            sendResult(player, false, "You are not in a party.", eco);
            return;
        }

        // Must be the leader
        if (!leaderUuid.equals(playerUuid)) {
            sendResult(player, false, "Only the party leader can invite players.", eco);
            return;
        }

        Party party = parties.get(leaderUuid);
        if (party == null) {
            sendResult(player, false, "Party not found.", eco);
            return;
        }

        // Check party size
        if (party.members.size() >= MAX_PARTY_SIZE) {
            sendResult(player, false, "Party is full (" + MAX_PARTY_SIZE + " max).", eco);
            return;
        }

        // Cannot invite yourself
        if (targetName.equalsIgnoreCase(player.getGameProfile().name())) {
            sendResult(player, false, "Cannot invite yourself.", eco);
            return;
        }

        // Target must be online
        ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            sendResult(player, false, "Player not found or offline: " + targetName, eco);
            return;
        }

        UUID targetUuid = target.getUUID();

        // Target must not already be in a party
        if (playerToParty.containsKey(targetUuid)) {
            sendResult(player, false, targetName + " is already in a party.", eco);
            return;
        }

        // Check if already invited
        if (party.pendingInvites.contains(targetUuid)) {
            sendResult(player, false, targetName + " has already been invited.", eco);
            return;
        }

        // Send invite
        party.pendingInvites.add(targetUuid);
        pendingInvites.put(targetUuid, leaderUuid);
        updateNameCache(target);

        sendResult(player, true, "Invited " + targetName + " to the party!", eco);

        // Notify target via chat
        target.sendSystemMessage(
            Component.literal(player.getGameProfile().name() + " invited you to their party! Use the Computer to respond.")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
        );
    }

    private static void handleAccept(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerUuid = player.getUUID();

        // Check if player has a pending invite
        UUID leaderUuid = pendingInvites.get(playerUuid);
        if (leaderUuid == null) {
            sendResult(player, false, "No pending party invite.", eco);
            return;
        }

        // Already in a party
        if (playerToParty.containsKey(playerUuid)) {
            pendingInvites.remove(playerUuid);
            sendResult(player, false, "You are already in a party.", eco);
            return;
        }

        Party party = parties.get(leaderUuid);
        if (party == null) {
            // Party was disbanded
            pendingInvites.remove(playerUuid);
            sendResult(player, false, "That party no longer exists.", eco);
            return;
        }

        // Check party size
        if (party.members.size() >= MAX_PARTY_SIZE) {
            pendingInvites.remove(playerUuid);
            party.pendingInvites.remove(playerUuid);
            sendResult(player, false, "Party is full.", eco);
            return;
        }

        // Join the party
        pendingInvites.remove(playerUuid);
        party.pendingInvites.remove(playerUuid);
        party.members.add(playerUuid);
        playerToParty.put(playerUuid, leaderUuid);

        String playerName = player.getGameProfile().name();
        sendResult(player, true, "You joined the party!", eco);

        // Notify all other party members via chat
        for (UUID memberUuid : party.members) {
            if (memberUuid.equals(playerUuid)) continue;
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(memberUuid);
            if (member != null) {
                member.sendSystemMessage(
                    Component.literal(playerName + " joined the party!").withStyle(ChatFormatting.GREEN)
                );
            }
        }
    }

    private static void handleDecline(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerUuid = player.getUUID();

        UUID leaderUuid = pendingInvites.remove(playerUuid);
        if (leaderUuid == null) {
            sendResult(player, false, "No pending party invite.", eco);
            return;
        }

        // Remove from party's pending list
        Party party = parties.get(leaderUuid);
        if (party != null) {
            party.pendingInvites.remove(playerUuid);
        }

        sendResult(player, true, "Party invite declined.", eco);

        // Notify leader if online
        ServerPlayer leader = level.getServer().getPlayerList().getPlayer(leaderUuid);
        if (leader != null) {
            leader.sendSystemMessage(
                Component.literal(player.getGameProfile().name() + " declined the party invite.").withStyle(ChatFormatting.YELLOW)
            );
        }
    }

    private static void handleKick(ServerPlayer player, String targetUuidStr, ServerLevel level, EconomyManager eco) {
        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(targetUuidStr.trim());
        } catch (IllegalArgumentException e) {
            sendResult(player, false, "Invalid player.", eco);
            return;
        }

        UUID playerUuid = player.getUUID();

        // Must be in a party and be the leader
        UUID leaderUuid = playerToParty.get(playerUuid);
        if (leaderUuid == null || !leaderUuid.equals(playerUuid)) {
            sendResult(player, false, "Only the party leader can kick players.", eco);
            return;
        }

        Party party = parties.get(leaderUuid);
        if (party == null) {
            sendResult(player, false, "Party not found.", eco);
            return;
        }

        // Cannot kick yourself
        if (targetUuid.equals(playerUuid)) {
            sendResult(player, false, "Cannot kick yourself. Use disband instead.", eco);
            return;
        }

        // Must be a member
        if (!party.members.contains(targetUuid)) {
            sendResult(player, false, "That player is not in your party.", eco);
            return;
        }

        // Remove from party
        party.members.remove(targetUuid);
        playerToParty.remove(targetUuid);

        String targetName = getPlayerName(targetUuid, level);
        sendResult(player, true, "Kicked " + targetName + " from the party.", eco);

        // Notify kicked player if online
        ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetUuid);
        if (target != null) {
            target.sendSystemMessage(
                Component.literal("You were kicked from the party by " + player.getGameProfile().name() + ".").withStyle(ChatFormatting.RED)
            );
        }

        // Notify remaining members
        for (UUID memberUuid : party.members) {
            if (memberUuid.equals(playerUuid)) continue;
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(memberUuid);
            if (member != null) {
                member.sendSystemMessage(
                    Component.literal(targetName + " was kicked from the party.").withStyle(ChatFormatting.YELLOW)
                );
            }
        }
    }

    private static void handleLeave(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerUuid = player.getUUID();

        UUID leaderUuid = playerToParty.get(playerUuid);
        if (leaderUuid == null) {
            sendResult(player, false, "You are not in a party.", eco);
            return;
        }

        // If leader leaves, disband
        if (leaderUuid.equals(playerUuid)) {
            handleDisband(player, level, eco);
            return;
        }

        Party party = parties.get(leaderUuid);
        if (party == null) {
            playerToParty.remove(playerUuid);
            sendResult(player, true, "Left the party.", eco);
            return;
        }

        // Remove from party
        party.members.remove(playerUuid);
        playerToParty.remove(playerUuid);

        String playerName = player.getGameProfile().name();
        sendResult(player, true, "You left the party.", eco);

        // Notify remaining members
        for (UUID memberUuid : party.members) {
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(memberUuid);
            if (member != null) {
                member.sendSystemMessage(
                    Component.literal(playerName + " left the party.").withStyle(ChatFormatting.YELLOW)
                );
            }
        }
    }

    private static void handleDisband(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerUuid = player.getUUID();

        UUID leaderUuid = playerToParty.get(playerUuid);
        if (leaderUuid == null) {
            sendResult(player, false, "You are not in a party.", eco);
            return;
        }

        if (!leaderUuid.equals(playerUuid)) {
            sendResult(player, false, "Only the party leader can disband the party.", eco);
            return;
        }

        Party party = parties.remove(leaderUuid);
        if (party == null) {
            playerToParty.remove(playerUuid);
            sendResult(player, true, "Party disbanded.", eco);
            return;
        }

        disbandParty(party, level);
        sendResult(player, true, "Party disbanded.", eco);
    }

    // --- Public API for other systems ---

    /**
     * Called when a player disconnects. If they are a leader, disbands the party.
     * If they are a member, removes them and notifies the leader.
     */
    public static void onPlayerDisconnect(ServerPlayer player, ServerLevel level) {
        UUID playerUuid = player.getUUID();
        updateNameCache(player);

        UUID leaderUuid = playerToParty.get(playerUuid);
        if (leaderUuid == null) {
            // Not in a party, but clean up any pending invite
            pendingInvites.remove(playerUuid);
            return;
        }

        if (leaderUuid.equals(playerUuid)) {
            // Leader disconnected — disband the party
            Party party = parties.remove(leaderUuid);
            if (party != null) {
                String leaderName = getPlayerName(playerUuid, level);
                // Remove all members (copy to avoid concurrent modification)
                for (UUID memberUuid : new ArrayList<>(party.members)) {
                    playerToParty.remove(memberUuid);
                    if (!memberUuid.equals(playerUuid)) {
                        ServerPlayer member = level.getServer().getPlayerList().getPlayer(memberUuid);
                        if (member != null) {
                            member.sendSystemMessage(
                                Component.literal("Party disbanded (" + leaderName + " disconnected).").withStyle(ChatFormatting.YELLOW)
                            );
                        }
                    }
                }
                // Clean up pending invites for this party (copy to avoid concurrent modification)
                for (UUID invitee : new ArrayList<>(party.pendingInvites)) {
                    pendingInvites.remove(invitee);
                }
            }
        } else {
            // Member disconnected — remove from party
            Party party = parties.get(leaderUuid);
            if (party != null) {
                party.members.remove(playerUuid);
                playerToParty.remove(playerUuid);

                String playerName = getPlayerName(playerUuid, level);
                for (UUID memberUuid : new ArrayList<>(party.members)) {
                    ServerPlayer member = level.getServer().getPlayerList().getPlayer(memberUuid);
                    if (member != null) {
                        member.sendSystemMessage(
                            Component.literal(playerName + " left the party (disconnected).").withStyle(ChatFormatting.YELLOW)
                        );
                    }
                }
            } else {
                playerToParty.remove(playerUuid);
            }
        }

        // Clean up any pending invite for this player
        pendingInvites.remove(playerUuid);

        // Remove disconnected player from name cache to bound memory
        nameCache.remove(playerUuid);
    }

    /**
     * Returns the set of all member UUIDs in the player's party (including the player).
     * Returns an empty set if the player is not in a party.
     */
    public static Set<UUID> getPartyMembers(UUID playerUuid) {
        UUID leaderUuid = playerToParty.get(playerUuid);
        if (leaderUuid == null) {
            return Collections.emptySet();
        }
        Party party = parties.get(leaderUuid);
        if (party == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(party.members);
    }

    /**
     * Returns true if the player is currently in a party.
     */
    public static boolean isInParty(UUID playerUuid) {
        return playerToParty.containsKey(playerUuid);
    }

    /** Returns the leader UUID for the given player's party, or null if not in a party. */
    public static UUID getPartyLeader(UUID playerUuid) {
        return playerToParty.get(playerUuid);
    }

    /** Returns the Party for the given leader UUID, or null if not found. */
    public static Party getParty(UUID leaderUuid) {
        return parties.get(leaderUuid);
    }

    /**
     * Returns an unmodifiable view of all active parties.
     */
    public static Map<UUID, Party> getAllParties() {
        return Collections.unmodifiableMap(parties);
    }

    /**
     * Returns a cached player name, or the UUID string if unknown.
     */
    public static String getCachedName(UUID uuid) {
        return nameCache.getOrDefault(uuid, uuid.toString().substring(0, 8));
    }

    // --- Data response ---

    private static void sendPartyData(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerUuid = player.getUUID();

        JsonObject root = new JsonObject();

        UUID leaderUuid = playerToParty.get(playerUuid);
        boolean inParty = leaderUuid != null && parties.containsKey(leaderUuid);

        root.addProperty("inParty", inParty);

        if (inParty) {
            Party party = parties.get(leaderUuid);

            // Leader info
            JsonObject leaderObj = new JsonObject();
            leaderObj.addProperty("name", getPlayerName(party.leader, level));
            leaderObj.addProperty("uuid", party.leader.toString());
            root.add("leader", leaderObj);

            // Members array
            JsonArray membersArr = new JsonArray();
            for (UUID memberUuid : party.members) {
                JsonObject m = new JsonObject();
                m.addProperty("name", getPlayerName(memberUuid, level));
                m.addProperty("uuid", memberUuid.toString());
                boolean online = level.getServer().getPlayerList().getPlayer(memberUuid) != null;
                m.addProperty("online", online);
                membersArr.add(m);
            }
            root.add("members", membersArr);

            // Pending invites (outgoing from this party)
            JsonArray pendingArr = new JsonArray();
            for (UUID inviteeUuid : party.pendingInvites) {
                JsonObject p = new JsonObject();
                p.addProperty("name", getPlayerName(inviteeUuid, level));
                p.addProperty("uuid", inviteeUuid.toString());
                pendingArr.add(p);
            }
            root.add("pendingInvites", pendingArr);
        }

        // Incoming invite (from another party's leader)
        UUID inviterLeader = pendingInvites.get(playerUuid);
        if (inviterLeader != null && !inParty) {
            JsonObject inviteObj = new JsonObject();
            inviteObj.addProperty("from", getPlayerName(inviterLeader, level));
            inviteObj.addProperty("fromUuid", inviterLeader.toString());
            root.add("incomingInvite", inviteObj);
        } else {
            root.add("incomingInvite", null);
        }

        sendResponse(player, "party_data", root.toString(), eco);
    }

    private static void sendResult(ServerPlayer player, boolean success, String message, EconomyManager eco) {
        JsonObject obj = new JsonObject();
        obj.addProperty("success", success);
        obj.addProperty("message", message);
        sendResponse(player, "party_result", obj.toString(), eco);
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }

    // --- Internal helpers ---

    private static void disbandParty(Party party, ServerLevel level) {
        String leaderName = getPlayerName(party.leader, level);

        // Remove all members from lookup (copy to avoid concurrent modification)
        for (UUID memberUuid : new ArrayList<>(party.members)) {
            playerToParty.remove(memberUuid);
            if (!memberUuid.equals(party.leader)) {
                ServerPlayer member = level.getServer().getPlayerList().getPlayer(memberUuid);
                if (member != null) {
                    member.sendSystemMessage(
                        Component.literal("The party has been disbanded by " + leaderName + ".").withStyle(ChatFormatting.YELLOW)
                    );
                }
            }
        }

        // Clean up pending invites for this party (copy to avoid concurrent modification)
        for (UUID invitee : new ArrayList<>(party.pendingInvites)) {
            pendingInvites.remove(invitee);
        }
    }

    private static void updateNameCache(ServerPlayer player) {
        nameCache.put(player.getUUID(), player.getGameProfile().name());
        // Evict stale entries if cache grows too large
        if (nameCache.size() > MAX_NAME_CACHE_SIZE) {
            evictStaleNameCacheEntries();
        }
    }

    /**
     * Removes nameCache entries for players not in any active party.
     */
    private static void evictStaleNameCacheEntries() {
        Set<UUID> activeUuids = new HashSet<>(playerToParty.keySet());
        // Also keep UUIDs with pending invites
        activeUuids.addAll(pendingInvites.keySet());
        nameCache.keySet().removeIf(uuid -> !activeUuids.contains(uuid));
    }

    private static String getPlayerName(UUID uuid, ServerLevel level) {
        ServerPlayer online = level.getServer().getPlayerList().getPlayer(uuid);
        if (online != null) {
            String name = online.getGameProfile().name();
            nameCache.put(uuid, name);
            return name;
        }
        return nameCache.getOrDefault(uuid, "Unknown");
    }

    /**
     * Clears all party state. Called on server shutdown/restart if needed.
     */
    public static void reset() {
        parties.clear();
        playerToParty.clear();
        pendingInvites.clear();
        nameCache.clear();
    }
}

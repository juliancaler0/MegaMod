package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.MegaMod;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MailHandler {

    private static final Map<UUID, List<MailData>> playerMail = new HashMap<>();
    // UUID -> name cache for offline player lookup (populated from mail data + online players)
    private static final Map<UUID, String> nameCache = new HashMap<>();
    private static boolean dirty = false;
    private static boolean loaded = false;
    private static final String FILE_NAME = "megamod_mail.dat";
    private static final int MAX_MAIL_PER_PLAYER = 100;
    private static final int MAX_BODY_LENGTH = 1000;
    private static final int MAX_SUBJECT_LENGTH = 100;

    public record MailData(String id, UUID fromUuid, String fromName, String subject, String body,
                           long timestamp, boolean read, String attachmentId, String attachmentName,
                           int attachmentCount, boolean attachmentClaimed) {}

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "mail_request": {
                ensureLoaded(level);
                sendMailData(player, eco);
                return true;
            }
            case "mail_send": {
                ensureLoaded(level);
                handleSend(player, jsonData, level, eco);
                return true;
            }
            case "mail_read": {
                ensureLoaded(level);
                handleMarkRead(player, jsonData, level, eco);
                return true;
            }
            case "mail_delete": {
                ensureLoaded(level);
                handleDelete(player, jsonData, level, eco);
                return true;
            }
            case "mail_claim": {
                ensureLoaded(level);
                handleClaim(player, jsonData, level, eco);
                return true;
            }
            case "mail_delete_read": {
                ensureLoaded(level);
                handleDeleteAllRead(player, level, eco);
                return true;
            }
            case "mail_attach_check": {
                handleAttachCheck(player, eco);
                return true;
            }
            default:
                return false;
        }
    }

    private static void handleSend(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String toName = obj.get("to").getAsString().trim();
            String subject = obj.get("subject").getAsString();
            String body = obj.get("body").getAsString();
            boolean attachItem = obj.has("attachItem") && obj.get("attachItem").getAsBoolean();

            // Enforce limits
            if (subject.length() > MAX_SUBJECT_LENGTH) {
                subject = subject.substring(0, MAX_SUBJECT_LENGTH);
            }
            if (body.length() > MAX_BODY_LENGTH) {
                body = body.substring(0, MAX_BODY_LENGTH);
            }

            // Cannot send to self
            if (toName.equalsIgnoreCase(player.getGameProfile().name())) {
                sendResponse(player, "mail_send_result", "{\"success\":false,\"error\":\"Cannot send mail to yourself\"}", eco);
                return;
            }

            // Find target player UUID - check online players first, then profile cache
            UUID targetUuid = null;

            // Update name cache with all currently online players
            for (ServerPlayer sp : level.getServer().getPlayerList().getPlayers()) {
                nameCache.put(sp.getUUID(), sp.getGameProfile().name());
            }

            // Check online players
            ServerPlayer onlineTarget = level.getServer().getPlayerList().getPlayerByName(toName);
            if (onlineTarget != null) {
                targetUuid = onlineTarget.getUUID();
                toName = onlineTarget.getGameProfile().name(); // get correct casing
            } else {
                // Offline player lookup via name cache (populated from mail data + online player history)
                for (Map.Entry<UUID, String> entry : nameCache.entrySet()) {
                    if (entry.getValue().equalsIgnoreCase(toName)) {
                        targetUuid = entry.getKey();
                        toName = entry.getValue(); // correct casing
                        break;
                    }
                }
            }

            if (targetUuid == null) {
                sendResponse(player, "mail_send_result", "{\"success\":false,\"error\":\"Player not found: " + escapeJson(toName) + "\"}", eco);
                return;
            }

            // Check recipient's inbox capacity
            List<MailData> targetInbox = playerMail.computeIfAbsent(targetUuid, k -> new ArrayList<>());
            if (targetInbox.size() >= MAX_MAIL_PER_PLAYER) {
                sendResponse(player, "mail_send_result", "{\"success\":false,\"error\":\"Recipient's mailbox is full\"}", eco);
                return;
            }

            // Handle attachment
            String attachmentId = "";
            String attachmentName = "";
            int attachmentCount = 0;

            if (attachItem) {
                ItemStack held = player.getMainHandItem();
                if (held.isEmpty() || held.getItem() == Items.AIR) {
                    sendResponse(player, "mail_send_result", "{\"success\":false,\"error\":\"No item in hand to attach\"}", eco);
                    return;
                }
                attachmentId = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
                attachmentName = held.getHoverName().getString();
                attachmentCount = 1;
                // Take one item from hand
                held.shrink(1);
            }

            // Create mail entry
            String mailId = "mail_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            MailData newMail = new MailData(
                mailId,
                player.getUUID(),
                player.getGameProfile().name(),
                subject,
                body,
                System.currentTimeMillis(),
                false,
                attachmentId,
                attachmentName,
                attachmentCount,
                false
            );

            // Add to front (newest first)
            targetInbox.add(0, newMail);
            dirty = true;
            saveToDisk(level);

            sendResponse(player, "mail_send_result", "{\"success\":true}", eco);

            // Notify recipient if online
            if (onlineTarget != null) {
                onlineTarget.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                        "[Mail] You have new mail from " + player.getGameProfile().name() + "!"
                    ).withStyle(net.minecraft.ChatFormatting.GOLD)
                );
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to handle mail attachment", e);
            sendResponse(player, "mail_send_result", "{\"success\":false,\"error\":\"Failed to send mail\"}", eco);
        }
    }

    private static void handleMarkRead(ServerPlayer player, String mailId, ServerLevel level, EconomyManager eco) {
        UUID playerId = player.getUUID();
        List<MailData> inbox = playerMail.get(playerId);
        if (inbox != null) {
            for (int i = 0; i < inbox.size(); i++) {
                MailData mail = inbox.get(i);
                if (mail.id.equals(mailId) && !mail.read) {
                    inbox.set(i, new MailData(
                        mail.id, mail.fromUuid, mail.fromName, mail.subject, mail.body,
                        mail.timestamp, true, mail.attachmentId, mail.attachmentName,
                        mail.attachmentCount, mail.attachmentClaimed
                    ));
                    dirty = true;
                    saveToDisk(level);
                    break;
                }
            }
        }
        sendMailData(player, eco);
    }

    private static void handleDelete(ServerPlayer player, String mailId, ServerLevel level, EconomyManager eco) {
        UUID playerId = player.getUUID();
        List<MailData> inbox = playerMail.get(playerId);
        if (inbox != null) {
            inbox.removeIf(m -> m.id.equals(mailId));
            dirty = true;
            saveToDisk(level);
        }
        sendMailData(player, eco);
    }

    private static void handleClaim(ServerPlayer player, String mailId, ServerLevel level, EconomyManager eco) {
        UUID playerId = player.getUUID();
        List<MailData> inbox = playerMail.get(playerId);
        if (inbox == null) {
            sendResponse(player, "mail_claim_result", "{\"success\":false}", eco);
            return;
        }

        for (int i = 0; i < inbox.size(); i++) {
            MailData mail = inbox.get(i);
            if (mail.id.equals(mailId)) {
                if (mail.attachmentClaimed || mail.attachmentId.isEmpty()) {
                    sendResponse(player, "mail_claim_result", "{\"success\":false}", eco);
                    return;
                }

                // Give item to player
                Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(mail.attachmentId));
                if (item == null || item == Items.AIR) {
                    sendResponse(player, "mail_claim_result", "{\"success\":false}", eco);
                    return;
                }

                ItemStack stack = new ItemStack(item, mail.attachmentCount);
                if (!player.getInventory().add(stack)) {
                    // Inventory full - drop at player's feet
                    player.drop(stack, false);
                }

                // Mark as claimed
                inbox.set(i, new MailData(
                    mail.id, mail.fromUuid, mail.fromName, mail.subject, mail.body,
                    mail.timestamp, mail.read, mail.attachmentId, mail.attachmentName,
                    mail.attachmentCount, true
                ));
                dirty = true;
                saveToDisk(level);
                sendResponse(player, "mail_claim_result", "{\"success\":true}", eco);
                sendMailData(player, eco);
                return;
            }
        }
        sendResponse(player, "mail_claim_result", "{\"success\":false}", eco);
    }

    private static void handleDeleteAllRead(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerId = player.getUUID();
        List<MailData> inbox = playerMail.get(playerId);
        if (inbox != null) {
            // Only delete read mail with no unclaimed attachment
            inbox.removeIf(m -> m.read && (m.attachmentId.isEmpty() || m.attachmentClaimed));
            dirty = true;
            saveToDisk(level);
        }
        sendMailData(player, eco);
    }

    private static void handleAttachCheck(ServerPlayer player, EconomyManager eco) {
        ItemStack held = player.getMainHandItem();
        JsonObject result = new JsonObject();
        if (held.isEmpty() || held.getItem() == Items.AIR) {
            result.addProperty("itemId", "");
            result.addProperty("itemName", "");
        } else {
            result.addProperty("itemId", BuiltInRegistries.ITEM.getKey(held.getItem()).toString());
            result.addProperty("itemName", held.getHoverName().getString());
        }
        sendResponse(player, "mail_attach_data", result.toString(), eco);
    }

    private static void sendMailData(ServerPlayer player, EconomyManager eco) {
        UUID playerId = player.getUUID();
        List<MailData> inbox = playerMail.getOrDefault(playerId, new ArrayList<>());

        JsonObject root = new JsonObject();
        JsonArray arr = new JsonArray();
        int unreadCount = 0;

        for (MailData mail : inbox) {
            JsonObject m = new JsonObject();
            m.addProperty("id", mail.id);
            m.addProperty("from", mail.fromName);
            m.addProperty("fromUuid", mail.fromUuid.toString());
            m.addProperty("subject", mail.subject);
            m.addProperty("body", mail.body);
            m.addProperty("timestamp", mail.timestamp);
            m.addProperty("read", mail.read);
            if (!mail.attachmentId.isEmpty()) {
                m.addProperty("attachmentId", mail.attachmentId);
                m.addProperty("attachmentName", mail.attachmentName);
                m.addProperty("attachmentCount", mail.attachmentCount);
                m.addProperty("attachmentClaimed", mail.attachmentClaimed);
            }
            arr.add(m);
            if (!mail.read) unreadCount++;
        }

        root.add("inbox", arr);
        root.addProperty("unreadCount", unreadCount);

        sendResponse(player, "mail_data", root.toString(), eco);
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }

    // --- NbtIo Persistence ---

    private static void ensureLoaded(ServerLevel level) {
        if (!loaded) {
            loadFromDisk(level);
            loaded = true;
        }
    }

    public static void loadFromDisk(ServerLevel level) {
        playerMail.clear();
        nameCache.clear();
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                CompoundTag players = root.getCompoundOrEmpty("players");
                for (String key : players.keySet()) {
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(key);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                    CompoundTag playerTag = players.getCompoundOrEmpty(key);
                    ListTag mailList = playerTag.getListOrEmpty("mail");
                    List<MailData> mails = new ArrayList<>();
                    for (int i = 0; i < mailList.size(); i++) {
                        CompoundTag mailTag = mailList.getCompoundOrEmpty(i);
                        String id = mailTag.getStringOr("id", "mail_0");
                        String fromUuidStr = mailTag.getStringOr("fromUuid", "");
                        UUID fromUuid;
                        try {
                            fromUuid = UUID.fromString(fromUuidStr);
                        } catch (IllegalArgumentException e) {
                            fromUuid = new UUID(0, 0);
                        }
                        String fromName = mailTag.getStringOr("fromName", "Unknown");
                        String subject = mailTag.getStringOr("subject", "");
                        String body = mailTag.getStringOr("body", "");
                        long timestamp = mailTag.getLongOr("timestamp", 0L);
                        boolean read = mailTag.getBooleanOr("read", false);
                        String attachmentId = mailTag.getStringOr("attachmentId", "");
                        String attachmentName = mailTag.getStringOr("attachmentName", "");
                        int attachmentCount = mailTag.getIntOr("attachmentCount", 0);
                        boolean attachmentClaimed = mailTag.getBooleanOr("attachmentClaimed", false);
                        mails.add(new MailData(id, fromUuid, fromName, subject, body,
                                timestamp, read, attachmentId, attachmentName, attachmentCount, attachmentClaimed));
                    }
                    playerMail.put(uuid, mails);
                }
            }
            // Populate name cache from all mail senders and recipients
            for (Map.Entry<UUID, List<MailData>> entry : playerMail.entrySet()) {
                for (MailData mail : entry.getValue()) {
                    if (mail.fromUuid != null && !mail.fromName.isEmpty() && !mail.fromName.equals("Unknown")) {
                        nameCache.put(mail.fromUuid, mail.fromName);
                    }
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load mail data", e);
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
            CompoundTag players = new CompoundTag();

            for (Map.Entry<UUID, List<MailData>> entry : playerMail.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                ListTag mailList = new ListTag();
                for (MailData mail : entry.getValue()) {
                    CompoundTag mailTag = new CompoundTag();
                    mailTag.putString("id", mail.id);
                    mailTag.putString("fromUuid", mail.fromUuid.toString());
                    mailTag.putString("fromName", mail.fromName);
                    mailTag.putString("subject", mail.subject);
                    mailTag.putString("body", mail.body);
                    mailTag.putLong("timestamp", mail.timestamp);
                    mailTag.putBoolean("read", mail.read);
                    mailTag.putString("attachmentId", mail.attachmentId);
                    mailTag.putString("attachmentName", mail.attachmentName);
                    mailTag.putInt("attachmentCount", mail.attachmentCount);
                    mailTag.putBoolean("attachmentClaimed", mail.attachmentClaimed);
                    mailList.add((Tag) mailTag);
                }
                playerTag.put("mail", (Tag) mailList);
                players.put(entry.getKey().toString(), (Tag) playerTag);
            }

            root.put("players", (Tag) players);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save mail data", e);
        }
    }

    public static void reset() {
        playerMail.clear();
        nameCache.clear();
        dirty = false;
        loaded = false;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Send a system-generated mail to a player (no sender player required).
     * Used by Death Recovery, bounty expiry, etc.
     */
    public static void sendSystemMail(UUID recipientUuid, String subject, String body, ServerLevel level) {
        ensureLoaded(level);
        List<MailData> inbox = playerMail.computeIfAbsent(recipientUuid, k -> new ArrayList<>());
        if (inbox.size() >= MAX_MAIL_PER_PLAYER) return;

        String mailId = "mail_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        MailData mail = new MailData(mailId, new UUID(0, 0), "System", subject, body,
            System.currentTimeMillis(), false, "", "", 0, false);
        inbox.add(0, mail);
        dirty = true;
        saveToDisk(level);
    }
}

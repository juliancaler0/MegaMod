package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.MegaMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NotesHandler {

    private static final Map<UUID, List<NoteData>> playerNotes = new HashMap<>();
    private static boolean dirty = false;
    private static boolean loaded = false;
    private static final String FILE_NAME = "megamod_notes.dat";
    private static final int MAX_NOTES_PER_PLAYER = 50;
    private static final int MAX_CONTENT_LENGTH = 10000;
    private static final int MAX_TITLE_LENGTH = 100;

    public record NoteData(String id, String title, String content, long lastModified) {}

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "notes_request": {
                ensureLoaded(level);
                sendNotesData(player, eco);
                return true;
            }
            case "notes_save": {
                ensureLoaded(level);
                handleSave(player, jsonData, level, eco);
                return true;
            }
            case "notes_delete": {
                ensureLoaded(level);
                handleDelete(player, jsonData, level, eco);
                return true;
            }
            case "notes_create": {
                ensureLoaded(level);
                handleCreate(player, level, eco);
                return true;
            }
            default:
                return false;
        }
    }

    private static void handleSave(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String id = obj.get("id").getAsString();
            String title = obj.get("title").getAsString();
            String content = obj.get("content").getAsString();

            // Enforce limits
            if (title.length() > MAX_TITLE_LENGTH) {
                title = title.substring(0, MAX_TITLE_LENGTH);
            }
            if (content.length() > MAX_CONTENT_LENGTH) {
                content = content.substring(0, MAX_CONTENT_LENGTH);
            }

            UUID playerId = player.getUUID();
            List<NoteData> notes = playerNotes.computeIfAbsent(playerId, k -> new ArrayList<>());

            // Find and update existing note
            for (int i = 0; i < notes.size(); i++) {
                if (notes.get(i).id.equals(id)) {
                    notes.set(i, new NoteData(id, title, content, System.currentTimeMillis()));
                    dirty = true;
                    saveToDisk(level);
                    sendNotesData(player, eco);
                    return;
                }
            }
            // Note not found — still send response so client isn't stuck
            sendNotesData(player, eco);
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to handle notes action", e);
        }
    }

    private static void handleDelete(ServerPlayer player, String noteId, ServerLevel level, EconomyManager eco) {
        UUID playerId = player.getUUID();
        List<NoteData> notes = playerNotes.get(playerId);
        if (notes != null) {
            notes.removeIf(n -> n.id.equals(noteId));
            dirty = true;
            saveToDisk(level);
        }
        sendNotesData(player, eco);
    }

    private static void handleCreate(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        UUID playerId = player.getUUID();
        List<NoteData> notes = playerNotes.computeIfAbsent(playerId, k -> new ArrayList<>());

        if (notes.size() >= MAX_NOTES_PER_PLAYER) {
            // Send current data back without creating
            sendNotesData(player, eco);
            return;
        }

        String newId = "note_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        NoteData newNote = new NoteData(newId, "", "", System.currentTimeMillis());
        notes.add(0, newNote); // Add to front so it appears first
        dirty = true;
        saveToDisk(level);
        sendNotesData(player, eco);
    }

    private static void sendNotesData(ServerPlayer player, EconomyManager eco) {
        UUID playerId = player.getUUID();
        List<NoteData> notes = playerNotes.getOrDefault(playerId, new ArrayList<>());

        JsonObject root = new JsonObject();
        JsonArray arr = new JsonArray();
        for (NoteData note : notes) {
            JsonObject n = new JsonObject();
            n.addProperty("id", note.id);
            n.addProperty("title", note.title);
            n.addProperty("content", note.content);
            n.addProperty("lastModified", note.lastModified);
            arr.add(n);
        }
        root.add("notes", arr);

        sendResponse(player, "notes_data", root.toString(), eco);
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
        playerNotes.clear();
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
                    ListTag notesList = playerTag.getListOrEmpty("notes");
                    List<NoteData> notes = new ArrayList<>();
                    for (int i = 0; i < notesList.size(); i++) {
                        CompoundTag noteTag = notesList.getCompoundOrEmpty(i);
                        String id = noteTag.getStringOr("id", "note_0");
                        String title = noteTag.getStringOr("title", "");
                        String content = noteTag.getStringOr("content", "");
                        long lastModified = noteTag.getLongOr("lastModified", 0L);
                        notes.add(new NoteData(id, title, content, lastModified));
                    }
                    playerNotes.put(uuid, notes);
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load notes data", e);
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

            for (Map.Entry<UUID, List<NoteData>> entry : playerNotes.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                ListTag notesList = new ListTag();
                for (NoteData note : entry.getValue()) {
                    CompoundTag noteTag = new CompoundTag();
                    noteTag.putString("id", note.id);
                    noteTag.putString("title", note.title);
                    noteTag.putString("content", note.content);
                    noteTag.putLong("lastModified", note.lastModified);
                    notesList.add((Tag) noteTag);
                }
                playerTag.put("notes", (Tag) notesList);
                players.put(entry.getKey().toString(), (Tag) playerTag);
            }

            root.put("players", (Tag) players);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save notes data", e);
        }
    }

    public static void reset() {
        playerNotes.clear();
        dirty = false;
        loaded = false;
    }
}

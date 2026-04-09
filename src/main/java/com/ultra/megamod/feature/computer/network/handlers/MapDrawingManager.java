package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MapDrawingManager {

    private static final String FILE_NAME = "megamod_map_drawings.dat";
    private static final int MAX_LINES_PER_PLAYER = 50;
    private static final int MAX_TEXTS_PER_PLAYER = 50;
    private static final int MAX_SHARED_LINES = 100;
    private static final int MAX_SHARED_TEXTS = 100;

    private static final Map<UUID, List<DrawingLine>> playerLines = new HashMap<>();
    private static final Map<UUID, List<DrawingText>> playerTexts = new HashMap<>();
    private static final List<DrawingLine> sharedLines = new ArrayList<>();
    private static final List<DrawingText> sharedTexts = new ArrayList<>();
    private static boolean dirty = false;
    private static boolean loaded = false;

    public record DrawingLine(String id, int x1, int z1, int x2, int z2, int color, String dimension) {}
    public record DrawingText(String id, String text, int x, int z, int color, String dimension) {}

    public static void ensureLoaded(ServerLevel level) {
        if (!loaded) {
            loadFromDisk(level);
        }
    }

    public static void addLine(UUID player, DrawingLine line, ServerLevel level) {
        List<DrawingLine> lines = playerLines.computeIfAbsent(player, k -> new ArrayList<>());
        if (lines.size() >= MAX_LINES_PER_PLAYER) return;
        lines.add(line);
        dirty = true;
        saveToDisk(level);
    }

    public static void addText(UUID player, DrawingText text, ServerLevel level) {
        List<DrawingText> texts = playerTexts.computeIfAbsent(player, k -> new ArrayList<>());
        if (texts.size() >= MAX_TEXTS_PER_PLAYER) return;
        texts.add(text);
        dirty = true;
        saveToDisk(level);
    }

    public static void deleteLine(UUID player, String id, ServerLevel level) {
        List<DrawingLine> lines = playerLines.get(player);
        if (lines != null) {
            lines.removeIf(l -> l.id.equals(id));
            dirty = true;
            saveToDisk(level);
        }
        // Also check shared lines
        if (sharedLines.removeIf(l -> l.id.equals(id))) {
            dirty = true;
            saveToDisk(level);
        }
    }

    public static void deleteText(UUID player, String id, ServerLevel level) {
        List<DrawingText> texts = playerTexts.get(player);
        if (texts != null) {
            texts.removeIf(t -> t.id.equals(id));
            dirty = true;
            saveToDisk(level);
        }
        // Also check shared texts
        if (sharedTexts.removeIf(t -> t.id.equals(id))) {
            dirty = true;
            saveToDisk(level);
        }
    }

    public static void addSharedLine(DrawingLine line, ServerLevel level) {
        if (sharedLines.size() >= MAX_SHARED_LINES) return;
        sharedLines.add(line);
        dirty = true;
        saveToDisk(level);
    }

    public static void addSharedText(DrawingText text, ServerLevel level) {
        if (sharedTexts.size() >= MAX_SHARED_TEXTS) return;
        sharedTexts.add(text);
        dirty = true;
        saveToDisk(level);
    }

    public static String buildDrawingsJson(UUID player, String dimension) {
        StringBuilder sb = new StringBuilder("{\"lines\":[");
        boolean first = true;

        // Player lines for this dimension
        List<DrawingLine> pLines = playerLines.getOrDefault(player, new ArrayList<>());
        for (DrawingLine line : pLines) {
            if (!dimension.equals(line.dimension)) continue;
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"id\":\"").append(escapeJson(line.id)).append("\"");
            sb.append(",\"x1\":").append(line.x1);
            sb.append(",\"z1\":").append(line.z1);
            sb.append(",\"x2\":").append(line.x2);
            sb.append(",\"z2\":").append(line.z2);
            sb.append(",\"color\":").append(line.color);
            sb.append(",\"shared\":false}");
        }

        // Shared lines for this dimension
        for (DrawingLine line : sharedLines) {
            if (!dimension.equals(line.dimension)) continue;
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"id\":\"").append(escapeJson(line.id)).append("\"");
            sb.append(",\"x1\":").append(line.x1);
            sb.append(",\"z1\":").append(line.z1);
            sb.append(",\"x2\":").append(line.x2);
            sb.append(",\"z2\":").append(line.z2);
            sb.append(",\"color\":").append(line.color);
            sb.append(",\"shared\":true}");
        }

        sb.append("],\"texts\":[");
        first = true;

        // Player texts for this dimension
        List<DrawingText> pTexts = playerTexts.getOrDefault(player, new ArrayList<>());
        for (DrawingText text : pTexts) {
            if (!dimension.equals(text.dimension)) continue;
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"id\":\"").append(escapeJson(text.id)).append("\"");
            sb.append(",\"text\":\"").append(escapeJson(text.text)).append("\"");
            sb.append(",\"x\":").append(text.x);
            sb.append(",\"z\":").append(text.z);
            sb.append(",\"color\":").append(text.color);
            sb.append(",\"shared\":false}");
        }

        // Shared texts for this dimension
        for (DrawingText text : sharedTexts) {
            if (!dimension.equals(text.dimension)) continue;
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"id\":\"").append(escapeJson(text.id)).append("\"");
            sb.append(",\"text\":\"").append(escapeJson(text.text)).append("\"");
            sb.append(",\"x\":").append(text.x);
            sb.append(",\"z\":").append(text.z);
            sb.append(",\"color\":").append(text.color);
            sb.append(",\"shared\":true}");
        }

        sb.append("]}");
        return sb.toString();
    }

    public static void loadFromDisk(ServerLevel level) {
        playerLines.clear();
        playerTexts.clear();
        sharedLines.clear();
        sharedTexts.clear();
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());

                // Player drawings
                CompoundTag players = root.getCompoundOrEmpty("players");
                for (String uuidStr : players.keySet()) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        CompoundTag playerTag = players.getCompoundOrEmpty(uuidStr);

                        // Lines
                        ListTag lineList = playerTag.getListOrEmpty("lines");
                        List<DrawingLine> lines = new ArrayList<>();
                        for (int i = 0; i < lineList.size(); i++) {
                            CompoundTag lineTag = lineList.getCompoundOrEmpty(i);
                            lines.add(new DrawingLine(
                                lineTag.getStringOr("id", ""),
                                lineTag.getIntOr("x1", 0),
                                lineTag.getIntOr("z1", 0),
                                lineTag.getIntOr("x2", 0),
                                lineTag.getIntOr("z2", 0),
                                lineTag.getIntOr("color", 0xFFFFFFFF),
                                lineTag.getStringOr("dimension", "minecraft:overworld")
                            ));
                        }
                        playerLines.put(uuid, lines);

                        // Texts
                        ListTag textList = playerTag.getListOrEmpty("texts");
                        List<DrawingText> texts = new ArrayList<>();
                        for (int i = 0; i < textList.size(); i++) {
                            CompoundTag textTag = textList.getCompoundOrEmpty(i);
                            texts.add(new DrawingText(
                                textTag.getStringOr("id", ""),
                                textTag.getStringOr("text", ""),
                                textTag.getIntOr("x", 0),
                                textTag.getIntOr("z", 0),
                                textTag.getIntOr("color", 0xFFFFFFFF),
                                textTag.getStringOr("dimension", "minecraft:overworld")
                            ));
                        }
                        playerTexts.put(uuid, texts);
                    } catch (Exception ignored) {}
                }

                // Shared drawings
                CompoundTag shared = root.getCompoundOrEmpty("shared");
                ListTag sharedLineList = shared.getListOrEmpty("lines");
                for (int i = 0; i < sharedLineList.size(); i++) {
                    CompoundTag lineTag = sharedLineList.getCompoundOrEmpty(i);
                    sharedLines.add(new DrawingLine(
                        lineTag.getStringOr("id", ""),
                        lineTag.getIntOr("x1", 0),
                        lineTag.getIntOr("z1", 0),
                        lineTag.getIntOr("x2", 0),
                        lineTag.getIntOr("z2", 0),
                        lineTag.getIntOr("color", 0xFFFFFFFF),
                        lineTag.getStringOr("dimension", "minecraft:overworld")
                    ));
                }
                ListTag sharedTextList = shared.getListOrEmpty("texts");
                for (int i = 0; i < sharedTextList.size(); i++) {
                    CompoundTag textTag = sharedTextList.getCompoundOrEmpty(i);
                    sharedTexts.add(new DrawingText(
                        textTag.getStringOr("id", ""),
                        textTag.getStringOr("text", ""),
                        textTag.getIntOr("x", 0),
                        textTag.getIntOr("z", 0),
                        textTag.getIntOr("color", 0xFFFFFFFF),
                        textTag.getStringOr("dimension", "minecraft:overworld")
                    ));
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load map drawing data", e);
        }
        loaded = true;
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

            // Player drawings
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, List<DrawingLine>> entry : playerLines.entrySet()) {
                String uuidStr = entry.getKey().toString();
                CompoundTag playerTag = players.getCompoundOrEmpty(uuidStr);
                // Write lines
                ListTag lineList = new ListTag();
                for (DrawingLine line : entry.getValue()) {
                    CompoundTag lineTag = new CompoundTag();
                    lineTag.putString("id", line.id);
                    lineTag.putInt("x1", line.x1);
                    lineTag.putInt("z1", line.z1);
                    lineTag.putInt("x2", line.x2);
                    lineTag.putInt("z2", line.z2);
                    lineTag.putInt("color", line.color);
                    lineTag.putString("dimension", line.dimension);
                    lineList.add((Tag) lineTag);
                }
                playerTag.put("lines", (Tag) lineList);
                // Write texts
                List<DrawingText> pTexts = playerTexts.getOrDefault(entry.getKey(), new ArrayList<>());
                ListTag textList = new ListTag();
                for (DrawingText text : pTexts) {
                    CompoundTag textTag = new CompoundTag();
                    textTag.putString("id", text.id);
                    textTag.putString("text", text.text);
                    textTag.putInt("x", text.x);
                    textTag.putInt("z", text.z);
                    textTag.putInt("color", text.color);
                    textTag.putString("dimension", text.dimension);
                    textList.add((Tag) textTag);
                }
                playerTag.put("texts", (Tag) textList);
                players.put(uuidStr, (Tag) playerTag);
            }
            // Also save text-only players (who have texts but no lines)
            for (Map.Entry<UUID, List<DrawingText>> entry : playerTexts.entrySet()) {
                String uuidStr = entry.getKey().toString();
                if (!playerLines.containsKey(entry.getKey())) {
                    CompoundTag playerTag = new CompoundTag();
                    playerTag.put("lines", (Tag) new ListTag());
                    ListTag textList = new ListTag();
                    for (DrawingText text : entry.getValue()) {
                        CompoundTag textTag = new CompoundTag();
                        textTag.putString("id", text.id);
                        textTag.putString("text", text.text);
                        textTag.putInt("x", text.x);
                        textTag.putInt("z", text.z);
                        textTag.putInt("color", text.color);
                        textTag.putString("dimension", text.dimension);
                        textList.add((Tag) textTag);
                    }
                    playerTag.put("texts", (Tag) textList);
                    players.put(uuidStr, (Tag) playerTag);
                }
            }
            root.put("players", (Tag) players);

            // Shared drawings
            CompoundTag shared = new CompoundTag();
            ListTag sharedLineList = new ListTag();
            for (DrawingLine line : sharedLines) {
                CompoundTag lineTag = new CompoundTag();
                lineTag.putString("id", line.id);
                lineTag.putInt("x1", line.x1);
                lineTag.putInt("z1", line.z1);
                lineTag.putInt("x2", line.x2);
                lineTag.putInt("z2", line.z2);
                lineTag.putInt("color", line.color);
                lineTag.putString("dimension", line.dimension);
                sharedLineList.add((Tag) lineTag);
            }
            shared.put("lines", (Tag) sharedLineList);
            ListTag sharedTextList = new ListTag();
            for (DrawingText text : sharedTexts) {
                CompoundTag textTag = new CompoundTag();
                textTag.putString("id", text.id);
                textTag.putString("text", text.text);
                textTag.putInt("x", text.x);
                textTag.putInt("z", text.z);
                textTag.putInt("color", text.color);
                textTag.putString("dimension", text.dimension);
                sharedTextList.add((Tag) textTag);
            }
            shared.put("texts", (Tag) sharedTextList);
            root.put("shared", (Tag) shared);

            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save map drawing data", e);
        }
    }

    public static void reset() {
        playerLines.clear();
        playerTexts.clear();
        sharedLines.clear();
        sharedTexts.clear();
        dirty = false;
        loaded = false;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}

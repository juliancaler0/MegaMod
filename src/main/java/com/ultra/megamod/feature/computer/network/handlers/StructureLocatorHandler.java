package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StructureLocatorHandler {

    // Structure display names mapped by their locate command IDs
    private static final String[][] STRUCTURES = {
        {"Village", "minecraft:village"},
        {"Desert Temple", "minecraft:desert_pyramid"},
        {"Jungle Temple", "minecraft:jungle_pyramid"},
        {"Swamp Hut", "minecraft:swamp_hut"},
        {"Igloo", "minecraft:igloo"},
        {"Stronghold", "minecraft:stronghold"},
        {"Monument", "minecraft:monument"},
        {"Fortress", "minecraft:fortress"},
        {"End City", "minecraft:end_city"},
        {"Mansion", "minecraft:mansion"},
        {"Trail Ruins", "minecraft:trail_ruins"},
        {"Trial Chambers", "minecraft:trial_chambers"},
        {"Ancient City", "minecraft:ancient_city"},
        {"Ruined Portal", "minecraft:ruined_portal"},
        {"Mineshaft", "minecraft:mineshaft"},
        {"Pillager Outpost", "minecraft:pillager_outpost"},
        {"Ocean Ruin", "minecraft:ocean_ruin"},
        {"Shipwreck", "minecraft:shipwreck"},
        {"Bastion", "minecraft:bastion_remnant"}
    };

    // Pattern to parse /locate output: "The nearest X is at [X, ~, Z] (N blocks away)"
    // Also handles: "The nearest X is at [X, Y, Z] (N blocks away)"
    private static final Pattern LOCATE_PATTERN = Pattern.compile("\\[(-?\\d+),\\s*~?,?\\s*(-?\\d+)?(?:,\\s*)?(-?\\d+)]\\s*\\((\\d+)\\s*block");

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!AdminSystem.isAdmin(player)) {
            return false;
        }

        switch (action) {
            case "locate_structure": {
                String structureId = jsonData.trim();
                String displayName = getStructureName(structureId);
                String result = AdminSystem.executeCommand(player, "locate structure " + structureId);
                LocateParsed parsed = parseLocateOutput(result, player);

                if (parsed != null) {
                    String json = buildSingleResult(displayName, parsed.x, parsed.y, parsed.z, parsed.distance, "structure", true);
                    sendResponse(player, "locate_result", json, eco);
                } else {
                    String json = "{\"name\":\"" + escapeJson(displayName) + "\",\"found\":false}";
                    sendResponse(player, "locate_result", json, eco);
                }
                return true;
            }

            case "locate_biome": {
                String biomeId = jsonData.trim();
                String displayName = getBiomeName(biomeId);
                String result = AdminSystem.executeCommand(player, "locate biome " + biomeId);
                LocateParsed parsed = parseLocateOutput(result, player);

                if (parsed != null) {
                    String json = buildSingleResult(displayName, parsed.x, parsed.y, parsed.z, parsed.distance, "biome", true);
                    sendResponse(player, "locate_result", json, eco);
                } else {
                    String json = "{\"name\":\"" + escapeJson(displayName) + "\",\"found\":false}";
                    sendResponse(player, "locate_result", json, eco);
                }
                return true;
            }

            case "locate_all_structures": {
                List<String> resultEntries = new ArrayList<>();

                for (String[] structure : STRUCTURES) {
                    String displayName = structure[0];
                    String structureId = structure[1];
                    try {
                        String result = AdminSystem.executeCommand(player, "locate structure " + structureId);
                        LocateParsed parsed = parseLocateOutput(result, player);
                        if (parsed != null) {
                            resultEntries.add("{\"name\":\"" + escapeJson(displayName) + "\""
                                + ",\"x\":" + parsed.x
                                + ",\"y\":" + parsed.y
                                + ",\"z\":" + parsed.z
                                + ",\"distance\":" + parsed.distance
                                + ",\"locType\":\"structure\"}");
                        }
                    } catch (Exception ignored) {
                        // Skip structures that fail (e.g., End City when in Overworld)
                    }
                }

                // Sort by distance (parse from the JSON entries)
                resultEntries.sort((a, b) -> {
                    int distA = extractDistance(a);
                    int distB = extractDistance(b);
                    return Integer.compare(distA, distB);
                });

                StringBuilder sb = new StringBuilder("{\"results\":[");
                for (int i = 0; i < resultEntries.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(resultEntries.get(i));
                }
                sb.append("]}");

                sendResponse(player, "locate_all_result", sb.toString(), eco);
                return true;
            }

            case "locate_teleport": {
                try {
                    String[] parts = jsonData.split(":");
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[2]);

                    // Teleport the player
                    player.teleportTo(level, x + 0.5, y, z + 0.5, Set.of(), player.getYRot(), player.getXRot(), false);

                    String json = "{\"success\":true,\"msg\":\"Teleported to " + x + ", " + y + ", " + z + "\"}";
                    sendResponse(player, "locate_teleport_result", json, eco);
                } catch (Exception e) {
                    String json = "{\"success\":false,\"msg\":\"Failed to teleport: " + escapeJson(e.getMessage()) + "\"}";
                    sendResponse(player, "locate_teleport_result", json, eco);
                }
                return true;
            }

            default:
                return false;
        }
    }

    private static LocateParsed parseLocateOutput(String output, ServerPlayer player) {
        if (output == null || output.isEmpty()) return null;

        Matcher matcher = LOCATE_PATTERN.matcher(output);
        if (matcher.find()) {
            try {
                int x = Integer.parseInt(matcher.group(1));
                // Group 2 may be the Y or the Z depending on format
                // Format 1: [X, ~, Z] (distance blocks away) - 2 coords with ~
                // Format 2: [X, Y, Z] (distance blocks away) - 3 coords
                int y;
                int z;
                int distance;

                if (matcher.group(2) != null && matcher.group(3) != null) {
                    // Three groups matched: X, Y, Z
                    y = Integer.parseInt(matcher.group(2));
                    z = Integer.parseInt(matcher.group(3));
                } else if (matcher.group(2) != null) {
                    // Two groups matched: X and Z (Y was ~)
                    y = (int) player.getY();
                    z = Integer.parseInt(matcher.group(2));
                } else {
                    return null;
                }

                distance = Integer.parseInt(matcher.group(4));
                return new LocateParsed(x, y, z, distance);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Fallback: try a simpler pattern for the common format "[X, ~, Z]"
        Pattern simplePattern = Pattern.compile("\\[(-?\\d+),\\s*~,\\s*(-?\\d+)].*?\\((\\d+)");
        Matcher simpleMatcher = simplePattern.matcher(output);
        if (simpleMatcher.find()) {
            try {
                int x = Integer.parseInt(simpleMatcher.group(1));
                int z = Integer.parseInt(simpleMatcher.group(2));
                int distance = Integer.parseInt(simpleMatcher.group(3));
                int y = (int) player.getY();
                return new LocateParsed(x, y, z, distance);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    private static String buildSingleResult(String name, int x, int y, int z, int distance, String locType, boolean found) {
        return "{\"name\":\"" + escapeJson(name) + "\""
            + ",\"x\":" + x
            + ",\"y\":" + y
            + ",\"z\":" + z
            + ",\"distance\":" + distance
            + ",\"locType\":\"" + locType + "\""
            + ",\"found\":" + found + "}";
    }

    private static String getStructureName(String structureId) {
        for (String[] structure : STRUCTURES) {
            if (structure[1].equals(structureId)) {
                return structure[0];
            }
        }
        // Fallback: derive from ID
        String name = structureId.contains(":") ? structureId.substring(structureId.indexOf(':') + 1) : structureId;
        name = name.replace('_', ' ');
        // Title case
        StringBuilder sb = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!sb.isEmpty()) sb.append(" ");
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) sb.append(word.substring(1));
            }
        }
        return sb.toString();
    }

    private static String getBiomeName(String biomeId) {
        String name = biomeId.contains(":") ? biomeId.substring(biomeId.indexOf(':') + 1) : biomeId;
        name = name.replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!sb.isEmpty()) sb.append(" ");
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) sb.append(word.substring(1));
            }
        }
        return sb.toString();
    }

    private static int extractDistance(String jsonEntry) {
        // Quick extraction of "distance":NNN from a JSON string
        int idx = jsonEntry.indexOf("\"distance\":");
        if (idx < 0) return Integer.MAX_VALUE;
        int start = idx + 11;
        int end = start;
        while (end < jsonEntry.length() && (Character.isDigit(jsonEntry.charAt(end)) || jsonEntry.charAt(end) == '-')) {
            end++;
        }
        try {
            return Integer.parseInt(jsonEntry.substring(start, end));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer((ServerPlayer) player, (CustomPacketPayload) new ComputerDataPayload(type, json, wallet, bank), (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private record LocateParsed(int x, int y, int z, int distance) {}
}

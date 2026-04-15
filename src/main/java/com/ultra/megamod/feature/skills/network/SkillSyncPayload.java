/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.Identifier
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 */
package com.ultra.megamod.feature.skills.network;

import com.ultra.megamod.feature.skills.SkillTreeType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SkillSyncPayload(String jsonData) implements CustomPacketPayload
{
    public static volatile Map<SkillTreeType, Integer> clientLevels = new EnumMap<SkillTreeType, Integer>(SkillTreeType.class);
    public static volatile Map<SkillTreeType, Integer> clientXp = new EnumMap<SkillTreeType, Integer>(SkillTreeType.class);
    public static volatile int clientPoints = 0;
    public static volatile Map<SkillTreeType, Integer> clientTreePoints = new EnumMap<SkillTreeType, Integer>(SkillTreeType.class);
    public static volatile Set<String> clientUnlockedNodes = new HashSet<String>();
    public static volatile Set<String> clientActiveSynergies = new HashSet<String>();
    public static volatile List<LeaderboardEntry> clientLeaderboard = new ArrayList<>();
    /** Computed skill bonuses (attribute name → applied modifier value) for overlay display. */
    public static volatile Map<String, Double> clientSkillBonuses = new HashMap<String, Double>();
    /** Per-tree prestige levels synced from server. */
    public static volatile Map<SkillTreeType, Integer> clientPrestige = new EnumMap<SkillTreeType, Integer>(SkillTreeType.class);
    /** True if this player has admin lock bypass active (admin + toggle enabled). */
    public static volatile boolean clientAdminLockBypass = false;

    /**
     * Class-branch point count retired with the class-selection system.
     * Returns 0 — callers should no longer rely on class-branch scaling.
     */
    public static int getClassBranchPoints() {
        return 0;
    }

    public record LeaderboardEntry(String name, int level) {}
    public static final CustomPacketPayload.Type<SkillSyncPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"skill_sync"));
    public static final StreamCodec<FriendlyByteBuf, SkillSyncPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, SkillSyncPayload>(){

        public SkillSyncPayload decode(FriendlyByteBuf buf) {
            return new SkillSyncPayload(buf.readUtf(Short.MAX_VALUE));
        }

        public void encode(FriendlyByteBuf buf, SkillSyncPayload payload) {
            buf.writeUtf(payload.jsonData(), Short.MAX_VALUE);
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(SkillSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> SkillSyncPayload.parseJsonToClient(payload.jsonData()));
    }

    private static void parseJsonToClient(String json) {
        try {
            String xpBlock;
            EnumMap<SkillTreeType, Integer> levels = new EnumMap<SkillTreeType, Integer>(SkillTreeType.class);
            EnumMap<SkillTreeType, Integer> xp = new EnumMap<SkillTreeType, Integer>(SkillTreeType.class);
            HashSet<String> nodes = new HashSet<String>();
            int points = 0;
            String levelsBlock = SkillSyncPayload.extractBlock(json, "\"levels\"");
            if (levelsBlock != null) {
                for (SkillTreeType type : SkillTreeType.values()) {
                    int val = SkillSyncPayload.extractInt(levelsBlock, "\"" + type.name() + "\"");
                    levels.put(type, val);
                }
            }
            if ((xpBlock = SkillSyncPayload.extractBlock(json, "\"xp\"")) != null) {
                for (SkillTreeType type : SkillTreeType.values()) {
                    int val = SkillSyncPayload.extractInt(xpBlock, "\"" + type.name() + "\"");
                    xp.put(type, val);
                }
            }
            points = SkillSyncPayload.extractInt(json, "\"points\"");
            EnumMap<SkillTreeType, Integer> treePoints = new EnumMap<SkillTreeType, Integer>(SkillTreeType.class);
            String treePointsBlock = SkillSyncPayload.extractBlock(json, "\"tree_points\"");
            if (treePointsBlock != null) {
                for (SkillTreeType type : SkillTreeType.values()) {
                    int val = SkillSyncPayload.extractInt(treePointsBlock, "\"" + type.name() + "\"");
                    treePoints.put(type, val);
                }
            }
            String nodesBlock = SkillSyncPayload.extractArray(json, "\"nodes\"");
            if (nodesBlock != null) {
                String inner = nodesBlock.trim();
                if (inner.startsWith("[")) {
                    inner = inner.substring(1);
                }
                if (inner.endsWith("]")) {
                    inner = inner.substring(0, inner.length() - 1);
                }
                if (!inner.isEmpty()) {
                    String[] parts;
                    for (String part : parts = inner.split(",")) {
                        String nodeId = part.trim();
                        if (nodeId.startsWith("\"") && nodeId.endsWith("\"")) {
                            nodeId = nodeId.substring(1, nodeId.length() - 1);
                        }
                        if (nodeId.isEmpty()) continue;
                        nodes.add(nodeId);
                    }
                }
            }
            // Parse synergies array
            HashSet<String> synergies = new HashSet<String>();
            String synergiesBlock = SkillSyncPayload.extractArray(json, "\"synergies\"");
            if (synergiesBlock != null) {
                String sInner = synergiesBlock.trim();
                if (sInner.startsWith("[")) {
                    sInner = sInner.substring(1);
                }
                if (sInner.endsWith("]")) {
                    sInner = sInner.substring(0, sInner.length() - 1);
                }
                if (!sInner.isEmpty()) {
                    String[] sParts;
                    for (String sPart : sParts = sInner.split(",")) {
                        String synId = sPart.trim();
                        if (synId.startsWith("\"") && synId.endsWith("\"")) {
                            synId = synId.substring(1, synId.length() - 1);
                        }
                        if (synId.isEmpty()) continue;
                        synergies.add(synId);
                    }
                }
            }
            // Parse leaderboard array of objects
            ArrayList<LeaderboardEntry> leaderboard = new ArrayList<>();
            String lbBlock = SkillSyncPayload.extractArray(json, "\"leaderboard\"");
            if (lbBlock != null) {
                String lbInner = lbBlock.trim();
                if (lbInner.startsWith("[")) lbInner = lbInner.substring(1);
                if (lbInner.endsWith("]")) lbInner = lbInner.substring(0, lbInner.length() - 1);
                // Split by },{
                String[] entries = lbInner.split("\\},\\s*\\{");
                for (String entry : entries) {
                    String e = entry.replace("{", "").replace("}", "").trim();
                    if (e.isEmpty()) continue;
                    String entryName = "Unknown";
                    int entryLevel = 0;
                    // Extract "name":"value"
                    int nameIdx = e.indexOf("\"name\"");
                    if (nameIdx >= 0) {
                        int firstQuote = e.indexOf('"', nameIdx + 6);
                        if (firstQuote >= 0) {
                            int colonAfter = e.indexOf(':', nameIdx);
                            int qStart = e.indexOf('"', colonAfter + 1);
                            int qEnd = e.indexOf('"', qStart + 1);
                            if (qStart >= 0 && qEnd > qStart) {
                                entryName = e.substring(qStart + 1, qEnd);
                            }
                        }
                    }
                    // Extract "level":value
                    int lvlIdx = e.indexOf("\"level\"");
                    if (lvlIdx >= 0) {
                        int colon = e.indexOf(':', lvlIdx);
                        if (colon >= 0) {
                            StringBuilder numSb = new StringBuilder();
                            for (int ci = colon + 1; ci < e.length(); ci++) {
                                char ch = e.charAt(ci);
                                if (ch >= '0' && ch <= '9') numSb.append(ch);
                                else if (numSb.length() > 0) break;
                            }
                            if (numSb.length() > 0) entryLevel = Integer.parseInt(numSb.toString());
                        }
                    }
                    leaderboard.add(new LeaderboardEntry(entryName, entryLevel));
                }
            }
            // Parse skill_bonuses object
            HashMap<String, Double> skillBonuses = new HashMap<>();
            String bonusBlock = SkillSyncPayload.extractBlock(json, "\"skill_bonuses\"");
            if (bonusBlock != null) {
                // Simple key:value parsing for "attr_name":1.234
                String bInner = bonusBlock.trim();
                if (bInner.startsWith("{")) bInner = bInner.substring(1);
                if (bInner.endsWith("}")) bInner = bInner.substring(0, bInner.length() - 1);
                String[] bParts = bInner.split(",");
                for (String bp : bParts) {
                    bp = bp.trim();
                    if (bp.isEmpty()) continue;
                    int colonIdx = bp.indexOf(':');
                    if (colonIdx < 0) continue;
                    String key = bp.substring(0, colonIdx).trim().replace("\"", "");
                    String valStr = bp.substring(colonIdx + 1).trim();
                    try {
                        skillBonuses.put(key, Double.parseDouble(valStr));
                    } catch (NumberFormatException ignored) {}
                }
            }
            // Parse per-tree prestige levels
            EnumMap<SkillTreeType, Integer> prestige = new EnumMap<SkillTreeType, Integer>(SkillTreeType.class);
            String prestigeBlock = SkillSyncPayload.extractBlock(json, "\"prestige\"");
            if (prestigeBlock != null) {
                for (SkillTreeType type : SkillTreeType.values()) {
                    int val = SkillSyncPayload.extractInt(prestigeBlock, "\"" + type.name() + "\"");
                    prestige.put(type, val);
                }
            }
            // Parse admin_lock_bypass flag
            boolean adminBypass = false;
            int abIdx = json.indexOf("\"admin_lock_bypass\"");
            if (abIdx >= 0) {
                int abColon = json.indexOf(':', abIdx);
                if (abColon >= 0) {
                    String abVal = json.substring(abColon + 1, Math.min(abColon + 10, json.length())).trim();
                    adminBypass = abVal.startsWith("true");
                }
            }
            clientLevels = levels;
            clientXp = xp;
            clientPoints = points;
            clientTreePoints = treePoints;
            clientUnlockedNodes = nodes;
            clientActiveSynergies = synergies;
            clientLeaderboard = leaderboard;
            clientSkillBonuses = skillBonuses;
            clientPrestige = prestige;
            clientAdminLockBypass = adminBypass;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static String extractBlock(String json, String key) {
        int keyIdx = json.indexOf(key);
        if (keyIdx < 0) {
            return null;
        }
        int braceStart = json.indexOf(123, keyIdx + key.length());
        if (braceStart < 0) {
            return null;
        }
        int depth = 0;
        for (int i = braceStart; i < json.length(); ++i) {
            char c = json.charAt(i);
            if (c == '{') {
                ++depth;
                continue;
            }
            if (c != '}' || --depth != 0) continue;
            return json.substring(braceStart, i + 1);
        }
        return null;
    }

    private static String extractArray(String json, String key) {
        int keyIdx = json.indexOf(key);
        if (keyIdx < 0) {
            return null;
        }
        int bracketStart = json.indexOf(91, keyIdx + key.length());
        if (bracketStart < 0) {
            return null;
        }
        int depth = 0;
        for (int i = bracketStart; i < json.length(); ++i) {
            char c = json.charAt(i);
            if (c == '[') {
                ++depth;
                continue;
            }
            if (c != ']' || --depth != 0) continue;
            return json.substring(bracketStart, i + 1);
        }
        return null;
    }

    private static int extractInt(String json, String key) {
        char c;
        int end;
        int start;
        int keyIdx = json.indexOf(key);
        if (keyIdx < 0) {
            return 0;
        }
        int colonIdx = json.indexOf(58, keyIdx + key.length());
        if (colonIdx < 0) {
            return 0;
        }
        for (start = colonIdx + 1; start < json.length() && json.charAt(start) == ' '; ++start) {
        }
        for (end = start; end < json.length() && (c = json.charAt(end)) != ',' && c != '}' && c != ']'; ++end) {
        }
        try {
            return Integer.parseInt(json.substring(start, end).trim());
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }
}


package com.ultra.megamod.feature.skills;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.computer.network.handlers.SettingsHandler;
import com.ultra.megamod.feature.skills.prestige.PrestigeManager;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = "megamod")
public class SkillBadges {

    // Admin-set custom badge overrides: UUID -> [title, colorCode]
    private static final Map<UUID, String[]> customBadges = new HashMap<>();
    private static boolean loaded = false;
    private static boolean dirty = false;
    private static final String FILE_NAME = "megamod_badges.dat";

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (!SettingsHandler.isEnabled(player.getUUID(), "skill_badge")) return;

        ServerLevel overworld = player.level().getServer().overworld();
        BadgeInfo badge = getBadge(player.getUUID(), overworld);
        if (badge == null) return;

        MutableComponent prefix = buildBadgeComponent(badge);
        MutableComponent decorated = prefix.append(Component.literal(" ")).append(event.getMessage());
        event.setMessage(decorated);
    }

    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!SettingsHandler.isEnabled(serverPlayer.getUUID(), "skill_badge")) return;

        ServerLevel overworld = serverPlayer.level().getServer().overworld();
        BadgeInfo badge = getBadge(serverPlayer.getUUID(), overworld);

        String playerName = serverPlayer.getGameProfile().name();
        MutableComponent displayName;

        // Prestige name coloring
        PrestigeManager prestige = PrestigeManager.get(overworld);
        int totalPrestige = prestige.getTotalPrestige(serverPlayer.getUUID());
        ChatFormatting nameColor = getPrestigeNameColor(totalPrestige);
        boolean bold = totalPrestige >= 25;

        MutableComponent nameComp = Component.literal(playerName);
        if (nameColor != null) {
            nameComp = nameComp.withStyle(nameColor);
        }
        if (bold) {
            nameComp = nameComp.withStyle(ChatFormatting.BOLD);
        }

        if (badge != null) {
            displayName = buildBadgeComponent(badge).append(Component.literal(" ")).append(nameComp);
        } else {
            displayName = nameComp;
        }

        event.setDisplayName(displayName);
    }

    private static ChatFormatting getPrestigeNameColor(int totalPrestige) {
        if (totalPrestige >= 25) return ChatFormatting.RED;
        if (totalPrestige >= 15) return ChatFormatting.GOLD;
        if (totalPrestige >= 5) return ChatFormatting.YELLOW;
        return null;
    }

    private static MutableComponent buildBadgeComponent(BadgeInfo badge) {
        MutableComponent result = Component.empty();

        // Prestige stars
        if (badge.totalPrestige > 0) {
            int stars = Math.min(badge.totalPrestige, 25);
            StringBuilder starStr = new StringBuilder();
            for (int i = 0; i < stars; i++) starStr.append("\u2605");
            result = result.append(Component.literal(starStr.toString()).withStyle(ChatFormatting.GOLD));
        }

        // Badge title
        result = result.append(Component.literal(badge.colorCode + "[" + badge.title + "]"));

        return result;
    }

    private static String getTreeColorCode(SkillTreeType tree) {
        return switch (tree) {
            case COMBAT -> "\u00a7c";
            case MINING -> "\u00a76";
            case FARMING -> "\u00a7a";
            case ARCANE -> "\u00a7d";
            case SURVIVAL -> "\u00a7e";
        };
    }

    // Color code mapping for custom badges
    private static final Map<String, String> COLOR_CODES = Map.ofEntries(
        Map.entry("red", "\u00a7c"), Map.entry("gold", "\u00a76"), Map.entry("green", "\u00a7a"),
        Map.entry("purple", "\u00a7d"), Map.entry("yellow", "\u00a7e"), Map.entry("aqua", "\u00a7b"),
        Map.entry("white", "\u00a7f"), Map.entry("blue", "\u00a79"), Map.entry("dark_red", "\u00a74"),
        Map.entry("dark_green", "\u00a72"), Map.entry("dark_purple", "\u00a75"), Map.entry("dark_aqua", "\u00a73"),
        Map.entry("gray", "\u00a77"), Map.entry("dark_gray", "\u00a78"), Map.entry("pink", "\u00a7d"),
        Map.entry("light_purple", "\u00a7d")
    );

    public static BadgeInfo getBadge(UUID playerId, ServerLevel overworld) {
        ensureLoaded(overworld);

        // Check for custom badge override first
        String[] custom = customBadges.get(playerId);
        if (custom != null) {
            PrestigeManager prestige = PrestigeManager.get(overworld);
            int totalPrestige = prestige.getTotalPrestige(playerId);
            String colorCode = COLOR_CODES.getOrDefault(custom[1], "\u00a7f");
            return new BadgeInfo(custom[0], null, 0, totalPrestige, colorCode);
        }

        // Compute from skill tree data
        SkillManager manager = SkillManager.get(overworld);
        Set<String> unlocked = manager.getUnlockedNodes(playerId);
        if (unlocked.isEmpty()) return null;

        SkillBranch bestBranch = null;
        int bestTier = 0;
        int bestNodeCount = 0;

        for (SkillBranch branch : SkillBranch.values()) {
            int highestTier = 0;
            int nodeCount = 0;
            for (int t = 1; t <= 5; t++) {
                String nodeId = branch.name().toLowerCase() + "_" + t;
                if (unlocked.contains(nodeId)) {
                    highestTier = t;
                    nodeCount++;
                }
            }
            if (highestTier >= 3) {
                if (highestTier > bestTier || (highestTier == bestTier && nodeCount > bestNodeCount)) {
                    bestBranch = branch;
                    bestTier = highestTier;
                    bestNodeCount = nodeCount;
                }
            }
        }

        if (bestBranch == null) return null;

        String title;
        if (bestTier == 5) {
            SkillNode t5 = SkillTreeDefinitions.getNodeById(bestBranch.name().toLowerCase() + "_5");
            title = t5 != null ? t5.displayName() : bestBranch.getDisplayName();
        } else if (bestTier == 4) {
            SkillNode t4 = SkillTreeDefinitions.getNodeById(bestBranch.name().toLowerCase() + "_4");
            title = t4 != null ? t4.displayName() : bestBranch.getDisplayName();
        } else {
            title = bestBranch.getDisplayName();
        }

        PrestigeManager prestige = PrestigeManager.get(overworld);
        int totalPrestige = prestige.getTotalPrestige(playerId);
        String colorCode = getTreeColorCode(bestBranch.getTreeType());

        return new BadgeInfo(title, bestBranch.getTreeType(), bestTier, totalPrestige, colorCode);
    }

    // --- Custom badge admin API ---

    public static void setCustomBadge(UUID playerId, String title, String color) {
        customBadges.put(playerId, new String[]{title, color});
        dirty = true;
    }

    public static void clearCustomBadge(UUID playerId) {
        customBadges.remove(playerId);
        dirty = true;
    }

    public static boolean hasCustomBadge(UUID playerId) {
        return customBadges.containsKey(playerId);
    }

    public static String getCustomTitle(UUID playerId) {
        String[] c = customBadges.get(playerId);
        return c != null ? c[0] : null;
    }

    public static String getCustomColor(UUID playerId) {
        String[] c = customBadges.get(playerId);
        return c != null ? c[1] : null;
    }

    // --- Persistence ---

    private static void ensureLoaded(ServerLevel level) {
        if (loaded) return;
        loaded = true;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                CompoundTag badges = root.getCompoundOrEmpty("badges");
                for (String key : badges.keySet()) {
                    CompoundTag entry = badges.getCompoundOrEmpty(key);
                    String title = entry.getStringOr("title", "");
                    String color = entry.getStringOr("color", "white");
                    if (!title.isEmpty()) {
                        customBadges.put(UUID.fromString(key), new String[]{title, color});
                    }
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load skill badge data", e);
        }
    }

    public static void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);
            CompoundTag root = new CompoundTag();
            CompoundTag badges = new CompoundTag();
            for (Map.Entry<UUID, String[]> entry : customBadges.entrySet()) {
                CompoundTag bTag = new CompoundTag();
                bTag.putString("title", entry.getValue()[0]);
                bTag.putString("color", entry.getValue()[1]);
                badges.put(entry.getKey().toString(), (Tag) bTag);
            }
            root.put("badges", (Tag) badges);
            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save skill badge data", e);
        }
    }

    public static void reset() {
        customBadges.clear();
        loaded = false;
        dirty = false;
    }

    public record BadgeInfo(String title, SkillTreeType tree, int tier, int totalPrestige, String colorCode) {
        // Convenience constructor for non-custom badges
        public BadgeInfo(String title, SkillTreeType tree, int tier, int totalPrestige) {
            this(title, tree, tier, totalPrestige, "\u00a7f");
        }
    }
}

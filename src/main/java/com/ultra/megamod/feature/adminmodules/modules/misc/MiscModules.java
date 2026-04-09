package com.ultra.megamod.feature.adminmodules.modules.misc;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

public class MiscModules {

    public static void register(Consumer<AdminModule> reg) {
        reg.accept(new AntiAFK());
        reg.accept(new BetterChat());
        reg.accept(new InventoryTweaks());
        reg.accept(new AntiPacketKick());
        reg.accept(new AutoReconnect());
        reg.accept(new SoundBlocker());
        reg.accept(new Notifier());
        reg.accept(new DiscordRPC());
        reg.accept(new CoordLogger());
        reg.accept(new BookBot());
        reg.accept(new ServerInfo());
        reg.accept(new AutoLog());
        reg.accept(new Spammer());
        reg.accept(new PacketLogger());
        reg.accept(new AntiCrash());
        // New modules
        reg.accept(new MessageAura());
        reg.accept(new Notebot());
        reg.accept(new ServerSpoof());
        reg.accept(new Swarm());
        // New Meteor-inspired modules (batch 2)
        reg.accept(new BetterBeacons());
    }

    // ── AntiAFK ────────────────────────────────────────────────────────────
    static class AntiAFK extends AdminModule {
        private ModuleSetting.IntSetting interval;
        private ModuleSetting.EnumSetting mode;
        int tick = 0;
        AntiAFK() { super("anti_afk", "AntiAFK", "Prevents AFK kick with subtle movements", ModuleCategory.MISC); }
        @Override protected void initSettings() {
            interval = integer("Interval", 200, 40, 600, "Ticks between movements");
            mode = enumVal("Mode", "Rotate", List.of("Rotate", "Jump", "Swing"), "Anti-AFK action");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % interval.getValue() != 0) return;
            switch (mode.getValue()) {
                case "Jump" -> {
                    if (player.onGround()) player.jumpFromGround();
                }
                case "Swing" -> {
                    player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
                }
                default -> { // Rotate
                    player.setYRot(player.getYRot() + 0.01f);
                }
            }
        }
    }

    // ── BetterChat ─────────────────────────────────────────────────────────
    static class BetterChat extends AdminModule {
        int tick = 0;
        BetterChat() { super("better_chat", "BetterChat", "Chat improvements and formatting with status updates every 10 seconds", ModuleCategory.MISC); }
        @Override public void onEnable(ServerPlayer player) {
            player.sendSystemMessage(Component.literal("[BetterChat] ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Enabled - status updates every 10 seconds").withStyle(ChatFormatting.GRAY)));
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Report every 200 ticks (10 seconds) instead of 20 to avoid flooding chat
            if (++tick % 200 != 0) return;
            int playerCount = level.getServer().getPlayerList().getPlayers().size();
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            long mspt = level.getServer().getAverageTickTimeNanos() / 1_000_000L;
            double tps = Math.min(20.0, mspt > 0 ? 1000.0 / mspt : 20.0);
            player.sendSystemMessage(Component.literal("[" + time + "] ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal("Players: " + playerCount).withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" | TPS: " + String.format("%.1f", tps)).withStyle(tps >= 18.0 ? ChatFormatting.GREEN : tps >= 15.0 ? ChatFormatting.YELLOW : ChatFormatting.RED))
                .append(Component.literal(" | MSPT: " + mspt + "ms").withStyle(ChatFormatting.GRAY)));
        }
    }

    // ── InventoryTweaks ────────────────────────────────────────────────────
    static class InventoryTweaks extends AdminModule {
        int tick = 0;
        InventoryTweaks() { super("inventory_tweaks", "InventoryTweaks", "Auto-stacks partial item stacks in inventory", ModuleCategory.MISC); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 60 != 0) return;
            var inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty() || stack.getCount() >= stack.getMaxStackSize()) continue;
                for (int j = i + 1; j < inv.getContainerSize(); j++) {
                    ItemStack other = inv.getItem(j);
                    if (other.isEmpty()) continue;
                    if (ItemStack.isSameItemSameComponents(stack, other)) {
                        int space = stack.getMaxStackSize() - stack.getCount();
                        int toMove = Math.min(space, other.getCount());
                        if (toMove > 0) {
                            stack.grow(toMove);
                            other.shrink(toMove);
                            if (other.isEmpty()) inv.setItem(j, ItemStack.EMPTY);
                        }
                        if (stack.getCount() >= stack.getMaxStackSize()) break;
                    }
                }
            }
        }
    }

    // ── AntiPacketKick ─────────────────────────────────────────────────────
    static class AntiPacketKick extends AdminModule {
        AntiPacketKick() { super("anti_packet_kick", "AntiPacketKick", "Caps extreme damage to prevent kick-inducing spikes", ModuleCategory.MISC); }
        @Override public void onDamage(ServerPlayer player, LivingDamageEvent.Pre event) {
            if (event.getNewDamage() > 100.0f) {
                event.setNewDamage(0.0f);
                player.sendSystemMessage(Component.literal("[AntiPacketKick] ").withStyle(ChatFormatting.RED)
                    .append(Component.literal("Blocked extreme damage: " + String.format("%.1f", event.getOriginalDamage())).withStyle(ChatFormatting.GRAY)));
            }
        }
    }

    // ── AutoReconnect ──────────────────────────────────────────────────────
    static class AutoReconnect extends AdminModule {
        private ModuleSetting.IntSetting healThreshold;
        AutoReconnect() { super("auto_reconnect", "AutoReconnect", "Auto-heals when health is critically low to prevent death", ModuleCategory.MISC); }
        @Override protected void initSettings() {
            healThreshold = integer("HealBelow", 4, 1, 10, "Health threshold to trigger emergency heal");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (player.getHealth() > 0 && player.getHealth() < healThreshold.getValue()) {
                player.setHealth(player.getMaxHealth());
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 3, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2, false, false));
                player.sendSystemMessage(Component.literal("[AutoReconnect] ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal("Emergency heal triggered!").withStyle(ChatFormatting.WHITE)));
            }
        }
    }

    // ── SoundBlocker ───────────────────────────────────────────────────────
    static class SoundBlocker extends AdminModule {
        private ModuleSetting.BoolSetting slowFall;
        private ModuleSetting.BoolSetting invisibility;
        SoundBlocker() { super("sound_blocker", "SoundBlocker", "Stealth mode: slow falling for silent landing, invisibility to avoid detection", ModuleCategory.MISC); }
        @Override protected void initSettings() {
            slowFall = bool("SlowFall", true, "Apply slow falling for silent landings");
            invisibility = bool("Invisibility", false, "Apply invisibility for full stealth");
        }
        @Override public void onEnable(ServerPlayer player) {
            player.sendSystemMessage(Component.literal("[SoundBlocker] ").withStyle(ChatFormatting.AQUA)
                .append(Component.literal("Stealth mode active").withStyle(ChatFormatting.GRAY)));
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (slowFall.getValue() && !player.hasEffect(MobEffects.SLOW_FALLING)) {
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, false));
            }
            if (invisibility.getValue() && !player.hasEffect(MobEffects.INVISIBILITY)) {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false));
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            player.removeEffect(MobEffects.SLOW_FALLING);
            player.removeEffect(MobEffects.INVISIBILITY);
        }
    }

    // ── Notifier ───────────────────────────────────────────────────────────
    static class Notifier extends AdminModule {
        private ModuleSetting.BoolSetting joinAlert;
        private ModuleSetting.BoolSetting mobAlert;
        private ModuleSetting.BoolSetting healthAlert;
        int tick = 0;
        private final Set<String> knownPlayers = new HashSet<>();
        private boolean mobAlertCooldown = false;
        private boolean healthAlertCooldown = false;
        Notifier() { super("notifier", "Notifier", "Alerts on new players, nearby mobs, and low health", ModuleCategory.MISC); }
        @Override protected void initSettings() {
            joinAlert = bool("JoinAlert", true, "Alert on new player joins");
            mobAlert = bool("MobAlert", true, "Alert on nearby hostile mobs");
            healthAlert = bool("HealthAlert", true, "Alert on low health");
        }
        @Override public void onEnable(ServerPlayer player) {
            knownPlayers.clear();
            mobAlertCooldown = false;
            healthAlertCooldown = false;
            ServerLevel lvl = (ServerLevel) player.level();
            for (ServerPlayer p : lvl.getServer().getPlayerList().getPlayers()) {
                knownPlayers.add(p.getGameProfile().name());
            }
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 40 != 0) return;
            // Check for new and departed players
            if (joinAlert.getValue()) {
                Set<String> currentPlayers = new HashSet<>();
                for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
                    String name = p.getGameProfile().name();
                    currentPlayers.add(name);
                    if (!knownPlayers.contains(name)) {
                        knownPlayers.add(name);
                        player.sendSystemMessage(Component.literal("[Notifier] ").withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal("Player joined: " + name).withStyle(ChatFormatting.WHITE)));
                    }
                }
                // Detect players who left
                Iterator<String> it = knownPlayers.iterator();
                while (it.hasNext()) {
                    String known = it.next();
                    if (!currentPlayers.contains(known)) {
                        it.remove();
                        player.sendSystemMessage(Component.literal("[Notifier] ").withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal("Player left: " + known).withStyle(ChatFormatting.RED)));
                    }
                }
            }
            // Check for nearby hostile mobs -- only alert once when first detected, reset when clear
            if (mobAlert.getValue()) {
                List<Monster> mobs = level.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(16));
                if (!mobs.isEmpty() && !mobAlertCooldown) {
                    mobAlertCooldown = true;
                    player.sendSystemMessage(Component.literal("[Notifier] ").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(mobs.size() + " hostile mob(s) within 16 blocks").withStyle(ChatFormatting.RED)));
                } else if (mobs.isEmpty()) {
                    mobAlertCooldown = false;
                }
            }
            // Low health warning -- only alert once, reset when health recovered
            if (healthAlert.getValue()) {
                if (player.getHealth() < 10.0f && !healthAlertCooldown) {
                    healthAlertCooldown = true;
                    player.sendSystemMessage(Component.literal("[Notifier] ").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal("LOW HEALTH: " + String.format("%.1f", player.getHealth()) + " HP!").withStyle(ChatFormatting.DARK_RED)));
                } else if (player.getHealth() >= 10.0f) {
                    healthAlertCooldown = false;
                }
            }
        }
    }

    // ── DiscordRPC ─────────────────────────────────────────────────────────
    static class DiscordRPC extends AdminModule {
        DiscordRPC() { super("discord_rpc", "DiscordRPC", "Dumps current server/player info to chat", ModuleCategory.MISC); }
        @Override public void onEnable(ServerPlayer player) {
            ServerLevel lvl = (ServerLevel) player.level();
            int playerCount = lvl.getServer().getPlayerList().getPlayers().size();
            String dimension = player.level().dimension().identifier().toString();
            BlockPos pos = player.blockPosition();
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            int food = player.getFoodData().getFoodLevel();
            int xpLevel = player.experienceLevel;
            long mspt = lvl.getServer().getAverageTickTimeNanos() / 1_000_000L;
            double tps = Math.min(20.0, mspt > 0 ? 1000.0 / mspt : 20.0);

            player.sendSystemMessage(Component.literal("=== Server Info Dump ===").withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(Component.literal("  Players Online: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(playerCount)).withStyle(ChatFormatting.WHITE)));
            player.sendSystemMessage(Component.literal("  Dimension: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(dimension).withStyle(ChatFormatting.AQUA)));
            player.sendSystemMessage(Component.literal("  Position: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).withStyle(ChatFormatting.WHITE)));
            player.sendSystemMessage(Component.literal("  Health: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.1f / %.1f", health, maxHealth)).withStyle(ChatFormatting.RED)));
            player.sendSystemMessage(Component.literal("  Food: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(food + "/20").withStyle(ChatFormatting.GREEN)));
            player.sendSystemMessage(Component.literal("  XP Level: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(xpLevel)).withStyle(ChatFormatting.LIGHT_PURPLE)));
            player.sendSystemMessage(Component.literal("  TPS: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.1f", tps)).withStyle(ChatFormatting.YELLOW)));
            player.sendSystemMessage(Component.literal("========================").withStyle(ChatFormatting.GOLD));
        }
    }

    // ── CoordLogger ────────────────────────────────────────────────────────
    static class CoordLogger extends AdminModule {
        private ModuleSetting.IntSetting interval;
        int tick = 0;
        CoordLogger() { super("coord_logger", "CoordLogger", "Logs all online player coordinates to chat", ModuleCategory.MISC); }
        @Override protected void initSettings() {
            interval = integer("Interval", 200, 40, 600, "Ticks between logs");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % interval.getValue() != 0) return;
            List<ServerPlayer> players = level.getServer().getPlayerList().getPlayers();
            player.sendSystemMessage(Component.literal("--- Player Coordinates ---").withStyle(ChatFormatting.GOLD));
            for (ServerPlayer p : players) {
                BlockPos pos = p.blockPosition();
                String dim = p.level().dimension().identifier().toString();
                ChatFormatting dimColor = dim.contains("nether") ? ChatFormatting.RED : dim.contains("end") ? ChatFormatting.DARK_PURPLE : ChatFormatting.GREEN;
                player.sendSystemMessage(Component.literal("  " + p.getGameProfile().name()).withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(" [" + dim + "]").withStyle(dimColor))
                    .append(Component.literal(" " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).withStyle(ChatFormatting.GRAY)));
            }
            player.sendSystemMessage(Component.literal("--------------------------").withStyle(ChatFormatting.GOLD));
        }
    }

    // ── BookBot ────────────────────────────────────────────────────────────
    static class BookBot extends AdminModule {
        int tick = 0;
        private int logCount = 0;
        BookBot() { super("book_bot", "BookBot", "Writes admin log entries to chat and replaces writable books with named copies", ModuleCategory.MISC); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 100 != 0) return;
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            BlockPos pos = player.blockPosition();
            String dim = player.level().dimension().identifier().toString();
            float health = player.getHealth();
            int food = player.getFoodData().getFoodLevel();
            int players = level.getServer().getPlayerList().getPlayers().size();
            logCount++;

            // Log to chat as a formatted admin log entry
            player.sendSystemMessage(Component.literal("[BookBot Log #" + logCount + "] ").withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.literal(time).withStyle(ChatFormatting.GRAY)));
            player.sendSystemMessage(Component.literal("  Pos: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" [" + dim + "]").withStyle(ChatFormatting.AQUA)));
            player.sendSystemMessage(Component.literal("  HP: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.1f", health)).withStyle(ChatFormatting.RED))
                .append(Component.literal(" | Food: " + food).withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" | Players: " + players).withStyle(ChatFormatting.YELLOW)));

            // If player has a writable book, replace it with a named written book
            var inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.is(Items.WRITABLE_BOOK)) {
                    // Set custom name on the book to mark it as logged
                    stack.set(DataComponents.CUSTOM_NAME,
                        Component.literal("Admin Log #" + logCount).withStyle(ChatFormatting.GOLD));
                    player.sendSystemMessage(Component.literal("  -> Named book in slot " + i).withStyle(ChatFormatting.DARK_GRAY));
                    break;
                }
            }
        }
    }

    // ── ServerInfo ─────────────────────────────────────────────────────────
    static class ServerInfo extends AdminModule {
        // Cached entity/chunk counts to avoid expensive per-tick recalculation
        private static int cachedEntityCount = 0;
        private static int cachedChunkCount = 0;
        private static int entityCountTick = 0;
        private static final int ENTITY_COUNT_INTERVAL = 100; // Recalculate every 100 ticks (5 seconds)

        ServerInfo() { super("server_info", "ServerInfo", "Displays detailed server information", ModuleCategory.MISC); }
        @Override public void onEnable(ServerPlayer player) {
            var server = ((ServerLevel) player.level()).getServer();
            long mspt = server.getAverageTickTimeNanos() / 1_000_000L;
            double tps = Math.min(20.0, mspt > 0 ? 1000.0 / mspt : 20.0);
            Runtime rt = Runtime.getRuntime();
            long usedMem = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
            long maxMem = rt.maxMemory() / (1024 * 1024);
            int playerCount = server.getPlayerList().getPlayers().size();
            int maxPlayers = server.getPlayerList().getMaxPlayers();

            // Count entities and loaded chunks across all levels (cached, refreshed every 5 seconds)
            if (++entityCountTick >= ENTITY_COUNT_INTERVAL || cachedEntityCount == 0) {
                entityCountTick = 0;
                int totalEntities = 0;
                int totalChunks = 0;
                for (ServerLevel lev : server.getAllLevels()) {
                    totalEntities += (int) java.util.stream.StreamSupport.stream(lev.getAllEntities().spliterator(), false).count();
                    totalChunks += lev.getChunkSource().getLoadedChunksCount();
                }
                cachedEntityCount = totalEntities;
                cachedChunkCount = totalChunks;
            }

            player.sendSystemMessage(Component.literal("=== Server Information ===").withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(Component.literal("  TPS: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.1f", tps)).withStyle(tps >= 18 ? ChatFormatting.GREEN : tps >= 15 ? ChatFormatting.YELLOW : ChatFormatting.RED))
                .append(Component.literal(" (" + mspt + "ms/tick)").withStyle(ChatFormatting.DARK_GRAY)));
            player.sendSystemMessage(Component.literal("  Memory: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(usedMem + "MB / " + maxMem + "MB").withStyle(ChatFormatting.WHITE)));
            player.sendSystemMessage(Component.literal("  Players: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(playerCount + "/" + maxPlayers).withStyle(ChatFormatting.WHITE)));
            player.sendSystemMessage(Component.literal("  Loaded Chunks: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(cachedChunkCount)).withStyle(ChatFormatting.WHITE)));
            player.sendSystemMessage(Component.literal("  Entities: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(cachedEntityCount)).withStyle(ChatFormatting.WHITE)));
            player.sendSystemMessage(Component.literal("  Java: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(System.getProperty("java.version")).withStyle(ChatFormatting.WHITE)));
            player.sendSystemMessage(Component.literal("  OS: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(System.getProperty("os.name") + " " + System.getProperty("os.arch")).withStyle(ChatFormatting.WHITE)));
            player.sendSystemMessage(Component.literal("===========================").withStyle(ChatFormatting.GOLD));
        }
    }

    // ── AutoLog ────────────────────────────────────────────────────────────
    static class AutoLog extends AdminModule {
        private ModuleSetting.IntSetting healthThreshold;
        AutoLog() { super("auto_log", "AutoLog", "Auto-disconnects when health is low", ModuleCategory.MISC); }
        @Override protected void initSettings() {
            healthThreshold = integer("Health", 4, 1, 19, "Health threshold to disconnect");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (player.getHealth() > 0 && player.getHealth() <= healthThreshold.getValue()) {
                player.sendSystemMessage(Component.literal("[AutoLog] ").withStyle(ChatFormatting.RED)
                    .append(Component.literal("Health critical! Disconnecting...").withStyle(ChatFormatting.WHITE)));
                player.connection.disconnect(Component.literal("AutoLog: Health dropped to " + String.format("%.1f", player.getHealth())));
            }
        }
    }

    // ── Spammer ────────────────────────────────────────────────────────────
    static class Spammer extends AdminModule {
        private ModuleSetting.IntSetting delay;
        int tick = 0;
        Spammer() { super("spammer", "Spammer", "Broadcasts a message to all players at interval", ModuleCategory.MISC); }
        @Override protected void initSettings() {
            delay = integer("Delay", 100, 20, 1200, "Ticks between messages");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % delay.getValue() != 0) return;
            Component msg = Component.literal("[MegaMod] ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("MegaMod Admin Broadcast").withStyle(ChatFormatting.WHITE));
            for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
                p.sendSystemMessage(msg);
            }
        }
    }

    // ── PacketLogger ───────────────────────────────────────────────────────
    static class PacketLogger extends AdminModule {
        int tick = 0;
        private final Map<String, Integer> eventCounts = new HashMap<>();
        PacketLogger() { super("packet_logger", "PacketLogger", "Logs player attacks, block breaks, and item uses", ModuleCategory.MISC); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 20 != 0) return;
            if (eventCounts.isEmpty()) return;
            player.sendSystemMessage(Component.literal("[PacketLog] ").withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.literal("Events: ").withStyle(ChatFormatting.GRAY)));
            for (Map.Entry<String, Integer> entry : eventCounts.entrySet()) {
                player.sendSystemMessage(Component.literal("  " + entry.getKey() + ": ").withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(String.valueOf(entry.getValue())).withStyle(ChatFormatting.YELLOW)));
            }
            eventCounts.clear();
        }
        @Override public void onDamage(ServerPlayer player, LivingDamageEvent.Pre event) {
            String source = event.getSource().type().msgId();
            float dmg = event.getNewDamage();
            eventCounts.merge("Damage(" + source + ", " + String.format("%.1f", dmg) + ")", 1, Integer::sum);
        }
        @Override public void onBreakSpeed(ServerPlayer player, net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed event) {
            eventCounts.merge("BlockBreak(speed=" + String.format("%.1f", event.getNewSpeed()) + ")", 1, Integer::sum);
        }
    }

    // ── AntiCrash ──────────────────────────────────────────────────────────
    static class AntiCrash extends AdminModule {
        AntiCrash() { super("anti_crash", "AntiCrash", "Caps damage at 20 and prevents void fall", ModuleCategory.MISC); }
        @Override public void onDamage(ServerPlayer player, LivingDamageEvent.Pre event) {
            if (event.getNewDamage() > 20.0f) {
                event.setNewDamage(20.0f);
            }
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Prevent falling into void
            if (player.getY() < -64) {
                player.teleportTo(player.getX(), 64.0, player.getZ());
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, false));
                player.sendSystemMessage(Component.literal("[AntiCrash] ").withStyle(ChatFormatting.RED)
                    .append(Component.literal("Prevented void death, teleported to Y=64").withStyle(ChatFormatting.WHITE)));
            }
            // Prevent extremely high Y coordinate
            if (player.getY() > 1000) {
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, false));
            }
        }
    }

    // ── MessageAura (NEW) ──────────────────────────────────────────────────
    static class MessageAura extends AdminModule {
        private final Set<String> greeted = new HashSet<>();
        int tick = 0;
        MessageAura() { super("message_aura", "MessageAura", "Auto-sends welcome message to nearby new players", ModuleCategory.MISC); }
        @Override public void onEnable(ServerPlayer player) {
            greeted.clear();
            greeted.add(player.getGameProfile().name()); // Don't greet self
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 20 != 0) return;
            List<ServerPlayer> nearby = level.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(16));
            for (ServerPlayer p : nearby) {
                String name = p.getGameProfile().name();
                if (!greeted.contains(name)) {
                    greeted.add(name);
                    p.sendSystemMessage(Component.literal("Welcome, " + name + "!").withStyle(ChatFormatting.GOLD));
                    player.sendSystemMessage(Component.literal("[MessageAura] ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal("Greeted " + name).withStyle(ChatFormatting.GRAY)));
                }
            }
        }
    }

    // ── Notebot (NEW) ──────────────────────────────────────────────────────
    static class Notebot extends AdminModule {
        private ModuleSetting.IntSetting speed;
        private ModuleSetting.IntSetting scanRadius;
        int tick = 0;
        private final List<BlockPos> noteBlocks = new ArrayList<>();
        private int noteIndex = 0;
        Notebot() { super("notebot", "Notebot", "Plays nearby note blocks in sequence to make music", ModuleCategory.MISC); }
        @Override protected void initSettings() {
            speed = integer("Speed", 4, 1, 10, "Notes per second");
            scanRadius = integer("Radius", 4, 2, 8, "Scan radius for note blocks");
        }
        @Override public void onEnable(ServerPlayer player) {
            // Scan for note blocks in radius
            noteBlocks.clear();
            noteIndex = 0;
            int r = scanRadius.getValue();
            BlockPos center = player.blockPosition();
            for (int x = -r; x <= r; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        if (player.level().getBlockState(pos).getBlock() instanceof NoteBlock) {
                            noteBlocks.add(pos);
                        }
                    }
                }
            }
            player.sendSystemMessage(Component.literal("[Notebot] ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal("Found " + noteBlocks.size() + " note blocks").withStyle(ChatFormatting.WHITE)));
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (noteBlocks.isEmpty()) return;
            // speed = notes per second; at 20 tps, interval = 20/speed ticks
            int interval = Math.max(1, 20 / speed.getValue());
            if (++tick % interval != 0) return;
            BlockPos pos = noteBlocks.get(noteIndex % noteBlocks.size());
            if (level.getBlockState(pos).getBlock() instanceof NoteBlock) {
                // Trigger the note block by sending a block event
                level.blockEvent(pos, level.getBlockState(pos).getBlock(), 0, 0);
            }
            noteIndex++;
        }
    }

    // ── ServerSpoof (NEW) ──────────────────────────────────────────────────
    static class ServerSpoof extends AdminModule {
        ServerSpoof() { super("server_spoof", "ServerSpoof", "Broadcasts spoofed server status info to chat", ModuleCategory.MISC); }
        @Override public void onEnable(ServerPlayer player) {
            var server = ((ServerLevel) player.level()).getServer();
            int real = server.getPlayerList().getPlayers().size();
            // Broadcast spoofed info
            Component header = Component.literal("=== Server Status ===").withStyle(ChatFormatting.AQUA);
            Component line1 = Component.literal("  Players: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal((real + 47) + "/" + (server.getPlayerList().getMaxPlayers() + 100)).withStyle(ChatFormatting.WHITE));
            Component line2 = Component.literal("  TPS: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("20.0 (perfect)").withStyle(ChatFormatting.GREEN));
            Component line3 = Component.literal("  Uptime: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("127 days, 14 hours").withStyle(ChatFormatting.WHITE));
            Component line4 = Component.literal("  Version: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("MegaMod Ultra v3.7.2").withStyle(ChatFormatting.LIGHT_PURPLE));
            Component footer = Component.literal("=====================").withStyle(ChatFormatting.AQUA);

            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(header);
                p.sendSystemMessage(line1);
                p.sendSystemMessage(line2);
                p.sendSystemMessage(line3);
                p.sendSystemMessage(line4);
                p.sendSystemMessage(footer);
            }
            player.sendSystemMessage(Component.literal("[ServerSpoof] ").withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.literal("Spoofed status broadcast sent").withStyle(ChatFormatting.GRAY)));
        }
    }

    // ── Swarm (NEW) ────────────────────────────────────────────────────────
    static class Swarm extends AdminModule {
        int tick = 0;
        // Admin names centralized in AdminSystem.ADMIN_USERNAMES
        Swarm() { super("swarm", "Swarm", "Coordinates admin players to mirror your movement", ModuleCategory.MISC); }
        @Override public void onEnable(ServerPlayer player) {
            player.sendSystemMessage(Component.literal("[Swarm] ").withStyle(ChatFormatting.DARK_GREEN)
                .append(Component.literal("Swarm mode active - other admins will mirror your position").withStyle(ChatFormatting.GRAY)));
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 5 != 0) return;
            Vec3 leaderPos = player.position();
            float leaderYaw = player.getYRot();
            float leaderPitch = player.getXRot();
            String leaderName = player.getGameProfile().name();

            for (ServerPlayer other : level.getServer().getPlayerList().getPlayers()) {
                if (other == player) continue;
                String otherName = other.getGameProfile().name();
                if (!com.ultra.megamod.feature.computer.admin.AdminSystem.ADMIN_USERNAMES.contains(otherName)) continue;
                // Only mirror if in same dimension
                if (other.level().dimension() == player.level().dimension()) {
                    // Teleport to slightly offset position (so they don't stack perfectly)
                    double offsetX = (otherName.hashCode() % 3) - 1;
                    double offsetZ = ((otherName.hashCode() / 3) % 3) - 1;
                    other.teleportTo((ServerLevel) other.level(),
                        leaderPos.x + offsetX, leaderPos.y, leaderPos.z + offsetZ,
                        Set.of(), leaderYaw, leaderPitch, false);
                }
            }
        }
    }

    // ── BetterBeacons (NEW) ───────────────────────────────────────────────
    static class BetterBeacons extends AdminModule {
        int tick = 0;
        BetterBeacons() { super("better_beacons", "BetterBeacons", "Applies all beacon effects at max level when near a beacon", ModuleCategory.MISC); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 40 != 0) return;
            // Check if a beacon block is within 4 blocks
            BlockPos center = player.blockPosition();
            boolean nearBeacon = false;
            for (int x = -4; x <= 4; x++) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -4; z <= 4; z++) {
                        if (level.getBlockState(center.offset(x, y, z)).getBlock() == net.minecraft.world.level.block.Blocks.BEACON) {
                            nearBeacon = true;
                            break;
                        }
                    }
                    if (nearBeacon) break;
                }
                if (nearBeacon) break;
            }
            if (nearBeacon) {
                // Apply all beacon effects at max level
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 100, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 100, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 100, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false));
            }
        }
    }
}

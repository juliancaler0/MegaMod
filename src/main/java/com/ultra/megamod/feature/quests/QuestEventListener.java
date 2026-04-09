package com.ultra.megamod.feature.quests;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.PlayerClassManager;
import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;
import com.ultra.megamod.feature.computer.network.handlers.PartyHandler;
import com.ultra.megamod.feature.quests.QuestDefinitions.QuestDef;
import com.ultra.megamod.feature.quests.QuestDefinitions.QuestTask;
import com.ultra.megamod.feature.quests.QuestDefinitions.QuestTaskType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens for game events and updates quest progress counters.
 * Also provides static hooks to be called from existing managers.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class QuestEventListener {

    // ─── NeoForge event subscribers ───

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = (ServerLevel) player.level();

        String dimensionId = event.getTo().identifier().toString();
        // Don't count the overworld as a "visited" dimension
        if (!"minecraft:overworld".equals(dimensionId)) {
            QuestProgressManager.get(level).addVisitedDimension(player.getUUID(), dimensionId);
            checkQuestCompletion(player, level);
        }
    }

    // ─── Static hooks (called from existing managers via single-line additions) ───

    public static void onBountyComplete(UUID playerUuid, ServerLevel level) {
        try {
            QuestProgressManager qpm = QuestProgressManager.get(level);
            qpm.incrementBountyComplete(playerUuid);
            propagateToParty(playerUuid, level, QuestTaskType.BOUNTY_COMPLETE);

            // cr_05: Complete a bounty hunt ("Master Assassin" — Rogue class quest)
            PlayerClass cls = PlayerClassManager.get(level).getPlayerClass(playerUuid);
            if (cls == PlayerClass.ROGUE && !qpm.isCompleted(playerUuid, "cr_05")
                    && qpm.arePrerequisitesMet(playerUuid, QuestDefinitions.get("cr_05"))) {
                qpm.completeQuest(playerUuid, "cr_05");
                notifyQuestComplete(playerUuid, level, "cr_05");
            }

            checkQuestCompletionByUuid(playerUuid, level);
        } catch (Exception ignored) {}
    }

    public static void onCasinoPlay(UUID playerUuid, ServerLevel level) {
        try {
            QuestProgressManager qpm = QuestProgressManager.get(level);
            qpm.incrementCasinoPlay(playerUuid);
            checkQuestCompletionByUuid(playerUuid, level);
        } catch (Exception ignored) {}
    }

    public static void onMarketplaceTrade(UUID playerUuid, ServerLevel level) {
        try {
            QuestProgressManager qpm = QuestProgressManager.get(level);
            qpm.incrementMarketplaceTrade(playerUuid);
            propagateToParty(playerUuid, level, QuestTaskType.TRADE_MARKETPLACE);
            checkQuestCompletionByUuid(playerUuid, level);
        } catch (Exception ignored) {}
    }

    // ─── Spell cast hook (called from SpellExecutor after successful cast) ───

    /**
     * Called when a spell is successfully cast. Auto-completes class quests that
     * require casting specific spells. Uses the spell ID to match quest requirements.
     */
    public static void onSpellCast(ServerPlayer player, String spellId) {
        try {
            ServerLevel level = (ServerLevel) player.level();
            UUID uuid = player.getUUID();
            QuestProgressManager qpm = QuestProgressManager.get(level);
            PlayerClass playerClass = PlayerClassManager.get(level).getPlayerClass(uuid);

            // Track spells cast in current combat session for multi-spell quests
            Set<String> sessionSpells = spellCombatSessions.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());
            sessionSpells.add(spellId);

            // --- Wizard quests ---
            if (playerClass == PlayerClass.WIZARD || playerClass == PlayerClass.NONE) {
                // cm_01: Cast Arcane Bolt
                if (spellId.equals("arcane_bolt") && !qpm.isCompleted(uuid, "cm_01") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cm_01"))) {
                    autoCompleteQuest(player, qpm, "cm_01");
                }
                // cm_02: Cast a Fire and Frost spell
                boolean hasFire = sessionSpells.stream().anyMatch(s -> isFireSpell(s));
                boolean hasFrost = sessionSpells.stream().anyMatch(s -> isFrostSpell(s));
                if (hasFire && hasFrost && !qpm.isCompleted(uuid, "cm_02") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cm_02"))) {
                    autoCompleteQuest(player, qpm, "cm_02");
                }
                // cm_03: Cast 3 different spells in one fight
                if (sessionSpells.size() >= 3 && !qpm.isCompleted(uuid, "cm_03") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cm_03"))) {
                    autoCompleteQuest(player, qpm, "cm_03");
                }
            }

            // --- Paladin quests ---
            if (playerClass == PlayerClass.PALADIN || playerClass == PlayerClass.NONE) {
                // cp_03: Cast Heal on yourself
                if (spellId.equals("heal") && !qpm.isCompleted(uuid, "cp_03") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cp_03"))) {
                    autoCompleteQuest(player, qpm, "cp_03");
                }
                // cp_05: Cast Circle of Healing in a party
                if (spellId.equals("circle_of_healing") && PartyHandler.isInParty(uuid)
                        && !qpm.isCompleted(uuid, "cp_05") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cp_05"))) {
                    autoCompleteQuest(player, qpm, "cp_05");
                }
            }

            // --- Warrior quests ---
            if (playerClass == PlayerClass.WARRIOR || playerClass == PlayerClass.NONE) {
                // cw_02: Use Charge and Shout in combat
                boolean hasCharge = sessionSpells.contains("charge");
                boolean hasShout = sessionSpells.contains("shout") || sessionSpells.contains("war_cry");
                if (hasCharge && hasShout && !qpm.isCompleted(uuid, "cw_02") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cw_02"))) {
                    autoCompleteQuest(player, qpm, "cw_02");
                }
                // cb_31: Use a Warrior spell ("Warrior's Fury")
                if (spellId.equals("charge") || spellId.equals("shout") || spellId.equals("war_cry")
                        || spellId.equals("shattering_throw") || spellId.equals("cleave")) {
                    if (!qpm.isCompleted(uuid, "cb_31") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cb_31"))) {
                        autoCompleteQuest(player, qpm, "cb_31");
                    }
                }
            }

            // --- Rogue quests ---
            if (playerClass == PlayerClass.ROGUE || playerClass == PlayerClass.NONE) {
                // cr_02: Use Vanish in combat
                if (spellId.equals("vanish") && !qpm.isCompleted(uuid, "cr_02") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cr_02"))) {
                    autoCompleteQuest(player, qpm, "cr_02");
                }
                // cr_04: Use Shadow Step 5 times (accumulate)
                if (spellId.equals("shadow_step")) {
                    int count = shadowStepCounts.merge(uuid, 1, Integer::sum);
                    if (count >= 5 && !qpm.isCompleted(uuid, "cr_04") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cr_04"))) {
                        autoCompleteQuest(player, qpm, "cr_04");
                    }
                }
                // cb_23: Use a Rogue spell ("Rogue's Cunning")
                if ((spellId.equals("vanish") || spellId.equals("shadow_step") || spellId.equals("backstab"))
                        && !qpm.isCompleted(uuid, "cb_23") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cb_23"))) {
                    autoCompleteQuest(player, qpm, "cb_23");
                }
            }

            // --- Ranger quests ---
            if (playerClass == PlayerClass.RANGER || playerClass == PlayerClass.NONE) {
                // ca_02: Use Power Shot on an enemy
                if (spellId.equals("power_shot") && !qpm.isCompleted(uuid, "ca_02") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("ca_02"))) {
                    autoCompleteQuest(player, qpm, "ca_02");
                }
                // ca_03: Use Barrage
                if (spellId.equals("barrage") && !qpm.isCompleted(uuid, "ca_03") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("ca_03"))) {
                    autoCompleteQuest(player, qpm, "ca_03");
                }
                // ca_04: Use Entangling Roots in a dungeon
                if (spellId.equals("entangling_roots") && isDungeonDimension(player)
                        && !qpm.isCompleted(uuid, "ca_04") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("ca_04"))) {
                    autoCompleteQuest(player, qpm, "ca_04");
                }
                // cb_24: Use a Ranger spell ("Ranger's Precision")
                if ((spellId.equals("power_shot") || spellId.equals("barrage") || spellId.equals("entangling_roots"))
                        && !qpm.isCompleted(uuid, "cb_24") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cb_24"))) {
                    autoCompleteQuest(player, qpm, "cb_24");
                }
            }

            // --- Generic spell quests (any class) ---
            // cb_22: Cast any spell ("Wizard's First Spell")
            if (!qpm.isCompleted(uuid, "cb_22") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cb_22"))) {
                autoCompleteQuest(player, qpm, "cb_22");
            }

            // cb_32: Cast spells from 3 spell schools ("School of Magic")
            Set<String> schools = spellSchoolSessions.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());
            schools.add(getSpellSchool(spellId));
            if (schools.size() >= 3 && !qpm.isCompleted(uuid, "cb_32") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cb_32"))) {
                autoCompleteQuest(player, qpm, "cb_32");
            }

            // cb_26: Cast 3 different spells in combat ("Spell Combo")
            if (sessionSpells.size() >= 3 && !qpm.isCompleted(uuid, "cb_26") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cb_26"))) {
                autoCompleteQuest(player, qpm, "cb_26");
            }

        } catch (Exception e) {
            MegaMod.LOGGER.debug("Error in quest spell hook: {}", e.getMessage());
        }
    }

    // ─── Equipment change hook (detects armor equips for class quests) ───

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        try {
            ServerLevel level = (ServerLevel) player.level();
            UUID uuid = player.getUUID();
            QuestProgressManager qpm = QuestProgressManager.get(level);

            String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(event.getTo().getItem()).toString();

            // cp_01: Equip a claymore or great hammer
            if ((itemId.contains("claymore") || itemId.contains("great_hammer"))
                    && !qpm.isCompleted(uuid, "cp_01") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cp_01"))) {
                autoCompleteQuest(player, qpm, "cp_01");
            }

            // cp_02: Equip a kite shield
            if (itemId.contains("kite_shield")
                    && !qpm.isCompleted(uuid, "cp_02") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cp_02"))) {
                autoCompleteQuest(player, qpm, "cp_02");
            }

            // cw_01: Equip warrior armor
            if ((itemId.contains("warrior_armor") || itemId.contains("berserker_armor"))
                    && !qpm.isCompleted(uuid, "cw_01") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cw_01"))) {
                autoCompleteQuest(player, qpm, "cw_01");
            }

            // cr_01: Equip a dagger or sickle
            if ((itemId.contains("dagger") || itemId.contains("sickle"))
                    && !qpm.isCompleted(uuid, "cr_01") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cr_01"))) {
                autoCompleteQuest(player, qpm, "cr_01");
            }

            // ca_01: Equip a bow or crossbow and a quiver (check both hand slots)
            if (itemId.contains("bow") || itemId.contains("crossbow") || itemId.contains("quiver") || itemId.contains("longbow")) {
                boolean hasBow = hasEquippedMatching(player, "bow", "crossbow", "longbow");
                boolean hasQuiver = hasEquippedMatching(player, "quiver");
                if (hasBow && hasQuiver && !qpm.isCompleted(uuid, "ca_01") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("ca_01"))) {
                    autoCompleteQuest(player, qpm, "ca_01");
                }
            }

            // Also run general quest check for auto-evaluatable quests
            checkQuestCompletion(player, level);
        } catch (Exception e) {
            MegaMod.LOGGER.debug("Error in quest equipment hook: {}", e.getMessage());
        }
    }

    // ─── Arena completion hook (called from ArenaManager.endArena) ───

    /**
     * Called when a player successfully completes an arena run.
     * Auto-completes arena-related CHECKMARK quests.
     */
    public static void onArenaComplete(UUID playerUuid, ServerLevel level, boolean isBossRush) {
        try {
            QuestProgressManager qpm = QuestProgressManager.get(level);

            // cb_17: Complete an Arena PvE run ("Arena Challenger")
            if (!qpm.isCompleted(playerUuid, "cb_17") && qpm.arePrerequisitesMet(playerUuid, QuestDefinitions.get("cb_17"))) {
                qpm.completeQuest(playerUuid, "cb_17");
                notifyQuestComplete(playerUuid, level, "cb_17");
            }

            // Increment arena run count for cb_18: Complete 5 Arena runs ("Arena Veteran")
            int runCount = arenaRunCounts.merge(playerUuid, 1, Integer::sum);
            if (runCount >= 5 && !qpm.isCompleted(playerUuid, "cb_18") && qpm.arePrerequisitesMet(playerUuid, QuestDefinitions.get("cb_18"))) {
                qpm.completeQuest(playerUuid, "cb_18");
                notifyQuestComplete(playerUuid, level, "cb_18");
            }

            // cw_04: Win an Arena bout (Warrior class quest)
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerUuid);
            if (player != null) {
                PlayerClass cls = PlayerClassManager.get(level).getPlayerClass(playerUuid);
                if (cls == PlayerClass.WARRIOR && !qpm.isCompleted(playerUuid, "cw_04") && qpm.arePrerequisitesMet(playerUuid, QuestDefinitions.get("cw_04"))) {
                    qpm.completeQuest(playerUuid, "cw_04");
                    notifyQuestComplete(playerUuid, level, "cw_04");
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.debug("Error in quest arena hook: {}", e.getMessage());
        }
    }

    // ─── Dungeon completion hook (called from DungeonManager) ───

    /**
     * Called when a player completes a dungeon. Auto-completes dungeon-related
     * class quests (e.g., cp_04: Complete a dungeon as Paladin).
     */
    public static void onDungeonComplete(UUID playerUuid, ServerLevel level) {
        try {
            QuestProgressManager qpm = QuestProgressManager.get(level);
            PlayerClass cls = PlayerClassManager.get(level).getPlayerClass(playerUuid);

            // cp_04: Complete a dungeon as Paladin
            if (cls == PlayerClass.PALADIN && !qpm.isCompleted(playerUuid, "cp_04") && qpm.arePrerequisitesMet(playerUuid, QuestDefinitions.get("cp_04"))) {
                qpm.completeQuest(playerUuid, "cp_04");
                notifyQuestComplete(playerUuid, level, "cp_04");
            }
        } catch (Exception e) {
            MegaMod.LOGGER.debug("Error in quest dungeon hook: {}", e.getMessage());
        }
    }

    // ─── Helper: per-player spell tracking for multi-spell quests ───

    /** Spells cast in current combat session (cleared periodically or on death). */
    private static final Map<UUID, Set<String>> spellCombatSessions = new ConcurrentHashMap<>();
    /** Spell schools used per session for the "3 schools" quest. */
    private static final Map<UUID, Set<String>> spellSchoolSessions = new ConcurrentHashMap<>();
    /** Shadow Step usage counts (persisted until quest complete). */
    private static final Map<UUID, Integer> shadowStepCounts = new ConcurrentHashMap<>();
    /** Arena run counts for the "5 arena runs" quest. */
    private static final Map<UUID, Integer> arenaRunCounts = new ConcurrentHashMap<>();

    /** Reset combat spell session (call on death or exiting combat). */
    public static void resetSpellSession(UUID uuid) {
        spellCombatSessions.remove(uuid);
        spellSchoolSessions.remove(uuid);
    }

    private static boolean isFireSpell(String spellId) {
        return spellId.contains("fire") || spellId.equals("fireball") || spellId.equals("inferno")
                || spellId.equals("flame_strike") || spellId.equals("meteor");
    }

    private static boolean isFrostSpell(String spellId) {
        return spellId.contains("frost") || spellId.equals("frostbolt") || spellId.equals("blizzard")
                || spellId.equals("ice_lance") || spellId.equals("frozen_orb");
    }

    private static String getSpellSchool(String spellId) {
        if (isFireSpell(spellId)) return "FIRE";
        if (isFrostSpell(spellId)) return "FROST";
        if (spellId.contains("heal") || spellId.equals("circle_of_healing") || spellId.equals("flash_heal")) return "HEALING";
        if (spellId.contains("shadow") || spellId.equals("vanish") || spellId.equals("backstab")) return "SOUL";
        if (spellId.contains("lightning") || spellId.equals("chain_lightning") || spellId.equals("thunder_bolt")) return "LIGHTNING";
        return "ARCANE"; // default
    }

    private static boolean isDungeonDimension(ServerPlayer player) {
        String dim = player.level().dimension().identifier().toString();
        return dim.contains("dungeon");
    }

    private static boolean hasEquippedMatching(ServerPlayer player, String... keywords) {
        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            net.minecraft.world.item.ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            for (String keyword : keywords) {
                if (id.contains(keyword)) return true;
            }
        }
        // Also check offhand
        net.minecraft.world.item.ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty()) {
            String offId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(offhand.getItem()).toString();
            for (String keyword : keywords) {
                if (offId.contains(keyword)) return true;
            }
        }
        return false;
    }

    private static void autoCompleteQuest(ServerPlayer player, QuestProgressManager qpm, String questId) {
        QuestDef def = QuestDefinitions.get(questId);
        if (def == null) return;

        // Verify class requirement
        PlayerClass required = QuestDefinitions.getClassRequirement(questId);
        if (required != null) {
            PlayerClass playerClass = PlayerClassManager.get((ServerLevel) player.level()).getPlayerClass(player.getUUID());
            if (playerClass != required && playerClass != PlayerClass.NONE) return;
        }

        qpm.completeQuest(player.getUUID(), questId);
        qpm.untrackQuest(player.getUUID(), questId);
        player.sendSystemMessage(Component.literal("[Quest Complete] ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(def.title()).withStyle(ChatFormatting.WHITE)));
    }

    private static void notifyQuestComplete(UUID uuid, ServerLevel level, String questId) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
        if (player != null) {
            QuestDef def = QuestDefinitions.get(questId);
            if (def != null) {
                QuestProgressManager.get(level).untrackQuest(uuid, questId);
                player.sendSystemMessage(Component.literal("[Quest Complete] ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(def.title()).withStyle(ChatFormatting.WHITE)));
            }
        }
    }

    // ─── Party propagation ───

    private static void propagateToParty(UUID playerUuid, ServerLevel level, QuestTaskType taskType) {
        try {
            if (!PartyHandler.isInParty(playerUuid)) return;
            // Get all party members via the party system
            UUID leaderUuid = getPartyLeader(playerUuid);
            if (leaderUuid == null) return;
            PartyHandler.Party party = getParty(leaderUuid);
            if (party == null) return;

            QuestProgressManager qpm = QuestProgressManager.get(level);
            for (UUID memberUuid : party.members()) {
                if (memberUuid.equals(playerUuid)) continue; // skip self
                switch (taskType) {
                    case BOUNTY_COMPLETE -> qpm.incrementBountyComplete(memberUuid);
                    case TRADE_MARKETPLACE -> qpm.incrementMarketplaceTrade(memberUuid);
                    default -> {} // other types don't propagate
                }
            }
        } catch (Exception ignored) {}
    }

    // ─── Party accessor helpers (using PartyHandler internals) ───

    private static UUID getPartyLeader(UUID playerUuid) {
        return PartyHandler.getPartyLeader(playerUuid);
    }

    private static PartyHandler.Party getParty(UUID leaderUuid) {
        return PartyHandler.getParty(leaderUuid);
    }

    // ─── Spell Book acquisition hook (called from inventory checks or item pickup) ───

    /**
     * Called when a player picks up or crafts a spell book item.
     * Auto-completes cm_04 (Arcane Scholar) for Wizards.
     */
    public static void onSpellBookAcquired(ServerPlayer player) {
        try {
            ServerLevel level = (ServerLevel) player.level();
            UUID uuid = player.getUUID();
            QuestProgressManager qpm = QuestProgressManager.get(level);
            if (!qpm.isCompleted(uuid, "cm_04") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cm_04"))) {
                PlayerClass cls = PlayerClassManager.get(level).getPlayerClass(uuid);
                if (cls == PlayerClass.WIZARD) {
                    autoCompleteQuest(player, qpm, "cm_04");
                }
            }
        } catch (Exception ignored) {}
    }

    // ─── Stealth break attack hook (called from combat handlers when attacking from vanish) ───

    /**
     * Called when a player breaks stealth (vanish) by attacking an enemy.
     * Auto-completes cr_03 (Backstab) for Rogues.
     */
    public static void onStealthBreakAttack(ServerPlayer player) {
        try {
            ServerLevel level = (ServerLevel) player.level();
            UUID uuid = player.getUUID();
            QuestProgressManager qpm = QuestProgressManager.get(level);

            // cr_03: Attack from stealth ("Backstab")
            if (!qpm.isCompleted(uuid, "cr_03") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cr_03"))) {
                autoCompleteQuest(player, qpm, "cr_03");
            }

            // cb_36: Break stealth by attacking an enemy ("Shadow Strike")
            if (!qpm.isCompleted(uuid, "cb_36") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cb_36"))) {
                autoCompleteQuest(player, qpm, "cb_36");
            }
        } catch (Exception ignored) {}
    }

    // ─── Sweeping attack hook (called from combat when hitting 3+ mobs with one swing) ───

    /**
     * Called when a player hits 3+ enemies with a single sweeping attack.
     * Auto-completes cw_03 (Cleave Master) for Warriors.
     */
    public static void onSweepingAttack(ServerPlayer player, int hitCount) {
        try {
            if (hitCount < 3) return;
            ServerLevel level = (ServerLevel) player.level();
            UUID uuid = player.getUUID();
            QuestProgressManager qpm = QuestProgressManager.get(level);

            if (!qpm.isCompleted(uuid, "cw_03") && qpm.arePrerequisitesMet(uuid, QuestDefinitions.get("cw_03"))) {
                PlayerClass cls = PlayerClassManager.get(level).getPlayerClass(uuid);
                if (cls == PlayerClass.WARRIOR) {
                    autoCompleteQuest(player, qpm, "cw_03");
                }
            }
        } catch (Exception ignored) {}
    }

    // ─── Completion checking ───

    /**
     * Checks if any in-progress quests for this player just completed.
     * Called after event-driven counter increments.
     */
    public static void checkQuestCompletion(ServerPlayer player, ServerLevel level) {
        UUID uuid = player.getUUID();
        QuestProgressManager qpm = QuestProgressManager.get(level);

        for (QuestDef def : QuestDefinitions.ALL_QUESTS.values()) {
            if (qpm.isCompleted(uuid, def.id())) continue;
            if (!qpm.arePrerequisitesMet(uuid, def)) continue;

            // Check if all tasks are satisfied
            boolean allComplete = true;
            for (QuestTask task : def.tasks()) {
                if (task.type() == QuestTaskType.CHECKMARK) {
                    allComplete = false; // checkmarks require manual trigger
                    break;
                }
                int progress = QuestTaskEvaluator.evaluate(player, task, level);
                if (progress < task.targetAmount()) {
                    allComplete = false;
                    break;
                }
            }

            if (allComplete) {
                qpm.completeQuest(uuid, def.id());
                // Auto-untrack completed quests
                qpm.untrackQuest(uuid, def.id());
                // Notify player
                player.sendSystemMessage(Component.literal("[Quest Complete] ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(def.title()).withStyle(ChatFormatting.WHITE)));
            }
        }
    }

    private static void checkQuestCompletionByUuid(UUID uuid, ServerLevel level) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
        if (player != null) checkQuestCompletion(player, level);
    }
}

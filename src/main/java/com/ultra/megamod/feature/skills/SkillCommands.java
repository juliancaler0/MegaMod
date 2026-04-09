/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.RegisterCommandsEvent
 */
package com.ultra.megamod.feature.skills;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.ultra.megamod.feature.computer.network.handlers.SettingsHandler;
import com.ultra.megamod.feature.skills.SkillEvents;
import com.ultra.megamod.feature.skills.SkillManager;
import com.ultra.megamod.feature.skills.SkillTreeDefinitions;
import com.ultra.megamod.feature.skills.SkillTreeType;
import com.ultra.megamod.feature.skills.prestige.PrestigeManager;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid="megamod")
public class SkillCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> skills = Commands.literal("skills")
            .then(Commands.literal("info").executes(context -> {
                ServerPlayer player = ((CommandSourceStack) context.getSource()).getPlayerOrException();
                ServerLevel level = player.level();
                SkillManager manager = SkillManager.get(level);
                player.sendSystemMessage(Component.literal("\u00a76=== Skill Trees ==="));
                for (SkillTreeType type : SkillTreeType.values()) {
                    int lvl = manager.getLevel(player.getUUID(), type);
                    int xp = manager.getXp(player.getUUID(), type);
                    int required = lvl < 50 ? SkillTreeType.xpForLevel(lvl) : 0;
                    String progress = lvl < 50 ? xp + "/" + required + " XP" : "MAX";
                    PrestigeManager prestige = PrestigeManager.get(level.getServer().overworld());
                    int pLvl = prestige.getPrestigeLevel(player.getUUID(), type);
                    String pStr = pLvl > 0 ? " \u00a7d[P" + pLvl + "]" : "";
                    player.sendSystemMessage(Component.literal("\u00a7e" + type.getDisplayName() + "\u00a7r: Level \u00a7a" + lvl + "\u00a7r (" + progress + ")" + pStr));
                }
                return 1;
            }))
            .then(Commands.literal("points").executes(context -> {
                ServerPlayer player = ((CommandSourceStack) context.getSource()).getPlayerOrException();
                ServerLevel level = player.level();
                SkillManager manager = SkillManager.get(level);
                int points = manager.getAvailablePoints(player.getUUID());
                player.sendSystemMessage(Component.literal("\u00a76Available Skill Points: \u00a7a" + points));
                int unlocked = manager.getUnlockedNodes(player.getUUID()).size();
                int total = SkillTreeDefinitions.getAllNodes().size();
                player.sendSystemMessage(Component.literal("\u00a76Nodes Unlocked: \u00a7a" + unlocked + "/" + total));
                return 1;
            }))
            .then(Commands.literal("addxp")
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
                .then(Commands.argument("tree", StringArgumentType.word())
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10000))
                        .executes(context -> {
                            ServerPlayer player = ((CommandSourceStack) context.getSource()).getPlayerOrException();
                            String treeName = StringArgumentType.getString(context, "tree");
                            int amount = IntegerArgumentType.getInteger(context, "amount");
                            SkillTreeType tree = parseTree(treeName);
                            if (tree == null) {
                                player.sendSystemMessage(Component.literal("\u00a7cInvalid tree name. Use: combat, mining, farming, arcane, survival"));
                                return 0;
                            }
                            ServerLevel level = player.level();
                            SkillManager manager = SkillManager.get(level);
                            int levelsGained = manager.addXp(player.getUUID(), tree, amount);
                            player.sendSystemMessage(Component.literal("\u00a7aAdded " + amount + " XP to " + tree.getDisplayName() + (levelsGained > 0 ? " (+" + levelsGained + " levels!)" : "")));
                            SkillEvents.syncToClient(player);
                            return 1;
                        }))));


        LiteralArgumentBuilder<CommandSourceStack> badge = Commands.literal("badge")
            .executes(context -> {
                ServerPlayer player = ((CommandSourceStack) context.getSource()).getPlayerOrException();
                boolean current = SettingsHandler.isEnabled(player.getUUID(), "skill_badge");
                // Toggle — we need to call the settings handler to flip
                SettingsHandler.toggleSetting(player.getUUID(), "skill_badge");
                boolean newVal = !current;
                player.sendSystemMessage(Component.literal("\u00a76Skill badge " + (newVal ? "\u00a7aenabled" : "\u00a7cdisabled") + "\u00a76."));
                return 1;
            });

        event.getDispatcher().register(Commands.literal("megamod").then(skills).then(badge));
    }

    private static SkillTreeType parseTree(String name) {
        for (SkillTreeType t : SkillTreeType.values()) {
            if (t.name().equalsIgnoreCase(name)) return t;
        }
        return null;
    }

}


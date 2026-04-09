package com.ultra.megamod.feature.combat;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Admin commands for managing player classes.
 * /megamod class set <player> <class> — Change a player's class
 * /megamod class info <player>        — View a player's current class
 * /megamod class reset <player>       — Reset a player's class to NONE
 * /megamod class list                 — List all players and their classes
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class ClassCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("megamod").then(
                Commands.literal("class")
                    .requires(src -> Commands.LEVEL_GAMEMASTERS.check(src.permissions()))
                    .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                            .then(Commands.argument("class", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (PlayerClass cls : PlayerClass.values()) {
                                        if (cls != PlayerClass.NONE) {
                                            builder.suggest(cls.name().toLowerCase());
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> setClass(
                                    ctx.getSource(),
                                    EntityArgument.getPlayer(ctx, "player"),
                                    StringArgumentType.getString(ctx, "class")
                                ))
                            )
                        )
                    )
                    .then(Commands.literal("info")
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(ctx -> showInfo(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))
                        )
                        .executes(ctx -> showInfo(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                    )
                    .then(Commands.literal("reset")
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(ctx -> resetClass(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))
                        )
                    )
                    .then(Commands.literal("list")
                        .executes(ctx -> listClasses(ctx.getSource()))
                    )
            )
        );
    }

    private static int setClass(CommandSourceStack source, ServerPlayer target, String className) {
        PlayerClass newClass;
        try {
            newClass = PlayerClass.valueOf(className.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("Invalid class: " + className + ". Valid: paladin, warrior, wizard, rogue, ranger")
                .withStyle(ChatFormatting.RED));
            return 0;
        }
        if (newClass == PlayerClass.NONE) {
            source.sendFailure(Component.literal("Use /megamod class reset to remove a class.")
                .withStyle(ChatFormatting.RED));
            return 0;
        }

        ServerLevel overworld = source.getServer().overworld();
        PlayerClassManager classManager = PlayerClassManager.get(overworld);
        PlayerClass oldClass = classManager.getPlayerClass(target.getUUID());
        classManager.setClass(target.getUUID(), newClass);
        classManager.saveToDisk(overworld);

        String targetName = target.getGameProfile().name();
        source.sendSuccess(() -> Component.literal("Set " + targetName + "'s class to ")
            .withStyle(ChatFormatting.GREEN)
            .append(Component.literal(newClass.getDisplayName())
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(newClass.getColor())))), true);

        // Notify the target player
        if (!target.equals(source.getPlayer())) {
            target.sendSystemMessage(Component.literal("Your class has been changed to ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(newClass.getDisplayName())
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(newClass.getColor()))))
                .append(Component.literal(" by an admin.").withStyle(ChatFormatting.GOLD)));
        }

        return 1;
    }

    private static int showInfo(CommandSourceStack source, ServerPlayer target) {
        ServerLevel overworld = source.getServer().overworld();
        PlayerClassManager classManager = PlayerClassManager.get(overworld);
        PlayerClass cls = classManager.getPlayerClass(target.getUUID());
        String targetName = target.getGameProfile().name();

        if (cls == PlayerClass.NONE) {
            source.sendSuccess(() -> Component.literal(targetName + " has not chosen a class yet.")
                .withStyle(ChatFormatting.GRAY), false);
        } else {
            source.sendSuccess(() -> Component.literal(targetName + "'s class: ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(cls.getDisplayName())
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(cls.getColor())))), false);
        }
        return 1;
    }

    private static int resetClass(CommandSourceStack source, ServerPlayer target) {
        ServerLevel overworld = source.getServer().overworld();
        PlayerClassManager classManager = PlayerClassManager.get(overworld);
        classManager.setClass(target.getUUID(), PlayerClass.NONE);
        classManager.saveToDisk(overworld);

        String targetName = target.getGameProfile().name();
        source.sendSuccess(() -> Component.literal("Reset " + targetName + "'s class to NONE.")
            .withStyle(ChatFormatting.YELLOW), true);

        if (!target.equals(source.getPlayer())) {
            target.sendSystemMessage(Component.literal("Your class has been reset by an admin. Choose a new class at any time.")
                .withStyle(ChatFormatting.YELLOW));
        }
        return 1;
    }

    private static int listClasses(CommandSourceStack source) {
        ServerLevel overworld = source.getServer().overworld();
        PlayerClassManager classManager = PlayerClassManager.get(overworld);
        var allClasses = classManager.getAllClasses();

        source.sendSuccess(() -> Component.literal("=== Player Classes ===").withStyle(ChatFormatting.GOLD), false);

        if (allClasses.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No players have chosen a class.").withStyle(ChatFormatting.GRAY), false);
            return 1;
        }

        for (var entry : allClasses.entrySet()) {
            ServerPlayer player = source.getServer().getPlayerList().getPlayer(entry.getKey());
            String name = player != null ? player.getGameProfile().name() : entry.getKey().toString().substring(0, 8) + "...";
            PlayerClass cls = entry.getValue();
            String status = player != null ? "" : " (offline)";
            source.sendSuccess(() -> Component.literal("  " + name + status + ": ")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(cls.getDisplayName())
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(cls.getColor())))), false);
        }
        return 1;
    }
}

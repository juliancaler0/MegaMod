package com.ultra.megamod.feature.worldedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ultra.megamod.feature.worldedit.brush.Brush;
import com.ultra.megamod.feature.worldedit.brush.BrushBinding;
import com.ultra.megamod.feature.worldedit.brush.ClipboardBrush;
import com.ultra.megamod.feature.worldedit.brush.CuboidBrush;
import com.ultra.megamod.feature.worldedit.brush.CylinderBrush;
import com.ultra.megamod.feature.worldedit.brush.SmoothBrush;
import com.ultra.megamod.feature.worldedit.brush.SphereBrush;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;
import com.ultra.megamod.feature.worldedit.pattern.PatternParser;
import com.ultra.megamod.feature.worldedit.session.LocalSession;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Brush binding commands. */
public final class BrushCommands {
    private BrushCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("we_brush")
            .then(Commands.literal("sphere").then(Commands.argument("size", IntegerArgumentType.integer(1))
                .then(Commands.argument("pattern", StringArgumentType.greedyString())
                    .executes(ctx -> bind(ctx.getSource(),
                        new SphereBrush(IntegerArgumentType.getInteger(ctx, "size"), false),
                        IntegerArgumentType.getInteger(ctx, "size"),
                        StringArgumentType.getString(ctx, "pattern"))))))
            .then(Commands.literal("hsphere").then(Commands.argument("size", IntegerArgumentType.integer(1))
                .then(Commands.argument("pattern", StringArgumentType.greedyString())
                    .executes(ctx -> bind(ctx.getSource(),
                        new SphereBrush(IntegerArgumentType.getInteger(ctx, "size"), true),
                        IntegerArgumentType.getInteger(ctx, "size"),
                        StringArgumentType.getString(ctx, "pattern"))))))
            .then(Commands.literal("cyl").then(Commands.argument("size", IntegerArgumentType.integer(1))
                .then(Commands.argument("height", IntegerArgumentType.integer(1))
                    .then(Commands.argument("pattern", StringArgumentType.greedyString())
                        .executes(ctx -> bind(ctx.getSource(),
                            new CylinderBrush(IntegerArgumentType.getInteger(ctx, "size"),
                                              IntegerArgumentType.getInteger(ctx, "height"), false),
                            IntegerArgumentType.getInteger(ctx, "size"),
                            StringArgumentType.getString(ctx, "pattern")))))))
            .then(Commands.literal("cube").then(Commands.argument("size", IntegerArgumentType.integer(1))
                .then(Commands.argument("pattern", StringArgumentType.greedyString())
                    .executes(ctx -> bind(ctx.getSource(),
                        new CuboidBrush(IntegerArgumentType.getInteger(ctx, "size")),
                        IntegerArgumentType.getInteger(ctx, "size"),
                        StringArgumentType.getString(ctx, "pattern"))))))
            .then(Commands.literal("smooth").then(Commands.argument("size", IntegerArgumentType.integer(1))
                .executes(ctx -> bind(ctx.getSource(),
                    new SmoothBrush(IntegerArgumentType.getInteger(ctx, "size"), 1),
                    IntegerArgumentType.getInteger(ctx, "size"), "air"))))
            .then(Commands.literal("paste").executes(ctx -> bindClipboard(ctx.getSource())))
            .then(Commands.literal("none").executes(ctx -> unbind(ctx.getSource())))
            .then(Commands.literal("list").executes(ctx -> list(ctx.getSource())))
        );
        d.register(Commands.literal("we_mask").then(Commands.argument("mask", StringArgumentType.greedyString())
            .executes(ctx -> setMask(ctx.getSource(), StringArgumentType.getString(ctx, "mask")))));
        d.register(Commands.literal("we_gmask").then(Commands.argument("mask", StringArgumentType.greedyString())
            .executes(ctx -> setGlobalMask(ctx.getSource(), StringArgumentType.getString(ctx, "mask")))));
    }

    private static int bind(CommandSourceStack src, Brush brush, int size, String patternStr) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Pattern pattern = PatternParser.parse(patternStr);
        if (pattern == null) { WECommandUtil.error(sp, "Bad pattern."); return 0; }
        BrushBinding b = new BrushBinding(brush, size, pattern);
        WECommandUtil.session(sp).setBrush(LocalSession.BrushSlot.MAIN, b);
        WECommandUtil.info(sp, "Brush bound: " + brush.describe() + " / " + patternStr);
        return 1;
    }

    private static int bindClipboard(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var ls = WECommandUtil.session(sp);
        if (ls.getClipboard() == null) { WECommandUtil.error(sp, "Clipboard empty."); return 0; }
        ls.setBrush(LocalSession.BrushSlot.MAIN, new BrushBinding(new ClipboardBrush(ls.getClipboard(), true), 1, null));
        WECommandUtil.info(sp, "Clipboard brush bound.");
        return 1;
    }

    private static int unbind(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        WECommandUtil.session(sp).clearBrush(LocalSession.BrushSlot.MAIN);
        WECommandUtil.info(sp, "Brush cleared.");
        return 1;
    }

    private static int list(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var ls = WECommandUtil.session(sp);
        BrushBinding main = ls.getBrush(LocalSession.BrushSlot.MAIN);
        sp.sendSystemMessage(Component.literal("Main brush: " + (main == null ? "none" : main.getBrush().describe())));
        return 1;
    }

    private static int setMask(CommandSourceStack src, String m) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var ls = WECommandUtil.session(sp);
        var brush = ls.getBrush(LocalSession.BrushSlot.MAIN);
        if (brush == null) { WECommandUtil.error(sp, "No active brush."); return 0; }
        if (m.equalsIgnoreCase("none") || m.isEmpty()) { brush.setMask(null); WECommandUtil.info(sp, "Brush mask cleared."); return 1; }
        var mask = com.ultra.megamod.feature.worldedit.mask.MaskParser.parse(m);
        if (mask == null) { WECommandUtil.error(sp, "Bad mask."); return 0; }
        brush.setMask(mask);
        WECommandUtil.info(sp, "Brush mask set.");
        return 1;
    }

    private static int setGlobalMask(CommandSourceStack src, String m) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var ls = WECommandUtil.session(sp);
        if (m.equalsIgnoreCase("none") || m.isEmpty()) { ls.setActiveMask(null); WECommandUtil.info(sp, "Global mask cleared."); return 1; }
        var mask = com.ultra.megamod.feature.worldedit.mask.MaskParser.parse(m);
        if (mask == null) { WECommandUtil.error(sp, "Bad mask."); return 0; }
        ls.setActiveMask(mask);
        WECommandUtil.info(sp, "Global mask set.");
        return 1;
    }
}

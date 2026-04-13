package com.ultra.megamod.feature.worldedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ultra.megamod.feature.worldedit.EditSession;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;
import com.ultra.megamod.feature.worldedit.pattern.PatternParser;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/**
 * Shape generation commands: sphere, cyl, pyramid, hsphere.
 */
public final class GenerationCommands {

    private GenerationCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("we_sphere")
            .then(Commands.argument("pattern", StringArgumentType.word())
                .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                    .executes(ctx -> sphere(ctx.getSource(),
                        StringArgumentType.getString(ctx, "pattern"),
                        IntegerArgumentType.getInteger(ctx, "radius"), true)))));
        d.register(Commands.literal("we_hsphere")
            .then(Commands.argument("pattern", StringArgumentType.word())
                .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                    .executes(ctx -> sphere(ctx.getSource(),
                        StringArgumentType.getString(ctx, "pattern"),
                        IntegerArgumentType.getInteger(ctx, "radius"), false)))));
        d.register(Commands.literal("we_cyl")
            .then(Commands.argument("pattern", StringArgumentType.word())
                .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                    .executes(ctx -> cyl(ctx.getSource(), StringArgumentType.getString(ctx, "pattern"),
                        IntegerArgumentType.getInteger(ctx, "radius"), 1, true))
                    .then(Commands.argument("height", IntegerArgumentType.integer(1))
                        .executes(ctx -> cyl(ctx.getSource(),
                            StringArgumentType.getString(ctx, "pattern"),
                            IntegerArgumentType.getInteger(ctx, "radius"),
                            IntegerArgumentType.getInteger(ctx, "height"), true))))));
        d.register(Commands.literal("we_hcyl")
            .then(Commands.argument("pattern", StringArgumentType.word())
                .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                    .then(Commands.argument("height", IntegerArgumentType.integer(1))
                        .executes(ctx -> cyl(ctx.getSource(),
                            StringArgumentType.getString(ctx, "pattern"),
                            IntegerArgumentType.getInteger(ctx, "radius"),
                            IntegerArgumentType.getInteger(ctx, "height"), false))))));
        d.register(Commands.literal("we_pyramid")
            .then(Commands.argument("pattern", StringArgumentType.word())
                .then(Commands.argument("size", IntegerArgumentType.integer(1))
                    .executes(ctx -> pyramid(ctx.getSource(),
                        StringArgumentType.getString(ctx, "pattern"),
                        IntegerArgumentType.getInteger(ctx, "size"), true)))));
        d.register(Commands.literal("we_hpyramid")
            .then(Commands.argument("pattern", StringArgumentType.word())
                .then(Commands.argument("size", IntegerArgumentType.integer(1))
                    .executes(ctx -> pyramid(ctx.getSource(),
                        StringArgumentType.getString(ctx, "pattern"),
                        IntegerArgumentType.getInteger(ctx, "size"), false)))));
        d.register(Commands.literal("we_generate")
            .then(Commands.argument("pattern", StringArgumentType.word())
                .then(Commands.argument("expr", StringArgumentType.greedyString())
                    .executes(ctx -> generate(ctx.getSource(),
                        StringArgumentType.getString(ctx, "pattern"),
                        StringArgumentType.getString(ctx, "expr"))))));
    }

    private static int sphere(CommandSourceStack src, String pat, int r, boolean filled) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Pattern pattern = PatternParser.parse(pat);
        if (pattern == null) { WECommandUtil.error(sp, "Bad pattern."); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        es.makeSphere(sp.blockPosition(), r, r, r, pattern, filled);
        WECommandUtil.finish(sp, es, (filled ? "sphere " : "hsphere ") + "r=" + r);
        return 1;
    }

    private static int cyl(CommandSourceStack src, String pat, int r, int h, boolean filled) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Pattern pattern = PatternParser.parse(pat);
        if (pattern == null) { WECommandUtil.error(sp, "Bad pattern."); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        es.makeCylinder(sp.blockPosition(), pattern, r, r, h, filled);
        WECommandUtil.finish(sp, es, (filled ? "cyl " : "hcyl ") + "r=" + r + " h=" + h);
        return 1;
    }

    private static int pyramid(CommandSourceStack src, String pat, int size, boolean filled) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Pattern pattern = PatternParser.parse(pat);
        if (pattern == null) { WECommandUtil.error(sp, "Bad pattern."); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        es.makePyramid(sp.blockPosition(), pattern, size, filled);
        WECommandUtil.finish(sp, es, (filled ? "pyramid" : "hpyramid") + " s=" + size);
        return 1;
    }

    private static int generate(CommandSourceStack src, String pat, String expr) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        // The WorldEdit generate command uses an expression language we are not porting;
        // MegaMod's port accepts a handful of simple keywords that map to existing shapes.
        WECommandUtil.info(sp, "Note: use /we_sphere, /we_cyl, /we_pyramid for parametric shapes. "
            + "The full expression language is not ported in MegaMod.");
        return 1;
    }
}

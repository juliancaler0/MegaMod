package com.ultra.megamod.feature.worldedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ultra.megamod.feature.worldedit.clipboard.Clipboard;
import com.ultra.megamod.feature.worldedit.clipboard.ClipboardIO;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * Schematic IO — save, load, list, delete. Operates on the existing
 * blueprints/ directory so saved clipboards are usable by the rest of
 * the mod's schematic pipeline.
 */
public final class SchematicCommands {
    private SchematicCommands() {}

    public static final String SCHEM_FOLDER = "worldedit_schematics";

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("we_schem")
            .then(Commands.literal("save").then(Commands.argument("name", StringArgumentType.word())
                .executes(ctx -> save(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("load").then(Commands.argument("name", StringArgumentType.word())
                .executes(ctx -> load(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("list").executes(ctx -> list(ctx.getSource())))
            .then(Commands.literal("delete").then(Commands.argument("name", StringArgumentType.word())
                .executes(ctx -> delete(ctx.getSource(), StringArgumentType.getString(ctx, "name")))))
        );
        // Legacy WorldEdit aliases
        d.register(Commands.literal("we_schematic")
            .redirect(d.getRoot().getChild("we_schem")));
    }

    private static Path folder() {
        Path base = Paths.get("blueprints", SCHEM_FOLDER);
        try {
            Files.createDirectories(base);
        } catch (IOException ignored) {}
        return base;
    }

    private static int save(CommandSourceStack src, String name) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Clipboard cb = WECommandUtil.session(sp).getClipboard();
        if (cb == null) { WECommandUtil.error(sp, "Clipboard empty."); return 0; }
        Path p = folder().resolve(name + ".litematic");
        var data = ClipboardIO.toSchematicData(cb);
        if (ClipboardIO.saveSchematic(p, data)) {
            WECommandUtil.info(sp, "Saved " + cb.getVolume() + " blocks to " + p.getFileName());
            return 1;
        }
        WECommandUtil.error(sp, "Save failed.");
        return 0;
    }

    private static int load(CommandSourceStack src, String name) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Path p = folder().resolve(name + ".litematic");
        if (!Files.exists(p)) { WECommandUtil.error(sp, "Not found: " + name); return 0; }
        Clipboard cb = ClipboardIO.loadSchematicAsClipboard(p);
        if (cb == null) { WECommandUtil.error(sp, "Load failed."); return 0; }
        WECommandUtil.session(sp).setClipboard(cb);
        WECommandUtil.info(sp, "Loaded '" + name + "' (" + cb.getVolume() + " blocks) into clipboard.");
        return 1;
    }

    private static int list(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        try (Stream<Path> s = Files.list(folder())) {
            List<Path> files = s.filter(p -> p.toString().endsWith(".litematic")).toList();
            WECommandUtil.info(sp, "Schematics (" + files.size() + "):");
            for (Path f : files) {
                String n = f.getFileName().toString();
                n = n.substring(0, n.length() - ".litematic".length());
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("  " + n));
            }
        } catch (IOException e) {
            WECommandUtil.error(sp, "Failed to list: " + e.getMessage());
        }
        return 1;
    }

    private static int delete(CommandSourceStack src, String name) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Path p = folder().resolve(name + ".litematic");
        try {
            if (Files.deleteIfExists(p)) WECommandUtil.info(sp, "Deleted " + name);
            else WECommandUtil.error(sp, "Not found: " + name);
        } catch (IOException e) {
            WECommandUtil.error(sp, "Delete failed: " + e.getMessage());
        }
        return 1;
    }
}

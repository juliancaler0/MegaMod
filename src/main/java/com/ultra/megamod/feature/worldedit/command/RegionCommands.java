package com.ultra.megamod.feature.worldedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ultra.megamod.feature.worldedit.EditSession;
import com.ultra.megamod.feature.worldedit.mask.Mask;
import com.ultra.megamod.feature.worldedit.mask.MaskParser;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;
import com.ultra.megamod.feature.worldedit.pattern.PatternParser;
import com.ultra.megamod.feature.worldedit.region.Region;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Region-wide operations: set, replace, walls, faces, overlay, etc.
 *
 * Commands:
 *   /we_set <pattern>
 *   /we_replace <from> <to>
 *   /we_stack <n> [dir]
 *   /we_move <n> [dir]
 *   /we_walls <pattern>
 *   /we_faces <pattern>
 *   /we_overlay <pattern>
 *   /we_center <pattern>
 *   /we_smooth [iterations]
 *   /we_regen
 *   /we_deform <expression>
 *   /we_hollow [thickness]
 *   /we_forest [density]
 *   /we_flora [density]
 *   /we_naturalize
 */
public final class RegionCommands {

    private RegionCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("we_set").then(Commands.argument("pattern", StringArgumentType.greedyString())
            .executes(ctx -> set(ctx.getSource(), StringArgumentType.getString(ctx, "pattern")))));
        d.register(Commands.literal("we_replace")
            .then(Commands.argument("from", StringArgumentType.word())
                .then(Commands.argument("to", StringArgumentType.greedyString())
                    .executes(ctx -> replace(ctx.getSource(),
                        StringArgumentType.getString(ctx, "from"),
                        StringArgumentType.getString(ctx, "to"))))));
        d.register(Commands.literal("we_stack").then(Commands.argument("n", IntegerArgumentType.integer(1))
            .executes(ctx -> stack(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n"), "forward"))
            .then(Commands.argument("dir", StringArgumentType.word())
                .executes(ctx -> stack(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n"),
                    StringArgumentType.getString(ctx, "dir"))))));
        d.register(Commands.literal("we_move").then(Commands.argument("n", IntegerArgumentType.integer(1))
            .executes(ctx -> move(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n"), "forward"))
            .then(Commands.argument("dir", StringArgumentType.word())
                .executes(ctx -> move(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n"),
                    StringArgumentType.getString(ctx, "dir"))))));
        d.register(Commands.literal("we_walls").then(Commands.argument("pattern", StringArgumentType.greedyString())
            .executes(ctx -> walls(ctx.getSource(), StringArgumentType.getString(ctx, "pattern")))));
        d.register(Commands.literal("we_faces").then(Commands.argument("pattern", StringArgumentType.greedyString())
            .executes(ctx -> faces(ctx.getSource(), StringArgumentType.getString(ctx, "pattern")))));
        d.register(Commands.literal("we_overlay").then(Commands.argument("pattern", StringArgumentType.greedyString())
            .executes(ctx -> overlay(ctx.getSource(), StringArgumentType.getString(ctx, "pattern")))));
        d.register(Commands.literal("we_center").then(Commands.argument("pattern", StringArgumentType.greedyString())
            .executes(ctx -> center(ctx.getSource(), StringArgumentType.getString(ctx, "pattern")))));
        d.register(Commands.literal("we_smooth")
            .executes(ctx -> smooth(ctx.getSource(), 1))
            .then(Commands.argument("iter", IntegerArgumentType.integer(1))
                .executes(ctx -> smooth(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "iter")))));
        d.register(Commands.literal("we_regen").executes(ctx -> regen(ctx.getSource())));
        d.register(Commands.literal("we_hollow")
            .executes(ctx -> hollow(ctx.getSource(), 1))
            .then(Commands.argument("t", IntegerArgumentType.integer(1))
                .executes(ctx -> hollow(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "t")))));
        d.register(Commands.literal("we_forest")
            .executes(ctx -> forest(ctx.getSource(), 5))
            .then(Commands.argument("dens", IntegerArgumentType.integer(1, 100))
                .executes(ctx -> forest(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "dens")))));
        d.register(Commands.literal("we_flora")
            .executes(ctx -> flora(ctx.getSource(), 10))
            .then(Commands.argument("dens", IntegerArgumentType.integer(1, 100))
                .executes(ctx -> flora(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "dens")))));
        d.register(Commands.literal("we_naturalize").executes(ctx -> naturalize(ctx.getSource())));
    }

    private static int set(CommandSourceStack src, String patternStr) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        Pattern p = PatternParser.parse(patternStr);
        if (p == null) { WECommandUtil.error(sp, "Invalid pattern: " + patternStr); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        es.set(r, p);
        WECommandUtil.finish(sp, es, "set " + patternStr);
        return 1;
    }

    private static int replace(CommandSourceStack src, String from, String to) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        Mask mask = MaskParser.parse(from);
        Pattern pattern = PatternParser.parse(to);
        if (mask == null || pattern == null) { WECommandUtil.error(sp, "Invalid arguments."); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        es.replace(r, mask, pattern);
        WECommandUtil.finish(sp, es, "replace " + from + "->" + to);
        return 1;
    }

    private static int walls(CommandSourceStack src, String patternStr) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        Pattern p = PatternParser.parse(patternStr);
        if (p == null) { WECommandUtil.error(sp, "Invalid pattern."); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        es.makeWalls(r, p);
        WECommandUtil.finish(sp, es, "walls " + patternStr);
        return 1;
    }

    private static int faces(CommandSourceStack src, String patternStr) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        Pattern p = PatternParser.parse(patternStr);
        if (p == null) { WECommandUtil.error(sp, "Invalid pattern."); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        es.makeFaces(r, p);
        WECommandUtil.finish(sp, es, "faces " + patternStr);
        return 1;
    }

    private static int overlay(CommandSourceStack src, String patternStr) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        Pattern p = PatternParser.parse(patternStr);
        if (p == null) { WECommandUtil.error(sp, "Invalid pattern."); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        es.overlay(r, p);
        WECommandUtil.finish(sp, es, "overlay " + patternStr);
        return 1;
    }

    private static int center(CommandSourceStack src, String patternStr) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        Pattern p = PatternParser.parse(patternStr);
        if (p == null) { WECommandUtil.error(sp, "Invalid pattern."); return 0; }
        BlockPos mn = r.getMinimumPoint();
        BlockPos mx = r.getMaximumPoint();
        int cx = (mn.getX() + mx.getX()) / 2;
        int cy = (mn.getY() + mx.getY()) / 2;
        int cz = (mn.getZ() + mx.getZ()) / 2;
        EditSession es = WECommandUtil.newEdit(sp);
        es.setBlock(new BlockPos(cx, cy, cz), p);
        WECommandUtil.finish(sp, es, "center " + patternStr);
        return 1;
    }

    private static int smooth(CommandSourceStack src, int iter) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        // simple smoothing pass: break hanging blocks
        for (int i = 0; i < iter; i++) {
            for (BlockPos p : r) {
                var st = sp.level().getBlockState(p);
                if (st.isAir()) continue;
                int exposed = 0;
                for (var dir : net.minecraft.core.Direction.values()) {
                    if (sp.level().getBlockState(p.relative(dir)).isAir()) exposed++;
                }
                if (exposed >= 5) es.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
        }
        WECommandUtil.finish(sp, es, "smooth x" + iter);
        return 1;
    }

    private static int regen(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        WECommandUtil.info(sp, "Chunk regeneration is not supported in MegaMod's engine. " +
            "Use /we_set air then rebuild manually.");
        return 1;
    }

    private static int hollow(CommandSourceStack src, int thickness) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        // carve interior
        BlockPos mn = r.getMinimumPoint();
        BlockPos mx = r.getMaximumPoint();
        for (BlockPos p : r) {
            boolean onShell = p.getX() - mn.getX() < thickness || mx.getX() - p.getX() < thickness
                           || p.getY() - mn.getY() < thickness || mx.getY() - p.getY() < thickness
                           || p.getZ() - mn.getZ() < thickness || mx.getZ() - p.getZ() < thickness;
            if (!onShell) es.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        }
        WECommandUtil.finish(sp, es, "hollow t=" + thickness);
        return 1;
    }

    private static int forest(CommandSourceStack src, int density) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        BlockPos mn = r.getMinimumPoint();
        BlockPos mx = r.getMaximumPoint();
        var rand = sp.level().random;
        int placed = 0;
        EditSession es = WECommandUtil.newEdit(sp);
        for (int x = mn.getX(); x <= mx.getX(); x++) {
            for (int z = mn.getZ(); z <= mx.getZ(); z++) {
                if (rand.nextInt(100) >= density) continue;
                for (int y = mx.getY(); y >= mn.getY(); y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (!sp.level().getBlockState(p).isAir()
                        && sp.level().getBlockState(p.above()).isAir()) {
                        placeSimpleOak(es, p.above(), rand);
                        placed++;
                        break;
                    }
                }
            }
        }
        WECommandUtil.finish(sp, es, "forest density=" + density);
        WECommandUtil.info(sp, "Planted " + placed + " tree(s).");
        return 1;
    }

    private static void placeSimpleOak(EditSession es, BlockPos base, net.minecraft.util.RandomSource rand) {
        int trunkHeight = 4 + rand.nextInt(3);
        var log = net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState();
        var leaves = net.minecraft.world.level.block.Blocks.OAK_LEAVES.defaultBlockState();
        for (int i = 0; i < trunkHeight; i++) es.setBlock(base.above(i), log);
        // leaf canopy: radius 2 top, radius 1 above
        BlockPos top = base.above(trunkHeight);
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
            for (int dy = -1; dy <= 0; dy++) {
                int dist = Math.abs(dx) + Math.abs(dz) + Math.abs(dy);
                if (dist <= 3) es.setBlock(top.offset(dx, dy, dz), leaves);
            }
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            if (dx == 0 || dz == 0) es.setBlock(top.offset(dx, 1, dz), leaves);
        }
        es.setBlock(top.above(), leaves);
    }

    private static int flora(CommandSourceStack src, int density) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        var rand = sp.level().random;
        EditSession es = WECommandUtil.newEdit(sp);
        BlockPos mn = r.getMinimumPoint();
        BlockPos mx = r.getMaximumPoint();
        var flowers = new net.minecraft.world.level.block.state.BlockState[] {
            net.minecraft.world.level.block.Blocks.POPPY.defaultBlockState(),
            net.minecraft.world.level.block.Blocks.DANDELION.defaultBlockState(),
            net.minecraft.world.level.block.Blocks.BLUE_ORCHID.defaultBlockState(),
            net.minecraft.world.level.block.Blocks.CORNFLOWER.defaultBlockState(),
            net.minecraft.world.level.block.Blocks.SHORT_GRASS.defaultBlockState(),
        };
        for (int x = mn.getX(); x <= mx.getX(); x++) {
            for (int z = mn.getZ(); z <= mx.getZ(); z++) {
                if (rand.nextInt(100) >= density) continue;
                for (int y = mx.getY(); y >= mn.getY(); y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    var st = sp.level().getBlockState(p);
                    if (st.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) && sp.level().getBlockState(p.above()).isAir()) {
                        es.setBlock(p.above(), flowers[rand.nextInt(flowers.length)]);
                        break;
                    }
                }
            }
        }
        WECommandUtil.finish(sp, es, "flora density=" + density);
        return 1;
    }

    private static int naturalize(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        // for each column, top solid -> grass block; 3 below -> dirt; rest stone
        BlockPos mn = r.getMinimumPoint();
        BlockPos mx = r.getMaximumPoint();
        var blocks = sp.level();
        for (int x = mn.getX(); x <= mx.getX(); x++) {
            for (int z = mn.getZ(); z <= mx.getZ(); z++) {
                int depth = 0;
                for (int y = mx.getY(); y >= mn.getY(); y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    var st = blocks.getBlockState(p);
                    if (st.isAir()) { depth = 0; continue; }
                    if (depth == 0) es.setBlock(p, net.minecraft.world.level.block.Blocks.GRASS_BLOCK.defaultBlockState());
                    else if (depth < 4) es.setBlock(p, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
                    else es.setBlock(p, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                    depth++;
                }
            }
        }
        WECommandUtil.finish(sp, es, "naturalize");
        return 1;
    }

    private static int stack(CommandSourceStack src, int n, String dir) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        BlockPos size = new BlockPos(r.getDimensions().getX(), r.getDimensions().getY(), r.getDimensions().getZ());
        BlockPos unit = unitDir(sp, dir);
        EditSession es = WECommandUtil.newEdit(sp);
        for (int i = 1; i <= n; i++) {
            BlockPos off = new BlockPos(unit.getX() * size.getX() * i, unit.getY() * size.getY() * i, unit.getZ() * size.getZ() * i);
            for (BlockPos p : r) {
                var st = sp.level().getBlockState(p);
                if (st.isAir()) continue;
                es.setBlock(p.offset(off), st);
            }
        }
        WECommandUtil.finish(sp, es, "stack " + n + " " + dir);
        return 1;
    }

    private static int move(CommandSourceStack src, int n, String dir) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        Region r = WECommandUtil.session(sp).getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        BlockPos unit = unitDir(sp, dir);
        BlockPos delta = new BlockPos(unit.getX() * n, unit.getY() * n, unit.getZ() * n);
        EditSession es = WECommandUtil.newEdit(sp);
        // capture original states, write new, erase source
        java.util.Map<BlockPos, net.minecraft.world.level.block.state.BlockState> snapshot = new java.util.HashMap<>();
        for (BlockPos p : r) snapshot.put(p.immutable(), sp.level().getBlockState(p));
        for (BlockPos p : r) es.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        for (var e : snapshot.entrySet()) {
            if (!e.getValue().isAir()) es.setBlock(e.getKey().offset(delta), e.getValue());
        }
        // shift selection to match
        r.shift(delta);
        if (r instanceof com.ultra.megamod.feature.worldedit.region.CuboidRegion cr) {
            WECommandUtil.session(sp).setPos1(cr.getPos1());
            WECommandUtil.session(sp).setPos2(cr.getPos2());
        }
        WECommandUtil.finish(sp, es, "move " + n + " " + dir);
        return 1;
    }

    private static BlockPos unitDir(ServerPlayer sp, String dir) {
        switch (dir.toLowerCase()) {
            case "n": case "north": return new BlockPos(0, 0, -1);
            case "s": case "south": return new BlockPos(0, 0, 1);
            case "e": case "east": return new BlockPos(1, 0, 0);
            case "w": case "west": return new BlockPos(-1, 0, 0);
            case "u": case "up": return new BlockPos(0, 1, 0);
            case "d": case "down": return new BlockPos(0, -1, 0);
            case "back": return flip(forwardUnit(sp));
            default: return forwardUnit(sp);
        }
    }

    private static BlockPos forwardUnit(ServerPlayer sp) {
        float yaw = sp.getYRot() % 360; if (yaw < 0) yaw += 360;
        if (yaw >= 45 && yaw < 135) return new BlockPos(-1, 0, 0);
        if (yaw >= 135 && yaw < 225) return new BlockPos(0, 0, -1);
        if (yaw >= 225 && yaw < 315) return new BlockPos(1, 0, 0);
        return new BlockPos(0, 0, 1);
    }

    private static BlockPos flip(BlockPos p) { return new BlockPos(-p.getX(), -p.getY(), -p.getZ()); }
}

package com.ultra.megamod.feature.worldedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ultra.megamod.feature.worldedit.WorldEditRegistry;
import com.ultra.megamod.feature.worldedit.region.CuboidRegion;
import com.ultra.megamod.feature.worldedit.region.CylinderRegion;
import com.ultra.megamod.feature.worldedit.region.EllipsoidRegion;
import com.ultra.megamod.feature.worldedit.region.Polygonal2DRegion;
import com.ultra.megamod.feature.worldedit.region.Region;
import com.ultra.megamod.feature.worldedit.region.SphereRegion;
import com.ultra.megamod.feature.worldedit.region.ConvexPolyhedralRegion;
import com.ultra.megamod.feature.worldedit.session.LocalSession;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Selection / wand / region-selector commands.
 * Commands:
 *   /we_wand                 - give a selection wand
 *   /we_pos1 [pos]           - set position 1 (defaults to feet)
 *   /we_pos2 [pos]           - set position 2
 *   /we_hpos1 /we_hpos2      - set to the block you are looking at
 *   /we_sel <type>           - change selector type (cuboid, poly, ellipsoid, sphere, cyl, convex)
 *   /we_size                 - display the size of your current selection
 *   /we_count <block>        - count occurrences of a block in the selection
 *   /we_distr                - distribution of blocks in the selection
 *   /we_chunk                - set selection to your current chunk
 *   /we_expand <n> [dir]
 *   /we_contract <n> [dir]
 *   /we_shift <n> [dir]
 *   /we_outset <n>
 *   /we_inset <n>
 */
public final class SelectionCommands {

    private SelectionCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> disp) {
        disp.register(Commands.literal("we_wand").executes(ctx -> wand(ctx.getSource())));
        disp.register(Commands.literal("we_pos1")
            .executes(ctx -> pos(ctx.getSource(), true, null))
            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(ctx -> pos(ctx.getSource(), true, BlockPosArgument.getBlockPos(ctx, "pos")))));
        disp.register(Commands.literal("we_pos2")
            .executes(ctx -> pos(ctx.getSource(), false, null))
            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(ctx -> pos(ctx.getSource(), false, BlockPosArgument.getBlockPos(ctx, "pos")))));
        disp.register(Commands.literal("we_hpos1").executes(ctx -> hpos(ctx.getSource(), true)));
        disp.register(Commands.literal("we_hpos2").executes(ctx -> hpos(ctx.getSource(), false)));
        disp.register(Commands.literal("we_sel")
            .then(Commands.argument("type", StringArgumentType.word())
                .executes(ctx -> selector(ctx.getSource(), StringArgumentType.getString(ctx, "type")))));
        disp.register(Commands.literal("we_size").executes(ctx -> size(ctx.getSource())));
        disp.register(Commands.literal("we_count")
            .then(Commands.argument("block", StringArgumentType.greedyString())
                .executes(ctx -> count(ctx.getSource(), StringArgumentType.getString(ctx, "block")))));
        disp.register(Commands.literal("we_distr").executes(ctx -> distr(ctx.getSource())));
        disp.register(Commands.literal("we_chunk").executes(ctx -> chunkSel(ctx.getSource())));
        disp.register(Commands.literal("we_expand")
            .then(Commands.argument("n", IntegerArgumentType.integer(0))
                .executes(ctx -> expand(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n"), "up"))
                .then(Commands.argument("dir", StringArgumentType.word())
                    .executes(ctx -> expand(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n"),
                        StringArgumentType.getString(ctx, "dir"))))));
        disp.register(Commands.literal("we_contract")
            .then(Commands.argument("n", IntegerArgumentType.integer(0))
                .executes(ctx -> contract(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n"), "up"))
                .then(Commands.argument("dir", StringArgumentType.word())
                    .executes(ctx -> contract(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n"),
                        StringArgumentType.getString(ctx, "dir"))))));
        disp.register(Commands.literal("we_shift")
            .then(Commands.argument("n", IntegerArgumentType.integer())
                .executes(ctx -> shift(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n"), "forward"))
                .then(Commands.argument("dir", StringArgumentType.word())
                    .executes(ctx -> shift(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n"),
                        StringArgumentType.getString(ctx, "dir"))))));
        disp.register(Commands.literal("we_outset")
            .then(Commands.argument("n", IntegerArgumentType.integer(0))
                .executes(ctx -> outset(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n")))));
        disp.register(Commands.literal("we_inset")
            .then(Commands.argument("n", IntegerArgumentType.integer(0))
                .executes(ctx -> inset(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n")))));
    }

    private static int wand(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.isAdmin(sp)) {
            src.sendFailure(Component.literal("Admin only.")); return 0;
        }
        sp.getInventory().add(new ItemStack(WorldEditRegistry.WAND.get()));
        WECommandUtil.info(sp, "Received WorldEdit wand. Left-click=pos1, right-click=pos2.");
        return 1;
    }

    private static int pos(CommandSourceStack src, boolean first, BlockPos p) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.isAdmin(sp)) { src.sendFailure(Component.literal("Admin only.")); return 0; }
        BlockPos pos = p == null ? sp.blockPosition() : p;
        var ls = WECommandUtil.session(sp);
        if (first) ls.setPos1(pos, sp.level().dimension());
        else ls.setPos2(pos, sp.level().dimension());
        WECommandUtil.info(sp, "pos" + (first ? "1" : "2") + " set to " + fmt(pos));
        return 1;
    }

    private static int hpos(CommandSourceStack src, boolean first) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.isAdmin(sp)) { src.sendFailure(Component.literal("Admin only.")); return 0; }
        var hit = RaycastHelper.block(sp, 300);
        if (hit == null) { WECommandUtil.error(sp, "No block in range."); return 0; }
        return pos(src, first, hit);
    }

    private static int selector(CommandSourceStack src, String type) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.isAdmin(sp)) { src.sendFailure(Component.literal("Admin only.")); return 0; }
        var ls = WECommandUtil.session(sp);
        try {
            ls.setSelectorType(LocalSession.SelectorType.valueOf(type.toUpperCase()));
            WECommandUtil.info(sp, "Selector: " + type);
            return 1;
        } catch (IllegalArgumentException e) {
            WECommandUtil.error(sp, "Unknown selector: " + type + ". Options: cuboid, extend, poly, ellipsoid, sphere, cyl, convex");
            return 0;
        }
    }

    private static int size(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.isAdmin(sp)) { src.sendFailure(Component.literal("Admin only.")); return 0; }
        var ls = WECommandUtil.session(sp);
        Region r = ls.getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        Vec3i d = r.getDimensions();
        WECommandUtil.info(sp, "Selection: " + d.getX() + "x" + d.getY() + "x" + d.getZ() + " (" + r.getVolume() + " blocks)");
        return 1;
    }

    private static int count(CommandSourceStack src, String blockSpec) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.isAdmin(sp)) { src.sendFailure(Component.literal("Admin only.")); return 0; }
        var ls = WECommandUtil.session(sp);
        Region r = ls.getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        var target = com.ultra.megamod.feature.worldedit.util.BlockStateParser.parse(blockSpec);
        if (target == null) { WECommandUtil.error(sp, "Unknown block: " + blockSpec); return 0; }
        int n = 0;
        for (BlockPos p : r) {
            if (sp.level().getBlockState(p).getBlock() == target.getBlock()) n++;
        }
        WECommandUtil.info(sp, "Count of " + blockSpec + ": " + n);
        return n;
    }

    private static int distr(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.isAdmin(sp)) { src.sendFailure(Component.literal("Admin only.")); return 0; }
        var ls = WECommandUtil.session(sp);
        Region r = ls.getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        int total = 0;
        for (BlockPos p : r) {
            String k = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(sp.level().getBlockState(p).getBlock()).toString();
            counts.merge(k, 1, Integer::sum);
            total++;
        }
        java.util.List<java.util.Map.Entry<String, Integer>> list = new java.util.ArrayList<>(counts.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        WECommandUtil.info(sp, "Block distribution (" + total + " blocks):");
        for (int i = 0; i < Math.min(list.size(), 20); i++) {
            var e = list.get(i);
            double pct = 100.0 * e.getValue() / Math.max(1, total);
            sp.sendSystemMessage(Component.literal(String.format("  %.1f%%  %6d  %s", pct, e.getValue(), e.getKey())));
        }
        return 1;
    }

    private static int chunkSel(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.isAdmin(sp)) { src.sendFailure(Component.literal("Admin only.")); return 0; }
        var chunk = sp.chunkPosition();
        int x = chunk.x << 4;
        int z = chunk.z << 4;
        BlockPos a = new BlockPos(x, sp.level().getMinY(), z);
        BlockPos b = new BlockPos(x + 15, sp.level().getMaxY(), z + 15);
        var ls = WECommandUtil.session(sp);
        ls.setPos1(a, sp.level().dimension());
        ls.setPos2(b, sp.level().dimension());
        WECommandUtil.info(sp, "Selected chunk " + chunk.x + ", " + chunk.z);
        return 1;
    }

    private static BlockPos dirToVec(ServerPlayer sp, String dir, int amount) {
        switch (dir.toLowerCase()) {
            case "n": case "north": return new BlockPos(0, 0, -amount);
            case "s": case "south": return new BlockPos(0, 0, amount);
            case "e": case "east": return new BlockPos(amount, 0, 0);
            case "w": case "west": return new BlockPos(-amount, 0, 0);
            case "u": case "up": return new BlockPos(0, amount, 0);
            case "d": case "down": return new BlockPos(0, -amount, 0);
            default: {
                // forward/back — derive from player yaw
                float yaw = sp.getYRot() % 360; if (yaw < 0) yaw += 360;
                int dx = 0, dz = 0;
                if (yaw >= 45 && yaw < 135) dx = -amount;
                else if (yaw >= 135 && yaw < 225) dz = -amount;
                else if (yaw >= 225 && yaw < 315) dx = amount;
                else dz = amount;
                if (dir.equalsIgnoreCase("back")) { dx = -dx; dz = -dz; }
                return new BlockPos(dx, 0, dz);
            }
        }
    }

    private static int expand(CommandSourceStack src, int n, String dir) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.isAdmin(sp)) { src.sendFailure(Component.literal("Admin only.")); return 0; }
        var ls = WECommandUtil.session(sp);
        Region r = ls.getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        if (dir.equalsIgnoreCase("vert") || dir.equalsIgnoreCase("vertical")) {
            r.expand(new BlockPos(0, 256, 0), new BlockPos(0, -256, 0));
        } else {
            r.expand(dirToVec(sp, dir, n));
        }
        if (r instanceof CuboidRegion cr) { ls.setPos1(cr.getPos1()); ls.setPos2(cr.getPos2()); }
        WECommandUtil.info(sp, "Expanded region.");
        return 1;
    }

    private static int contract(CommandSourceStack src, int n, String dir) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.isAdmin(sp)) { src.sendFailure(Component.literal("Admin only.")); return 0; }
        var ls = WECommandUtil.session(sp);
        Region r = ls.getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        r.contract(dirToVec(sp, dir, n));
        if (r instanceof CuboidRegion cr) { ls.setPos1(cr.getPos1()); ls.setPos2(cr.getPos2()); }
        WECommandUtil.info(sp, "Contracted region.");
        return 1;
    }

    private static int shift(CommandSourceStack src, int n, String dir) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.isAdmin(sp)) { src.sendFailure(Component.literal("Admin only.")); return 0; }
        var ls = WECommandUtil.session(sp);
        Region r = ls.getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        r.shift(dirToVec(sp, dir, n));
        if (r instanceof CuboidRegion cr) { ls.setPos1(cr.getPos1()); ls.setPos2(cr.getPos2()); }
        WECommandUtil.info(sp, "Shifted region.");
        return 1;
    }

    private static int outset(CommandSourceStack src, int n) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.isAdmin(sp)) { src.sendFailure(Component.literal("Admin only.")); return 0; }
        var ls = WECommandUtil.session(sp);
        Region r = ls.getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        r.expand(new BlockPos(n, 0, 0), new BlockPos(-n, 0, 0),
                 new BlockPos(0, n, 0), new BlockPos(0, -n, 0),
                 new BlockPos(0, 0, n), new BlockPos(0, 0, -n));
        if (r instanceof CuboidRegion cr) { ls.setPos1(cr.getPos1()); ls.setPos2(cr.getPos2()); }
        WECommandUtil.info(sp, "Outset by " + n);
        return 1;
    }

    private static int inset(CommandSourceStack src, int n) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.isAdmin(sp)) { src.sendFailure(Component.literal("Admin only.")); return 0; }
        var ls = WECommandUtil.session(sp);
        Region r = ls.getSelectionRegion();
        if (r == null) { WECommandUtil.error(sp, "No selection."); return 0; }
        r.contract(new BlockPos(n, 0, 0), new BlockPos(-n, 0, 0),
                   new BlockPos(0, n, 0), new BlockPos(0, -n, 0),
                   new BlockPos(0, 0, n), new BlockPos(0, 0, -n));
        if (r instanceof CuboidRegion cr) { ls.setPos1(cr.getPos1()); ls.setPos2(cr.getPos2()); }
        WECommandUtil.info(sp, "Inset by " + n);
        return 1;
    }

    private static String fmt(BlockPos p) { return p.getX() + ", " + p.getY() + ", " + p.getZ(); }
}

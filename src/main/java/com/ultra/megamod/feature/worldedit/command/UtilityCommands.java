package com.ultra.megamod.feature.worldedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ultra.megamod.feature.worldedit.EditSession;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;
import com.ultra.megamod.feature.worldedit.pattern.PatternParser;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

/** Drain, fixwater, fixlava, snow, thaw, ex, removeabove, etc. */
public final class UtilityCommands {
    private UtilityCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("we_drain").then(Commands.argument("r", IntegerArgumentType.integer(1))
            .executes(ctx -> drain(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "r")))));
        d.register(Commands.literal("we_fixwater").then(Commands.argument("r", IntegerArgumentType.integer(1))
            .executes(ctx -> fixwater(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "r")))));
        d.register(Commands.literal("we_fixlava").then(Commands.argument("r", IntegerArgumentType.integer(1))
            .executes(ctx -> fixlava(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "r")))));
        d.register(Commands.literal("we_snow").then(Commands.argument("r", IntegerArgumentType.integer(1))
            .executes(ctx -> snow(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "r")))));
        d.register(Commands.literal("we_thaw").then(Commands.argument("r", IntegerArgumentType.integer(1))
            .executes(ctx -> thaw(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "r")))));
        d.register(Commands.literal("we_ex").then(Commands.argument("r", IntegerArgumentType.integer(1))
            .executes(ctx -> extinguish(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "r")))));
        d.register(Commands.literal("we_removeabove")
            .executes(ctx -> removeAbove(ctx.getSource(), 1, 256))
            .then(Commands.argument("size", IntegerArgumentType.integer(1))
                .executes(ctx -> removeAbove(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "size"), 256))
                .then(Commands.argument("h", IntegerArgumentType.integer(1))
                    .executes(ctx -> removeAbove(ctx.getSource(),
                        IntegerArgumentType.getInteger(ctx, "size"),
                        IntegerArgumentType.getInteger(ctx, "h"))))));
        d.register(Commands.literal("we_removebelow")
            .executes(ctx -> removeBelow(ctx.getSource(), 1, 256))
            .then(Commands.argument("size", IntegerArgumentType.integer(1))
                .executes(ctx -> removeBelow(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "size"), 256))));
        d.register(Commands.literal("we_removenear")
            .then(Commands.argument("block", StringArgumentType.word())
                .then(Commands.argument("r", IntegerArgumentType.integer(1))
                    .executes(ctx -> removeNear(ctx.getSource(),
                        StringArgumentType.getString(ctx, "block"),
                        IntegerArgumentType.getInteger(ctx, "r"))))));
        d.register(Commands.literal("we_replacenear")
            .then(Commands.argument("r", IntegerArgumentType.integer(1))
                .then(Commands.argument("from", StringArgumentType.word())
                    .then(Commands.argument("to", StringArgumentType.word())
                        .executes(ctx -> replaceNear(ctx.getSource(),
                            IntegerArgumentType.getInteger(ctx, "r"),
                            StringArgumentType.getString(ctx, "from"),
                            StringArgumentType.getString(ctx, "to")))))));
        d.register(Commands.literal("we_butcher")
            .executes(ctx -> butcher(ctx.getSource(), 32))
            .then(Commands.argument("r", IntegerArgumentType.integer(1))
                .executes(ctx -> butcher(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "r")))));
        d.register(Commands.literal("we_remove")
            .then(Commands.argument("type", StringArgumentType.word())
                .then(Commands.argument("r", IntegerArgumentType.integer(1))
                    .executes(ctx -> removeEntities(ctx.getSource(),
                        StringArgumentType.getString(ctx, "type"),
                        IntegerArgumentType.getInteger(ctx, "r"))))));
        d.register(Commands.literal("we_help").executes(ctx -> help(ctx.getSource())));
    }

    private static int drain(CommandSourceStack src, int r) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        EditSession es = WECommandUtil.newEdit(sp);
        es.drainPool(sp.blockPosition(), r);
        WECommandUtil.finish(sp, es, "drain r=" + r);
        return 1;
    }
    private static int fixwater(CommandSourceStack src, int r) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        EditSession es = WECommandUtil.newEdit(sp);
        es.fixWater(sp.blockPosition(), r);
        WECommandUtil.finish(sp, es, "fixwater r=" + r);
        return 1;
    }
    private static int fixlava(CommandSourceStack src, int r) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        EditSession es = WECommandUtil.newEdit(sp);
        es.fixLava(sp.blockPosition(), r);
        WECommandUtil.finish(sp, es, "fixlava r=" + r);
        return 1;
    }

    private static int snow(CommandSourceStack src, int r) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        EditSession es = WECommandUtil.newEdit(sp);
        BlockPos o = sp.blockPosition();
        var snow = Blocks.SNOW.defaultBlockState();
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
            if (x * x + z * z > r * r) continue;
            for (int y = sp.level().getMaxY() - 1; y > sp.level().getMinY(); y--) {
                BlockPos p = new BlockPos(o.getX() + x, y, o.getZ() + z);
                var st = sp.level().getBlockState(p);
                if (!st.isAir()) {
                    if (sp.level().getBlockState(p.above()).isAir()) es.setBlock(p.above(), snow);
                    break;
                }
            }
        }
        WECommandUtil.finish(sp, es, "snow r=" + r);
        return 1;
    }

    private static int thaw(CommandSourceStack src, int r) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        EditSession es = WECommandUtil.newEdit(sp);
        BlockPos o = sp.blockPosition();
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++)
            for (int y = -r; y <= r; y++) {
                BlockPos p = o.offset(x, y, z);
                var st = sp.level().getBlockState(p);
                if (st.is(Blocks.SNOW) || st.is(Blocks.SNOW_BLOCK) || st.is(Blocks.ICE))
                    es.setBlock(p, Blocks.AIR.defaultBlockState());
            }
        WECommandUtil.finish(sp, es, "thaw r=" + r);
        return 1;
    }

    private static int extinguish(CommandSourceStack src, int r) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        EditSession es = WECommandUtil.newEdit(sp);
        BlockPos o = sp.blockPosition();
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++)
            for (int y = -r; y <= r; y++) {
                BlockPos p = o.offset(x, y, z);
                if (sp.level().getBlockState(p).is(Blocks.FIRE))
                    es.setBlock(p, Blocks.AIR.defaultBlockState());
            }
        WECommandUtil.finish(sp, es, "extinguish r=" + r);
        return 1;
    }

    private static int removeAbove(CommandSourceStack src, int size, int h) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        EditSession es = WECommandUtil.newEdit(sp);
        es.removeAbove(sp.blockPosition(), size, h);
        WECommandUtil.finish(sp, es, "removeabove");
        return 1;
    }

    private static int removeBelow(CommandSourceStack src, int size, int h) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        EditSession es = WECommandUtil.newEdit(sp);
        es.removeBelow(sp.blockPosition(), size, h);
        WECommandUtil.finish(sp, es, "removebelow");
        return 1;
    }

    private static int removeNear(CommandSourceStack src, String block, int r) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var target = com.ultra.megamod.feature.worldedit.util.BlockStateParser.parse(block);
        if (target == null) { WECommandUtil.error(sp, "Bad block."); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        BlockPos o = sp.blockPosition();
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) for (int y = -r; y <= r; y++) {
            BlockPos p = o.offset(x, y, z);
            if (sp.level().getBlockState(p).getBlock() == target.getBlock())
                es.setBlock(p, Blocks.AIR.defaultBlockState());
        }
        WECommandUtil.finish(sp, es, "removenear " + block);
        return 1;
    }

    private static int replaceNear(CommandSourceStack src, int r, String from, String to) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var target = com.ultra.megamod.feature.worldedit.util.BlockStateParser.parse(from);
        Pattern p = PatternParser.parse(to);
        if (target == null || p == null) { WECommandUtil.error(sp, "Bad args."); return 0; }
        EditSession es = WECommandUtil.newEdit(sp);
        BlockPos o = sp.blockPosition();
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) for (int y = -r; y <= r; y++) {
            BlockPos bp = o.offset(x, y, z);
            if (sp.level().getBlockState(bp).getBlock() == target.getBlock())
                es.setBlock(bp, p);
        }
        WECommandUtil.finish(sp, es, "replacenear " + from + "->" + to);
        return 1;
    }

    private static int butcher(CommandSourceStack src, int r) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        BlockPos o = sp.blockPosition();
        var aabb = net.minecraft.world.phys.AABB.of(new net.minecraft.world.level.levelgen.structure.BoundingBox(
            o.getX() - r, o.getY() - r, o.getZ() - r, o.getX() + r, o.getY() + r, o.getZ() + r));
        int count = 0;
        for (var e : sp.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, aabb)) {
            e.discard();
            count++;
        }
        WECommandUtil.info(sp, "Butchered " + count + " mob(s).");
        return 1;
    }

    private static int removeEntities(CommandSourceStack src, String type, int r) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        BlockPos o = sp.blockPosition();
        var aabb = net.minecraft.world.phys.AABB.of(new net.minecraft.world.level.levelgen.structure.BoundingBox(
            o.getX() - r, o.getY() - r, o.getZ() - r, o.getX() + r, o.getY() + r, o.getZ() + r));
        int count = 0;
        for (var e : sp.level().getEntities(sp, aabb)) {
            boolean match = switch (type.toLowerCase()) {
                case "items", "item" -> e instanceof net.minecraft.world.entity.item.ItemEntity;
                case "arrows", "arrow" -> e instanceof net.minecraft.world.entity.projectile.arrow.AbstractArrow;
                case "experience", "xp" -> e instanceof net.minecraft.world.entity.ExperienceOrb;
                case "paintings", "paint" -> e instanceof net.minecraft.world.entity.decoration.painting.Painting;
                case "itemframes" -> e instanceof net.minecraft.world.entity.decoration.ItemFrame;
                case "boats" -> e instanceof net.minecraft.world.entity.vehicle.boat.Boat;
                case "minecarts" -> {
                    String cn = e.getClass().getName();
                    yield cn.toLowerCase().contains("minecart");
                }
                default -> false;
            };
            if (match) { e.discard(); count++; }
        }
        WECommandUtil.info(sp, "Removed " + count + " " + type);
        return 1;
    }

    private static int help(CommandSourceStack src) {
        src.sendSuccess(() -> net.minecraft.network.chat.Component.literal(
            "Use the Computer -> Admin -> World Edit tab for the full command wiki."), false);
        return 1;
    }
}

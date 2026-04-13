package com.ultra.megamod.feature.worldedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.ultra.megamod.feature.worldedit.WorldEditRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** /we_tool subcommands — hand out the special tools. */
public final class ToolCommands {
    private ToolCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("we_tool")
            .then(Commands.literal("wand").executes(ctx -> give(ctx.getSource(), WorldEditRegistry.WAND.get().getDefaultInstance())))
            .then(Commands.literal("farwand").executes(ctx -> give(ctx.getSource(), WorldEditRegistry.FAR_WAND.get().getDefaultInstance())))
            .then(Commands.literal("brush").executes(ctx -> give(ctx.getSource(), WorldEditRegistry.BRUSH.get().getDefaultInstance())))
            .then(Commands.literal("info").executes(ctx -> give(ctx.getSource(), WorldEditRegistry.INFO_TOOL.get().getDefaultInstance())))
            .then(Commands.literal("tree").executes(ctx -> give(ctx.getSource(), WorldEditRegistry.TREE_PLANTER.get().getDefaultInstance())))
            .then(Commands.literal("superpickaxe").executes(ctx -> give(ctx.getSource(), WorldEditRegistry.SUPER_PICKAXE.get().getDefaultInstance())))
        );
        // /we_super on/off toggles the super pickaxe flag too
        d.register(Commands.literal("we_super").executes(ctx -> toggleSuper(ctx.getSource())));
    }

    private static int give(CommandSourceStack src, ItemStack stack) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        sp.getInventory().add(stack);
        WECommandUtil.info(sp, "Received " + stack.getHoverName().getString());
        return 1;
    }

    private static int toggleSuper(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var ls = WECommandUtil.session(sp);
        ls.setSuperPickaxe(!ls.isSuperPickaxe());
        WECommandUtil.info(sp, "Super pickaxe: " + (ls.isSuperPickaxe() ? "on" : "off"));
        return 1;
    }
}

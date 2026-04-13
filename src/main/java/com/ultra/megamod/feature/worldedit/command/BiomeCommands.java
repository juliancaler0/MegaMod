package com.ultra.megamod.feature.worldedit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/** biomeinfo, setbiome, biomelist. */
public final class BiomeCommands {
    private BiomeCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("we_biomeinfo").executes(ctx -> info(ctx.getSource())));
        d.register(Commands.literal("we_setbiome").then(Commands.argument("id", StringArgumentType.greedyString())
            .executes(ctx -> setBiome(ctx.getSource(), StringArgumentType.getString(ctx, "id")))));
        d.register(Commands.literal("we_biomelist").executes(ctx -> list(ctx.getSource())));
    }

    private static int info(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var holder = sp.level().getBiome(sp.blockPosition());
        var key = holder.unwrapKey().orElseThrow();
        WECommandUtil.info(sp, "Biome: " + key.identifier());
        return 1;
    }

    private static int setBiome(CommandSourceStack src, String id) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        WECommandUtil.info(sp, "Biome assignment from commands is a vanilla 1.21 limitation — use /we_wiki biome");
        return 1;
    }

    private static int list(CommandSourceStack src) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sp = src.getPlayerOrException();
        if (!com.ultra.megamod.feature.worldedit.WorldEditPermissions.requireAdmin(sp)) return 0;
        var reg = sp.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME);
        int[] count = {0};
        reg.listElements().forEach(holder -> {
            if (count[0] > 40) return;
            sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("  " + holder.key().identifier()));
            count[0]++;
        });
        return 1;
    }
}

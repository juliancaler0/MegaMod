package com.ultra.megamod.lib.spellengine.internals;


import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class SpellEngineCommands {
    public static void register(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        var registryAccess = event.getBuildContext();
        dispatcher.register(Commands.literal("spell_cooldown")
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
                .then(Commands.literal("reset").then(
                        Commands.argument("players", EntityArgument.player())
                                .then(Commands.argument("spell", ResourceArgument.resource(registryAccess, SpellRegistry.KEY))
                                        .executes(context -> {
                                            var players = EntityArgument.getPlayers(context, "players");
                                            var spell = ResourceArgument.getResource(context, "spell", SpellRegistry.KEY);
                                            return executeResetCooldown(players, spell);
                                        })
                                )
                ))
                .then(Commands.literal("clear").then(
                        Commands.argument("players", EntityArgument.players())
                                .executes(context -> {
                                    var players = EntityArgument.getPlayers(context, "players");
                                    return executeResetCooldown(players, null);
                                })
                ))
        );
    }

    private static int executeResetCooldown(Collection<ServerPlayer> players, @Nullable Holder<Spell> spell) {
        for (var player: players) {
            Identifier spellId = null;
            if (spell != null) {
                spellId = spell.unwrapKey().get().identifier();
            }
            ((SpellCasterEntity) player).getCooldownManager().reset(spellId);
        }
        return 0;
    }
}

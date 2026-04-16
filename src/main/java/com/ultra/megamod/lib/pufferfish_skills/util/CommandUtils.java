package com.ultra.megamod.lib.pufferfish_skills.util;

import com.google.common.collect.ObjectArrays;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;

import java.util.Collection;
import java.util.Locale;

public class CommandUtils {

	public static void sendSuccess(CommandContext<CommandSourceStack> context, ServerPlayer player, String command, Object... args) {
		context.getSource().sendFeedback(() -> SkillsMod.createTranslatable(
				"command", command + ".success", ObjectArrays.concat(args, player.getDisplayName())
		), true);
	}

	public static void sendSuccess(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, String command, Object... args) {
		if (players.size() == 1) {
			context.getSource().sendFeedback(() -> SkillsMod.createTranslatable(
					"command", command + ".success.single", ObjectArrays.concat(args, players.iterator().next().getDisplayName())
			), true);
		} else {
			context.getSource().sendFeedback(() -> SkillsMod.createTranslatable(
					"command", command + ".success.multiple", ObjectArrays.concat(args, players.size())
			), true);
		}
	}

	public static void suggestIdentifiers(Iterable<Identifier> ids, SuggestionsBuilder builder) {
		var remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
		var hasColon = remaining.indexOf(':') != -1;
		for (var id : ids) {
			if (hasColon) {
				if (SharedSuggestionProvider.shouldSuggest(remaining, id.toString())) {
					builder.suggest(id.toString());
				}
			} else if (SharedSuggestionProvider.shouldSuggest(remaining, id.getNamespace())) {
				builder.suggest(id.toString());
			} else if (id.getNamespace().equals(SkillsAPI.MOD_ID)) {
				if (SharedSuggestionProvider.shouldSuggest(remaining, id.getPath())) {
					builder.suggest(id.toString());
				}
			}
		}
	}
}

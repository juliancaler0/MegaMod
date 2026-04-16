package com.ultra.megamod.lib.pufferfish_skills.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.Category;
import com.ultra.megamod.lib.pufferfish_skills.api.Skill;

import java.util.concurrent.CompletableFuture;

public class SkillArgumentType implements ArgumentType<String> {

	private static final DynamicCommandExceptionType NO_SUCH_SKILL = new DynamicCommandExceptionType(
			id -> SkillsMod.createTranslatable("command", "no_such_skill", id)
	);

	private final String categoryArgumentName;

	private SkillArgumentType(String categoryArgumentName) {
		this.categoryArgumentName = categoryArgumentName;
	}

	public static SkillArgumentType skillFromCategory(String categoryArgumentName) {
		return new SkillArgumentType(categoryArgumentName);
	}

	public static Skill getSkillFromCategory(CommandContext<CommandSourceStack> context, String name, Category category) throws CommandSyntaxException {
		var skillId = context.getArgument(name, String.class);
		return category.getSkill(skillId).orElseThrow(() -> NO_SUCH_SKILL.create(skillId));
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		return reader.readUnquotedString();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		var source = context.getSource();
		if (source instanceof CommandSourceStack) {
			var categoryId = SkillsMod.convertIdentifier(context.getArgument(categoryArgumentName, Identifier.class));
			SkillsMod.getInstance()
					.getSkills(categoryId)
					.ifPresent(skills -> SharedSuggestionProvider.suggest(skills, builder));
			return builder.buildFuture();
		} else if (source instanceof SharedSuggestionProvider commandSource) {
			return commandSource.customSuggestion(context);
		}
		return Suggestions.empty();
	}

	public static class Serializer implements ArgumentTypeInfo<SkillArgumentType, Serializer.Properties> {

		@Override
		public void serializeToNetwork(Properties properties, FriendlyByteBuf buf) {
			buf.writeUtf(properties.categoryArgumentName);
		}

		@Override
		public Properties deserializeFromNetwork(FriendlyByteBuf buf) {
			return new Properties(buf.readUtf());
		}

		@Override
		public void serializeToJson(Properties properties, JsonObject jsonObject) {
			jsonObject.addProperty("category_argument_name", properties.categoryArgumentName);
		}

		@Override
		public Properties unpack(SkillArgumentType skillArgumentType) {
			return new Properties(skillArgumentType.categoryArgumentName);
		}

		public final class Properties implements Template<SkillArgumentType> {
			private final String categoryArgumentName;

			public Properties(String categoryArgumentName) {
				this.categoryArgumentName = categoryArgumentName;
			}

			@Override
			public SkillArgumentType instantiate(CommandBuildContext commandRegistryAccess) {
				return new SkillArgumentType(this.categoryArgumentName);
			}

			@Override
			public ArgumentTypeInfo<SkillArgumentType, ?> type() {
				return Serializer.this;
			}
		}
	}
}

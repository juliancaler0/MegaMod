package com.ultra.megamod.lib.pufferfish_skills.client.network.packets.in;

import net.minecraft.advancements.AdvancementType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ComponentSerialization;
import com.ultra.megamod.lib.pufferfish_skills.api.Skill;
import com.ultra.megamod.lib.pufferfish_skills.client.config.ClientBackgroundConfig;
import com.ultra.megamod.lib.pufferfish_skills.client.config.ClientCategoryConfig;
import com.ultra.megamod.lib.pufferfish_skills.client.config.ClientFrameConfig;
import com.ultra.megamod.lib.pufferfish_skills.client.config.ClientIconConfig;
import com.ultra.megamod.lib.pufferfish_skills.client.config.colors.ClientColorConfig;
import com.ultra.megamod.lib.pufferfish_skills.client.config.colors.ClientColorsConfig;
import com.ultra.megamod.lib.pufferfish_skills.client.config.colors.ClientConnectionsColorsConfig;
import com.ultra.megamod.lib.pufferfish_skills.client.config.colors.ClientFillStrokeColorsConfig;
import com.ultra.megamod.lib.pufferfish_skills.client.config.skill.ClientSkillConfig;
import com.ultra.megamod.lib.pufferfish_skills.client.config.skill.ClientSkillConnectionConfig;
import com.ultra.megamod.lib.pufferfish_skills.client.config.skill.ClientSkillDefinitionConfig;
import com.ultra.megamod.lib.pufferfish_skills.client.data.ClientCategoryData;
import com.ultra.megamod.lib.pufferfish_skills.common.BackgroundPosition;
import com.ultra.megamod.lib.pufferfish_skills.common.FrameType;
import com.ultra.megamod.lib.pufferfish_skills.common.IconType;
import com.ultra.megamod.lib.pufferfish_skills.network.InPacket;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShowCategoryInPacket implements InPacket {
	private final ClientCategoryData category;

	private ShowCategoryInPacket(ClientCategoryData category) {
		this.category = category;
	}

	public static ShowCategoryInPacket read(RegistryFriendlyByteBuf buf) {
		var category = readCategory(buf);

		return new ShowCategoryInPacket(category);
	}

	public static ClientCategoryData readCategory(RegistryFriendlyByteBuf buf) {
		var id = Identifier.parse(buf.readUtf());

		var title = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
		var description = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
		var extraDescription = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
		var icon = readSkillIcon(buf);
		var background = readBackground(buf);
		var colors = readColors(buf);
		var exclusiveRoot = buf.readBoolean();
		var spentPointsLimit = buf.readInt();

		var defCount = buf.readVarInt();
		var definitions = new HashMap<String, ClientSkillDefinitionConfig>();
		for (int i = 0; i < defCount; i++) {
			var def = readDefinition(buf);
			definitions.put(def.id(), def);
		}

		var skillCount = buf.readVarInt();
		var skills = new HashMap<String, ClientSkillConfig>();
		for (int i = 0; i < skillCount; i++) {
			var skill = readSkill(buf);
			skills.put(skill.id(), skill);
		}

		var normalConnCount = buf.readVarInt();
		var normalConnections = new ArrayList<ClientSkillConnectionConfig>();
		for (int i = 0; i < normalConnCount; i++) {
			normalConnections.add(readSkillConnection(buf));
		}

		var exclConnCount = buf.readVarInt();
		var exclusiveConnections = new ArrayList<ClientSkillConnectionConfig>();
		for (int i = 0; i < exclConnCount; i++) {
			exclusiveConnections.add(readSkillConnection(buf));
		}

		var statesCount = buf.readVarInt();
		var skillsStates = new HashMap<String, Skill.State>();
		for (int i = 0; i < statesCount; i++) {
			skillsStates.put(buf.readUtf(), buf.readEnum(Skill.State.class));
		}

		var spentPoints = buf.readInt();
		var earnedPoints = buf.readInt();

		var levelLimit = Integer.MAX_VALUE;
		var currentLevel = Integer.MIN_VALUE;
		var currentExperience = Integer.MIN_VALUE;
		var requiredExperience = Integer.MIN_VALUE;
		if (buf.readBoolean()) {
			levelLimit = buf.readInt();
			currentLevel = buf.readInt();
			currentExperience = buf.readInt();
			requiredExperience = buf.readInt();
		}

		var category = new ClientCategoryConfig(
				id,
				title,
				description,
				extraDescription,
				icon,
				background,
				colors,
				exclusiveRoot,
				spentPointsLimit,
				levelLimit,
				definitions,
				skills,
				normalConnections,
				exclusiveConnections
		);

		return new ClientCategoryData(
				category,
				skillsStates,
				spentPoints,
				earnedPoints,
				currentLevel,
				currentExperience,
				requiredExperience
		);
	}

	public static ClientSkillDefinitionConfig readDefinition(RegistryFriendlyByteBuf buf) {
		var id = buf.readUtf();
		var title = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
		var description = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
		var extraDescription = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
		var frame = readFrameIcon(buf);
		var icon = readSkillIcon(buf);
		var size = buf.readFloat();
		var cost = buf.readInt();
		var requiredSkills = buf.readInt();
		var requiredPoints = buf.readInt();
		var requiredSpentPoints = buf.readInt();
		var requiredExclusions = buf.readInt();

		return new ClientSkillDefinitionConfig(
				id,
				title,
				description,
				extraDescription,
				icon,
				frame,
				size,
				cost,
				requiredSkills,
				requiredPoints,
				requiredSpentPoints,
				requiredExclusions
		);
	}

	public static ClientIconConfig readSkillIcon(RegistryFriendlyByteBuf buf) {
		var type = buf.readEnum(IconType.class);
		return switch (type) {
			case EFFECT -> {
				var effect = Identifier.parse(buf.readUtf());
				yield new ClientIconConfig.EffectIconConfig(BuiltInRegistries.MOB_EFFECT.getValue(effect));
			}
			case ITEM -> {
				var itemStack = ItemStack.STREAM_CODEC.decode(buf);
				yield new ClientIconConfig.ItemIconConfig(itemStack);
			}
			case TEXTURE -> {
				var texture = Identifier.parse(buf.readUtf());
				yield new ClientIconConfig.TextureIconConfig(texture);
			}
		};
	}

	public static ClientFrameConfig readFrameIcon(FriendlyByteBuf buf) {
		var type = buf.readEnum(FrameType.class);
		return switch (type) {
			case ADVANCEMENT -> {
				var advancementFrame = buf.readEnum(AdvancementType.class);
				yield new ClientFrameConfig.AdvancementFrameConfig(advancementFrame);
			}
			case TEXTURE -> {
				var lockedTexture = buf.readBoolean() ? Optional.of(Identifier.parse(buf.readUtf())) : Optional.<net.minecraft.resources.Identifier>empty();
				var availableTexture = Identifier.parse(buf.readUtf());
				var affordableTexture = buf.readBoolean() ? Optional.of(Identifier.parse(buf.readUtf())) : Optional.<net.minecraft.resources.Identifier>empty();
				var unlockedTexture = Identifier.parse(buf.readUtf());
				var excludedTexture = buf.readBoolean() ? Optional.of(Identifier.parse(buf.readUtf())) : Optional.<net.minecraft.resources.Identifier>empty();
				yield new ClientFrameConfig.TextureFrameConfig(
						lockedTexture,
						availableTexture,
						affordableTexture,
						unlockedTexture,
						excludedTexture
				);
			}
		};
	}

	public static ClientBackgroundConfig readBackground(FriendlyByteBuf buf) {
		var texture = Identifier.parse(buf.readUtf());
		var width = buf.readInt();
		var height = buf.readInt();
		var position = buf.readEnum(BackgroundPosition.class);

		return ClientBackgroundConfig.create(texture, width, height, position);
	}

	public static ClientColorsConfig readColors(FriendlyByteBuf buf) {
		var connections = readConnectionsColors(buf);
		var points = readFillStrokeColors(buf);

		return new ClientColorsConfig(connections, points);
	}

	public static ClientConnectionsColorsConfig readConnectionsColors(FriendlyByteBuf buf) {
		var locked = readFillStrokeColors(buf);
		var available = readFillStrokeColors(buf);
		var affordable = readFillStrokeColors(buf);
		var unlocked = readFillStrokeColors(buf);
		var excluded = readFillStrokeColors(buf);

		return new ClientConnectionsColorsConfig(locked, available, affordable, unlocked, excluded);
	}

	public static ClientFillStrokeColorsConfig readFillStrokeColors(FriendlyByteBuf buf) {
		var fill = readColor(buf);
		var stroke = readColor(buf);

		return new ClientFillStrokeColorsConfig(fill, stroke);
	}

	public static ClientColorConfig readColor(FriendlyByteBuf buf) {
		var argb = buf.readInt();

		return new ClientColorConfig(argb);
	}

	public static ClientSkillConfig readSkill(FriendlyByteBuf buf) {
		var id = buf.readUtf();
		var x = buf.readInt();
		var y = buf.readInt();
		var definition = buf.readUtf();
		var isRoot = buf.readBoolean();

		return new ClientSkillConfig(id, x, y, definition, isRoot);
	}

	public static ClientSkillConnectionConfig readSkillConnection(FriendlyByteBuf buf) {
		var skillAId = buf.readUtf();
		var skillBId = buf.readUtf();
		var bidirectional = buf.readBoolean();

		return new ClientSkillConnectionConfig(skillAId, skillBId, bidirectional);
	}

	public ClientCategoryData getCategory() {
		return category;
	}
}

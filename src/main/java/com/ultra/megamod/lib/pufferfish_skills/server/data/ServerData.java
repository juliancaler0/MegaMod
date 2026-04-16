package com.ultra.megamod.lib.pufferfish_skills.server.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerData {
	private static ServerData INSTANCE;

	private final Map<UUID, PlayerData> players = new HashMap<>();
	private final Path dataFile;
	private boolean dirty = false;

	private ServerData(Path dataFile) {
		this.dataFile = dataFile;
	}

	private static ServerData read(CompoundTag tag, Path dataFile) {
		var playersData = new ServerData(dataFile);

		var playersNbt = tag.getCompoundOrEmpty("players");
		playersNbt.keySet().forEach(key -> playersData.players.put(
				UUID.fromString(key),
				PlayerData.read(playersNbt.getCompoundOrEmpty(key))
		));

		return playersData;
	}

	private CompoundTag writeNbt(CompoundTag nbt) {
		var playersNbt = new CompoundTag();
		for (var entry : players.entrySet()) {
			playersNbt.put(
					entry.getKey().toString(),
					entry.getValue().writeNbt(new CompoundTag())
			);
		}
		nbt.put("players", playersNbt);

		return nbt;
	}

	public void save() {
		if (!dirty) return;
		try {
			Files.createDirectories(dataFile.getParent());
			NbtIo.writeCompressed(writeNbt(new CompoundTag()), dataFile);
			dirty = false;
		} catch (IOException e) {
			throw new RuntimeException("Failed to save puffish_skills data", e);
		}
	}

	public static ServerData getOrCreate(MinecraftServer server) {
		if (INSTANCE != null) return INSTANCE;

		var worldDir = server.getWorldPath(LevelResource.ROOT);
		var dataFile = worldDir.resolve("data").resolve(SkillsAPI.MOD_ID + ".dat");

		if (Files.exists(dataFile)) {
			try {
				var tag = NbtIo.readCompressed(dataFile, NbtAccounter.unlimitedHeap());
				INSTANCE = read(tag, dataFile);
			} catch (IOException e) {
				INSTANCE = new ServerData(dataFile);
			}
		} else {
			INSTANCE = new ServerData(dataFile);
		}

		return INSTANCE;
	}

	public static void reset() {
		if (INSTANCE != null) {
			INSTANCE.save();
		}
		INSTANCE = null;
	}

	public PlayerData getPlayerData(ServerPlayer player) {
		dirty = true;
		return players.computeIfAbsent(player.getUUID(), uuid -> PlayerData.empty());
	}

	public void putPlayerData(ServerPlayer player, PlayerData data) {
		players.put(player.getUUID(), data);
		dirty = true;
	}
}

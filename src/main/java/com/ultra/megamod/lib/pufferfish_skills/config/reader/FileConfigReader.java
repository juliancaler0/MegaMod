package com.ultra.megamod.lib.pufferfish_skills.config.reader;

import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonPath;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.util.PathUtils;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileConfigReader extends ConfigReader {
	private final Path modConfigDir;

	public FileConfigReader(Path modConfigDir) {
		this.modConfigDir = modConfigDir;
	}

	public Result<JsonElement, Problem> readFile(Path file) {
		return JsonElement.parseFile(
				file,
				JsonPath.create(modConfigDir.relativize(file).toString())
		);
	}

	@Override
	public Result<JsonElement, Problem> read(Path path) {
		return readFile(modConfigDir.resolve(path));
	}

	@Override
	public boolean exists(Path path) {
		return Files.exists(modConfigDir.resolve(path));
	}
}

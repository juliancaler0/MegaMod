package com.ultra.megamod.lib.pufferfish_skills.config.reader;

import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonPath;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.util.PathUtils;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;

import java.nio.file.Path;

public class PackConfigReader extends ConfigReader {
	private final ResourceManager resourceManager;
	private final String namespace;

	public PackConfigReader(ResourceManager resourceManager, String namespace) {
		this.resourceManager = resourceManager;
		this.namespace = namespace;
	}

	public Result<JsonElement, Problem> readResource(Identifier id, Resource resource) {
		try (var reader = resource.openAsReader()) {
			return JsonElement.parseReader(reader, JsonPath.create(id.toString()));
		} catch (Exception e) {
			return Result.failure(Problem.message("Failed to read resource `" + id + "`"));
		}
	}

	@Override
	public Result<JsonElement, Problem> read(Path path) {
		var id = Identifier.fromNamespaceAndPath(namespace, PathUtils.pathToString(Path.of(SkillsAPI.MOD_ID).resolve(path)));

		return resourceManager.getResource(id)
				.map(resource -> readResource(id, resource))
				.orElseGet(() -> Result.failure(Problem.message("Resource `" + id + "` does not exist")));
	}

	@Override
	public boolean exists(Path path) {
		var id = Identifier.fromNamespaceAndPath(namespace, PathUtils.pathToString(Path.of(SkillsAPI.MOD_ID).resolve(path)));

		return resourceManager.getResource(id).isPresent();
	}
}

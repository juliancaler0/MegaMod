package com.ultra.megamod.lib.pufferfish_skills.config.skill;

import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.util.DisposeContext;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SkillDefinitionsConfig {
	private final Set<String> valid;
	private final Map<String, SkillDefinitionConfig> definitions;

	private SkillDefinitionsConfig(Set<String> valid, Map<String, SkillDefinitionConfig> definitions) {
		this.valid = valid;
		this.definitions = definitions;
	}

	public static Result<SkillDefinitionsConfig, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<SkillDefinitionsConfig, Problem> parse(JsonObject rootObject, ConfigContext context) {
		return rootObject.getAsMap((id, element) -> SkillDefinitionConfig.parse(id, element, context))
				.mapFailure(problems -> Problem.combine(problems.values()))
				.mapSuccess(map -> new SkillDefinitionsConfig(
						map.keySet(),
						map.entrySet()
								.stream()
								.flatMap(entry -> entry.getValue()
										.map(value -> Map.entry(entry.getKey(), value))
										.stream()
								)
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
				));
	}

	public boolean isValid(String id) {
		return valid.contains(id);
	}

	public boolean isLoaded(String id) {
		return definitions.containsKey(id);
	}

	public Optional<SkillDefinitionConfig> getById(String id) {
		return Optional.ofNullable(definitions.get(id));
	}

	public Collection<SkillDefinitionConfig> getAll() {
		return definitions.values();
	}

	public void dispose(DisposeContext context) {
		for (var definition : definitions.values()) {
			definition.dispose(context);
		}
	}
}

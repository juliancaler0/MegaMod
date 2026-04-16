package com.ultra.megamod.lib.pufferfish_skills.api.json;

import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.impl.json.JsonPathImpl;

import java.util.List;
import java.util.Optional;

public interface JsonPath {
	static JsonPath create(String name) {
		return new JsonPathImpl(List.of("`" + name + "`"));
	}

	JsonPath getArray(long index);

	JsonPath getObject(String key);

	Optional<JsonPath> getParent();

	Problem createProblem(String message);

	@Override
	String toString();
}

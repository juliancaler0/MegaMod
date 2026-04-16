package com.ultra.megamod.lib.pufferfish_skills.util;

import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonPath;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.impl.json.JsonPathImpl;

public class JsonPathFailure {
	public static Problem expectedToExist(JsonPath path) {
		return ((JsonPathImpl) path).expectedToExist();
	}

	public static Problem expectedToExistAndBe(JsonPath path, String str) {
		return ((JsonPathImpl) path).expectedToExistAndBe(str);
	}

	public static Problem expectedToBe(JsonPath path, String str) {
		return ((JsonPathImpl) path).expectedToBe(str);
	}
}

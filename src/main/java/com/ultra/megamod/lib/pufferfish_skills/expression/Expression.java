package com.ultra.megamod.lib.pufferfish_skills.expression;

import java.util.Map;

public interface Expression<T> {
	T eval(Map<String, T> variables);
}

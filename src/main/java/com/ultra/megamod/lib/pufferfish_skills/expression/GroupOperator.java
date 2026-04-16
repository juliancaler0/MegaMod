package com.ultra.megamod.lib.pufferfish_skills.expression;

public record GroupOperator(String openToken, String closeToken) {

	public static GroupOperator create(String openToken, String closeToken) {
		return new GroupOperator(openToken, closeToken);
	}

}

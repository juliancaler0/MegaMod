package com.ultra.megamod.lib.pufferfish_skills.impl.experience.source;

import net.minecraft.server.MinecraftServer;
import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSourceConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.util.VersionContext;

public record ExperienceSourceConfigContextImpl(
		ConfigContext context,
		Result<JsonElement, Problem> maybeDataElement
) implements ExperienceSourceConfigContext, VersionContext {

	@Override
	public MinecraftServer getServer() {
		return context.getServer();
	}

	@Override
	public void emitWarning(String message) {
		context.emitWarning(message);
	}

	@Override
	public Result<JsonElement, Problem> getData() {
		return maybeDataElement;
	}

	@Override
	public int getVersion() {
		if (context instanceof VersionContext versionContext) {
			return versionContext.getVersion();
		}
		return Integer.MIN_VALUE;
	}
}

package com.ultra.megamod.lib.pufferfish_skills.impl.calculation.operation;

import net.minecraft.server.MinecraftServer;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.config.ConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.util.VersionContext;

public record OperationConfigContextImpl(
		ConfigContext context,
		Result<JsonElement, Problem> maybeDataElement
) implements OperationConfigContext, VersionContext {

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

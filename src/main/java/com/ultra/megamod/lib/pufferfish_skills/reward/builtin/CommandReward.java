package com.ultra.megamod.lib.pufferfish_skills.reward.builtin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.Reward;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.RewardConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.RewardDisposeContext;
import com.ultra.megamod.lib.pufferfish_skills.api.reward.RewardUpdateContext;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.util.LegacyUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("command");

	private final Map<UUID, Integer> counts = new HashMap<>();

	private final String command;
	private final String unlockCommand;
	private final String lockCommand;

	private CommandReward(String command, String unlockCommand, String lockCommand) {
		this.command = command;
		this.unlockCommand = unlockCommand;
		this.lockCommand = lockCommand;
	}

	public static void register() {
		SkillsAPI.registerReward(
				ID,
				CommandReward::parse
		);
	}

	private static Result<CommandReward, Problem> parse(RewardConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(LegacyUtils.wrapNoUnused(CommandReward::parse, context));
	}

	private static Result<CommandReward, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var command = rootObject.get("command")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(jsonElement -> jsonElement.getAsString()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse("");

		var unlockCommand = rootObject.get("unlock_command")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(jsonElement -> jsonElement.getAsString()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse("");

		var lockCommand = rootObject.get("lock_command")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(jsonElement -> jsonElement.getAsString()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse("");

		if (problems.isEmpty()) {
			return Result.success(new CommandReward(
					command,
					unlockCommand,
					lockCommand
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	private void executeCommand(ServerPlayer player, String command) {
		if (command.isBlank()) {
			return;
		}

		var server = SkillsMod.getInstance().getPlayerServer(player);

		server.getCommands().performPrefixedCommand(
				player.createCommandSourceStack()
						.withSuppressedOutput()
						.withPermission(net.minecraft.server.permissions.PermissionSet.ALL_PERMISSIONS),
				command
		);
	}

	@Override
	public void update(RewardUpdateContext context) {
		var player = context.getPlayer();

		if (context.isAction()) {
			executeCommand(player, command);
		}

		counts.compute(player.getUUID(), (uuid, count) -> {
			if (count == null) {
				count = 0;
			}

			while (context.getCount() > count) {
				executeCommand(player, unlockCommand);
				count++;
			}
			while (context.getCount() < count) {
				executeCommand(player, lockCommand);
				count--;
			}

			return count;
		});
	}

	@Override
	public void dispose(RewardDisposeContext context) {
		for (var entry : counts.entrySet()) {
			var player = context.getServer().getPlayerList().getPlayer(entry.getKey());
			if (player == null) {
				continue;
			}
			for (var i = 0; i < entry.getValue(); i++) {
				executeCommand(player, lockCommand);
			}
		}
		counts.clear();
	}
}

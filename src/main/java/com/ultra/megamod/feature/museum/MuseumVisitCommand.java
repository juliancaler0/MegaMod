package com.ultra.megamod.feature.museum;

import com.mojang.brigadier.CommandDispatcher;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.handlers.FriendsHandler;
import com.ultra.megamod.feature.dimensions.DimensionHelper;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dimensions.PocketManager;
import com.ultra.megamod.feature.museum.dimension.MuseumDimensionManager;
import com.ultra.megamod.feature.museum.dimension.MuseumDisplayManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.UUID;

@EventBusSubscriber(modid = MegaMod.MODID)
public class MuseumVisitCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("museum")
            .then(Commands.literal("visit")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> {
                        ServerPlayer visitor = ctx.getSource().getPlayerOrException();
                        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                        return visitMuseum(visitor, target);
                    })
                )
            )
        );
    }

    private static int visitMuseum(ServerPlayer visitor, ServerPlayer target) {
        if (visitor.getUUID().equals(target.getUUID())) {
            visitor.sendSystemMessage(Component.literal("Use the Museum Block to enter your own museum!").withStyle(ChatFormatting.YELLOW));
            return 0;
        }

        // Require friendship (admins bypass)
        if (!AdminSystem.isAdmin(visitor) && !FriendsHandler.areFriends(visitor.getUUID(), target.getUUID())) {
            visitor.sendSystemMessage(Component.literal("You can only visit friends' museums! Add them as a friend first.").withStyle(ChatFormatting.RED));
            return 0;
        }

        ServerLevel overworld = visitor.level().getServer().overworld();
        PocketManager pockets = PocketManager.get(overworld);
        MuseumDimensionManager dimManager = MuseumDimensionManager.get(overworld);

        UUID targetId = target.getUUID();

        if (!dimManager.isMuseumInitialized(targetId)) {
            visitor.sendSystemMessage(Component.literal(target.getGameProfile().name() + " hasn't built their museum yet!").withStyle(ChatFormatting.RED));
            return 0;
        }

        BlockPos origin = pockets.getMuseumPocket(targetId);
        if (origin == null) {
            visitor.sendSystemMessage(Component.literal("Could not find that player's museum!").withStyle(ChatFormatting.RED));
            return 0;
        }

        ServerLevel museumLevel = visitor.level().getServer().getLevel(MegaModDimensions.MUSEUM);
        if (museumLevel == null) {
            visitor.sendSystemMessage(Component.literal("Museum dimension is not available!").withStyle(ChatFormatting.RED));
            return 0;
        }

        // Teleport to target's museum spawn
        BlockPos spawnPos = origin.offset(10, 1, 4);
        DimensionHelper.teleportToDimension(visitor, MegaModDimensions.MUSEUM, spawnPos, 0.0f, 0.0f);

        // Rebuild displays using the TARGET player's data so the visitor sees their collection
        MuseumDisplayManager.rebuildWings(museumLevel, origin, targetId, null);

        visitor.sendSystemMessage(Component.literal("Visiting " + target.getGameProfile().name() + "'s Museum!").withStyle(ChatFormatting.GOLD));
        target.sendSystemMessage(Component.literal(visitor.getGameProfile().name() + " is visiting your museum!").withStyle(ChatFormatting.AQUA));

        return 1;
    }
}

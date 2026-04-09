package com.ultra.megamod.feature.citizen.request.network;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.citizen.request.IRequest;
import com.ultra.megamod.feature.citizen.request.RequestManager;
import com.ultra.megamod.feature.citizen.request.RequestState;
import com.ultra.megamod.feature.citizen.request.StandardToken;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers network payloads for the colony request system.
 * Must be registered on the mod event bus in MegaMod constructor.
 */
public class RequestNetwork {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("megamod").versioned("1.0").optional();

        // Server -> Client: sync request state
        registrar.playToClient(
            RequestPayload.TYPE,
            RequestPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                RequestPayload.lastResponse = payload;
            })
        );

        // Client -> Server: player fulfills a request
        registrar.playToServer(
            RequestFulfillPayload.TYPE,
            RequestFulfillPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    handleFulfill(player, payload.requestToken());
                }
            })
        );
    }

    /**
     * Handles a player manually fulfilling a request.
     * Validates the item in the player's inventory matches the request,
     * removes the item, and completes the request.
     */
    private static void handleFulfill(ServerPlayer player, java.util.UUID requestToken) {
        ServerLevel level = (ServerLevel) player.level();
        RequestManager manager = RequestManager.get(level.getServer().overworld());
        IRequest request = manager.getRequest(requestToken);

        if (request == null) {
            player.sendSystemMessage(Component.literal("Request not found.").withStyle(ChatFormatting.RED));
            return;
        }

        if (request.getState() == RequestState.COMPLETED || request.getState() == RequestState.CANCELLED) {
            player.sendSystemMessage(Component.literal("This request is already " + request.getState().name().toLowerCase() + ".")
                .withStyle(ChatFormatting.YELLOW));
            return;
        }

        // Admin bypass: complete request without requiring the item in inventory
        if (AdminSystem.isAdmin(player)) {
            manager.completeRequest(new StandardToken(requestToken), ItemStack.EMPTY);
            player.sendSystemMessage(Component.literal("[Admin] Request fulfilled for free! ("
                + request.getRequestable().getDescription() + ")").withStyle(ChatFormatting.GREEN));
            return;
        }

        // Search the player's inventory for a matching item
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && request.getRequestable().matches(stack)) {
                int needed = request.getRequestable().getCount();
                int available = stack.getCount();
                int toRemove = Math.min(needed, available);

                ItemStack delivery = stack.copy();
                delivery.setCount(toRemove);
                stack.shrink(toRemove);

                manager.completeRequest(new StandardToken(requestToken), delivery);
                player.sendSystemMessage(Component.literal("Request fulfilled! Delivered " + toRemove + "x "
                    + delivery.getHoverName().getString() + ".").withStyle(ChatFormatting.GREEN));
                return;
            }
        }

        player.sendSystemMessage(Component.literal("You don't have the required item: "
            + request.getRequestable().getDescription()).withStyle(ChatFormatting.RED));
    }
}

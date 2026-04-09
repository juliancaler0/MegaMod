package com.ultra.megamod.feature.citizen.network.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.request.*;
import com.ultra.megamod.feature.citizen.request.types.DeliveryRequest;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.UUID;

/**
 * Server-side handler for PostBox screen actions.
 * Handles: postbox_requests, request_detail, request_action, postbox_fulfill_all
 */
public class PostBoxHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        switch (action) {
            case "postbox_requests" -> {
                handlePostBoxRequests(player, level, eco);
                return true;
            }
            case "request_detail" -> {
                handleRequestDetail(player, jsonData, level, eco);
                return true;
            }
            case "request_action" -> {
                handleRequestAction(player, jsonData, level, eco);
                return true;
            }
            case "postbox_fulfill_all" -> {
                handleFulfillAll(player, level, eco);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * Sends all active requests to the client for the PostBox main screen.
     */
    private static void handlePostBoxRequests(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        RequestManager requestManager = RequestManager.get(level);
        List<IRequest> active = requestManager.getAllActiveRequests();
        boolean isAdmin = AdminSystem.isAdmin(player);

        JsonObject response = new JsonObject();
        response.addProperty("isAdmin", isAdmin);

        JsonArray requestsArray = new JsonArray();
        for (IRequest request : active) {
            JsonObject reqObj = new JsonObject();
            reqObj.addProperty("id", request.getToken().getId().toString());
            reqObj.addProperty("description", request.getRequestable().getDescription());
            reqObj.addProperty("requester", request.getRequester().getRequesterName());
            reqObj.addProperty("type", getRequestType(request));
            reqObj.addProperty("status", request.getState().name().toLowerCase());

            // Check if this request can be fulfilled by the player (player has the items)
            boolean playerResolvable = canPlayerFulfill(player, request);
            reqObj.addProperty("playerResolvable", playerResolvable);

            requestsArray.add(reqObj);
        }
        response.add("requests", requestsArray);

        sendResponse(player, "postbox_requests", response.toString(), eco);
    }

    /**
     * Sends detail data for a specific request.
     */
    private static void handleRequestDetail(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject input = JsonParser.parseString(jsonData).getAsJsonObject();
            String requestIdStr = input.has("requestId") ? input.get("requestId").getAsString() : "";
            UUID requestId = UUID.fromString(requestIdStr);

            RequestManager requestManager = RequestManager.get(level);
            IRequest request = requestManager.getRequest(requestId);

            JsonObject response = new JsonObject();
            if (request == null) {
                response.addProperty("description", "Request not found");
                response.addProperty("state", "cancelled");
                response.addProperty("playerResolvable", false);
            } else {
                response.addProperty("description", request.getRequestable().getDescription());
                response.addProperty("requester", request.getRequester().getRequesterName());
                response.addProperty("building", request.getRequester().getRequesterName());
                response.addProperty("state", request.getState().name().toLowerCase());
                response.addProperty("playerResolvable", canPlayerFulfill(player, request));

                // Add required items list
                JsonArray items = new JsonArray();
                IRequestable requestable = request.getRequestable();
                JsonObject itemObj = new JsonObject();
                itemObj.addProperty("name", requestable.getDescription());
                itemObj.addProperty("count", requestable.getCount());
                items.add(itemObj);
                response.add("items", items);
            }

            sendResponse(player, "request_detail", response.toString(), eco);
        } catch (Exception e) {
            MegaMod.LOGGER.error("Error handling request_detail", e);
            sendResponse(player, "request_detail", "{\"description\":\"Error\",\"state\":\"cancelled\",\"playerResolvable\":false}", eco);
        }
    }

    /**
     * Handles fulfill/cancel actions on a request.
     */
    private static void handleRequestAction(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject input = JsonParser.parseString(jsonData).getAsJsonObject();
            String requestIdStr = input.has("requestId") ? input.get("requestId").getAsString() : "";
            String actionType = input.has("action") ? input.get("action").getAsString() : "";
            UUID requestId = UUID.fromString(requestIdStr);

            RequestManager requestManager = RequestManager.get(level);
            IRequest request = requestManager.getRequest(requestId);

            JsonObject result = new JsonObject();
            if (request == null) {
                result.addProperty("success", false);
                result.addProperty("message", "Request not found.");
            } else if ("cancel".equals(actionType)) {
                requestManager.cancelRequest(request.getToken());
                result.addProperty("success", true);
                result.addProperty("message", "Request cancelled.");
            } else if ("fulfill".equals(actionType)) {
                // Check player inventory for the items
                IRequestable requestable = request.getRequestable();
                boolean fulfilled = tryFulfillFromPlayer(player, requestable);
                if (fulfilled) {
                    requestManager.completeRequest(request.getToken(), ItemStack.EMPTY);
                    result.addProperty("success", true);
                    result.addProperty("message", "Request fulfilled!");
                } else {
                    result.addProperty("success", false);
                    result.addProperty("message", "You don't have the required items.");
                }
            } else {
                result.addProperty("success", false);
                result.addProperty("message", "Unknown action.");
            }

            sendResponse(player, "request_action_result", result.toString(), eco);
        } catch (Exception e) {
            MegaMod.LOGGER.error("Error handling request_action", e);
            sendResponse(player, "request_action_result", "{\"success\":false,\"message\":\"Server error.\"}", eco);
        }
    }

    /**
     * Admin: fulfills all active requests instantly.
     */
    private static void handleFulfillAll(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        if (!AdminSystem.isAdmin(player)) {
            sendResponse(player, "postbox_result", "{\"success\":false,\"message\":\"Admin only.\"}", eco);
            return;
        }

        RequestManager requestManager = RequestManager.get(level);
        List<IRequest> active = requestManager.getAllActiveRequests();
        int fulfilled = 0;
        for (IRequest request : active) {
            requestManager.completeRequest(request.getToken(), ItemStack.EMPTY);
            fulfilled++;
        }

        JsonObject result = new JsonObject();
        result.addProperty("success", true);
        result.addProperty("message", "Fulfilled " + fulfilled + " requests.");
        sendResponse(player, "postbox_result", result.toString(), eco);
    }

    // ---- Helpers ----

    /**
     * Checks if the player has the items needed to fulfill the request.
     */
    private static boolean canPlayerFulfill(ServerPlayer player, IRequest request) {
        if (request.getState() == RequestState.COMPLETED || request.getState() == RequestState.CANCELLED) {
            return false;
        }
        IRequestable requestable = request.getRequestable();
        int neededCount = requestable.getCount();
        int foundCount = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && requestable.matchesItem(stack)) {
                foundCount += stack.getCount();
                if (foundCount >= neededCount) return true;
            }
        }
        return false;
    }

    /**
     * Tries to take items from the player's inventory to fulfill a request.
     */
    private static boolean tryFulfillFromPlayer(ServerPlayer player, IRequestable requestable) {
        int neededCount = requestable.getCount();
        int foundCount = 0;

        // First pass: count available items
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && requestable.matchesItem(stack)) {
                foundCount += stack.getCount();
            }
        }

        if (foundCount < neededCount) return false;

        // Second pass: consume items
        int remaining = neededCount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && requestable.matchesItem(stack)) {
                int toTake = Math.min(stack.getCount(), remaining);
                stack.shrink(toTake);
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
                remaining -= toTake;
            }
        }
        return true;
    }

    private static String getRequestType(IRequest request) {
        IRequestable requestable = request.getRequestable();
        String className = requestable.getClass().getSimpleName().toLowerCase();
        if (className.contains("delivery")) return "Delivery";
        if (className.contains("crafting")) return "Crafting";
        if (className.contains("food")) return "Food";
        if (className.contains("tool")) return "Tool";
        if (className.contains("pickup")) return "Pickup";
        return "Delivery";
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        try {
            int wallet = eco.getWallet(player.getUUID());
            int bank = eco.getBank(player.getUUID());
            PacketDistributor.sendToPlayer(player,
                    (CustomPacketPayload) new ComputerDataPayload(type, json, wallet, bank),
                    (CustomPacketPayload[]) new CustomPacketPayload[0]);
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to send postbox response", e);
            PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, 0, 0));
        }
    }
}

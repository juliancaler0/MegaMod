package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public class InventoryViewerHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        // Only handle inventory viewer actions — don't intercept other actions
        if (!action.startsWith("inv_view")) {
            return false;
        }
        if (!AdminSystem.isAdmin(player)) {
            return false;
        }

        switch (action) {
            case "inv_view_request": {
                try {
                    UUID targetId = UUID.fromString(jsonData.trim());
                    ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetId);
                    if (target == null) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Player not online\"}", eco);
                        return true;
                    }
                    String json = serializeInventory(target);
                    sendResponse(player, "inv_view_data", json, eco);
                } catch (Exception e) {
                    sendResponse(player, "inv_view_result", "{\"msg\":\"Error: " + escapeJson(e.getMessage()) + "\"}", eco);
                }
                return true;
            }
            case "inv_view_delete": {
                try {
                    String[] parts = jsonData.split(":", 2);
                    UUID targetId = UUID.fromString(parts[0].trim());
                    int slot = Integer.parseInt(parts[1].trim());
                    ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetId);
                    if (target == null) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Player not online\"}", eco);
                        return true;
                    }
                    if (slot < 0 || slot >= target.getInventory().getContainerSize()) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Invalid slot index\"}", eco);
                        return true;
                    }
                    ItemStack removed = target.getInventory().getItem(slot);
                    String itemName = removed.isEmpty() ? "nothing" : removed.getHoverName().getString();
                    target.getInventory().setItem(slot, ItemStack.EMPTY);
                    target.inventoryMenu.broadcastChanges();
                    sendResponse(player, "inv_view_result", "{\"msg\":\"Deleted " + escapeJson(itemName) + " from slot " + slot + "\"}", eco);
                } catch (Exception e) {
                    sendResponse(player, "inv_view_result", "{\"msg\":\"Error deleting item: " + escapeJson(e.getMessage()) + "\"}", eco);
                }
                return true;
            }
            case "inv_view_copy": {
                try {
                    String[] parts = jsonData.split(":", 2);
                    UUID targetId = UUID.fromString(parts[0].trim());
                    int slot = Integer.parseInt(parts[1].trim());
                    ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetId);
                    if (target == null) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Player not online\"}", eco);
                        return true;
                    }
                    if (slot < 0 || slot >= target.getInventory().getContainerSize()) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Invalid slot index\"}", eco);
                        return true;
                    }
                    ItemStack stack = target.getInventory().getItem(slot);
                    if (stack.isEmpty()) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Slot is empty\"}", eco);
                        return true;
                    }
                    ItemStack copy = stack.copy();
                    boolean added = player.getInventory().add(copy);
                    if (added) {
                        player.inventoryMenu.broadcastChanges();
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Copied " + escapeJson(stack.getHoverName().getString()) + " to your inventory\"}", eco);
                    } else {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Your inventory is full\"}", eco);
                    }
                } catch (Exception e) {
                    sendResponse(player, "inv_view_result", "{\"msg\":\"Error copying item: " + escapeJson(e.getMessage()) + "\"}", eco);
                }
                return true;
            }
            case "inv_view_set_count": {
                try {
                    String[] parts = jsonData.split(":", 3);
                    UUID targetId = UUID.fromString(parts[0].trim());
                    int slot = Integer.parseInt(parts[1].trim());
                    int newCount = Integer.parseInt(parts[2].trim());
                    ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetId);
                    if (target == null) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Player not online\"}", eco);
                        return true;
                    }
                    if (slot < 0 || slot >= target.getInventory().getContainerSize()) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Invalid slot index\"}", eco);
                        return true;
                    }
                    ItemStack stack = target.getInventory().getItem(slot);
                    if (stack.isEmpty()) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Slot is empty\"}", eco);
                        return true;
                    }
                    newCount = Math.max(0, Math.min(newCount, stack.getMaxStackSize()));
                    if (newCount == 0) {
                        target.getInventory().setItem(slot, ItemStack.EMPTY);
                    } else {
                        stack.setCount(newCount);
                    }
                    target.inventoryMenu.broadcastChanges();
                    sendResponse(player, "inv_view_result", "{\"msg\":\"Set count to " + newCount + "\"}", eco);
                } catch (Exception e) {
                    sendResponse(player, "inv_view_result", "{\"msg\":\"Error setting count: " + escapeJson(e.getMessage()) + "\"}", eco);
                }
                return true;
            }
            case "inv_view_clear": {
                try {
                    UUID targetId = UUID.fromString(jsonData.trim());
                    ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetId);
                    if (target == null) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Player not online\"}", eco);
                        return true;
                    }
                    target.getInventory().clearContent();
                    target.inventoryMenu.broadcastChanges();
                    sendResponse(player, "inv_view_result", "{\"msg\":\"Cleared inventory for " + escapeJson(target.getGameProfile().name()) + "\"}", eco);
                } catch (Exception e) {
                    sendResponse(player, "inv_view_result", "{\"msg\":\"Error clearing inventory: " + escapeJson(e.getMessage()) + "\"}", eco);
                }
                return true;
            }
            case "inv_view_copy_all": {
                try {
                    UUID targetId = UUID.fromString(jsonData.trim());
                    ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetId);
                    if (target == null) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Player not online\"}", eco);
                        return true;
                    }
                    int copied = 0;
                    int failed = 0;
                    for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
                        ItemStack stack = target.getInventory().getItem(i);
                        if (!stack.isEmpty()) {
                            ItemStack copy = stack.copy();
                            if (player.getInventory().add(copy)) {
                                copied++;
                            } else {
                                failed++;
                            }
                        }
                    }
                    player.inventoryMenu.broadcastChanges();
                    String msg = "Copied " + copied + " items";
                    if (failed > 0) {
                        msg += " (" + failed + " failed - inventory full)";
                    }
                    sendResponse(player, "inv_view_result", "{\"msg\":\"" + escapeJson(msg) + "\"}", eco);
                } catch (Exception e) {
                    sendResponse(player, "inv_view_result", "{\"msg\":\"Error copying inventory: " + escapeJson(e.getMessage()) + "\"}", eco);
                }
                return true;
            }
            case "inv_view_give": {
                // Format: "targetUUID:itemId:count"
                try {
                    String[] parts = jsonData.split(":", 3);
                    UUID targetId = UUID.fromString(parts[0].trim());
                    String itemId = parts[1].trim();
                    int count = Integer.parseInt(parts[2].trim());
                    ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetId);
                    if (target == null) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Player not online\"}", eco);
                        return true;
                    }
                    net.minecraft.resources.Identifier loc = net.minecraft.resources.Identifier.parse(
                        itemId.contains(":") ? itemId : "minecraft:" + itemId);
                    net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.getValue(loc);
                    if (item == null || item == net.minecraft.world.item.Items.AIR) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Unknown item: " + escapeJson(itemId) + "\"}", eco);
                        return true;
                    }
                    count = Math.max(1, Math.min(count, 64));
                    ItemStack stack = new ItemStack(item, count);
                    boolean added = target.getInventory().add(stack);
                    target.inventoryMenu.broadcastChanges();
                    if (added) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Gave " + count + "x " + escapeJson(itemId) + " to " + escapeJson(target.getGameProfile().name()) + "\"}", eco);
                    } else {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Inventory full\"}", eco);
                    }
                } catch (Exception e) {
                    sendResponse(player, "inv_view_result", "{\"msg\":\"Error: " + escapeJson(e.getMessage()) + "\"}", eco);
                }
                return true;
            }
            case "inv_view_repair": {
                // Format: "targetUUID:slot"
                try {
                    String[] parts = jsonData.split(":", 2);
                    UUID targetId = UUID.fromString(parts[0].trim());
                    int slot = Integer.parseInt(parts[1].trim());
                    ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetId);
                    if (target == null) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Player not online\"}", eco);
                        return true;
                    }
                    ItemStack stack = target.getInventory().getItem(slot);
                    if (stack.isEmpty()) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Slot is empty\"}", eco);
                        return true;
                    }
                    if (!stack.isDamageableItem()) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Item is not damageable\"}", eco);
                        return true;
                    }
                    stack.setDamageValue(0);
                    target.inventoryMenu.broadcastChanges();
                    sendResponse(player, "inv_view_result", "{\"msg\":\"Repaired " + escapeJson(stack.getHoverName().getString()) + "\"}", eco);
                } catch (Exception e) {
                    sendResponse(player, "inv_view_result", "{\"msg\":\"Error: " + escapeJson(e.getMessage()) + "\"}", eco);
                }
                return true;
            }
            case "inv_view_sort": {
                // Format: "targetUUID"
                try {
                    UUID targetId = UUID.fromString(jsonData.trim());
                    ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetId);
                    if (target == null) {
                        sendResponse(player, "inv_view_result", "{\"msg\":\"Player not online\"}", eco);
                        return true;
                    }
                    // Collect all items from inventory slots 9-35 (main inv, not hotbar)
                    java.util.List<ItemStack> items = new java.util.ArrayList<>();
                    for (int i = 9; i < 36; i++) {
                        ItemStack s = target.getInventory().getItem(i);
                        if (!s.isEmpty()) {
                            items.add(s.copy());
                        }
                        target.getInventory().setItem(i, ItemStack.EMPTY);
                    }
                    // Sort by item ID then count
                    items.sort((a, b) -> {
                        String idA = BuiltInRegistries.ITEM.getKey(a.getItem()).toString();
                        String idB = BuiltInRegistries.ITEM.getKey(b.getItem()).toString();
                        int cmp = idA.compareTo(idB);
                        if (cmp != 0) return cmp;
                        return Integer.compare(b.getCount(), a.getCount());
                    });
                    // Place sorted items back
                    int slotIdx = 9;
                    for (ItemStack s : items) {
                        if (slotIdx < 36) {
                            target.getInventory().setItem(slotIdx, s);
                            slotIdx++;
                        }
                    }
                    target.inventoryMenu.broadcastChanges();
                    sendResponse(player, "inv_view_result", "{\"msg\":\"Sorted inventory (" + items.size() + " stacks)\"}", eco);
                } catch (Exception e) {
                    sendResponse(player, "inv_view_result", "{\"msg\":\"Error: " + escapeJson(e.getMessage()) + "\"}", eco);
                }
                return true;
            }
            default:
                return false;
        }
    }

    private static String serializeInventory(ServerPlayer target) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"playerName\":\"").append(escapeJson(target.getGameProfile().name())).append("\"");
        sb.append(",\"slots\":[");

        boolean first = true;
        for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
            ItemStack stack = target.getInventory().getItem(i);

            if (!first) sb.append(",");
            first = false;

            sb.append("{\"slot\":").append(i);

            if (stack.isEmpty()) {
                sb.append(",\"itemId\":\"minecraft:air\"");
                sb.append(",\"name\":\"Empty\"");
                sb.append(",\"count\":0");
                sb.append(",\"durability\":0");
                sb.append(",\"maxDurability\":0");
                sb.append(",\"enchants\":\"\"");
                sb.append(",\"customData\":\"\"");
            } else {
                String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                String displayName = stack.getHoverName().getString();

                sb.append(",\"itemId\":\"").append(escapeJson(itemId)).append("\"");
                sb.append(",\"name\":\"").append(escapeJson(displayName)).append("\"");
                sb.append(",\"count\":").append(stack.getCount());

                // Durability
                if (stack.isDamageableItem()) {
                    sb.append(",\"durability\":").append(stack.getDamageValue());
                    sb.append(",\"maxDurability\":").append(stack.getMaxDamage());
                } else {
                    sb.append(",\"durability\":0");
                    sb.append(",\"maxDurability\":0");
                }

                // Enchantments
                sb.append(",\"enchants\":\"");
                try {
                    ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                    StringBuilder enchSb = new StringBuilder();
                    enchantments.entrySet().forEach(entry -> {
                        if (enchSb.length() > 0) enchSb.append(", ");
                        String enchName = entry.getKey().unwrapKey()
                            .map(key -> key.identifier().getPath())
                            .orElse("unknown");
                        // Capitalize first letter of each word
                        String[] words = enchName.split("_");
                        StringBuilder formatted = new StringBuilder();
                        for (String word : words) {
                            if (formatted.length() > 0) formatted.append(" ");
                            if (!word.isEmpty()) {
                                formatted.append(Character.toUpperCase(word.charAt(0)));
                                if (word.length() > 1) formatted.append(word.substring(1));
                            }
                        }
                        formatted.append(" ").append(toRoman(entry.getIntValue()));
                        enchSb.append(formatted);
                    });
                    sb.append(escapeJson(enchSb.toString()));
                } catch (Exception e) {
                    // Silently skip enchantment errors
                }
                sb.append("\"");

                // Custom data (weapon stats, relic data, etc.)
                sb.append(",\"customData\":\"");
                try {
                    CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    CompoundTag tag = customData.copyTag();
                    if (!tag.isEmpty()) {
                        StringBuilder cdSb = new StringBuilder();
                        for (String key : tag.keySet()) {
                            if (cdSb.length() > 0) cdSb.append(", ");
                            cdSb.append(key).append(": ").append(tagValueToString(tag, key));
                        }
                        sb.append(escapeJson(cdSb.toString()));
                    }
                } catch (Exception e) {
                    // Silently skip custom data errors
                }
                sb.append("\"");
            }

            sb.append("}");
        }

        sb.append("]}");
        return sb.toString();
    }

    private static String tagValueToString(CompoundTag tag, String key) {
        try {
            // Try different types - using 1.21.11 CompoundTag API with Optional returns
            // We use a generic approach: get the tag element and convert to string
            if (tag.contains(key)) {
                // Use the generic get which returns a Tag
                var tagElement = tag.get(key);
                if (tagElement != null) {
                    String val = tagElement.toString();
                    // Trim very long values
                    if (val.length() > 60) val = val.substring(0, 57) + "...";
                    return val;
                }
            }
        } catch (Exception e) {
            // Fall through
        }
        return "?";
    }

    private static String toRoman(int number) {
        if (number <= 0) return String.valueOf(number);
        if (number == 1) return "I";
        if (number == 2) return "II";
        if (number == 3) return "III";
        if (number == 4) return "IV";
        if (number == 5) return "V";
        if (number == 6) return "VI";
        if (number == 7) return "VII";
        if (number == 8) return "VIII";
        if (number == 9) return "IX";
        if (number == 10) return "X";
        return String.valueOf(number);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(
            (ServerPlayer) player,
            (CustomPacketPayload) new ComputerDataPayload(type, json, wallet, bank),
            (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }
}

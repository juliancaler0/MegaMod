package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.citizen.CitizenManager;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.economy.shop.MegaShop;
import com.ultra.megamod.feature.economy.shop.ShopItem;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminSearchHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if ("admin_search".equals(action) || "admin_search_action".equals(action)) {
            if (!com.ultra.megamod.feature.computer.admin.AdminSystem.isAdmin(player)) {
                return true; // Consume the action but do nothing for non-admins
            }
            if ("admin_search".equals(action)) {
                handleSearch(player, jsonData.toLowerCase(Locale.ROOT), level, eco);
            } else {
                handleAction(player, jsonData, level, eco);
            }
            return true;
        }
        return false;
    }

    private static void handleSearch(ServerPlayer player, String query, ServerLevel level, EconomyManager eco) {
        List<String> results = new ArrayList<>();
        int maxPerCategory = 8;

        // Search online players
        int count = 0;
        for (ServerPlayer sp : level.getServer().getPlayerList().getPlayers()) {
            String name = sp.getGameProfile().name();
            if (name.toLowerCase(Locale.ROOT).contains(query) && count++ < maxPerCategory) {
                int wallet = eco.getWallet(sp.getUUID());
                int bank = eco.getBank(sp.getUUID());
                results.add(result("Players", name,
                        "Online | Wallet: " + wallet + " | Bank: " + bank,
                        "tp_player " + name));
            }
        }

        // Search items
        count = 0;
        for (Identifier id : BuiltInRegistries.ITEM.keySet()) {
            String path = id.getPath();
            String full = id.toString();
            if ((path.contains(query) || full.contains(query)) && count++ < maxPerCategory) {
                results.add(result("Items", full, "Give 1 to self", "give_item " + full));
            }
        }

        // Search blocks
        count = 0;
        for (Identifier id : BuiltInRegistries.BLOCK.keySet()) {
            String path = id.getPath();
            if ((path.contains(query) || id.toString().contains(query)) && count++ < maxPerCategory) {
                results.add(result("Blocks", id.toString(), "Block", ""));
            }
        }

        // Search entity types
        count = 0;
        for (Identifier id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            String path = id.getPath();
            if ((path.contains(query) || id.toString().contains(query)) && count++ < maxPerCategory) {
                results.add(result("Entities", id.toString(), "Summon near you", "summon_entity " + id));
            }
        }

        // Search feature toggles
        ServerLevel overworld = level.getServer().overworld();
        FeatureToggleManager toggles = FeatureToggleManager.get(overworld);
        for (var def : FeatureToggleManager.ALL_FEATURES) {
            if (def.id().contains(query) || def.name().toLowerCase(Locale.ROOT).contains(query)) {
                boolean enabled = toggles.isEnabled(def.id());
                results.add(result("Toggles", def.name(),
                        (enabled ? "Enabled" : "Disabled") + " | " + def.category(),
                        "toggle " + def.id()));
            }
        }

        // Search citizens
        count = 0;
        CitizenManager cm = CitizenManager.get(overworld);
        for (var ownerEntry : cm.getAllCitizens().entrySet()) {
            for (var rec : ownerEntry.getValue()) {
                String cName = rec.name() != null ? rec.name() : rec.job().getDisplayName();
                if (cName.toLowerCase(Locale.ROOT).contains(query) && count++ < maxPerCategory) {
                    results.add(result("Citizens", cName,
                            rec.job().getDisplayName() + " | Owner: " + ownerEntry.getKey().toString().substring(0, 8),
                            ""));
                }
            }
        }

        // Search today's shop items
        MegaShop shop = MegaShop.get(overworld);
        List<ShopItem> shopItems = shop.getTodaysItems(overworld);
        for (int i = 0; i < shopItems.size(); i++) {
            ShopItem si = shopItems.get(i);
            if (si.displayName().toLowerCase(Locale.ROOT).contains(query)
                    || si.itemId().toLowerCase(Locale.ROOT).contains(query)) {
                results.add(result("Shop", si.displayName(),
                        "Buy: " + si.buyPrice() + " MC | Sell: " + si.sellPrice() + " MC | Slot " + i,
                        ""));
            }
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < results.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(results.get(i));
        }
        sb.append("]");

        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player,
                new ComputerDataPayload("admin_search_results", sb.toString(), wallet, bank),
                new CustomPacketPayload[0]);
    }

    private static void handleAction(ServerPlayer player, String actionData, ServerLevel level, EconomyManager eco) {
        if (actionData.startsWith("tp_player ")) {
            String targetName = actionData.substring(10);
            ServerPlayer target = level.getServer().getPlayerList().getPlayerByName(targetName);
            if (target != null) {
                player.teleportTo((ServerLevel) target.level(),
                        target.getX(), target.getY(), target.getZ(),
                        java.util.Set.of(), target.getYRot(), target.getXRot(), false);
            }
        } else if (actionData.startsWith("give_item ")) {
            String itemId = actionData.substring(10);
            try {
                Identifier id = Identifier.parse(itemId);
                var item = BuiltInRegistries.ITEM.getOptional(id);
                if (item.isPresent()) {
                    ItemStack stack = new ItemStack((ItemLike) item.get());
                    if (!player.getInventory().add(stack)) {
                        player.spawnAtLocation(level, stack);
                    }
                }
            } catch (Exception ignored) {}
        } else if (actionData.startsWith("summon_entity ")) {
            String entityId = actionData.substring(14);
            try {
                Identifier id = Identifier.parse(entityId);
                var type = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
                if (type.isPresent()) {
                    var entity = type.get().create(level, EntitySpawnReason.COMMAND);
                    if (entity != null) {
                        entity.setPos(player.getX() + 2, player.getY(), player.getZ());
                        level.addFreshEntity(entity);
                    }
                }
            } catch (Exception ignored) {}
        } else if (actionData.startsWith("toggle ")) {
            String toggleId = actionData.substring(7);
            ServerLevel overworld = level.getServer().overworld();
            FeatureToggleManager toggles = FeatureToggleManager.get(overworld);
            boolean current = toggles.isEnabled(toggleId);
            toggles.setEnabled(toggleId, !current);
        }
    }

    private static String result(String cat, String name, String detail, String action) {
        return "{\"cat\":\"" + escape(cat) + "\",\"name\":\"" + escape(name)
                + "\",\"detail\":\"" + escape(detail) + "\",\"action\":\"" + escape(action) + "\"}";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

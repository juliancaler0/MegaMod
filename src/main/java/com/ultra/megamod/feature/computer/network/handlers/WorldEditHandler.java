package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.worldedit.WorldEditManager;
import com.ultra.megamod.feature.worldedit.WorldEditRegistry;
import com.ultra.megamod.feature.worldedit.brush.BrushBinding;
import com.ultra.megamod.feature.worldedit.clipboard.Clipboard;
import com.ultra.megamod.feature.worldedit.clipboard.ClipboardIO;
import com.ultra.megamod.feature.worldedit.region.Region;
import com.ultra.megamod.feature.worldedit.session.LocalSession;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Paths;

/**
 * Handler for the admin WorldEdit panel network actions.
 * All actions are admin-only.
 *
 * Client -> Server actions:
 *   we_get_status        -> we_status_data
 *   we_toggle_mode       -> we_status_data (gives/removes wand)
 *   we_save_selection    -> we_status_data
 *   we_clear_selection   -> we_status_data
 *   we_clear_clipboard   -> we_status_data
 *   we_brush_list        -> we_status_data
 */
public final class WorldEditHandler {
    private WorldEditHandler() {}

    public static boolean handle(ServerPlayer player, String action, String jsonData,
                                 ServerLevel level, EconomyManager eco) {
        if (!action.startsWith("we_")) return false;
        if (!AdminSystem.isAdmin(player)) return true; // silently swallow

        switch (action) {
            case "we_get_status" -> sendStatus(player, eco, null);
            case "we_toggle_mode" -> {
                boolean enable = Boolean.parseBoolean(jsonData);
                LocalSession ls = WorldEditManager.getSession(player);
                ls.setWeMode(enable);
                if (enable) {
                    giveWandIfMissing(player);
                } else {
                    removeWand(player);
                }
                sendStatus(player, eco, enable ? "WorldEdit mode enabled." : "WorldEdit mode disabled.");
            }
            case "we_save_selection" -> {
                LocalSession ls = WorldEditManager.getSession(player);
                Region r = ls.getSelectionRegion();
                if (r == null) { sendStatus(player, eco, "No selection to save."); return true; }
                Clipboard cb = ClipboardIO.copyFromWorld(level, r, player.blockPosition(), "panel_save");
                var data = ClipboardIO.toSchematicData(cb);
                var p = Paths.get("blueprints", "worldedit_schematics",
                    "panel_" + System.currentTimeMillis() + ".litematic");
                boolean ok = ClipboardIO.saveSchematic(p, data);
                sendStatus(player, eco, ok ? ("Saved " + cb.getVolume() + " blocks to " + p.getFileName())
                                           : "Save failed.");
            }
            case "we_clear_selection" -> {
                LocalSession ls = WorldEditManager.getSession(player);
                ls.setSelectionRegion(null);
                ls.setPos1(null);
                ls.setPos2(null);
                sendStatus(player, eco, "Selection cleared.");
            }
            case "we_clear_clipboard" -> {
                WorldEditManager.getSession(player).setClipboard(null);
                sendStatus(player, eco, "Clipboard cleared.");
            }
            case "we_brush_list" -> {
                LocalSession ls = WorldEditManager.getSession(player);
                BrushBinding b = ls.getBrush(LocalSession.BrushSlot.MAIN);
                sendStatus(player, eco, b == null ? "No brush bound." : "Brush: " + b.getBrush().describe());
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private static void giveWandIfMissing(ServerPlayer player) {
        var wand = WorldEditRegistry.WAND.get();
        var inv = player.getInventory();
        for (int i = 0; i < inv.getNonEquipmentItems().size(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.getItem() == wand) return;
        }
        // Try main hand if empty
        if (player.getMainHandItem().isEmpty()) {
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack(wand));
        } else {
            player.getInventory().add(new ItemStack(wand));
        }
    }

    private static void removeWand(ServerPlayer player) {
        var wand = WorldEditRegistry.WAND.get();
        var inv = player.getInventory();
        for (int i = 0; i < inv.getNonEquipmentItems().size(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.getItem() == wand) inv.setItem(i, ItemStack.EMPTY);
        }
        if (player.getMainHandItem().getItem() == wand) {
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        if (player.getOffhandItem().getItem() == wand) {
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    private static void sendStatus(ServerPlayer player, EconomyManager eco, String statusMessage) {
        LocalSession ls = WorldEditManager.getSession(player);
        Region r = ls.getSelectionRegion();
        Clipboard cb = ls.getClipboard();
        BrushBinding brush = ls.getBrush(LocalSession.BrushSlot.MAIN);

        StringBuilder json = new StringBuilder(512);
        json.append("{\"mode\":").append(ls.isWeMode());
        json.append(",\"selectionVolume\":").append(r == null ? 0 : r.getVolume());
        json.append(",\"clipboardSize\":").append(cb == null ? 0 : cb.getVolume());
        json.append(",\"undoDepth\":").append(ls.getHistory().undoDepth());
        json.append(",\"redoDepth\":").append(ls.getHistory().redoDepth());
        json.append(",\"mask\":\"").append(esc(ls.getActiveMask() == null ? "none" : ls.getActiveMask().getClass().getSimpleName())).append('"');
        json.append(",\"pattern\":\"").append(esc(ls.getActivePattern() == null ? "none" : ls.getActivePattern().getClass().getSimpleName())).append('"');
        BlockPos p1 = ls.getPos1();
        BlockPos p2 = ls.getPos2();
        json.append(",\"pos1\":\"").append(p1 == null ? "-" : p1.getX() + "," + p1.getY() + "," + p1.getZ()).append('"');
        json.append(",\"pos2\":\"").append(p2 == null ? "-" : p2.getX() + "," + p2.getY() + "," + p2.getZ()).append('"');
        json.append(",\"brush\":\"").append(esc(brush == null ? "none" : brush.getBrush().describe())).append('"');
        if (statusMessage != null) json.append(",\"message\":\"").append(esc(statusMessage)).append('"');
        json.append('}');

        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload("we_status_data", json.toString(), wallet, bank));
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}

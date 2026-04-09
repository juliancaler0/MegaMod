package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.AdminModuleManager;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class AdminModulesHandler {

    /**
     * Handles admin module actions from the computer panel.
     * Returns true if the action was handled, false otherwise.
     */
    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!AdminSystem.isAdmin(player)) {
            return false;
        }

        switch (action) {
            case "adminmod_request": {
                AdminModuleManager mgr = AdminModuleManager.get();
                mgr.loadFromDisk(level);
                String json = mgr.toJson();
                sendResponse(player, "adminmod_data", json, eco);
                return true;
            }

            case "adminmod_toggle": {
                AdminModuleManager mgr = AdminModuleManager.get();
                mgr.loadFromDisk(level);
                AdminModule module = mgr.getModule(jsonData);
                if (module != null) {
                    module.toggle(player);
                    mgr.saveToDisk(level);
                }
                sendResponse(player, "adminmod_data", mgr.toJson(), eco);
                return true;
            }

            case "adminmod_set_setting": {
                // jsonData format: "moduleId:settingName:newValue"
                AdminModuleManager mgr = AdminModuleManager.get();
                mgr.loadFromDisk(level);
                int firstColon = jsonData.indexOf(':');
                if (firstColon > 0) {
                    int secondColon = jsonData.indexOf(':', firstColon + 1);
                    if (secondColon > firstColon) {
                        String moduleId = jsonData.substring(0, firstColon);
                        String settingName = jsonData.substring(firstColon + 1, secondColon);
                        String newValue = jsonData.substring(secondColon + 1);

                        AdminModule module = mgr.getModule(moduleId);
                        if (module != null) {
                            ModuleSetting<?> setting = module.getSetting(settingName);
                            if (setting != null) {
                                try {
                                    setting.deserializeValue(newValue);
                                } catch (Exception ignored) {}
                                mgr.saveToDisk(level);
                            }
                        }
                    }
                }
                sendResponse(player, "adminmod_data", mgr.toJson(), eco);
                return true;
            }

            case "adminmod_set_togglekey": {
                // jsonData format: "moduleId:keyName"
                AdminModuleManager mgr = AdminModuleManager.get();
                mgr.loadFromDisk(level);
                int colon = jsonData.indexOf(':');
                if (colon > 0) {
                    String moduleId = jsonData.substring(0, colon);
                    String keyName = jsonData.substring(colon + 1);
                    AdminModule module = mgr.getModule(moduleId);
                    if (module != null) {
                        module.setToggleKey(keyName);
                        mgr.saveToDisk(level);
                    }
                }
                sendResponse(player, "adminmod_data", mgr.toJson(), eco);
                return true;
            }

            case "adminmod_enable_category": {
                AdminModuleManager mgr = AdminModuleManager.get();
                mgr.loadFromDisk(level);
                ModuleCategory category = parseCategoryOrNull(jsonData);
                if (category != null) {
                    for (AdminModule module : mgr.getModulesByCategory(category)) {
                        if (!module.isEnabled()) {
                            module.setEnabled(true);
                            module.onEnable(player);
                        }
                    }
                    mgr.saveToDisk(level);
                }
                sendResponse(player, "adminmod_data", mgr.toJson(), eco);
                return true;
            }

            case "adminmod_disable_category": {
                AdminModuleManager mgr = AdminModuleManager.get();
                mgr.loadFromDisk(level);
                ModuleCategory category = parseCategoryOrNull(jsonData);
                if (category != null) {
                    for (AdminModule module : mgr.getModulesByCategory(category)) {
                        if (module.isEnabled()) {
                            module.onDisable(player);
                            module.setEnabled(false);
                        }
                    }
                    mgr.saveToDisk(level);
                }
                sendResponse(player, "adminmod_data", mgr.toJson(), eco);
                return true;
            }

            case "adminmod_enable_all": {
                AdminModuleManager mgr = AdminModuleManager.get();
                mgr.loadFromDisk(level);
                for (AdminModule module : mgr.getAllModules()) {
                    if (!module.isEnabled()) {
                        module.setEnabled(true);
                        module.onEnable(player);
                    }
                }
                mgr.saveToDisk(level);
                sendResponse(player, "adminmod_data", mgr.toJson(), eco);
                return true;
            }

            case "adminmod_disable_all": {
                AdminModuleManager mgr = AdminModuleManager.get();
                mgr.loadFromDisk(level);
                for (AdminModule module : mgr.getAllModules()) {
                    if (module.isEnabled()) {
                        module.onDisable(player);
                        module.setEnabled(false);
                    }
                }
                mgr.saveToDisk(level);
                sendResponse(player, "adminmod_data", mgr.toJson(), eco);
                return true;
            }

            default:
                return false;
        }
    }

    /**
     * Parses a category name string to the ModuleCategory enum, returning null if invalid.
     */
    private static ModuleCategory parseCategoryOrNull(String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            return ModuleCategory.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }
}

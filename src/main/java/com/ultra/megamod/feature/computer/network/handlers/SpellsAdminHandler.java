package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainer;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainerHelper;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.internals.SpellCooldownManager;
import com.ultra.megamod.lib.spellengine.internals.SpellTriggers;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin handler for SpellEngine-aware tools:
 *  - Spell Container Editor (mainhand / offhand)
 *  - Spell Cooldown Override
 *  - Spell Registry Viewer
 *  - Passive Trigger Tester
 *
 * All actions are namespaced under {@code spells_*} so the admin gate in
 * {@link com.ultra.megamod.feature.computer.network.ComputerActionHandler}
 * can trivially reject them for non-admins.
 */
public class SpellsAdminHandler {

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!action.startsWith("spells_")) return false;
        if (!AdminSystem.isAdmin(player)) return true; // consume silently

        switch (action) {
            case "spells_request_registry": {
                sendResponse(player, "spells_registry_data", buildRegistryJson(level), eco);
                return true;
            }
            case "spells_request_spell_detail": {
                sendResponse(player, "spells_spell_detail", buildSpellDetailJson(level, jsonData), eco);
                return true;
            }
            case "spells_request_container": {
                // jsonData: "mainhand" | "offhand"
                sendResponse(player, "spells_container_data", buildContainerJson(player, jsonData), eco);
                return true;
            }
            case "spells_container_add_spell": {
                // jsonData: "mainhand:megamod:fireball" or "offhand:megamod:healing"
                String[] parts = jsonData.split(":", 2);
                if (parts.length < 2) {
                    sendResponse(player, "spells_result", "{\"success\":false,\"msg\":\"Invalid payload\"}", eco);
                    return true;
                }
                EquipmentSlot slot = resolveSlot(parts[0]);
                Identifier spellId = safeParseId(parts[1]);
                if (slot == null || spellId == null) {
                    sendResponse(player, "spells_result", "{\"success\":false,\"msg\":\"Bad slot or spell ID\"}", eco);
                    return true;
                }
                ItemStack stack = player.getItemBySlot(slot);
                if (stack.isEmpty()) {
                    sendResponse(player, "spells_result", "{\"success\":false,\"msg\":\"" + slot.getName() + " is empty\"}", eco);
                    return true;
                }
                SpellContainer container = SpellContainerHelper.containerFromItemStack(stack);
                if (container == null) {
                    // create a minimal writable container
                    container = new SpellContainer(SpellContainer.ContentType.ANY, "", "", slot.getName(), 8, List.of(), 0);
                }
                List<String> ids = new ArrayList<>(container.spell_ids());
                if (!ids.contains(spellId.toString())) {
                    ids.add(spellId.toString());
                }
                SpellContainer updated = container.copyWith(ids);
                stack.set(SpellDataComponents.SPELL_CONTAINER, updated);
                sendResponse(player, "spells_result", "{\"success\":true,\"msg\":\"Added " + spellId + "\"}", eco);
                sendResponse(player, "spells_container_data", buildContainerJson(player, parts[0]), eco);
                return true;
            }
            case "spells_container_remove_spell": {
                // jsonData: "mainhand:megamod:fireball"
                String[] parts = jsonData.split(":", 2);
                if (parts.length < 2) {
                    sendResponse(player, "spells_result", "{\"success\":false,\"msg\":\"Invalid payload\"}", eco);
                    return true;
                }
                EquipmentSlot slot = resolveSlot(parts[0]);
                Identifier spellId = safeParseId(parts[1]);
                if (slot == null || spellId == null) {
                    sendResponse(player, "spells_result", "{\"success\":false,\"msg\":\"Bad slot or spell ID\"}", eco);
                    return true;
                }
                ItemStack stack = player.getItemBySlot(slot);
                if (stack.isEmpty()) {
                    sendResponse(player, "spells_result", "{\"success\":false,\"msg\":\"" + slot.getName() + " is empty\"}", eco);
                    return true;
                }
                SpellContainer container = SpellContainerHelper.containerFromItemStack(stack);
                if (container == null) {
                    sendResponse(player, "spells_result", "{\"success\":false,\"msg\":\"No container on item\"}", eco);
                    return true;
                }
                List<String> ids = new ArrayList<>(container.spell_ids());
                ids.remove(spellId.toString());
                SpellContainer updated = container.copyWith(ids);
                stack.set(SpellDataComponents.SPELL_CONTAINER, updated);
                sendResponse(player, "spells_result", "{\"success\":true,\"msg\":\"Removed " + spellId + "\"}", eco);
                sendResponse(player, "spells_container_data", buildContainerJson(player, parts[0]), eco);
                return true;
            }
            case "spells_container_clear": {
                // jsonData: "mainhand" | "offhand" — strips the spell container data component entirely
                EquipmentSlot slot = resolveSlot(jsonData);
                if (slot == null) {
                    sendResponse(player, "spells_result", "{\"success\":false,\"msg\":\"Bad slot\"}", eco);
                    return true;
                }
                ItemStack stack = player.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    stack.remove(SpellDataComponents.SPELL_CONTAINER);
                }
                sendResponse(player, "spells_result", "{\"success\":true,\"msg\":\"Cleared container on " + slot.getName() + "\"}", eco);
                sendResponse(player, "spells_container_data", buildContainerJson(player, jsonData), eco);
                return true;
            }
            case "spells_request_cooldowns": {
                // jsonData: target player UUID (optional; empty = self)
                ServerPlayer target = resolveTargetPlayer(player, jsonData);
                sendResponse(player, "spells_cooldowns_data", buildCooldownsJson(target), eco);
                return true;
            }
            case "spells_cooldown_reset": {
                // jsonData: "<uuid>:<spellId>" or "<uuid>:" to clear all
                String[] parts = jsonData.split(":", 2);
                if (parts.length < 1) return true;
                ServerPlayer target = resolveTargetPlayer(player, parts[0]);
                String rest = parts.length > 1 ? parts[1] : "";
                SpellCooldownManager mgr = ((SpellCasterEntity) target).getCooldownManager();
                if (rest == null || rest.isEmpty()) {
                    mgr.reset(null);
                    sendResponse(player, "spells_result", "{\"success\":true,\"msg\":\"Cleared all cooldowns for " + target.getGameProfile().name() + "\"}", eco);
                } else {
                    Identifier spellId = safeParseId(rest);
                    if (spellId == null) {
                        sendResponse(player, "spells_result", "{\"success\":false,\"msg\":\"Bad spell ID\"}", eco);
                        return true;
                    }
                    mgr.reset(spellId);
                    sendResponse(player, "spells_result", "{\"success\":true,\"msg\":\"Reset " + spellId + "\"}", eco);
                }
                sendResponse(player, "spells_cooldowns_data", buildCooldownsJson(target), eco);
                return true;
            }
            case "spells_trigger_fire": {
                // jsonData: trigger type name (e.g. MELEE_IMPACT)
                Spell.Trigger.Type type = parseTriggerType(jsonData);
                if (type == null) {
                    sendResponse(player, "spells_trigger_result",
                            "{\"fired\":[],\"msg\":\"Unknown trigger type: " + escape(jsonData) + "\"}", eco);
                    return true;
                }
                String msg = simulateTrigger(player, type);
                sendResponse(player, "spells_trigger_result", msg, eco);
                return true;
            }
            default:
                return false;
        }
    }

    // --------------------------------------------------------------------
    // Registry JSON

    private static String buildRegistryJson(ServerLevel level) {
        StringBuilder sb = new StringBuilder(4096);
        sb.append("{\"spells\":[");
        boolean first = true;
        try {
            var registry = level.registryAccess().lookupOrThrow(SpellRegistry.KEY);
            List<Holder.Reference<Spell>> entries = registry.listElements().toList();
            for (Holder.Reference<Spell> holder : entries) {
                if (!first) sb.append(',');
                first = false;
                Identifier id = holder.getKey().identifier();
                Spell spell = holder.value();
                sb.append("{\"id\":\"").append(escape(id.toString())).append("\"");
                sb.append(",\"type\":\"").append(spell.type != null ? spell.type.name() : "ACTIVE").append("\"");
                sb.append(",\"school\":\"").append(spell.school != null ? escape(spell.school.id.toString()) : "").append("\"");
                sb.append(",\"deliver\":\"").append(spell.deliver != null && spell.deliver.type != null ? spell.deliver.type.name() : "").append("\"");
                sb.append(",\"target\":\"").append(spell.target != null && spell.target.type != null ? spell.target.type.name() : "").append("\"");
                sb.append(",\"tier\":").append(spell.tier);
                sb.append('}');
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to build spell registry JSON", e);
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String buildSpellDetailJson(ServerLevel level, String spellIdStr) {
        Identifier id = safeParseId(spellIdStr);
        if (id == null) return "{\"id\":\"" + escape(spellIdStr) + "\",\"error\":\"invalid id\"}";
        try {
            var registry = level.registryAccess().lookupOrThrow(SpellRegistry.KEY);
            var spellOpt = registry.get(id);
            if (spellOpt.isEmpty()) return "{\"id\":\"" + escape(spellIdStr) + "\",\"error\":\"not found\"}";
            var spell = spellOpt.get().value();
            // Use Gson directly to serialize full spell JSON
            String raw = com.google.gson.JsonParser.parseString(new com.google.gson.Gson().toJson(spell)).toString();
            return "{\"id\":\"" + escape(id.toString()) + "\",\"json\":" + raw + "}";
        } catch (Exception e) {
            return "{\"id\":\"" + escape(spellIdStr) + "\",\"error\":\"" + escape(e.getMessage()) + "\"}";
        }
    }

    // --------------------------------------------------------------------
    // Container JSON

    private static String buildContainerJson(ServerPlayer player, String slotName) {
        EquipmentSlot slot = resolveSlot(slotName);
        if (slot == null) slot = EquipmentSlot.MAINHAND;
        ItemStack stack = player.getItemBySlot(slot);
        StringBuilder sb = new StringBuilder(512);
        sb.append("{\"slot\":\"").append(slot.getName()).append("\"");
        sb.append(",\"itemId\":\"");
        if (stack.isEmpty()) {
            sb.append("minecraft:air\",\"itemName\":\"Empty\",\"hasContainer\":false,\"spells\":[]}");
            return sb.toString();
        }
        var itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        sb.append(escape(itemId != null ? itemId.toString() : "unknown")).append("\"");
        sb.append(",\"itemName\":\"").append(escape(stack.getHoverName().getString())).append("\"");
        SpellContainer container = SpellContainerHelper.containerFromItemStack(stack);
        if (container == null) {
            sb.append(",\"hasContainer\":false,\"spells\":[]}");
            return sb.toString();
        }
        sb.append(",\"hasContainer\":true");
        sb.append(",\"access\":\"").append(container.access() != null ? container.access().name() : "NONE").append("\"");
        sb.append(",\"pool\":\"").append(escape(container.pool() != null ? container.pool() : "")).append("\"");
        sb.append(",\"containerSlot\":\"").append(escape(container.slot() != null ? container.slot() : "")).append("\"");
        sb.append(",\"maxSpells\":").append(container.max_spell_count());
        sb.append(",\"spells\":[");
        boolean first = true;
        for (String sid : container.spell_ids()) {
            if (!first) sb.append(',');
            first = false;
            sb.append("\"").append(escape(sid)).append("\"");
        }
        sb.append("]}");
        return sb.toString();
    }

    // --------------------------------------------------------------------
    // Cooldowns JSON

    private static String buildCooldownsJson(ServerPlayer target) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("{\"player\":\"").append(escape(target.getGameProfile().name())).append("\"");
        sb.append(",\"uuid\":\"").append(target.getUUID()).append("\"");
        sb.append(",\"cooldowns\":[");
        try {
            SpellCooldownManager mgr = ((SpellCasterEntity) target).getCooldownManager();
            List<Identifier> ids = mgr.spellsOnCooldown();
            boolean first = true;
            for (Identifier id : ids) {
                if (!first) sb.append(',');
                first = false;
                // Look up remaining ticks via registry holder if possible
                int remaining = 0;
                try {
                    var reg = target.level().registryAccess().lookupOrThrow(SpellRegistry.KEY);
                    var holder = reg.get(id).orElse(null);
                    if (holder != null) {
                        remaining = mgr.getCooldownDuration(holder);
                    }
                } catch (Exception ignored) {}
                sb.append("{\"id\":\"").append(escape(id.toString())).append("\"");
                sb.append(",\"remainingTicks\":").append(remaining).append('}');
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to build cooldowns JSON", e);
        }
        sb.append("]}");
        return sb.toString();
    }

    // --------------------------------------------------------------------
    // Trigger simulation

    private static Spell.Trigger.Type parseTriggerType(String name) {
        if (name == null) return null;
        try { return Spell.Trigger.Type.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT)); }
        catch (Exception e) { return null; }
    }

    /**
     * Dry-run enumeration of passive spells on equipped items that WOULD match
     * the requested trigger type. We do not synthesize fake damage sources,
     * arrows, or impacts, since those require real world state — instead we
     * report which passives the admin currently has equipped for that trigger
     * and perform a live call for trigger types that are safe (SPELL_CAST,
     * MELEE_IMPACT on self with no target, EFFECT_TICK with no effect).
     *
     * This is intentionally conservative: firing synthetic events for triggers
     * like ARROW_IMPACT or DAMAGE_TAKEN without a real source entity would
     * either NPE deep in SpellTriggers or misleadingly skip the chance/damage
     * condition checks.
     */
    private static String simulateTrigger(ServerPlayer player, Spell.Trigger.Type type) {
        StringBuilder fired = new StringBuilder();
        fired.append("{\"type\":\"").append(type.name()).append("\"");
        fired.append(",\"matches\":[");
        int matchCount = 0;
        boolean first = true;
        try {
            for (var entry : com.ultra.megamod.lib.spellengine.internals.container.SpellContainerSource.passiveSpellsOf(player)) {
                var spell = entry.value();
                if (spell.passive == null || spell.passive.triggers == null) continue;
                for (var trigger : spell.passive.triggers) {
                    if (trigger.type == type) {
                        if (!first) fired.append(',');
                        first = false;
                        matchCount++;
                        fired.append("{\"spell\":\"").append(escape(entry.unwrapKey().get().identifier().toString())).append("\"");
                        fired.append(",\"chance\":").append(trigger.chance);
                        fired.append(",\"stage\":\"").append(trigger.stage != null ? trigger.stage.name() : "POST").append("\"}");
                    }
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.warn("Trigger simulation scan failed", e);
        }
        fired.append(']');
        fired.append(",\"matchCount\":").append(matchCount);

        // Only actually fire triggers we can safely synthesize without fake world state.
        String note;
        switch (type) {
            case SPELL_CAST:
            case MELEE_IMPACT: {
                try {
                    if (type == Spell.Trigger.Type.MELEE_IMPACT) {
                        SpellTriggers.onMeleeImpact(player, player); // self-target is harmless
                    } else {
                        // Cannot fire SPELL_CAST without a spell; just report scan results
                    }
                    note = matchCount + " passive spell(s) on equipped items match " + type.name()
                            + (type == Spell.Trigger.Type.MELEE_IMPACT ? "; fired against self." : "; scan only (SPELL_CAST needs a live cast).");
                } catch (Exception e) {
                    note = "Error firing " + type.name() + ": " + e.getMessage();
                }
                break;
            }
            default: {
                note = matchCount + " passive spell(s) on equipped items match " + type.name()
                        + "; live firing skipped (needs real world event — arrow, damage source, etc.).";
            }
        }
        player.sendSystemMessage(Component.literal("[SpellTrigger] " + note));
        fired.append(",\"msg\":\"").append(escape(note)).append("\"}");
        return fired.toString();
    }

    // --------------------------------------------------------------------
    // Helpers

    private static EquipmentSlot resolveSlot(String s) {
        if (s == null) return null;
        switch (s.toLowerCase(java.util.Locale.ROOT)) {
            case "mainhand": return EquipmentSlot.MAINHAND;
            case "offhand":  return EquipmentSlot.OFFHAND;
            case "head":     return EquipmentSlot.HEAD;
            case "chest":    return EquipmentSlot.CHEST;
            case "legs":     return EquipmentSlot.LEGS;
            case "feet":     return EquipmentSlot.FEET;
            default: return null;
        }
    }

    private static Identifier safeParseId(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Identifier.parse(s); } catch (Exception e) { return null; }
    }

    private static ServerPlayer resolveTargetPlayer(ServerPlayer admin, String uuidStr) {
        if (uuidStr == null || uuidStr.isEmpty()) return admin;
        try {
            var uuid = java.util.UUID.fromString(uuidStr);
            ServerPlayer p = admin.level().getServer().getPlayerList().getPlayer(uuid);
            return p != null ? p : admin;
        } catch (Exception e) {
            return admin;
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private static void sendResponse(ServerPlayer player, String type, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ComputerDataPayload(type, json, wallet, bank));
    }
}

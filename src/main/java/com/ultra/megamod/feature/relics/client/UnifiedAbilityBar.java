package com.ultra.megamod.feature.relics.client;

import com.ultra.megamod.feature.combat.spell.SpellAbilityBridge;
import com.ultra.megamod.feature.combat.spell.SpellDefinition;
import com.ultra.megamod.feature.combat.spell.SpellRegistry;
import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicData;
import com.ultra.megamod.feature.relics.weapons.RpgWeaponItem;
import com.ultra.megamod.feature.relics.weapons.RpgWeaponRegistry;
import com.ultra.megamod.feature.relics.network.AccessoryPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Client-side singleton tracking all ability bar UI state:
 * - Which accessory is selected (G+Scroll cycles)
 * - Which ability is selected per accessory (G tap flips)
 * - Which weapon ability is selected (R+Scroll cycles)
 * - Held-item change detection for weapon bar rebuild
 */
public class UnifiedAbilityBar {

    // --- Accessory state ---
    private static List<AccessoryAbilityEntry> accessoryEntries = new ArrayList<>();
    private static int selectedAccessoryIndex = 0;
    private static final Map<String, Integer> selectedAbilityPerSlot = new HashMap<>();

    // --- Weapon state ---
    private static List<WeaponAbilityEntry> weaponEntries = new ArrayList<>();
    private static int selectedWeaponAbilityIndex = 0;

    // --- Held-item change detection ---
    private static ItemStack lastHeldItem = ItemStack.EMPTY;
    private static ItemStack lastOffhandItem = ItemStack.EMPTY;

    // --- Records ---
    public record AccessoryAbilityEntry(String slotName, String itemId, String relicName,
                                        List<String> castableAbilities) {}
    public record WeaponAbilityEntry(String abilityName, String displayName, boolean isRpgSkill) {}

    // --- Getters ---
    public static List<AccessoryAbilityEntry> getAccessoryEntries() { return accessoryEntries; }
    public static int getSelectedAccessoryIndex() { return selectedAccessoryIndex; }
    public static List<WeaponAbilityEntry> getWeaponEntries() { return weaponEntries; }
    public static int getSelectedWeaponAbilityIndex() { return selectedWeaponAbilityIndex; }

    public static int getSelectedAbilityIndex(String slotName) {
        return selectedAbilityPerSlot.getOrDefault(slotName, 0);
    }

    public static AccessoryAbilityEntry getSelectedAccessory() {
        if (accessoryEntries.isEmpty()) return null;
        if (selectedAccessoryIndex >= accessoryEntries.size()) selectedAccessoryIndex = 0;
        return accessoryEntries.get(selectedAccessoryIndex);
    }

    public static boolean hasWeaponAbilities() { return !weaponEntries.isEmpty(); }
    public static boolean hasAccessoryAbilities() { return !accessoryEntries.isEmpty(); }
    public static int getAccessoryCount() { return accessoryEntries.size(); }

    // --- Rebuild from sync data ---

    public static void rebuildAccessories() {
        Map<String, String> equipped = AccessoryPayload.AccessorySyncPayload.clientEquipped;
        accessoryEntries = new ArrayList<>();

        if (equipped != null && !equipped.isEmpty()) {
            for (Map.Entry<String, String> entry : equipped.entrySet()) {
                String slotName = entry.getKey();
                String itemId = entry.getValue();
                if ("NONE".equals(slotName)) continue;

                Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
                if (item instanceof RelicItem relicItem) {
                    List<String> castable = new ArrayList<>();
                    for (RelicAbility ability : relicItem.getAbilities()) {
                        if (ability.castType() != RelicAbility.CastType.PASSIVE) {
                            castable.add(ability.name());
                        }
                    }
                    if (!castable.isEmpty()) {
                        accessoryEntries.add(new AccessoryAbilityEntry(
                                slotName, itemId, relicItem.getRelicName(), castable));
                    }
                }
            }
        }

        // Also check offhand for shields with skills
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack offhand = mc.player.getOffhandItem();
            if (!offhand.isEmpty()) {
                Item offItem = offhand.getItem();
                List<String> skillNames = null;
                String displayName = null;
                String itemId = BuiltInRegistries.ITEM.getKey(offItem).toString();

                if (offItem instanceof RpgWeaponItem rpg && !rpg.getSkills().isEmpty()) {
                    skillNames = rpg.getSkills().stream()
                            .map(RpgWeaponItem.WeaponSkill::name)
                            .collect(Collectors.toList());
                    displayName = rpg.getWeaponName();
                } else {
                    List<RpgWeaponItem.WeaponSkill> regSkills = RpgWeaponRegistry.getSkillsForWeapon(itemId);
                    if (!regSkills.isEmpty()) {
                        skillNames = regSkills.stream()
                                .map(RpgWeaponItem.WeaponSkill::name)
                                .collect(Collectors.toList());
                        displayName = offhand.getHoverName().getString();
                    }
                }

                if (skillNames != null && !skillNames.isEmpty()) {
                    accessoryEntries.add(new AccessoryAbilityEntry(
                            "OFFHAND", itemId, displayName, skillNames));
                }
            }
        }

        if (selectedAccessoryIndex >= accessoryEntries.size()) {
            selectedAccessoryIndex = 0;
        }
    }

    public static void rebuildWeaponAbilities(ItemStack heldItem) {
        weaponEntries = new ArrayList<>();
        selectedWeaponAbilityIndex = 0;
        if (heldItem.isEmpty()) return;

        Item item = heldItem.getItem();
        if (item instanceof RelicItem relicItem) {
            int relicLevel = RelicData.getLevel(heldItem);
            List<RelicAbility> abilities = relicItem.getAbilities();
            for (RelicAbility ability : abilities) {
                // Must match server's resolveAbilityByIndex filter: skip PASSIVE and level-locked
                if (ability.castType() == RelicAbility.CastType.PASSIVE) continue;
                if (!RelicData.isAbilityUnlocked(relicLevel, ability, abilities)) continue;
                weaponEntries.add(new WeaponAbilityEntry(ability.name(), ability.name(), false));
            }
        } else if (item instanceof RpgWeaponItem rpgItem) {
            for (RpgWeaponItem.WeaponSkill skill : rpgItem.getSkills()) {
                weaponEntries.add(new WeaponAbilityEntry(skill.name(), skill.name(), true));
            }
            // If no built-in skills, check SpellAbilityBridge for mapped spells (class weapons)
            if (rpgItem.getSkills().isEmpty()) {
                String registryName = BuiltInRegistries.ITEM.getKey(item).toString();
                addSpellEntryIfMapped(registryName);
            }
        } else {
            String registryName = BuiltInRegistries.ITEM.getKey(item).toString();
            List<RpgWeaponItem.WeaponSkill> skills = RpgWeaponRegistry.getSkillsForWeapon(registryName);
            for (RpgWeaponItem.WeaponSkill skill : skills) {
                weaponEntries.add(new WeaponAbilityEntry(skill.name(), skill.name(), true));
            }
            // Also check SpellAbilityBridge for non-RpgWeapon items with spell mappings
            if (skills.isEmpty()) {
                addSpellEntryIfMapped(registryName);
            }
        }
    }

    /**
     * Checks SpellAbilityBridge for weapon spell mappings and adds synthetic
     * WeaponAbilityEntries so they show in the ability HUD. Each entry uses the spell's
     * display name with the school name in brackets, e.g. "Fireball [Fire]".
     * Supports multiple spells per weapon for cycling via R+Scroll.
     */
    private static void addSpellEntryIfMapped(String weaponRegistryName) {
        List<String> spellIds = SpellAbilityBridge.getSpellsForWeapon(weaponRegistryName);
        for (String spellId : spellIds) {
            SpellDefinition spell = SpellRegistry.get(spellId);
            if (spell == null) continue;
            String displayName = spell.name() + " [" + spell.school().displayName + "]";
            // Use spellId as the ability name key so cooldown lookups work correctly
            weaponEntries.add(new WeaponAbilityEntry(spellId, displayName, true));
        }
    }

    // --- Scroll handlers ---

    public static void onScrollWithG(int direction) {
        if (accessoryEntries.isEmpty()) return;
        selectedAccessoryIndex = (selectedAccessoryIndex + direction + accessoryEntries.size())
                % accessoryEntries.size();
    }

    public static void onScrollWithR(int direction) {
        if (weaponEntries.isEmpty()) return;
        selectedWeaponAbilityIndex = (selectedWeaponAbilityIndex + direction + weaponEntries.size())
                % weaponEntries.size();
    }

    public static void onTapG() {
        if (accessoryEntries.isEmpty()) return;
        if (selectedAccessoryIndex >= accessoryEntries.size()) selectedAccessoryIndex = 0;
        AccessoryAbilityEntry entry = accessoryEntries.get(selectedAccessoryIndex);
        int currentIdx = selectedAbilityPerSlot.getOrDefault(entry.slotName(), 0);
        int nextIdx = (currentIdx + 1) % entry.castableAbilities().size();
        selectedAbilityPerSlot.put(entry.slotName(), nextIdx);
    }

    // --- Per-tick update ---

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack currentHeld = mc.player.getMainHandItem();
        if (!ItemStack.isSameItem(currentHeld, lastHeldItem)) {
            lastHeldItem = currentHeld.copy();
            rebuildWeaponAbilities(currentHeld);
        }

        // Track offhand changes for shield skills
        ItemStack currentOffhand = mc.player.getOffhandItem();
        if (!ItemStack.isSameItem(currentOffhand, lastOffhandItem)) {
            lastOffhandItem = currentOffhand.copy();
            rebuildAccessories();
        }
    }

    /** Returns true if the offhand shield has been added to accessory entries. */
    public static boolean hasShieldAbilities() {
        return accessoryEntries.stream().anyMatch(e -> "OFFHAND".equals(e.slotName()));
    }
}

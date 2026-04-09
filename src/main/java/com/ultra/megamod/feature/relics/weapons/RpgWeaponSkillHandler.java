package com.ultra.megamod.feature.relics.weapons;

import com.ultra.megamod.feature.combat.spell.SpellAbilityBridge;
import com.ultra.megamod.feature.combat.spell.SpellDefinition;
import com.ultra.megamod.feature.combat.spell.SpellExecutor;
import com.ultra.megamod.feature.combat.spell.SpellRegistry;
import com.ultra.megamod.feature.skills.locks.SkillLockManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Bridges RPG weapon skills into the unified ability system.
 * Called by AbilityCastHandler when the held item is an RpgWeaponItem.
 *
 * Handles two cases:
 * 1. Weapons with built-in WeaponSkills (legacy RPG weapons) -> delegates to RpgWeaponEvents
 * 2. Weapons with SpellAbilityBridge mappings (class weapons: wands, staves) -> casts via SpellExecutor
 *
 * The skillIndex corresponds to whichever list populates the weapon ability bar on the client:
 * - For case 1: index into the WeaponSkill list
 * - For case 2: index into the SpellAbilityBridge spell list for that weapon
 */
public class RpgWeaponSkillHandler {

    public static void handleCast(ServerPlayer player, ItemStack stack, int skillIndex) {
        // Enforce skill locks -- locked weapons cannot use abilities
        if (!SkillLockManager.canUseItem(player, stack)) {
            player.displayClientMessage(
                    Component.literal("You haven't unlocked the skill to use this weapon!")
                            .withStyle(ChatFormatting.RED), true);
            return;
        }

        String registryName = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        // Check for custom skill overrides on the item first, then fall back to registry
        List<RpgWeaponItem.WeaponSkill> skills = RpgWeaponEvents.getEffectiveSkills(stack);
        if (skills.isEmpty()) {
            skills = RpgWeaponRegistry.getSkillsForWeapon(registryName);
        }

        // If the weapon has built-in skills, use the original flow
        if (!skills.isEmpty()) {
            if (skillIndex < 0 || skillIndex >= skills.size()) return;
            RpgWeaponItem.WeaponSkill skill = skills.get(skillIndex);
            RpgWeaponEvents.executeSkillPublic(player, registryName, skill, stack);
            return;
        }

        // No built-in skills -- check SpellAbilityBridge for mapped spells (class weapons)
        List<String> spellIds = SpellAbilityBridge.getSpellsForWeapon(registryName);
        if (spellIds.isEmpty()) return;

        // Resolve the selected spell by index (supports cycling through multiple spells)
        int idx = Math.max(0, Math.min(skillIndex, spellIds.size() - 1));
        String spellId = spellIds.get(idx);
        SpellDefinition spell = SpellRegistry.get(spellId);
        if (spell == null) return;

        boolean cast = SpellExecutor.cast(player, spell);
        if (cast) {
            String displayName = spell.name();
            player.displayClientMessage(
                    Component.literal(displayName + "!").withStyle(ChatFormatting.GOLD), true);
        }
    }
}

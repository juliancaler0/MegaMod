package com.ultra.megamod.feature.skills.prestige;

import com.ultra.megamod.feature.attributes.AttributeHelper;
import com.ultra.megamod.feature.attributes.MegaModAttributes;
import com.ultra.megamod.feature.combat.PlayerClassManager;
import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;
import com.ultra.megamod.feature.skills.SkillTreeType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Applies class-specific permanent bonuses when a player prestiges a skill tree.
 * Each prestige level grants an additional +5% to the class-specific attribute.
 *
 * - COMBAT + Paladin:  +5% HEALING_POWER per prestige
 * - COMBAT + Warrior:  +5% CRITICAL_DAMAGE per prestige
 * - ARCANE + Wizard:   +5% SPELL_HASTE per prestige
 * - SURVIVAL + Rogue:  +5% DODGE_CHANCE per prestige
 * - SURVIVAL + Ranger: +5% RANGED_DAMAGE per prestige
 *
 * Call {@link #applyClassPrestigeBonuses(ServerPlayer)} on login and after prestige
 * to reapply all earned bonuses.
 */
public final class PrestigeClassBonusHandler {

    private PrestigeClassBonusHandler() {}

    private static final double BONUS_PER_PRESTIGE = 5.0; // +5 flat attribute points per prestige level

    // Modifier IDs for each class-prestige combination
    private static final Identifier PALADIN_COMBAT_MOD = Identifier.fromNamespaceAndPath("megamod", "prestige_class.paladin_combat");
    private static final Identifier WARRIOR_COMBAT_MOD = Identifier.fromNamespaceAndPath("megamod", "prestige_class.warrior_combat");
    private static final Identifier WIZARD_ARCANE_MOD = Identifier.fromNamespaceAndPath("megamod", "prestige_class.wizard_arcane");
    private static final Identifier ROGUE_SURVIVAL_MOD = Identifier.fromNamespaceAndPath("megamod", "prestige_class.rogue_survival");
    private static final Identifier RANGER_SURVIVAL_MOD = Identifier.fromNamespaceAndPath("megamod", "prestige_class.ranger_survival");

    /**
     * Called when a player prestiges. Checks class + tree combination and
     * applies the class-specific bonus, then notifies the player.
     *
     * @param player the player who just prestiged
     * @param tree   the skill tree that was prestiged
     */
    public static void onPrestige(ServerPlayer player, SkillTreeType tree) {
        ServerLevel overworld = player.level().getServer().overworld();
        PlayerClassManager classManager = PlayerClassManager.get(overworld);
        PlayerClass cls = classManager.getPlayerClass(player.getUUID());
        if (cls == PlayerClass.NONE) return;

        String bonusName = null;

        switch (tree) {
            case COMBAT -> {
                if (cls == PlayerClass.PALADIN) {
                    bonusName = "+5% Healing Power";
                } else if (cls == PlayerClass.WARRIOR) {
                    bonusName = "+5% Critical Damage";
                }
            }
            case ARCANE -> {
                if (cls == PlayerClass.WIZARD) {
                    bonusName = "+5% Spell Haste";
                }
            }
            case SURVIVAL -> {
                if (cls == PlayerClass.ROGUE) {
                    bonusName = "+5% Dodge Chance";
                } else if (cls == PlayerClass.RANGER) {
                    bonusName = "+5% Ranged Damage";
                }
            }
            default -> { /* MINING, FARMING have no class-specific prestige bonuses */ }
        }

        if (bonusName != null) {
            // Reapply all accumulated bonuses (easier than tracking incremental)
            applyClassPrestigeBonuses(player);
            player.sendSystemMessage(Component.literal(
                "\u00A76\u2605 \u00A7eClass Prestige Bonus: " + bonusName + " (" + cls.getDisplayName() + ")")
                .withStyle(ChatFormatting.GOLD));
        }
    }

    /**
     * (Re)applies all class-specific prestige attribute modifiers for a player.
     * Should be called on login and after any prestige event to keep modifiers in sync.
     */
    public static void applyClassPrestigeBonuses(ServerPlayer player) {
        ServerLevel overworld = player.level().getServer().overworld();
        PlayerClassManager classManager = PlayerClassManager.get(overworld);
        PrestigeManager prestige = PrestigeManager.get(overworld);
        PlayerClass cls = classManager.getPlayerClass(player.getUUID());

        // Remove all class-prestige modifiers first (clean slate)
        AttributeHelper.removeModifier(player, MegaModAttributes.HEALING_POWER, PALADIN_COMBAT_MOD);
        AttributeHelper.removeModifier(player, MegaModAttributes.CRITICAL_DAMAGE, WARRIOR_COMBAT_MOD);
        AttributeHelper.removeModifier(player, MegaModAttributes.SPELL_HASTE, WIZARD_ARCANE_MOD);
        AttributeHelper.removeModifier(player, MegaModAttributes.DODGE_CHANCE, ROGUE_SURVIVAL_MOD);
        AttributeHelper.removeModifier(player, MegaModAttributes.RANGED_DAMAGE, RANGER_SURVIVAL_MOD);

        if (cls == PlayerClass.NONE) return;

        java.util.UUID uuid = player.getUUID();

        switch (cls) {
            case PALADIN -> {
                int combatPrestige = prestige.getPrestigeLevel(uuid, SkillTreeType.COMBAT);
                if (combatPrestige > 0) {
                    double bonus = combatPrestige * BONUS_PER_PRESTIGE;
                    AttributeHelper.addModifier(player, MegaModAttributes.HEALING_POWER, PALADIN_COMBAT_MOD,
                        bonus, AttributeModifier.Operation.ADD_VALUE);
                }
            }
            case WARRIOR -> {
                int combatPrestige = prestige.getPrestigeLevel(uuid, SkillTreeType.COMBAT);
                if (combatPrestige > 0) {
                    double bonus = combatPrestige * BONUS_PER_PRESTIGE;
                    AttributeHelper.addModifier(player, MegaModAttributes.CRITICAL_DAMAGE, WARRIOR_COMBAT_MOD,
                        bonus, AttributeModifier.Operation.ADD_VALUE);
                }
            }
            case WIZARD -> {
                int arcanePrestige = prestige.getPrestigeLevel(uuid, SkillTreeType.ARCANE);
                if (arcanePrestige > 0) {
                    double bonus = arcanePrestige * BONUS_PER_PRESTIGE;
                    AttributeHelper.addModifier(player, MegaModAttributes.SPELL_HASTE, WIZARD_ARCANE_MOD,
                        bonus, AttributeModifier.Operation.ADD_VALUE);
                }
            }
            case ROGUE -> {
                int survivalPrestige = prestige.getPrestigeLevel(uuid, SkillTreeType.SURVIVAL);
                if (survivalPrestige > 0) {
                    double bonus = survivalPrestige * BONUS_PER_PRESTIGE;
                    AttributeHelper.addModifier(player, MegaModAttributes.DODGE_CHANCE, ROGUE_SURVIVAL_MOD,
                        bonus, AttributeModifier.Operation.ADD_VALUE);
                }
            }
            case RANGER -> {
                int survivalPrestige = prestige.getPrestigeLevel(uuid, SkillTreeType.SURVIVAL);
                if (survivalPrestige > 0) {
                    double bonus = survivalPrestige * BONUS_PER_PRESTIGE;
                    AttributeHelper.addModifier(player, MegaModAttributes.RANGED_DAMAGE, RANGER_SURVIVAL_MOD,
                        bonus, AttributeModifier.Operation.ADD_VALUE);
                }
            }
            default -> {}
        }
    }
}

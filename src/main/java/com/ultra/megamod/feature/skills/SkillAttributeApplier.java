/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 */
package com.ultra.megamod.feature.skills;

import com.ultra.megamod.feature.attributes.AttributeHelper;
import com.ultra.megamod.feature.attributes.MegaModAttributes;
import com.ultra.megamod.feature.skills.SkillManager;
import com.ultra.megamod.feature.skills.SkillNode;
import com.ultra.megamod.feature.skills.SkillTreeDefinitions;
import com.ultra.megamod.feature.skills.SkillTreeType;
import com.ultra.megamod.feature.skills.prestige.PrestigeManager;
import com.ultra.megamod.feature.skills.synergy.SynergyManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class SkillAttributeApplier {
    private static final Map<String, Holder<Attribute>> ATTRIBUTE_MAP = new HashMap<String, Holder<Attribute>>();
    private static final Set<String> FLAT_STATS = Set.of(
        "attack_damage", "armor", "max_health",
        "health_regen_bonus", "fire_damage_bonus", "lightning_damage_bonus", "shadow_damage_bonus",
        "ice_damage_bonus", "poison_damage_bonus", "jump_height_bonus",
        "armor_shred", "stun_chance", "thorns_damage", "excavation_reach"
    );

    /** Last computed bonuses per player, keyed by UUID then attribute name → applied value.
     *  Used to send skill bonus data to clients for overlay display. */
    private static final Map<java.util.UUID, Map<String, Double>> lastComputedBonuses = new java.util.concurrent.ConcurrentHashMap<>();

    private SkillAttributeApplier() {
    }

    /** Get the last computed skill bonuses for a player (attribute name → applied value). */
    public static Map<String, Double> getComputedBonuses(java.util.UUID playerId) {
        return lastComputedBonuses.getOrDefault(playerId, Map.of());
    }

    private static final double[] DIMINISHING_FACTORS = {1.0, 0.75, 0.5, 0.25, 0.25};

    public static void recalculate(ServerPlayer player) {
        ServerLevel serverLevel = player.level();
        SkillManager manager = SkillManager.get(serverLevel);
        Set<String> unlockedNodes = manager.getUnlockedNodes(player.getUUID());

        // Phase 1: Group bonuses by attribute and by tree
        // Map<attributeName, Map<treeType, totalBonus>>
        Map<String, Map<SkillTreeType, Double>> attrByTree = new HashMap<>();
        for (String nodeId : unlockedNodes) {
            SkillNode node = SkillTreeDefinitions.getNodeById(nodeId);
            if (node == null) continue;
            SkillTreeType tree = node.branch().getTreeType();
            for (Map.Entry<String, Double> bonus : node.bonuses().entrySet()) {
                attrByTree.computeIfAbsent(bonus.getKey(), k -> new LinkedHashMap<>())
                    .merge(tree, bonus.getValue(), Double::sum);
            }
        }

        // Phase 2: Apply diminishing returns for cross-tree stacking
        HashMap<String, Double> totalBonuses = new HashMap<>();
        for (Map.Entry<String, Map<SkillTreeType, Double>> entry : attrByTree.entrySet()) {
            String attrName = entry.getKey();
            Map<SkillTreeType, Double> treeContribs = entry.getValue();
            if (treeContribs.size() <= 1) {
                // Single tree contributing — no diminishing
                double sum = 0;
                for (double v : treeContribs.values()) sum += v;
                totalBonuses.put(attrName, sum);
            } else {
                // Sort tree contributions by size (descending)
                List<Double> sorted = new ArrayList<>(treeContribs.values());
                sorted.sort((a, b) -> Double.compare(b, a));
                double total = 0;
                for (int i = 0; i < sorted.size(); i++) {
                    double factor = i < DIMINISHING_FACTORS.length ? DIMINISHING_FACTORS[i] : 0.25;
                    total += sorted.get(i) * factor;
                }
                totalBonuses.put(attrName, total);
            }
        }

        // Phase 3: Apply prestige bonus
        PrestigeManager prestige = PrestigeManager.get(serverLevel.getServer().overworld());
        for (SkillTreeType tree : SkillTreeType.values()) {
            double prestigeBonus = prestige.getPrestigeBonus(player.getUUID(), tree);
            if (prestigeBonus <= 0) continue;
            // Boost all attributes that come from this tree's nodes
            for (String nodeId : unlockedNodes) {
                SkillNode node = SkillTreeDefinitions.getNodeById(nodeId);
                if (node == null || node.branch().getTreeType() != tree) continue;
                for (Map.Entry<String, Double> bonus : node.bonuses().entrySet()) {
                    String attrName = bonus.getKey();
                    double currentTotal = totalBonuses.getOrDefault(attrName, 0.0);
                    // Add prestige percentage of the raw bonus
                    totalBonuses.put(attrName, currentTotal + bonus.getValue() * prestigeBonus);
                }
            }
        }

        // Phase 3.5: Merge cross-branch synergy bonuses
        Map<String, Double> synergyBonuses = SynergyManager.getSynergyBonuses(unlockedNodes);
        for (Map.Entry<String, Double> syn : synergyBonuses.entrySet()) {
            // Only merge bonuses that have a corresponding attribute mapping;
            // flag-type bonuses (first_hit_crit, ore_mining_haste, etc.) are
            // handled by SynergyEffects event handlers instead.
            if (ATTRIBUTE_MAP.containsKey(syn.getKey())) {
                totalBonuses.merge(syn.getKey(), syn.getValue(), Double::sum);
            }
        }

        // Phase 4: Passive Mastery — unspent points grant +0.5 to primary tree stat per point (capped at 10 pts = +5 max)
        for (SkillTreeType tree : SkillTreeType.values()) {
            int unspent = manager.getUnspentPoints(player.getUUID(), tree);
            if (unspent <= 0) continue;
            int effectiveUnspent = Math.min(unspent, 10); // cap at 10 unspent points
            double masteryBonus = effectiveUnspent * 0.5;
            String primaryStat = getPrimaryStatForTree(tree);
            if (primaryStat != null) {
                totalBonuses.merge(primaryStat, masteryBonus, Double::sum);
            }
        }

        // Store computed bonuses for client sync (overlay display)
        Map<String, Double> appliedValues = new HashMap<>();

        // Phase 5: Apply to attributes
        for (Map.Entry<String, Holder<Attribute>> entry : ATTRIBUTE_MAP.entrySet()) {
            AttributeModifier.Operation op;
            String attrName = entry.getKey();
            Holder<Attribute> holder = entry.getValue();
            Identifier modifierId = Identifier.fromNamespaceAndPath((String)"megamod", (String)("skill_" + attrName));
            AttributeHelper.removeModifier((LivingEntity)player, holder, modifierId);
            double value = totalBonuses.getOrDefault(attrName, 0.0);
            if (value == 0.0) continue;
            if ("movement_speed".equals(attrName)) {
                // Movement speed uses multiplied base since vanilla base is 0.1
                op = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                value /= 100.0;
            } else {
                // All other stats (including custom attributes with base 0.0) use flat ADD_VALUE.
                // ADD_MULTIPLIED_BASE would produce 0 for attributes with base 0.0.
                op = AttributeModifier.Operation.ADD_VALUE;
            }
            AttributeHelper.addModifier((LivingEntity)player, holder, modifierId, value, op);
            appliedValues.put(attrName, value);
        }
        lastComputedBonuses.put(player.getUUID(), appliedValues);
    }

    public static void removeAll(ServerPlayer player) {
        for (Map.Entry<String, Holder<Attribute>> entry : ATTRIBUTE_MAP.entrySet()) {
            Identifier modifierId = Identifier.fromNamespaceAndPath((String)"megamod", (String)("skill_" + entry.getKey()));
            AttributeHelper.removeModifier((LivingEntity)player, entry.getValue(), modifierId);
        }
    }

    private static String getPrimaryStatForTree(SkillTreeType tree) {
        return switch (tree) {
            case COMBAT -> "attack_damage";
            case MINING -> "mining_speed_bonus";
            case FARMING -> "farming_xp_bonus";
            case ARCANE -> "ability_power";
            case SURVIVAL -> "movement_speed";
        };
    }

    static {
        ATTRIBUTE_MAP.put("attack_damage", (Holder<Attribute>)Attributes.ATTACK_DAMAGE);
        ATTRIBUTE_MAP.put("armor", (Holder<Attribute>)Attributes.ARMOR);
        ATTRIBUTE_MAP.put("max_health", (Holder<Attribute>)Attributes.MAX_HEALTH);
        ATTRIBUTE_MAP.put("movement_speed", (Holder<Attribute>)Attributes.MOVEMENT_SPEED);
        ATTRIBUTE_MAP.put("critical_chance", (Holder<Attribute>)MegaModAttributes.CRITICAL_CHANCE);
        ATTRIBUTE_MAP.put("critical_damage", (Holder<Attribute>)MegaModAttributes.CRITICAL_DAMAGE);
        ATTRIBUTE_MAP.put("dodge_chance", (Holder<Attribute>)MegaModAttributes.DODGE_CHANCE);
        ATTRIBUTE_MAP.put("lifesteal", (Holder<Attribute>)MegaModAttributes.LIFESTEAL);
        ATTRIBUTE_MAP.put("combo_speed", (Holder<Attribute>)MegaModAttributes.COMBO_SPEED);
        ATTRIBUTE_MAP.put("mining_xp_bonus", (Holder<Attribute>)MegaModAttributes.MINING_XP_BONUS);
        ATTRIBUTE_MAP.put("mining_speed_bonus", (Holder<Attribute>)MegaModAttributes.MINING_SPEED_BONUS);
        ATTRIBUTE_MAP.put("loot_fortune", (Holder<Attribute>)MegaModAttributes.LOOT_FORTUNE);
        ATTRIBUTE_MAP.put("fall_damage_reduction", (Holder<Attribute>)MegaModAttributes.FALL_DAMAGE_REDUCTION);
        ATTRIBUTE_MAP.put("megacoin_bonus", (Holder<Attribute>)MegaModAttributes.MEGACOIN_BONUS);
        ATTRIBUTE_MAP.put("farming_xp_bonus", (Holder<Attribute>)MegaModAttributes.FARMING_XP_BONUS);
        ATTRIBUTE_MAP.put("xp_bonus", (Holder<Attribute>)MegaModAttributes.XP_BONUS);
        ATTRIBUTE_MAP.put("hunger_efficiency", (Holder<Attribute>)MegaModAttributes.HUNGER_EFFICIENCY);
        ATTRIBUTE_MAP.put("health_regen_bonus", (Holder<Attribute>)MegaModAttributes.HEALTH_REGEN_BONUS);
        ATTRIBUTE_MAP.put("ability_power", (Holder<Attribute>)MegaModAttributes.ABILITY_POWER);
        ATTRIBUTE_MAP.put("arcane_xp_bonus", (Holder<Attribute>)MegaModAttributes.ARCANE_XP_BONUS);
        ATTRIBUTE_MAP.put("cooldown_reduction", (Holder<Attribute>)MegaModAttributes.COOLDOWN_REDUCTION);
        ATTRIBUTE_MAP.put("fire_damage_bonus", (Holder<Attribute>)MegaModAttributes.FIRE_DAMAGE_BONUS);
        ATTRIBUTE_MAP.put("lightning_damage_bonus", (Holder<Attribute>)MegaModAttributes.LIGHTNING_DAMAGE_BONUS);
        ATTRIBUTE_MAP.put("shadow_damage_bonus", (Holder<Attribute>)MegaModAttributes.SHADOW_DAMAGE_BONUS);
        ATTRIBUTE_MAP.put("spell_range", (Holder<Attribute>)MegaModAttributes.SPELL_RANGE);
        ATTRIBUTE_MAP.put("survival_xp_bonus", (Holder<Attribute>)MegaModAttributes.SURVIVAL_XP_BONUS);
        ATTRIBUTE_MAP.put("combat_xp_bonus", (Holder<Attribute>)MegaModAttributes.COMBAT_XP_BONUS);
        ATTRIBUTE_MAP.put("sell_bonus", (Holder<Attribute>)MegaModAttributes.SELL_BONUS);
        // New unique secondaries
        ATTRIBUTE_MAP.put("armor_shred", (Holder<Attribute>)MegaModAttributes.ARMOR_SHRED);
        ATTRIBUTE_MAP.put("stun_chance", (Holder<Attribute>)MegaModAttributes.STUN_CHANCE);
        ATTRIBUTE_MAP.put("thorns_damage", (Holder<Attribute>)MegaModAttributes.THORNS_DAMAGE);
        ATTRIBUTE_MAP.put("vein_sense", (Holder<Attribute>)MegaModAttributes.VEIN_SENSE);
        ATTRIBUTE_MAP.put("excavation_reach", (Holder<Attribute>)MegaModAttributes.EXCAVATION_REACH);
        ATTRIBUTE_MAP.put("brilliance", (Holder<Attribute>)MegaModAttributes.BRILLIANCE);
        ATTRIBUTE_MAP.put("jump_height_bonus", (Holder<Attribute>)MegaModAttributes.JUMP_HEIGHT_BONUS);
        ATTRIBUTE_MAP.put("ice_resistance_bonus", (Holder<Attribute>)MegaModAttributes.ICE_RESISTANCE_BONUS);
        ATTRIBUTE_MAP.put("poison_resistance_bonus", (Holder<Attribute>)MegaModAttributes.POISON_RESISTANCE_BONUS);
        ATTRIBUTE_MAP.put("beast_affinity", (Holder<Attribute>)MegaModAttributes.BEAST_AFFINITY);
        ATTRIBUTE_MAP.put("poison_damage_bonus", (Holder<Attribute>)MegaModAttributes.POISON_DAMAGE_BONUS);
        ATTRIBUTE_MAP.put("swim_speed_bonus", (Holder<Attribute>)MegaModAttributes.SWIM_SPEED_BONUS);
        ATTRIBUTE_MAP.put("ice_damage_bonus", (Holder<Attribute>)MegaModAttributes.ICE_DAMAGE_BONUS);
        ATTRIBUTE_MAP.put("lightning_resistance_bonus", (Holder<Attribute>)MegaModAttributes.LIGHTNING_RESISTANCE_BONUS);
        ATTRIBUTE_MAP.put("shadow_resistance_bonus", (Holder<Attribute>)MegaModAttributes.SHADOW_RESISTANCE_BONUS);
        ATTRIBUTE_MAP.put("prey_sense", (Holder<Attribute>)MegaModAttributes.PREY_SENSE);
    }
}


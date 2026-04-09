/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.RangedAttribute
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package com.ultra.megamod.feature.attributes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MegaModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create((ResourceKey)Registries.ATTRIBUTE, (String)"megamod");
    public static final DeferredHolder<Attribute, Attribute> FIRE_DAMAGE_BONUS = ATTRIBUTES.register("fire_damage_bonus", () -> new RangedAttribute("attribute.megamod.fire_damage_bonus", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> ICE_DAMAGE_BONUS = ATTRIBUTES.register("ice_damage_bonus", () -> new RangedAttribute("attribute.megamod.ice_damage_bonus", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> LIGHTNING_DAMAGE_BONUS = ATTRIBUTES.register("lightning_damage_bonus", () -> new RangedAttribute("attribute.megamod.lightning_damage_bonus", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> POISON_DAMAGE_BONUS = ATTRIBUTES.register("poison_damage_bonus", () -> new RangedAttribute("attribute.megamod.poison_damage_bonus", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> HOLY_DAMAGE_BONUS = ATTRIBUTES.register("holy_damage_bonus", () -> new RangedAttribute("attribute.megamod.holy_damage_bonus", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> SHADOW_DAMAGE_BONUS = ATTRIBUTES.register("shadow_damage_bonus", () -> new RangedAttribute("attribute.megamod.shadow_damage_bonus", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> CRITICAL_DAMAGE = ATTRIBUTES.register("critical_damage", () -> new RangedAttribute("attribute.megamod.critical_damage", 50.0, 0.0, 500.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> CRITICAL_CHANCE = ATTRIBUTES.register("critical_chance", () -> new RangedAttribute("attribute.megamod.critical_chance", 5.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> FIRE_RESISTANCE_BONUS = ATTRIBUTES.register("fire_resistance_bonus", () -> new RangedAttribute("attribute.megamod.fire_resistance_bonus", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> ICE_RESISTANCE_BONUS = ATTRIBUTES.register("ice_resistance_bonus", () -> new RangedAttribute("attribute.megamod.ice_resistance_bonus", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> LIGHTNING_RESISTANCE_BONUS = ATTRIBUTES.register("lightning_resistance_bonus", () -> new RangedAttribute("attribute.megamod.lightning_resistance_bonus", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> POISON_RESISTANCE_BONUS = ATTRIBUTES.register("poison_resistance_bonus", () -> new RangedAttribute("attribute.megamod.poison_resistance_bonus", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> HOLY_RESISTANCE_BONUS = ATTRIBUTES.register("holy_resistance_bonus", () -> new RangedAttribute("attribute.megamod.holy_resistance_bonus", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> SHADOW_RESISTANCE_BONUS = ATTRIBUTES.register("shadow_resistance_bonus", () -> new RangedAttribute("attribute.megamod.shadow_resistance_bonus", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> MINING_SPEED_BONUS = ATTRIBUTES.register("mining_speed_bonus", () -> new RangedAttribute("attribute.megamod.mining_speed_bonus", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> SWIM_SPEED_BONUS = ATTRIBUTES.register("swim_speed_bonus", () -> new RangedAttribute("attribute.megamod.swim_speed_bonus", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> DODGE_CHANCE = ATTRIBUTES.register("dodge_chance", () -> new RangedAttribute("attribute.megamod.dodge_chance", 0.0, 0.0, 75.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> JUMP_HEIGHT_BONUS = ATTRIBUTES.register("jump_height_bonus", () -> new RangedAttribute("attribute.megamod.jump_height_bonus", 0.0, 0.0, 10.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> FALL_DAMAGE_REDUCTION = ATTRIBUTES.register("fall_damage_reduction", () -> new RangedAttribute("attribute.megamod.fall_damage_reduction", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> HEALTH_REGEN_BONUS = ATTRIBUTES.register("health_regen_bonus", () -> new RangedAttribute("attribute.megamod.health_regen_bonus", 0.0, 0.0, 20.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> HUNGER_EFFICIENCY = ATTRIBUTES.register("hunger_efficiency", () -> new RangedAttribute("attribute.megamod.hunger_efficiency", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> XP_BONUS = ATTRIBUTES.register("xp_bonus", () -> new RangedAttribute("attribute.megamod.xp_bonus", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> LOOT_FORTUNE = ATTRIBUTES.register("loot_fortune", () -> new RangedAttribute("attribute.megamod.loot_fortune", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> MEGACOIN_BONUS = ATTRIBUTES.register("megacoin_bonus", () -> new RangedAttribute("attribute.megamod.megacoin_bonus", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> SHOP_DISCOUNT = ATTRIBUTES.register("shop_discount", () -> new RangedAttribute("attribute.megamod.shop_discount", 0.0, 0.0, 50.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> SELL_BONUS = ATTRIBUTES.register("sell_bonus", () -> new RangedAttribute("attribute.megamod.sell_bonus", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> LIFESTEAL = ATTRIBUTES.register("lifesteal", () -> new RangedAttribute("attribute.megamod.lifesteal", 0.0, 0.0, 50.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> THORNS_DAMAGE = ATTRIBUTES.register("thorns_damage", () -> new RangedAttribute("attribute.megamod.thorns_damage", 0.0, 0.0, 50.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> ARMOR_SHRED = ATTRIBUTES.register("armor_shred", () -> new RangedAttribute("attribute.megamod.armor_shred", 0.0, 0.0, 50.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> COMBO_SPEED = ATTRIBUTES.register("combo_speed", () -> new RangedAttribute("attribute.megamod.combo_speed", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> STUN_CHANCE = ATTRIBUTES.register("stun_chance", () -> new RangedAttribute("attribute.megamod.stun_chance", 0.0, 0.0, 30.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> COOLDOWN_REDUCTION = ATTRIBUTES.register("cooldown_reduction", () -> new RangedAttribute("attribute.megamod.cooldown_reduction", 0.0, 0.0, 75.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> ABILITY_POWER = ATTRIBUTES.register("ability_power", () -> new RangedAttribute("attribute.megamod.ability_power", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> MANA_EFFICIENCY = ATTRIBUTES.register("mana_efficiency", () -> new RangedAttribute("attribute.megamod.mana_efficiency", 0.0, 0.0, 50.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> SPELL_RANGE = ATTRIBUTES.register("spell_range", () -> new RangedAttribute("attribute.megamod.spell_range", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> COMBAT_XP_BONUS = ATTRIBUTES.register("combat_xp_bonus", () -> new RangedAttribute("attribute.megamod.combat_xp_bonus", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> MINING_XP_BONUS = ATTRIBUTES.register("mining_xp_bonus", () -> new RangedAttribute("attribute.megamod.mining_xp_bonus", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> FARMING_XP_BONUS = ATTRIBUTES.register("farming_xp_bonus", () -> new RangedAttribute("attribute.megamod.farming_xp_bonus", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> ARCANE_XP_BONUS = ATTRIBUTES.register("arcane_xp_bonus", () -> new RangedAttribute("attribute.megamod.arcane_xp_bonus", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> SURVIVAL_XP_BONUS = ATTRIBUTES.register("survival_xp_bonus", () -> new RangedAttribute("attribute.megamod.survival_xp_bonus", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> BRILLIANCE = ATTRIBUTES.register("brilliance", () -> new RangedAttribute("attribute.megamod.brilliance", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> BEAST_AFFINITY = ATTRIBUTES.register("beast_affinity", () -> new RangedAttribute("attribute.megamod.beast_affinity", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> PREY_SENSE = ATTRIBUTES.register("prey_sense", () -> new RangedAttribute("attribute.megamod.prey_sense", 0.0, 0.0, 50.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> VEIN_SENSE = ATTRIBUTES.register("vein_sense", () -> new RangedAttribute("attribute.megamod.vein_sense", 0.0, 0.0, 50.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> EXCAVATION_REACH = ATTRIBUTES.register("excavation_reach", () -> new RangedAttribute("attribute.megamod.excavation_reach", 0.0, 0.0, 5.0).setSyncable(true));

    // ─── Spell Power attributes (from SpellPower/SpellEngine port) ───
    public static final DeferredHolder<Attribute, Attribute> ARCANE_POWER = ATTRIBUTES.register("arcane_power", () -> new RangedAttribute("attribute.megamod.arcane_power", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> HEALING_POWER = ATTRIBUTES.register("healing_power", () -> new RangedAttribute("attribute.megamod.healing_power", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> SOUL_POWER = ATTRIBUTES.register("soul_power", () -> new RangedAttribute("attribute.megamod.soul_power", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> SPELL_HASTE = ATTRIBUTES.register("spell_haste", () -> new RangedAttribute("attribute.megamod.spell_haste", 0.0, 0.0, 100.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> RANGED_DAMAGE = ATTRIBUTES.register("ranged_damage", () -> new RangedAttribute("attribute.megamod.ranged_damage", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> HOLY_POWER = ATTRIBUTES.register("holy_power", () -> new RangedAttribute("attribute.megamod.holy_power", 0.0, 0.0, 200.0).setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> POISON_POWER = ATTRIBUTES.register("poison_power", () -> new RangedAttribute("attribute.megamod.poison_power", 0.0, 0.0, 200.0).setSyncable(true));

    /** All custom attribute holders for bulk registration on player entities. */
    private static final DeferredHolder<Attribute, Attribute>[] ALL_CUSTOM = new DeferredHolder[]{
        FIRE_DAMAGE_BONUS, ICE_DAMAGE_BONUS, LIGHTNING_DAMAGE_BONUS, POISON_DAMAGE_BONUS,
        HOLY_DAMAGE_BONUS, SHADOW_DAMAGE_BONUS, CRITICAL_DAMAGE, CRITICAL_CHANCE,
        FIRE_RESISTANCE_BONUS, ICE_RESISTANCE_BONUS, LIGHTNING_RESISTANCE_BONUS,
        POISON_RESISTANCE_BONUS, HOLY_RESISTANCE_BONUS, SHADOW_RESISTANCE_BONUS,
        MINING_SPEED_BONUS, SWIM_SPEED_BONUS, DODGE_CHANCE, JUMP_HEIGHT_BONUS,
        FALL_DAMAGE_REDUCTION, HEALTH_REGEN_BONUS, HUNGER_EFFICIENCY, XP_BONUS,
        LOOT_FORTUNE, MEGACOIN_BONUS, SHOP_DISCOUNT, SELL_BONUS, LIFESTEAL,
        THORNS_DAMAGE, ARMOR_SHRED, COMBO_SPEED, STUN_CHANCE, COOLDOWN_REDUCTION,
        ABILITY_POWER, MANA_EFFICIENCY, SPELL_RANGE, COMBAT_XP_BONUS, MINING_XP_BONUS,
        FARMING_XP_BONUS, ARCANE_XP_BONUS, SURVIVAL_XP_BONUS,
        BRILLIANCE, BEAST_AFFINITY, PREY_SENSE, VEIN_SENSE, EXCAVATION_REACH,
        ARCANE_POWER, HEALING_POWER, SOUL_POWER, SPELL_HASTE, RANGED_DAMAGE,
        HOLY_POWER, POISON_POWER
    };

    public static void init(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
        modBus.addListener(MegaModAttributes::onAttributeModification);
    }

    /**
     * Register all custom attributes on Player entities so getAttribute() returns
     * a valid instance and modifiers can be applied/synced.
     */
    private static void onAttributeModification(EntityAttributeModificationEvent event) {
        for (DeferredHolder<Attribute, Attribute> holder : ALL_CUSTOM) {
            if (!event.has(EntityType.PLAYER, holder)) {
                event.add(EntityType.PLAYER, holder);
            }
        }
    }
}


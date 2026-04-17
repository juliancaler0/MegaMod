package com.ultra.megamod.lib.puffish_attributes.api;

import com.ultra.megamod.lib.puffish_attributes.util.DeferredSetup;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class PuffishAttributes {
	private PuffishAttributes() { }

	public static final Identifier STAMINA_ID = DeferredSetup.createIdentifier("stamina");
	public static final Holder<Attribute> STAMINA = DeferredSetup.registerAttribute(STAMINA_ID, DeferredSetup.createClampedAttribute(STAMINA_ID, 4.0, 0.0, 1024.0).setSyncable(true));

	public static final Identifier MAGIC_DAMAGE_ID = DeferredSetup.createIdentifier("magic_damage");
	public static final Holder<Attribute> MAGIC_DAMAGE = DeferredSetup.registerAttribute(MAGIC_DAMAGE_ID, DeferredSetup.createDynamicAttribute(MAGIC_DAMAGE_ID).setSyncable(true));

	public static final Identifier MELEE_DAMAGE_ID = DeferredSetup.createIdentifier("melee_damage");
	public static final Holder<Attribute> MELEE_DAMAGE = DeferredSetup.registerAttribute(MELEE_DAMAGE_ID, DeferredSetup.createDynamicAttribute(MELEE_DAMAGE_ID).setSyncable(true));

	public static final Identifier RANGED_DAMAGE_ID = DeferredSetup.createIdentifier("ranged_damage");
	public static final Holder<Attribute> RANGED_DAMAGE = DeferredSetup.registerAttribute(RANGED_DAMAGE_ID, DeferredSetup.createDynamicAttribute(RANGED_DAMAGE_ID).setSyncable(true));

	public static final Identifier TAMED_DAMAGE_ID = DeferredSetup.createIdentifier("tamed_damage");
	public static final Holder<Attribute> TAMED_DAMAGE = DeferredSetup.registerAttribute(TAMED_DAMAGE_ID, DeferredSetup.createDynamicAttribute(TAMED_DAMAGE_ID).setSyncable(true));

	public static final Identifier SWORD_DAMAGE_ID = DeferredSetup.createIdentifier("sword_damage");
	public static final Holder<Attribute> SWORD_DAMAGE = DeferredSetup.registerAttribute(SWORD_DAMAGE_ID, DeferredSetup.createDynamicAttribute(SWORD_DAMAGE_ID).setSyncable(true));

	public static final Identifier AXE_DAMAGE_ID = DeferredSetup.createIdentifier("axe_damage");
	public static final Holder<Attribute> AXE_DAMAGE = DeferredSetup.registerAttribute(AXE_DAMAGE_ID, DeferredSetup.createDynamicAttribute(AXE_DAMAGE_ID).setSyncable(true));

	public static final Identifier TRIDENT_DAMAGE_ID = DeferredSetup.createIdentifier("trident_damage");
	public static final Holder<Attribute> TRIDENT_DAMAGE = DeferredSetup.registerAttribute(TRIDENT_DAMAGE_ID, DeferredSetup.createDynamicAttribute(TRIDENT_DAMAGE_ID).setSyncable(true));

	public static final Identifier MACE_DAMAGE_ID = DeferredSetup.createIdentifier("mace_damage");
	public static final Holder<Attribute> MACE_DAMAGE = DeferredSetup.registerAttribute(MACE_DAMAGE_ID, DeferredSetup.createDynamicAttribute(MACE_DAMAGE_ID).setSyncable(true));

	public static final Identifier FORTUNE_ID = DeferredSetup.createIdentifier("fortune");
	public static final Holder<Attribute> FORTUNE = DeferredSetup.registerAttribute(FORTUNE_ID, DeferredSetup.createDynamicAttribute(FORTUNE_ID).setSyncable(true));

	public static final Identifier HEALING_ID = DeferredSetup.createIdentifier("healing");
	public static final Holder<Attribute> HEALING = DeferredSetup.registerAttribute(HEALING_ID, DeferredSetup.createDynamicAttribute(HEALING_ID).setSyncable(true));

	public static final Identifier JUMP_ID = DeferredSetup.createIdentifier("jump");
	public static final Holder<Attribute> JUMP = DeferredSetup.registerAttribute(JUMP_ID, DeferredSetup.createDynamicAttribute(JUMP_ID).setSyncable(true));

	public static final Identifier RESISTANCE_ID = DeferredSetup.createIdentifier("resistance");
	public static final Holder<Attribute> RESISTANCE = DeferredSetup.registerAttribute(RESISTANCE_ID, DeferredSetup.createDynamicAttribute(RESISTANCE_ID).setSyncable(true));

	public static final Identifier MAGIC_RESISTANCE_ID = DeferredSetup.createIdentifier("magic_resistance");
	public static final Holder<Attribute> MAGIC_RESISTANCE = DeferredSetup.registerAttribute(MAGIC_RESISTANCE_ID, DeferredSetup.createDynamicAttribute(MAGIC_RESISTANCE_ID).setSyncable(true));

	public static final Identifier MELEE_RESISTANCE_ID = DeferredSetup.createIdentifier("melee_resistance");
	public static final Holder<Attribute> MELEE_RESISTANCE = DeferredSetup.registerAttribute(MELEE_RESISTANCE_ID, DeferredSetup.createDynamicAttribute(MELEE_RESISTANCE_ID).setSyncable(true));

	public static final Identifier RANGED_RESISTANCE_ID = DeferredSetup.createIdentifier("ranged_resistance");
	public static final Holder<Attribute> RANGED_RESISTANCE = DeferredSetup.registerAttribute(RANGED_RESISTANCE_ID, DeferredSetup.createDynamicAttribute(RANGED_RESISTANCE_ID).setSyncable(true));

	public static final Identifier TAMED_RESISTANCE_ID = DeferredSetup.createIdentifier("tamed_resistance");
	public static final Holder<Attribute> TAMED_RESISTANCE = DeferredSetup.registerAttribute(TAMED_RESISTANCE_ID, DeferredSetup.createDynamicAttribute(TAMED_RESISTANCE_ID).setSyncable(true));

	public static final Identifier MINING_SPEED_ID = DeferredSetup.createIdentifier("mining_speed");
	public static final Holder<Attribute> MINING_SPEED = DeferredSetup.registerAttribute(MINING_SPEED_ID, DeferredSetup.createDynamicAttribute(MINING_SPEED_ID).setSyncable(true));

	public static final Identifier BREAKING_SPEED_ID = DeferredSetup.createIdentifier("breaking_speed");
	public static final Holder<Attribute> BREAKING_SPEED = DeferredSetup.registerAttribute(BREAKING_SPEED_ID, DeferredSetup.createDynamicAttribute(BREAKING_SPEED_ID).setSyncable(true));

	public static final Identifier PICKAXE_SPEED_ID = DeferredSetup.createIdentifier("pickaxe_speed");
	public static final Holder<Attribute> PICKAXE_SPEED = DeferredSetup.registerAttribute(PICKAXE_SPEED_ID, DeferredSetup.createDynamicAttribute(PICKAXE_SPEED_ID).setSyncable(true));

	public static final Identifier AXE_SPEED_ID = DeferredSetup.createIdentifier("axe_speed");
	public static final Holder<Attribute> AXE_SPEED = DeferredSetup.registerAttribute(AXE_SPEED_ID, DeferredSetup.createDynamicAttribute(AXE_SPEED_ID).setSyncable(true));

	public static final Identifier SHOVEL_SPEED_ID = DeferredSetup.createIdentifier("shovel_speed");
	public static final Holder<Attribute> SHOVEL_SPEED = DeferredSetup.registerAttribute(SHOVEL_SPEED_ID, DeferredSetup.createDynamicAttribute(SHOVEL_SPEED_ID).setSyncable(true));

	public static final Identifier SPRINTING_SPEED_ID = DeferredSetup.createIdentifier("sprinting_speed");
	public static final Holder<Attribute> SPRINTING_SPEED = DeferredSetup.registerAttribute(SPRINTING_SPEED_ID, DeferredSetup.createDynamicAttribute(SPRINTING_SPEED_ID).setSyncable(true));

	public static final Identifier MOUNT_SPEED_ID = DeferredSetup.createIdentifier("mount_speed");
	public static final Holder<Attribute> MOUNT_SPEED = DeferredSetup.registerAttribute(MOUNT_SPEED_ID, DeferredSetup.createDynamicAttribute(MOUNT_SPEED_ID).setSyncable(true));

	public static final Identifier CONSUMING_SPEED_ID = DeferredSetup.createIdentifier("consuming_speed");
	public static final Holder<Attribute> CONSUMING_SPEED = DeferredSetup.registerAttribute(CONSUMING_SPEED_ID, DeferredSetup.createDynamicAttribute(CONSUMING_SPEED_ID).setSyncable(true));

	public static final Identifier KNOCKBACK_ID = DeferredSetup.createIdentifier("knockback");
	public static final Holder<Attribute> KNOCKBACK = DeferredSetup.registerAttribute(KNOCKBACK_ID, DeferredSetup.createDynamicAttribute(KNOCKBACK_ID).setSyncable(true));

	public static final Identifier REPAIR_COST_ID = DeferredSetup.createIdentifier("repair_cost");
	public static final Holder<Attribute> REPAIR_COST = DeferredSetup.registerAttribute(REPAIR_COST_ID, DeferredSetup.createDynamicAttribute(REPAIR_COST_ID).setSyncable(true));

	public static final Identifier ARMOR_SHRED_ID = DeferredSetup.createIdentifier("armor_shred");
	public static final Holder<Attribute> ARMOR_SHRED = DeferredSetup.registerAttribute(ARMOR_SHRED_ID, DeferredSetup.createDynamicAttribute(ARMOR_SHRED_ID).setSyncable(true));

	public static final Identifier TOUGHNESS_SHRED_ID = DeferredSetup.createIdentifier("toughness_shred");
	public static final Holder<Attribute> TOUGHNESS_SHRED = DeferredSetup.registerAttribute(TOUGHNESS_SHRED_ID, DeferredSetup.createDynamicAttribute(TOUGHNESS_SHRED_ID).setSyncable(true));

	public static final Identifier PROTECTION_SHRED_ID = DeferredSetup.createIdentifier("protection_shred");
	public static final Holder<Attribute> PROTECTION_SHRED = DeferredSetup.registerAttribute(PROTECTION_SHRED_ID, DeferredSetup.createDynamicAttribute(PROTECTION_SHRED_ID).setSyncable(true));

	public static final Identifier RESISTANCE_SHRED_ID = DeferredSetup.createIdentifier("resistance_shred");
	public static final Holder<Attribute> RESISTANCE_SHRED = DeferredSetup.registerAttribute(RESISTANCE_SHRED_ID, DeferredSetup.createDynamicAttribute(RESISTANCE_SHRED_ID).setSyncable(true));

	public static final Identifier MAGIC_RESISTANCE_SHRED_ID = DeferredSetup.createIdentifier("magic_resistance_shred");
	public static final Holder<Attribute> MAGIC_RESISTANCE_SHRED = DeferredSetup.registerAttribute(MAGIC_RESISTANCE_SHRED_ID, DeferredSetup.createDynamicAttribute(MAGIC_RESISTANCE_SHRED_ID).setSyncable(true));

	public static final Identifier MELEE_RESISTANCE_SHRED_ID = DeferredSetup.createIdentifier("melee_resistance_shred");
	public static final Holder<Attribute> MELEE_RESISTANCE_SHRED = DeferredSetup.registerAttribute(MELEE_RESISTANCE_SHRED_ID, DeferredSetup.createDynamicAttribute(MELEE_RESISTANCE_SHRED_ID).setSyncable(true));

	public static final Identifier RANGED_RESISTANCE_SHRED_ID = DeferredSetup.createIdentifier("ranged_resistance_shred");
	public static final Holder<Attribute> RANGED_RESISTANCE_SHRED = DeferredSetup.registerAttribute(RANGED_RESISTANCE_SHRED_ID, DeferredSetup.createDynamicAttribute(RANGED_RESISTANCE_SHRED_ID).setSyncable(true));

	public static final Identifier NATURAL_REGENERATION_ID = DeferredSetup.createIdentifier("natural_regeneration");
	public static final Holder<Attribute> NATURAL_REGENERATION = DeferredSetup.registerAttribute(NATURAL_REGENERATION_ID, DeferredSetup.createDynamicAttribute(NATURAL_REGENERATION_ID).setSyncable(true));

	public static final Identifier DAMAGE_REFLECTION_ID = DeferredSetup.createIdentifier("damage_reflection");
	public static final Holder<Attribute> DAMAGE_REFLECTION = DeferredSetup.registerAttribute(DAMAGE_REFLECTION_ID, DeferredSetup.createDynamicAttribute(DAMAGE_REFLECTION_ID).setSyncable(true));

	public static final Identifier STEALTH_ID = DeferredSetup.createIdentifier("stealth");
	public static final Holder<Attribute> STEALTH = DeferredSetup.registerAttribute(STEALTH_ID, DeferredSetup.createDynamicAttribute(STEALTH_ID).setSyncable(true));

	public static final Identifier LIFE_STEAL_ID = DeferredSetup.createIdentifier("life_steal");
	public static final Holder<Attribute> LIFE_STEAL = DeferredSetup.registerAttribute(LIFE_STEAL_ID, DeferredSetup.createDynamicAttribute(LIFE_STEAL_ID).setSyncable(true));

	public static final Identifier FALL_REDUCTION_ID = DeferredSetup.createIdentifier("fall_reduction");
	public static final Holder<Attribute> FALL_REDUCTION = DeferredSetup.registerAttribute(FALL_REDUCTION_ID, DeferredSetup.createDynamicAttribute(FALL_REDUCTION_ID).setSyncable(true));

	public static final Identifier BOW_PROJECTILE_SPEED_ID = DeferredSetup.createIdentifier("bow_projectile_speed");
	public static final Holder<Attribute> BOW_PROJECTILE_SPEED = DeferredSetup.registerAttribute(BOW_PROJECTILE_SPEED_ID, DeferredSetup.createDynamicAttribute(BOW_PROJECTILE_SPEED_ID).setSyncable(true));

	public static final Identifier CROSSBOW_PROJECTILE_SPEED_ID = DeferredSetup.createIdentifier("crossbow_projectile_speed");
	public static final Holder<Attribute> CROSSBOW_PROJECTILE_SPEED = DeferredSetup.registerAttribute(CROSSBOW_PROJECTILE_SPEED_ID, DeferredSetup.createDynamicAttribute(CROSSBOW_PROJECTILE_SPEED_ID).setSyncable(true));

	public static final Identifier EXPERIENCE_ID = DeferredSetup.createIdentifier("experience");
	public static final Holder<Attribute> EXPERIENCE = DeferredSetup.registerAttribute(EXPERIENCE_ID, DeferredSetup.createDynamicAttribute(EXPERIENCE_ID).setSyncable(true));
}

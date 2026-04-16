package com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationFactory;

import java.util.Optional;

public final class BuiltinPrototypes {
	private BuiltinPrototypes() { }

	public static final Prototype<Double> NUMBER = Prototype.create(SkillsMod.createIdentifier("number"));
	public static final Prototype<Boolean> BOOLEAN = Prototype.create(SkillsMod.createIdentifier("boolean"));
	public static final Prototype<MinecraftServer> SERVER = Prototype.create(SkillsMod.createIdentifier("server"));
	public static final Prototype<ServerLevel> WORLD = Prototype.create(SkillsMod.createIdentifier("world"));
	public static final Prototype<EntityType<?>> ENTITY_TYPE = Prototype.create(SkillsMod.createIdentifier("entity_type"));
	public static final Prototype<Entity> ENTITY = Prototype.create(SkillsMod.createIdentifier("entity"));
	public static final Prototype<LivingEntity> LIVING_ENTITY = Prototype.create(SkillsMod.createIdentifier("living_entity"));
	public static final Prototype<ServerPlayer> PLAYER = Prototype.create(SkillsMod.createIdentifier("player"));
	public static final Prototype<Item> ITEM = Prototype.create(SkillsMod.createIdentifier("item"));
	public static final Prototype<ItemStack> ITEM_STACK = Prototype.create(SkillsMod.createIdentifier("item_stack"));
	public static final Prototype<Block> BLOCK = Prototype.create(SkillsMod.createIdentifier("block"));
	public static final Prototype<BlockState> BLOCK_STATE = Prototype.create(SkillsMod.createIdentifier("block_state"));
	public static final Prototype<DamageType> DAMAGE_TYPE = Prototype.create(SkillsMod.createIdentifier("damage_type"));
	public static final Prototype<DamageSource> DAMAGE_SOURCE = Prototype.create(SkillsMod.createIdentifier("damage_source"));
	public static final Prototype<StatType<?>> STAT_TYPE = Prototype.create(SkillsMod.createIdentifier("stat_type"));
	public static final Prototype<Stat<?>> STAT = Prototype.create(SkillsMod.createIdentifier("stat"));
	public static final Prototype<MobEffectInstance> STATUS_EFFECT_INSTANCE = Prototype.create(SkillsMod.createIdentifier("status_effect_instance"));
	public static final Prototype<AttributeInstance> ENTITY_ATTRIBUTE_INSTANCE = Prototype.create(SkillsMod.createIdentifier("entity_attribute_instance"));

	static {
		WORLD.registerOperation(
				Identifier.parse("get_server"),
				SERVER,
				OperationFactory.create(ServerLevel::getServer)
		);
		WORLD.registerOperation(
				Identifier.parse("get_time_of_day"),
				NUMBER,
				OperationFactory.create(world -> (double) world.getDayTime())
		);

		ENTITY.registerOperation(
				Identifier.parse("get_type"),
				ENTITY_TYPE,
				OperationFactory.create(Entity::getType)
		);
		ENTITY.registerOperation(
				Identifier.parse("get_world"),
				WORLD,
				OperationFactory.create(entity -> (ServerLevel) entity.level())
		);

		LIVING_ENTITY.registerOperation(
				Identifier.parse("as_entity"),
				ENTITY,
				OperationFactory.create(p -> p)
		);
		LIVING_ENTITY.registerOperation(
				Identifier.parse("get_world"),
				WORLD,
				OperationFactory.create(livingEntity -> (ServerLevel) livingEntity.level())
		);
		LIVING_ENTITY.registerOperation(
				Identifier.parse("get_type"),
				ENTITY_TYPE,
				OperationFactory.create(Entity::getType)
		);
		LIVING_ENTITY.registerOperation(
				Identifier.parse("get_max_health"),
				NUMBER,
				OperationFactory.create(livingEntity -> (double) livingEntity.getMaxHealth())
		);
		LIVING_ENTITY.registerOperation(
				Identifier.parse("get_health"),
				NUMBER,
				OperationFactory.create(livingEntity -> (double) livingEntity.getHealth())
		);

		PLAYER.registerOperation(
				Identifier.parse("as_living_entity"),
				LIVING_ENTITY,
				OperationFactory.create(p -> p)
		);
		PLAYER.registerOperation(
				Identifier.parse("as_entity"),
				ENTITY,
				OperationFactory.create(p -> p)
		);
		PLAYER.registerOperation(
				Identifier.parse("get_world"),
				WORLD,
				OperationFactory.create(player -> (ServerLevel) player.level())
		);

		ITEM.registerOperation(
				Identifier.parse("get_saturation_modifier"),
				NUMBER,
				OperationFactory.create(item -> {
					var fc = item.getComponents().get(DataComponents.FOOD);
					return fc == null ? 0.0 : fc.saturation();
				})
		);
		ITEM.registerOperation(
				Identifier.parse("get_nutrition"),
				NUMBER,
				OperationFactory.create(item -> {
					var fc = item.getComponents().get(DataComponents.FOOD);
					return fc == null ? 0.0 : fc.nutrition();
				})
		);

		ITEM_STACK.registerOperation(
				Identifier.parse("get_item"),
				ITEM,
				OperationFactory.create(ItemStack::getItem)
		);
		ITEM_STACK.registerOperation(
				Identifier.parse("get_count"),
				NUMBER,
				OperationFactory.create(itemStack -> (double) itemStack.getCount())
		);

		BLOCK.registerOperation(
				Identifier.parse("get_hardness"),
				NUMBER,
				OperationFactory.create(block -> (double) block.defaultDestroyTime())
		);
		BLOCK.registerOperation(
				Identifier.parse("get_blast_resistance"),
				NUMBER,
				OperationFactory.create(block -> (double) block.getExplosionResistance())
		);

		BLOCK_STATE.registerOperation(
				Identifier.parse("get_block"),
				BLOCK,
				OperationFactory.create(BlockState::getBlock)
		);

		DAMAGE_SOURCE.registerOperation(
				Identifier.parse("get_type"),
				DAMAGE_TYPE,
				OperationFactory.create(DamageSource::getType)
		);
		DAMAGE_SOURCE.registerOperation(
				Identifier.parse("get_attacker"),
				ENTITY,
				OperationFactory.createOptional(damageSource -> Optional.ofNullable(damageSource.getEntity()))
		);
		DAMAGE_SOURCE.registerOperation(
				Identifier.parse("get_source"),
				ENTITY,
				OperationFactory.createOptional(damageSource -> Optional.ofNullable(damageSource.getSource()))
		);

		STAT.registerOperation(
				Identifier.parse("get_type"),
				STAT_TYPE,
				OperationFactory.create(Stat::getType)
		);

		STATUS_EFFECT_INSTANCE.registerOperation(
				Identifier.parse("get_level"),
				NUMBER,
				OperationFactory.create(effect -> (double) (effect.getAmplifier() + 1))
		);
		STATUS_EFFECT_INSTANCE.registerOperation(
				Identifier.parse("get_duration"),
				NUMBER,
				OperationFactory.create(effect -> (double) effect.getDuration())
		);

		ENTITY_ATTRIBUTE_INSTANCE.registerOperation(
				Identifier.parse("get_value"),
				NUMBER,
				OperationFactory.create(AttributeInstance::getValue)
		);
		ENTITY_ATTRIBUTE_INSTANCE.registerOperation(
				Identifier.parse("get_base_value"),
				NUMBER,
				OperationFactory.create(AttributeInstance::getBaseValue)
		);
	}
}

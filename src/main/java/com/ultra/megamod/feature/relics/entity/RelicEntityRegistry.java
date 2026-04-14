package com.ultra.megamod.feature.relics.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RelicEntityRegistry
{
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
		DeferredRegister.create(Registries.ENTITY_TYPE, "megamod");

	public static final DeferredHolder<EntityType<?>, EntityType<ShadowGlaiveEntity>> SHADOW_GLAIVE =
		ENTITY_TYPES.register("relic_shadow_glaive", () -> EntityType.Builder.<ShadowGlaiveEntity>of(ShadowGlaiveEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(1)
			.build(entityKey("relic_shadow_glaive")));

	public static final DeferredHolder<EntityType<?>, EntityType<ShadowSawEntity>> SHADOW_SAW =
		ENTITY_TYPES.register("relic_shadow_saw", () -> EntityType.Builder.<ShadowSawEntity>of(ShadowSawEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(1)
			.build(entityKey("relic_shadow_saw")));

	public static final DeferredHolder<EntityType<?>, EntityType<SporeEntity>> SPORE =
		ENTITY_TYPES.register("relic_spore", () -> EntityType.Builder.<SporeEntity>of(SporeEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
			.build(entityKey("relic_spore")));

	public static final DeferredHolder<EntityType<?>, EntityType<StalactiteEntity>> STALACTITE =
		ENTITY_TYPES.register("relic_stalactite", () -> EntityType.Builder.<StalactiteEntity>of(StalactiteEntity::new, MobCategory.MISC)
			.sized(0.5F, 1.5F).clientTrackingRange(8).updateInterval(5)
			.build(entityKey("relic_stalactite")));

	public static final DeferredHolder<EntityType<?>, EntityType<ShockwaveEntity>> SHOCKWAVE =
		ENTITY_TYPES.register("relic_shockwave", () -> EntityType.Builder.<ShockwaveEntity>of(ShockwaveEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F).clientTrackingRange(6).updateInterval(2)
			.build(entityKey("relic_shockwave")));

	public static final DeferredHolder<EntityType<?>, EntityType<DissectionEntity>> DISSECTION =
		ENTITY_TYPES.register("relic_dissection", () -> EntityType.Builder.<DissectionEntity>of(DissectionEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(1)
			.build(entityKey("relic_dissection")));

	public static final DeferredHolder<EntityType<?>, EntityType<LifeEssenceEntity>> LIFE_ESSENCE =
		ENTITY_TYPES.register("relic_life_essence", () -> EntityType.Builder.<LifeEssenceEntity>of(LifeEssenceEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F).clientTrackingRange(8).updateInterval(2)
			.build(entityKey("relic_life_essence")));

	public static final DeferredHolder<EntityType<?>, EntityType<BlockSimulationEntity>> BLOCK_SIMULATION =
		ENTITY_TYPES.register("relic_block_simulation", () -> EntityType.Builder.<BlockSimulationEntity>of(BlockSimulationEntity::new, MobCategory.MISC)
			.sized(0.98F, 0.98F).clientTrackingRange(8).updateInterval(5)
			.build(entityKey("relic_block_simulation")));

	public static final DeferredHolder<EntityType<?>, EntityType<ArrowRainEntity>> ARROW_RAIN =
		ENTITY_TYPES.register("relic_arrow_rain", () -> EntityType.Builder.<ArrowRainEntity>of(ArrowRainEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(1)
			.build(entityKey("relic_arrow_rain")));

	public static final DeferredHolder<EntityType<?>, EntityType<SolidSnowballEntity>> SOLID_SNOWBALL =
		ENTITY_TYPES.register("relic_solid_snowball", () -> EntityType.Builder.<SolidSnowballEntity>of(SolidSnowballEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F).clientTrackingRange(8).updateInterval(1)
			.build(entityKey("relic_solid_snowball")));

	public static final DeferredHolder<EntityType<?>, EntityType<RelicExperienceOrbEntity>> RELIC_XP_ORB =
		ENTITY_TYPES.register("relic_experience_orb", () -> EntityType.Builder.<RelicExperienceOrbEntity>of(RelicExperienceOrbEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F).clientTrackingRange(6).updateInterval(20)
			.build(entityKey("relic_experience_orb")));

	public static final DeferredHolder<EntityType<?>, EntityType<ThrownRelicExperienceBottleEntity>> THROWN_RELIC_XP_BOTTLE =
		ENTITY_TYPES.register("thrown_relic_experience_bottle", () -> EntityType.Builder.<ThrownRelicExperienceBottleEntity>of(ThrownRelicExperienceBottleEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
			.build(entityKey("thrown_relic_experience_bottle")));

	private static net.minecraft.resources.ResourceKey<EntityType<?>> entityKey(String name)
	{
		return net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE,
			Identifier.fromNamespaceAndPath("megamod", name));
	}

	public static void init(IEventBus modEventBus)
	{
		ENTITY_TYPES.register(modEventBus);
	}
}

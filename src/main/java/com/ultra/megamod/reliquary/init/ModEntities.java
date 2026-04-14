package com.ultra.megamod.reliquary.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.ultra.megamod.reliquary.Reliquary;
import com.ultra.megamod.reliquary.entity.*;
import com.ultra.megamod.reliquary.entity.potion.AphroditePotion;
import com.ultra.megamod.reliquary.entity.potion.FertilePotion;
import com.ultra.megamod.reliquary.entity.potion.ThrownPotion;
import com.ultra.megamod.reliquary.entity.shot.*;

import java.util.function.Supplier;

public class ModEntities {
	public static final TagKey<EntityType<?>> IGNORED_BY_INTERDICTION_TORCH_TAG = TagKey.create(Registries.ENTITY_TYPE, Reliquary.getRL("ignored_by_interdiction_torch"));

	private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Reliquary.MOD_ID);

	public static final Supplier<EntityType<AphroditePotion>> APHRODITE_POTION = ENTITY_TYPES.register("aphrodite_potion", () -> getDefaultSizeEntityType(AphroditePotion::new, "aphrodite_potion"));
	public static final Supplier<EntityType<FertilePotion>> FERTILE_POTION = ENTITY_TYPES.register("fertile_potion", () -> getDefaultSizeEntityType(FertilePotion::new, "fertile_potion"));
	public static final Supplier<EntityType<ThrownPotion>> THROWN_POTION = ENTITY_TYPES.register("thrown_potion", () -> getDefaultSizeEntityType(ThrownPotion::new, "thrown_potion"));
	public static final Supplier<EntityType<BlazeShot>> BLAZE_SHOT = ENTITY_TYPES.register("blaze_shot", () -> getShotEntityType(BlazeShot::new, "blaze_shot"));
	public static final Supplier<EntityType<BusterShot>> BUSTER_SHOT = ENTITY_TYPES.register("buster_shot", () -> getShotEntityType(BusterShot::new, "buster_shot"));
	public static final Supplier<EntityType<ConcussiveShot>> CONCUSSIVE_SHOT = ENTITY_TYPES.register("concussive_shot", () -> getShotEntityType(ConcussiveShot::new, "concussive_shot"));
	public static final Supplier<EntityType<EnderShot>> ENDER_SHOT = ENTITY_TYPES.register("ender_shot", () -> getShotEntityType(EnderShot::new, "ender_shot"));
	public static final Supplier<EntityType<ExorcismShot>> EXORCISM_SHOT = ENTITY_TYPES.register("exorcism_shot", () -> getShotEntityType(ExorcismShot::new, "exorcism_shot"));
	public static final Supplier<EntityType<NeutralShot>> NEUTRAL_SHOT = ENTITY_TYPES.register("neutral_shot", () -> getShotEntityType(NeutralShot::new, "neutral_shot"));
	public static final Supplier<EntityType<SandShot>> SAND_SHOT = ENTITY_TYPES.register("sand_shot", () -> getShotEntityType(SandShot::new, "sand_shot"));
	public static final Supplier<EntityType<SeekerShot>> SEEKER_SHOT = ENTITY_TYPES.register("seeker_shot", () -> getShotEntityType(SeekerShot::new, "seeker_shot"));
	public static final Supplier<EntityType<StormShot>> STORM_SHOT = ENTITY_TYPES.register("storm_shot", () -> getShotEntityType(StormShot::new, "storm_shot"));
	public static final Supplier<EntityType<EnderStaffProjectile>> ENDER_STAFF_PROJECTILE = ENTITY_TYPES.register("ender_staff_projectile", () -> getEntityType(EnderStaffProjectile::new, 0.25F, 0.25F, 256, "ender_staff_projectile"));
	public static final Supplier<EntityType<GlowingWater>> GLOWING_WATER = ENTITY_TYPES.register("glowing_water", () -> getDefaultSizeEntityType(GlowingWater::new, "glowing_water"));
	public static final Supplier<EntityType<HolyHandGrenade>> HOLY_HAND_GRENADE = ENTITY_TYPES.register("holy_hand_grenade", () -> getDefaultSizeEntityType(HolyHandGrenade::new, "holy_hand_grenade"));
	public static final Supplier<EntityType<KrakenSlime>> KRAKEN_SLIME = ENTITY_TYPES.register("kraken_slime", () -> getDefaultSizeEntityType(KrakenSlime::new, "kraken_slime"));
	public static final Supplier<EntityType<LyssaHook>> LYSSA_HOOK = ENTITY_TYPES.register("lyssa_hook", () -> getDefaultSizeEntityType(LyssaHook::new, "lyssa_hook"));
	public static final Supplier<EntityType<TippedArrow>> TIPPED_ARROW = ENTITY_TYPES.register("tipped_arrow", () -> getDefaultSizeEntityType(TippedArrow::new, "tipped_arrow"));
	public static final Supplier<EntityType<SpecialSnowball>> SPECIAL_SNOWBALL = ENTITY_TYPES.register("special_snowball", () -> getEntityType(SpecialSnowball::new, 0.01F, 0.01F, 128, "special_snowball"));

	public static final ResourceKey<DamageType> BULLET_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, Reliquary.getRL("bullet"));

	private ModEntities() {}

	public static void registerListeners(IEventBus modBus) {
		ENTITY_TYPES.register(modBus);
	}

	private static <T extends Entity> EntityType<T> getDefaultSizeEntityType(EntityType.EntityFactory<T> factory, String name) {
		return getEntityType(factory, 0.25F, 0.25F, 128, name);
	}

	private static <T extends ShotBase> EntityType<T> getShotEntityType(EntityType.EntityFactory<T> factory, String name) {
		return getEntityType(factory, 0.01F, 0.01F, 128, name);
	}

	private static <T extends Entity> EntityType<T> getEntityType(EntityType.EntityFactory<T> factory, float width, float height, int trackingRange, String name) {
		// Port note (1.21.11): EntityType.Builder#build(String) was replaced with
		// build(ResourceKey<EntityType<?>>). We build a key from the Reliquary namespace + the
		// same name we pass to DeferredRegister#register, matching the pattern used by
		// RelicEntityRegistry elsewhere in the codebase.
		ResourceKey<EntityType<?>> key = ResourceKey.create(
				Registries.ENTITY_TYPE,
				net.minecraft.resources.Identifier.fromNamespaceAndPath(Reliquary.MOD_ID, name));
		return EntityType.Builder.of(factory, MobCategory.MISC)
				.sized(width, height).updateInterval(5).setTrackingRange(trackingRange).setShouldReceiveVelocityUpdates(true)
				.build(key);
	}
}

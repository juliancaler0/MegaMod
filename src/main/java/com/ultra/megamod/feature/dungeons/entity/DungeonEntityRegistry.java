/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.level.material.PushReaction
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredBlock
 *  net.neoforged.neoforge.registries.DeferredItem
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.DeferredRegister$Blocks
 *  net.neoforged.neoforge.registries.DeferredRegister$Items
 */
package com.ultra.megamod.feature.dungeons.entity;

import com.ultra.megamod.feature.dungeons.DungeonSoundRegistry;
import com.ultra.megamod.feature.dungeons.block.DungeonGateBlock;
import com.ultra.megamod.feature.dungeons.block.DungeonGateKeyHoleBlock;
import com.ultra.megamod.feature.dungeons.boss.DungeonAltarBlock;
import com.ultra.megamod.feature.dungeons.boss.DungeonKeeperBoss;
import com.ultra.megamod.feature.dungeons.boss.FogWallBlock;
import com.ultra.megamod.feature.dungeons.boss.OssukageBoss;
import com.ultra.megamod.feature.dungeons.boss.WraithBoss;
import com.ultra.megamod.feature.dungeons.client.DungeonEntityRenderers;
import com.ultra.megamod.feature.dungeons.entity.CeruleanArrowEntity;
import com.ultra.megamod.feature.dungeons.entity.CrystalArrowEntity;
import com.ultra.megamod.feature.dungeons.entity.CrystallineBeamEntity;
import com.ultra.megamod.feature.dungeons.entity.DungeonMobEntity;
import com.ultra.megamod.feature.dungeons.entity.DungeonSlimeEntity;
import com.ultra.megamod.feature.dungeons.entity.HollowEntity;
import com.ultra.megamod.feature.dungeons.entity.MinionEntity;
import com.ultra.megamod.feature.dungeons.entity.RatEntity;
import com.ultra.megamod.feature.dungeons.entity.UndeadKnightEntity;
import com.ultra.megamod.feature.dungeons.item.DungeonMiniKeyItem;
import com.ultra.megamod.feature.dungeons.item.FangOnAStickItem;
import com.ultra.megamod.feature.dungeons.item.OldSkeletonBoneItem;
import com.ultra.megamod.feature.dungeons.item.OldSkeletonHeadItem;
import com.ultra.megamod.feature.dungeons.item.OssukageSwordItem;
import com.ultra.megamod.feature.dungeons.item.RatFangItem;
import com.ultra.megamod.feature.dungeons.boss.FrostmawBoss;
import com.ultra.megamod.feature.dungeons.boss.WroughtnautBoss;
import com.ultra.megamod.feature.dungeons.boss.UmvuthiBoss;
import com.ultra.megamod.feature.dungeons.boss.ChaosSpawnerBoss;
import com.ultra.megamod.feature.dungeons.entity.NagaEntity;
import com.ultra.megamod.feature.dungeons.entity.GrottolEntity;
import com.ultra.megamod.feature.dungeons.entity.LanternEntity;
import com.ultra.megamod.feature.dungeons.entity.FoliaathEntity;
import com.ultra.megamod.feature.dungeons.entity.UmvuthanaEntity;
import com.ultra.megamod.feature.dungeons.entity.SpawnerCarrierEntity;
import com.ultra.megamod.feature.dungeons.entity.BluffEntity;
import com.ultra.megamod.feature.dungeons.entity.BabyFoliaathEntity;
import com.ultra.megamod.feature.dungeons.entity.PoisonBallEntity;
import com.ultra.megamod.feature.dungeons.entity.IceBallEntity;
import com.ultra.megamod.feature.dungeons.entity.GhostBulletEntity;
import com.ultra.megamod.feature.dungeons.entity.SolarBeamEntity;
import com.ultra.megamod.feature.dungeons.entity.AxeSwingEntity;
import com.ultra.megamod.feature.dungeons.entity.DartEntity;
import com.ultra.megamod.feature.dungeons.boss.SculptorBoss;
import com.ultra.megamod.feature.dungeons.item.NagaFangDaggerItem;
import com.ultra.megamod.feature.dungeons.item.WroughtAxeItem;
import com.ultra.megamod.feature.dungeons.item.WroughtHelmItem;
import com.ultra.megamod.feature.dungeons.item.IceCrystalItem;
import com.ultra.megamod.feature.dungeons.item.SpearItem;
import com.ultra.megamod.feature.dungeons.item.GlowingJellyItem;
import com.ultra.megamod.feature.dungeons.item.LifeStealerItem;
import com.ultra.megamod.feature.dungeons.item.ScepterOfChaosItem;
import com.ultra.megamod.feature.dungeons.item.SolVisageItem;
import com.ultra.megamod.feature.dungeons.item.FoliaathSeedItem;
import com.ultra.megamod.feature.dungeons.item.EarthrendGauntletItem;
import com.ultra.megamod.feature.dungeons.item.UmvuthanaMaskItem;
import com.ultra.megamod.feature.dungeons.item.GreatExperienceBottleItem;
import com.ultra.megamod.feature.dungeons.item.GeomancerArmorItem;
import com.ultra.megamod.feature.dungeons.item.BlowgunItem;
import com.ultra.megamod.feature.dungeons.item.DartItem;
import com.ultra.megamod.feature.dungeons.item.CapturedGrottolItem;
import com.ultra.megamod.feature.dungeons.item.BluffRodItem;
import com.ultra.megamod.feature.dungeons.block.SpikeBlock;
import com.ultra.megamod.feature.dungeons.block.ExplosiveBarrelBlock;
import com.ultra.megamod.feature.dungeons.block.WallRackBlock;
import com.ultra.megamod.feature.dungeons.block.PileBlock;
import com.ultra.megamod.feature.dungeons.block.BookPileBlock;
import com.ultra.megamod.feature.dungeons.block.PebbleBlock;
import com.ultra.megamod.feature.dungeons.block.DungeonWallTorch;
import com.ultra.megamod.feature.dungeons.block.WallPlatformBlock;
import com.ultra.megamod.feature.dungeons.loot.DungeonExclusiveItems;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DungeonEntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create((ResourceKey)Registries.ENTITY_TYPE, (String)"megamod");
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks((String)"megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems((String)"megamod");
    private static final ResourceKey<EntityType<?>> WRAITH_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"wraith_boss"));
    private static final ResourceKey<EntityType<?>> OSSUKAGE_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"ossukage_boss"));
    private static final ResourceKey<EntityType<?>> DUNGEON_MOB_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"dungeon_mob"));
    private static final ResourceKey<EntityType<?>> MINION_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"minion"));
    private static final ResourceKey<EntityType<?>> DUNGEON_RAT_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"dungeon_rat"));
    private static final ResourceKey<EntityType<?>> KUNAI_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"kunai"));
    private static final ResourceKey<EntityType<?>> UNDEAD_KNIGHT_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "undead_knight"));
    private static final ResourceKey<EntityType<?>> DUNGEON_SLIME_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "dungeon_slime"));
    private static final ResourceKey<EntityType<?>> HOLLOW_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "hollow"));
    private static final ResourceKey<EntityType<?>> DUNGEON_KEEPER_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "dungeon_keeper"));
    private static final ResourceKey<EntityType<?>> CRYSTALLINE_BEAM_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "crystalline_beam"));
    private static final ResourceKey<EntityType<?>> CERULEAN_ARROW_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "cerulean_arrow"));
    private static final ResourceKey<EntityType<?>> CRYSTAL_ARROW_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "crystal_arrow"));
    // New mob keys
    private static final ResourceKey<EntityType<?>> NAGA_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "naga"));
    private static final ResourceKey<EntityType<?>> GROTTOL_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "grottol"));
    private static final ResourceKey<EntityType<?>> LANTERN_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "lantern"));
    private static final ResourceKey<EntityType<?>> FOLIAATH_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "foliaath"));
    private static final ResourceKey<EntityType<?>> UMVUTHANA_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "umvuthana"));
    private static final ResourceKey<EntityType<?>> SPAWNER_CARRIER_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "spawner_carrier"));
    // New boss keys
    private static final ResourceKey<EntityType<?>> FROSTMAW_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "frostmaw_boss"));
    private static final ResourceKey<EntityType<?>> WROUGHTNAUT_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "wroughtnaut_boss"));
    private static final ResourceKey<EntityType<?>> UMVUTHI_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "umvuthi_boss"));
    private static final ResourceKey<EntityType<?>> CHAOS_SPAWNER_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "chaos_spawner_boss"));
    // New entity keys
    private static final ResourceKey<EntityType<?>> SCULPTOR_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "sculptor_boss"));
    private static final ResourceKey<EntityType<?>> BLUFF_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "bluff"));
    private static final ResourceKey<EntityType<?>> BABY_FOLIAATH_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "baby_foliaath"));
    private static final ResourceKey<EntityType<?>> UMVUTHANA_RAPTOR_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "umvuthana_raptor"));
    private static final ResourceKey<EntityType<?>> UMVUTHANA_FOLLOWER_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "umvuthana_follower"));
    private static final ResourceKey<EntityType<?>> UMVUTHANA_CRANE_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "umvuthana_crane"));
    private static final ResourceKey<EntityType<?>> POISON_BALL_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "poison_ball"));
    private static final ResourceKey<EntityType<?>> ICE_BALL_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "ice_ball"));
    private static final ResourceKey<EntityType<?>> GHOST_BULLET_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "ghost_bullet"));
    private static final ResourceKey<EntityType<?>> SOLAR_BEAM_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "solar_beam"));
    private static final ResourceKey<EntityType<?>> AXE_SWING_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "axe_swing"));
    private static final ResourceKey<EntityType<?>> DART_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "dart"));
    private static final ResourceKey<EntityType<?>> CHAOS_SPAWNER_GATEKEEPER_KEY = ResourceKey.create((ResourceKey)Registries.ENTITY_TYPE, (Identifier)Identifier.fromNamespaceAndPath("megamod", "chaos_spawner_gatekeeper"));
    public static final Supplier<EntityType<WraithBoss>> WRAITH_BOSS = ENTITY_TYPES.register("wraith_boss", () -> EntityType.Builder.of(WraithBoss::new, (MobCategory)MobCategory.MONSTER).sized(0.8f, 1.8f).clientTrackingRange(10).build(WRAITH_KEY));
    public static final Supplier<EntityType<OssukageBoss>> OSSUKAGE_BOSS = ENTITY_TYPES.register("ossukage_boss", () -> EntityType.Builder.of(OssukageBoss::new, (MobCategory)MobCategory.MONSTER).sized(1.2f, 2.6f).clientTrackingRange(10).build(OSSUKAGE_KEY));
    public static final Supplier<EntityType<DungeonMobEntity>> DUNGEON_MOB = ENTITY_TYPES.register("dungeon_mob", () -> EntityType.Builder.of(DungeonMobEntity::new, (MobCategory)MobCategory.MONSTER).sized(0.6f, 1.95f).clientTrackingRange(8).build(DUNGEON_MOB_KEY));
    public static final Supplier<EntityType<MinionEntity>> MINION = ENTITY_TYPES.register("minion", () -> EntityType.Builder.of(MinionEntity::new, (MobCategory)MobCategory.MONSTER).sized(0.6f, 1.99f).clientTrackingRange(8).build(MINION_KEY));
    public static final Supplier<EntityType<RatEntity>> DUNGEON_RAT = ENTITY_TYPES.register("dungeon_rat", () -> EntityType.Builder.of(RatEntity::new, (MobCategory)MobCategory.MONSTER).sized(1.2f, 1.0f).clientTrackingRange(8).build(DUNGEON_RAT_KEY));
    public static final Supplier<EntityType<KunaiEntity>> KUNAI = ENTITY_TYPES.register("kunai", () -> EntityType.Builder.<KunaiEntity>of(KunaiEntity::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build(KUNAI_KEY));
    public static final Supplier<EntityType<UndeadKnightEntity>> UNDEAD_KNIGHT = ENTITY_TYPES.register("undead_knight", () -> EntityType.Builder.of(UndeadKnightEntity::new, (MobCategory)MobCategory.MONSTER).sized(0.8f, 2.3f).clientTrackingRange(8).build(UNDEAD_KNIGHT_KEY));
    public static final Supplier<EntityType<DungeonSlimeEntity>> DUNGEON_SLIME = ENTITY_TYPES.register("dungeon_slime", () -> EntityType.Builder.<DungeonSlimeEntity>of(DungeonSlimeEntity::new, (MobCategory)MobCategory.MONSTER).sized(1.2f, 1.2f).clientTrackingRange(8).build(DUNGEON_SLIME_KEY));
    public static final Supplier<EntityType<HollowEntity>> HOLLOW = ENTITY_TYPES.register("hollow", () -> EntityType.Builder.of(HollowEntity::new, (MobCategory)MobCategory.MONSTER).sized(0.6f, 1.95f).clientTrackingRange(8).build(HOLLOW_KEY));
    public static final Supplier<EntityType<DungeonKeeperBoss>> DUNGEON_KEEPER = ENTITY_TYPES.register("dungeon_keeper", () -> EntityType.Builder.of(DungeonKeeperBoss::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 2.8f).clientTrackingRange(10).build(DUNGEON_KEEPER_KEY));
    public static final Supplier<EntityType<CrystallineBeamEntity>> CRYSTALLINE_BEAM = ENTITY_TYPES.register("crystalline_beam", () -> EntityType.Builder.<CrystallineBeamEntity>of(CrystallineBeamEntity::new, (MobCategory)MobCategory.MISC).sized(0.4f, 0.4f).clientTrackingRange(4).updateInterval(10).build(CRYSTALLINE_BEAM_KEY));
    public static final Supplier<EntityType<CeruleanArrowEntity>> CERULEAN_ARROW = ENTITY_TYPES.register("cerulean_arrow", () -> EntityType.Builder.<CeruleanArrowEntity>of(CeruleanArrowEntity::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build(CERULEAN_ARROW_KEY));
    public static final Supplier<EntityType<CrystalArrowEntity>> CRYSTAL_ARROW = ENTITY_TYPES.register("crystal_arrow", () -> EntityType.Builder.<CrystalArrowEntity>of(CrystalArrowEntity::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build(CRYSTAL_ARROW_KEY));
    // New mob entity types
    public static final Supplier<EntityType<NagaEntity>> NAGA = ENTITY_TYPES.register("naga", () -> EntityType.Builder.of(NagaEntity::new, (MobCategory)MobCategory.MONSTER).sized(3.5f, 2.5f).clientTrackingRange(8).build(NAGA_KEY));
    public static final Supplier<EntityType<GrottolEntity>> GROTTOL = ENTITY_TYPES.register("grottol", () -> EntityType.Builder.of(GrottolEntity::new, (MobCategory)MobCategory.MONSTER).sized(0.7f, 0.8f).clientTrackingRange(8).build(GROTTOL_KEY));
    public static final Supplier<EntityType<LanternEntity>> LANTERN = ENTITY_TYPES.register("lantern", () -> EntityType.Builder.of(LanternEntity::new, (MobCategory)MobCategory.MONSTER).sized(0.6f, 0.8f).clientTrackingRange(8).build(LANTERN_KEY));
    public static final Supplier<EntityType<FoliaathEntity>> FOLIAATH = ENTITY_TYPES.register("foliaath", () -> EntityType.Builder.of(FoliaathEntity::new, (MobCategory)MobCategory.MONSTER).sized(1.2f, 1.8f).clientTrackingRange(8).build(FOLIAATH_KEY));
    public static final Supplier<EntityType<UmvuthanaEntity>> UMVUTHANA = ENTITY_TYPES.register("umvuthana", () -> EntityType.Builder.of(UmvuthanaEntity::new, (MobCategory)MobCategory.MONSTER).sized(0.8f, 1.8f).clientTrackingRange(8).build(UMVUTHANA_KEY));
    public static final Supplier<EntityType<SpawnerCarrierEntity>> SPAWNER_CARRIER = ENTITY_TYPES.register("spawner_carrier", () -> EntityType.Builder.of(SpawnerCarrierEntity::new, (MobCategory)MobCategory.MONSTER).sized(0.8f, 1.95f).clientTrackingRange(8).build(SPAWNER_CARRIER_KEY));
    // New boss entity types
    public static final Supplier<EntityType<FrostmawBoss>> FROSTMAW_BOSS = ENTITY_TYPES.register("frostmaw_boss", () -> EntityType.Builder.of(FrostmawBoss::new, (MobCategory)MobCategory.MONSTER).sized(3.5f, 5.0f).clientTrackingRange(10).build(FROSTMAW_KEY));
    public static final Supplier<EntityType<WroughtnautBoss>> WROUGHTNAUT_BOSS = ENTITY_TYPES.register("wroughtnaut_boss", () -> EntityType.Builder.of(WroughtnautBoss::new, (MobCategory)MobCategory.MONSTER).sized(2.4f, 3.2f).clientTrackingRange(10).build(WROUGHTNAUT_KEY));
    public static final Supplier<EntityType<UmvuthiBoss>> UMVUTHI_BOSS = ENTITY_TYPES.register("umvuthi_boss", () -> EntityType.Builder.of(UmvuthiBoss::new, (MobCategory)MobCategory.MONSTER).sized(2.0f, 4.0f).clientTrackingRange(10).build(UMVUTHI_KEY));
    public static final Supplier<EntityType<ChaosSpawnerBoss>> CHAOS_SPAWNER_BOSS = ENTITY_TYPES.register("chaos_spawner_boss", () -> EntityType.Builder.of(ChaosSpawnerBoss::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 2.5f).clientTrackingRange(10).build(CHAOS_SPAWNER_KEY));
    // New entity types
    public static final Supplier<EntityType<SculptorBoss>> SCULPTOR_BOSS = ENTITY_TYPES.register("sculptor_boss", () -> EntityType.Builder.of(SculptorBoss::new, (MobCategory)MobCategory.MONSTER).sized(2.4f, 3.5f).clientTrackingRange(10).build(SCULPTOR_KEY));
    public static final Supplier<EntityType<BluffEntity>> BLUFF = ENTITY_TYPES.register("bluff", () -> EntityType.Builder.of(BluffEntity::new, (MobCategory)MobCategory.MONSTER).sized(0.6f, 0.6f).clientTrackingRange(8).build(BLUFF_KEY));
    public static final Supplier<EntityType<BabyFoliaathEntity>> BABY_FOLIAATH = ENTITY_TYPES.register("baby_foliaath", () -> EntityType.Builder.of(BabyFoliaathEntity::new, (MobCategory)MobCategory.MONSTER).sized(0.5f, 0.8f).clientTrackingRange(8).build(BABY_FOLIAATH_KEY));
    // Umvuthana variants (ported from MowziesMobs)
    public static final Supplier<EntityType<UmvuthanaRaptorEntity>> UMVUTHANA_RAPTOR = ENTITY_TYPES.register("umvuthana_raptor", () -> EntityType.Builder.of(UmvuthanaRaptorEntity::new, (MobCategory)MobCategory.MONSTER).sized(0.9f, 2.0f).clientTrackingRange(8).build(UMVUTHANA_RAPTOR_KEY));
    public static final Supplier<EntityType<UmvuthanaFollowerEntity>> UMVUTHANA_FOLLOWER = ENTITY_TYPES.register("umvuthana_follower", () -> EntityType.Builder.of(UmvuthanaFollowerEntity::new, (MobCategory)MobCategory.MONSTER).sized(0.8f, 1.8f).clientTrackingRange(8).build(UMVUTHANA_FOLLOWER_KEY));
    public static final Supplier<EntityType<UmvuthanaCraneEntity>> UMVUTHANA_CRANE = ENTITY_TYPES.register("umvuthana_crane", () -> EntityType.Builder.of(UmvuthanaCraneEntity::new, (MobCategory)MobCategory.MONSTER).sized(0.9f, 2.0f).clientTrackingRange(8).build(UMVUTHANA_CRANE_KEY));
    public static final Supplier<EntityType<PoisonBallEntity>> POISON_BALL = ENTITY_TYPES.register("poison_ball", () -> EntityType.Builder.<PoisonBallEntity>of(PoisonBallEntity::new, (MobCategory)MobCategory.MISC).sized(0.4f, 0.4f).clientTrackingRange(4).updateInterval(10).build(POISON_BALL_KEY));
    public static final Supplier<EntityType<IceBallEntity>> ICE_BALL = ENTITY_TYPES.register("ice_ball", () -> EntityType.Builder.<IceBallEntity>of(IceBallEntity::new, (MobCategory)MobCategory.MISC).sized(0.4f, 0.4f).clientTrackingRange(4).updateInterval(10).build(ICE_BALL_KEY));
    public static final Supplier<EntityType<GhostBulletEntity>> GHOST_BULLET = ENTITY_TYPES.register("ghost_bullet", () -> EntityType.Builder.<GhostBulletEntity>of(GhostBulletEntity::new, (MobCategory)MobCategory.MISC).sized(0.4f, 0.4f).clientTrackingRange(4).updateInterval(10).build(GHOST_BULLET_KEY));
    public static final Supplier<EntityType<SolarBeamEntity>> SOLAR_BEAM = ENTITY_TYPES.register("solar_beam", () -> EntityType.Builder.<SolarBeamEntity>of(SolarBeamEntity::new, (MobCategory)MobCategory.MISC).sized(0.4f, 0.4f).clientTrackingRange(4).updateInterval(10).build(SOLAR_BEAM_KEY));
    public static final Supplier<EntityType<AxeSwingEntity>> AXE_SWING = ENTITY_TYPES.register("axe_swing", () -> EntityType.Builder.<AxeSwingEntity>of(AxeSwingEntity::new, (MobCategory)MobCategory.MISC).sized(0.4f, 0.4f).clientTrackingRange(4).updateInterval(10).build(AXE_SWING_KEY));
    public static final Supplier<EntityType<DartEntity>> DART = ENTITY_TYPES.register("dart", () -> EntityType.Builder.<DartEntity>of(DartEntity::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(20).build(DART_KEY));
    public static final Supplier<EntityType<ChaosSpawnerEntity>> CHAOS_SPAWNER_GATEKEEPER = ENTITY_TYPES.register("chaos_spawner_gatekeeper", () -> EntityType.Builder.of(ChaosSpawnerEntity::new, (MobCategory)MobCategory.MONSTER).sized(1.0f, 2.5f).clientTrackingRange(10).build(CHAOS_SPAWNER_GATEKEEPER_KEY));
    public static final DeferredBlock<FogWallBlock> FOG_WALL_BLOCK = BLOCKS.registerBlock("fog_wall", FogWallBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(-1.0f, 3600000.0f).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK).isViewBlocking((state, level, pos) -> false));
    public static final DeferredItem<BlockItem> FOG_WALL_ITEM = ITEMS.registerSimpleBlockItem("fog_wall", FOG_WALL_BLOCK);
    public static final DeferredBlock<DungeonAltarBlock> DUNGEON_ALTAR_BLOCK = BLOCKS.registerBlock("dungeon_altar", DungeonAltarBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).sound(SoundType.METAL).strength(2.0f, 15.0f).noOcclusion().pushReaction(PushReaction.BLOCK));
    public static final DeferredItem<BlockItem> DUNGEON_ALTAR_ITEM = ITEMS.registerSimpleBlockItem("dungeon_altar", DUNGEON_ALTAR_BLOCK);
    public static final DeferredBlock<DungeonGateBlock> DUNGEON_GATE_BLOCK = BLOCKS.registerBlock("dungeon_gate", DungeonGateBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(-1.0f, 3600000.0f).noLootTable().pushReaction(PushReaction.BLOCK));
    public static final DeferredItem<BlockItem> DUNGEON_GATE_ITEM = ITEMS.registerSimpleBlockItem("dungeon_gate", DUNGEON_GATE_BLOCK);
    public static final DeferredBlock<DungeonGateKeyHoleBlock> DUNGEON_KEYHOLE_BLOCK = BLOCKS.registerBlock("dungeon_keyhole", DungeonGateKeyHoleBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(2.0f, 15.0f).pushReaction(PushReaction.BLOCK));
    public static final DeferredItem<BlockItem> DUNGEON_KEYHOLE_ITEM = ITEMS.registerSimpleBlockItem("dungeon_keyhole", DUNGEON_KEYHOLE_BLOCK);
    // New dungeon blocks
    public static final DeferredBlock<SpikeBlock> SPIKE_BLOCK = BLOCKS.registerBlock("spike_block", SpikeBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.5f, 6.0f).noOcclusion());
    public static final DeferredItem<BlockItem> SPIKE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("spike_block", SPIKE_BLOCK);
    public static final DeferredBlock<ExplosiveBarrelBlock> EXPLOSIVE_BARREL_BLOCK = BLOCKS.registerBlock("explosive_barrel", ExplosiveBarrelBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0f, 1.0f));
    public static final DeferredItem<BlockItem> EXPLOSIVE_BARREL_ITEM = ITEMS.registerSimpleBlockItem("explosive_barrel", EXPLOSIVE_BARREL_BLOCK);
    public static final DeferredBlock<WallRackBlock> WALL_RACK_BLOCK = BLOCKS.registerBlock("wall_rack", WallRackBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f, 3.0f).noOcclusion());
    public static final DeferredItem<BlockItem> WALL_RACK_ITEM = ITEMS.registerSimpleBlockItem("wall_rack", WALL_RACK_BLOCK);
    // DNL-ported decoration blocks
    public static final DeferredBlock<PileBlock> COBBLESTONE_PEBBLES = BLOCKS.registerBlock("cobblestone_pebbles", PileBlock::new, () -> BlockBehaviour.Properties.of().instabreak().noOcclusion().sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> COBBLESTONE_PEBBLES_ITEM = ITEMS.registerSimpleBlockItem("cobblestone_pebbles", COBBLESTONE_PEBBLES);
    public static final DeferredBlock<PileBlock> MOSSY_COBBLESTONE_PEBBLES = BLOCKS.registerBlock("mossy_cobblestone_pebbles", PileBlock::new, () -> BlockBehaviour.Properties.of().instabreak().noOcclusion().sound(SoundType.STONE));
    public static final DeferredItem<BlockItem> MOSSY_COBBLESTONE_PEBBLES_ITEM = ITEMS.registerSimpleBlockItem("mossy_cobblestone_pebbles", MOSSY_COBBLESTONE_PEBBLES);
    public static final DeferredBlock<PileBlock> IRON_INGOT_PILE = BLOCKS.registerBlock("iron_ingot_pile", PileBlock::new, () -> BlockBehaviour.Properties.of().instabreak().noOcclusion().sound(SoundType.METAL));
    public static final DeferredItem<BlockItem> IRON_INGOT_PILE_ITEM = ITEMS.registerSimpleBlockItem("iron_ingot_pile", IRON_INGOT_PILE);
    public static final DeferredBlock<PileBlock> GOLD_INGOT_PILE = BLOCKS.registerBlock("gold_ingot_pile", PileBlock::new, () -> BlockBehaviour.Properties.of().instabreak().noOcclusion().sound(SoundType.METAL));
    public static final DeferredItem<BlockItem> GOLD_INGOT_PILE_ITEM = ITEMS.registerSimpleBlockItem("gold_ingot_pile", GOLD_INGOT_PILE);
    public static final DeferredBlock<BookPileBlock> BOOK_PILE = BLOCKS.registerBlock("book_pile", BookPileBlock::new, () -> BlockBehaviour.Properties.of().instabreak().noOcclusion().sound(SoundType.WOOL));
    public static final DeferredItem<BlockItem> BOOK_PILE_ITEM = ITEMS.registerSimpleBlockItem("book_pile", BOOK_PILE);
    public static final DeferredBlock<DungeonWallTorch> DUNGEON_WALL_TORCH = BLOCKS.registerBlock("dungeon_wall_torch", DungeonWallTorch::new, () -> BlockBehaviour.Properties.of().noCollision().instabreak().sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY));
    public static final DeferredItem<BlockItem> DUNGEON_WALL_TORCH_ITEM = ITEMS.registerSimpleBlockItem("dungeon_wall_torch", DUNGEON_WALL_TORCH);
    public static final DeferredBlock<WallPlatformBlock> WALL_PLATFORM = BLOCKS.registerBlock("wooden_wall_platform", WallPlatformBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(3.0f).sound(SoundType.WOOD).ignitedByLava().noOcclusion());
    public static final DeferredItem<BlockItem> WALL_PLATFORM_ITEM = ITEMS.registerSimpleBlockItem("wooden_wall_platform", WALL_PLATFORM);

    public static final DeferredItem<DungeonMiniKeyItem> DUNGEON_MINI_KEY = ITEMS.registerItem("dungeon_mini_key", p -> new DungeonMiniKeyItem(p));
    public static final DeferredItem<OssukageSwordItem> OSSUKAGE_SWORD = ITEMS.registerItem("ossukage_sword", p -> new OssukageSwordItem(p));
    public static final DeferredItem<RatFangItem> RAT_FANG = ITEMS.registerItem("rat_fang", p -> new RatFangItem(p));
    public static final DeferredItem<FangOnAStickItem> FANG_ON_A_STICK = ITEMS.registerItem("fang_on_a_stick", p -> new FangOnAStickItem(p));
    public static final DeferredItem<OldSkeletonBoneItem> SKELETON_BONE = ITEMS.registerItem("skeleton_bone", p -> new OldSkeletonBoneItem(p));
    public static final DeferredItem<OldSkeletonHeadItem> SKELETON_HEAD = ITEMS.registerItem("skeleton_head", p -> new OldSkeletonHeadItem(p));
    public static final DeferredItem<SpawnEggItem> DUNGEON_RAT_SPAWN_EGG = ITEMS.registerItem("dungeon_rat_spawn_egg", p -> new SpawnEggItem(p.spawnEgg(DUNGEON_RAT.get())));
    public static final DeferredItem<Item> CERULEAN_ARROW_ITEM = ITEMS.registerItem("cerulean_arrow", p -> new Item(p.stacksTo(64)));
    public static final DeferredItem<Item> CRYSTAL_ARROW_ITEM = ITEMS.registerItem("crystal_arrow", p -> new Item(p.stacksTo(64)));
    // New dungeon items
    public static final DeferredItem<NagaFangDaggerItem> NAGA_FANG_DAGGER = ITEMS.registerItem("naga_fang_dagger", p -> new NagaFangDaggerItem(p));
    public static final DeferredItem<WroughtAxeItem> WROUGHT_AXE = ITEMS.registerItem("wrought_axe", p -> new WroughtAxeItem(p));
    public static final DeferredItem<WroughtHelmItem> WROUGHT_HELM = ITEMS.registerItem("wrought_helm", p -> new WroughtHelmItem(p));
    public static final DeferredItem<IceCrystalItem> ICE_CRYSTAL = ITEMS.registerItem("ice_crystal", p -> new IceCrystalItem(p));
    public static final DeferredItem<SpearItem> SPEAR = ITEMS.registerItem("spear", p -> new SpearItem(p));
    public static final DeferredItem<GlowingJellyItem> GLOWING_JELLY = ITEMS.registerItem("glowing_jelly", p -> new GlowingJellyItem(p));
    public static final DeferredItem<LifeStealerItem> LIFE_STEALER = ITEMS.registerItem("life_stealer", p -> new LifeStealerItem(p));
    public static final DeferredItem<ScepterOfChaosItem> SCEPTER_OF_CHAOS = ITEMS.registerItem("scepter_of_chaos", p -> new ScepterOfChaosItem(p));
    public static final DeferredItem<SolVisageItem> SOL_VISAGE = ITEMS.registerItem("sol_visage", p -> new SolVisageItem(p));
    public static final DeferredItem<FoliaathSeedItem> FOLIAATH_SEED = ITEMS.registerItem("foliaath_seed", p -> new FoliaathSeedItem(p));
    // New items
    public static final DeferredItem<EarthrendGauntletItem> EARTHREND_GAUNTLET = ITEMS.registerItem("earthrend_gauntlet", p -> new EarthrendGauntletItem(p));
    public static final DeferredItem<UmvuthanaMaskItem> MASK_OF_FEAR = ITEMS.registerItem("mask_of_fear", p -> new UmvuthanaMaskItem(p, UmvuthanaMaskItem.MaskType.FEAR));
    public static final DeferredItem<UmvuthanaMaskItem> MASK_OF_FURY = ITEMS.registerItem("mask_of_fury", p -> new UmvuthanaMaskItem(p, UmvuthanaMaskItem.MaskType.FURY));
    public static final DeferredItem<UmvuthanaMaskItem> MASK_OF_FAITH = ITEMS.registerItem("mask_of_faith", p -> new UmvuthanaMaskItem(p, UmvuthanaMaskItem.MaskType.FAITH));
    public static final DeferredItem<UmvuthanaMaskItem> MASK_OF_RAGE = ITEMS.registerItem("mask_of_rage", p -> new UmvuthanaMaskItem(p, UmvuthanaMaskItem.MaskType.RAGE));
    public static final DeferredItem<UmvuthanaMaskItem> MASK_OF_MISERY = ITEMS.registerItem("mask_of_misery", p -> new UmvuthanaMaskItem(p, UmvuthanaMaskItem.MaskType.MISERY));
    public static final DeferredItem<UmvuthanaMaskItem> MASK_OF_BLISS = ITEMS.registerItem("mask_of_bliss", p -> new UmvuthanaMaskItem(p, UmvuthanaMaskItem.MaskType.BLISS));
    public static final DeferredItem<GreatExperienceBottleItem> GREAT_EXPERIENCE_BOTTLE = ITEMS.registerItem("great_experience_bottle", p -> new GreatExperienceBottleItem(p));
    public static final DeferredItem<GeomancerArmorItem> GEOMANCER_HELM = ITEMS.registerItem("geomancer_helm", p -> new GeomancerArmorItem(p, "Helm"));
    public static final DeferredItem<GeomancerArmorItem> GEOMANCER_CHEST = ITEMS.registerItem("geomancer_chest", p -> new GeomancerArmorItem(p, "Chestplate"));
    public static final DeferredItem<GeomancerArmorItem> GEOMANCER_LEGS = ITEMS.registerItem("geomancer_legs", p -> new GeomancerArmorItem(p, "Leggings"));
    public static final DeferredItem<GeomancerArmorItem> GEOMANCER_BOOTS = ITEMS.registerItem("geomancer_boots", p -> new GeomancerArmorItem(p, "Boots"));
    public static final DeferredItem<BlowgunItem> BLOWGUN = ITEMS.registerItem("blowgun", p -> new BlowgunItem(p));
    public static final DeferredItem<DartItem> DART_ITEM = ITEMS.registerItem("dart_ammo", p -> new DartItem(p));
    public static final DeferredItem<CapturedGrottolItem> CAPTURED_GROTTOL = ITEMS.registerItem("captured_grottol", p -> new CapturedGrottolItem(p));
    public static final DeferredItem<BluffRodItem> BLUFF_ROD = ITEMS.registerItem("bluff_rod", p -> new BluffRodItem(p));

    public static void init(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        DungeonSoundRegistry.SOUNDS.register(modBus);
        modBus.addListener((EntityAttributeCreationEvent event) -> {
            event.put(WRAITH_BOSS.get(), WraithBoss.createWraithAttributes().build());
            event.put(OSSUKAGE_BOSS.get(), OssukageBoss.createOssukageAttributes().build());
            event.put(DUNGEON_MOB.get(), DungeonMobEntity.createDungeonMobAttributes().build());
            event.put(MINION.get(), MinionEntity.createMinionAttributes().build());
            event.put(DUNGEON_RAT.get(), RatEntity.createRatAttributes().build());
            event.put(UNDEAD_KNIGHT.get(), UndeadKnightEntity.createKnightAttributes().build());
            event.put(DUNGEON_SLIME.get(), DungeonSlimeEntity.createDungeonSlimeAttributes().build());
            event.put(HOLLOW.get(), HollowEntity.createHollowAttributes().build());
            event.put(DUNGEON_KEEPER.get(), DungeonKeeperBoss.createKeeperAttributes().build());
            // New mobs
            event.put(NAGA.get(), NagaEntity.createNagaAttributes().build());
            event.put(GROTTOL.get(), GrottolEntity.createGrottolAttributes().build());
            event.put(LANTERN.get(), LanternEntity.createLanternAttributes().build());
            event.put(FOLIAATH.get(), FoliaathEntity.createFoliaathAttributes().build());
            event.put(UMVUTHANA.get(), UmvuthanaEntity.createUmvuthanaAttributes().build());
            event.put(SPAWNER_CARRIER.get(), SpawnerCarrierEntity.createSpawnerCarrierAttributes().build());
            // New bosses
            event.put(FROSTMAW_BOSS.get(), FrostmawBoss.createFrostmawAttributes().build());
            event.put(WROUGHTNAUT_BOSS.get(), WroughtnautBoss.createWroughtnautAttributes().build());
            event.put(UMVUTHI_BOSS.get(), UmvuthiBoss.createUmvuthiAttributes().build());
            event.put(CHAOS_SPAWNER_BOSS.get(), ChaosSpawnerBoss.createChaosSpawnerAttributes().build());
            // New entities
            event.put(SCULPTOR_BOSS.get(), SculptorBoss.createSculptorAttributes().build());
            event.put(BLUFF.get(), BluffEntity.createBluffAttributes().build());
            event.put(BABY_FOLIAATH.get(), BabyFoliaathEntity.createBabyFoliaathAttributes().build());
            event.put(CHAOS_SPAWNER_GATEKEEPER.get(), ChaosSpawnerEntity.createGatekeeperAttributes().build());
            // Umvuthana variants
            event.put(UMVUTHANA_RAPTOR.get(), UmvuthanaRaptorEntity.createAttributes().build());
            event.put(UMVUTHANA_FOLLOWER.get(), UmvuthanaFollowerEntity.createAttributes().build());
            event.put(UMVUTHANA_CRANE.get(), UmvuthanaCraneEntity.createAttributes().build());
        });
        modBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == ResourceKey.create((ResourceKey) Registries.CREATIVE_MODE_TAB, (Identifier) Identifier.fromNamespaceAndPath((String) "megamod", (String) "megamod_dungeons_tab"))) {
                // Dungeon materials & drops (not weapons/armor/relics — those have their own tabs)
                event.accept((ItemLike) RAT_FANG.get());
                event.accept((ItemLike) SKELETON_BONE.get());
                event.accept((ItemLike) SKELETON_HEAD.get());
                event.accept((ItemLike) DUNGEON_RAT_SPAWN_EGG.get());
                event.accept((ItemLike) CERULEAN_ARROW_ITEM.get());
                event.accept((ItemLike) CRYSTAL_ARROW_ITEM.get());
                event.accept((ItemLike) ICE_CRYSTAL.get());
                event.accept((ItemLike) GLOWING_JELLY.get());
                event.accept((ItemLike) FOLIAATH_SEED.get());
                event.accept((ItemLike) SPIKE_BLOCK_ITEM.get());
                event.accept((ItemLike) EXPLOSIVE_BARREL_ITEM.get());
                event.accept((ItemLike) WALL_RACK_ITEM.get());
                event.accept((ItemLike) GREAT_EXPERIENCE_BOTTLE.get());
                event.accept((ItemLike) DART_ITEM.get());
                event.accept((ItemLike) CAPTURED_GROTTOL.get());
                event.accept((ItemLike) BLUFF_ROD.get());
                // Weapons moved to Weapons tab, armor to Armor tab, masks to Relics tab
            }
        });
        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT) {
            modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> DungeonEntityRenderers.onRegisterRenderers(event));
            modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> DungeonEntityRenderers.onRegisterLayerDefinitions(event));
        }
    }
}


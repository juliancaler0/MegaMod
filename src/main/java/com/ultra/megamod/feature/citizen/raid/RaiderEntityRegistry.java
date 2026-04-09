package com.ultra.megamod.feature.citizen.raid;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registers all 18 raider entity types (6 cultures x 3 tiers)
 * and their attribute mappings.
 */
public class RaiderEntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
            (ResourceKey) Registries.ENTITY_TYPE, (String) "megamod"
    );

    // --- Resource keys ---
    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create((ResourceKey) Registries.ENTITY_TYPE,
                (Identifier) Identifier.fromNamespaceAndPath("megamod", name));
    }

    // === Barbarians ===
    public static final Supplier<EntityType<EntityBarbarian>> BARBARIAN = ENTITY_TYPES.register("raider_barbarian",
            () -> EntityType.Builder.of(EntityBarbarian::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_barbarian")));

    public static final Supplier<EntityType<EntityArcherBarbarian>> ARCHER_BARBARIAN = ENTITY_TYPES.register("raider_archer_barbarian",
            () -> EntityType.Builder.of(EntityArcherBarbarian::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_archer_barbarian")));

    public static final Supplier<EntityType<EntityChiefBarbarian>> CHIEF_BARBARIAN = ENTITY_TYPES.register("raider_chief_barbarian",
            () -> EntityType.Builder.of(EntityChiefBarbarian::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_chief_barbarian")));

    // === Pirates ===
    public static final Supplier<EntityType<EntityPirate>> PIRATE = ENTITY_TYPES.register("raider_pirate",
            () -> EntityType.Builder.of(EntityPirate::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_pirate")));

    public static final Supplier<EntityType<EntityArcherPirate>> ARCHER_PIRATE = ENTITY_TYPES.register("raider_archer_pirate",
            () -> EntityType.Builder.of(EntityArcherPirate::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_archer_pirate")));

    public static final Supplier<EntityType<EntityCaptainPirate>> CAPTAIN_PIRATE = ENTITY_TYPES.register("raider_captain_pirate",
            () -> EntityType.Builder.of(EntityCaptainPirate::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_captain_pirate")));

    // === Egyptians ===
    public static final Supplier<EntityType<EntityMummy>> MUMMY = ENTITY_TYPES.register("raider_mummy",
            () -> EntityType.Builder.of(EntityMummy::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_mummy")));

    public static final Supplier<EntityType<EntityArcherMummy>> ARCHER_MUMMY = ENTITY_TYPES.register("raider_archer_mummy",
            () -> EntityType.Builder.of(EntityArcherMummy::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_archer_mummy")));

    public static final Supplier<EntityType<EntityPharao>> PHARAO = ENTITY_TYPES.register("raider_pharao",
            () -> EntityType.Builder.of(EntityPharao::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_pharao")));

    // === Norsemen ===
    public static final Supplier<EntityType<EntityShieldmaiden>> SHIELDMAIDEN = ENTITY_TYPES.register("raider_shieldmaiden",
            () -> EntityType.Builder.of(EntityShieldmaiden::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_shieldmaiden")));

    public static final Supplier<EntityType<EntityNorsemenArcher>> NORSEMEN_ARCHER = ENTITY_TYPES.register("raider_norsemen_archer",
            () -> EntityType.Builder.of(EntityNorsemenArcher::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_norsemen_archer")));

    public static final Supplier<EntityType<EntityNorsemenChief>> NORSEMEN_CHIEF = ENTITY_TYPES.register("raider_norsemen_chief",
            () -> EntityType.Builder.of(EntityNorsemenChief::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_norsemen_chief")));

    // === Amazons ===
    public static final Supplier<EntityType<EntityAmazonSpearman>> AMAZON_SPEARMAN = ENTITY_TYPES.register("raider_amazon_spearman",
            () -> EntityType.Builder.of(EntityAmazonSpearman::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_amazon_spearman")));

    public static final Supplier<EntityType<EntityArcherAmazon>> ARCHER_AMAZON = ENTITY_TYPES.register("raider_archer_amazon",
            () -> EntityType.Builder.of(EntityArcherAmazon::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_archer_amazon")));

    public static final Supplier<EntityType<EntityAmazonChief>> AMAZON_CHIEF = ENTITY_TYPES.register("raider_amazon_chief",
            () -> EntityType.Builder.of(EntityAmazonChief::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_amazon_chief")));

    // === Drowned Pirates ===
    public static final Supplier<EntityType<EntityDrownedPirate>> DROWNED_PIRATE = ENTITY_TYPES.register("raider_drowned_pirate",
            () -> EntityType.Builder.of(EntityDrownedPirate::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_drowned_pirate")));

    public static final Supplier<EntityType<EntityDrownedArcherPirate>> DROWNED_ARCHER_PIRATE = ENTITY_TYPES.register("raider_drowned_archer_pirate",
            () -> EntityType.Builder.of(EntityDrownedArcherPirate::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_drowned_archer_pirate")));

    public static final Supplier<EntityType<EntityDrownedCaptainPirate>> DROWNED_CAPTAIN_PIRATE = ENTITY_TYPES.register("raider_drowned_captain_pirate",
            () -> EntityType.Builder.of(EntityDrownedCaptainPirate::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(key("raider_drowned_captain_pirate")));

    /**
     * Register all raider entity types and their attributes to the mod event bus.
     */
    public static void init(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);

        modBus.addListener((EntityAttributeCreationEvent event) -> {
            // Barbarians
            event.put(BARBARIAN.get(), EntityBarbarian.createBarbarianAttributes().build());
            event.put(ARCHER_BARBARIAN.get(), EntityArcherBarbarian.createArcherBarbarianAttributes().build());
            event.put(CHIEF_BARBARIAN.get(), EntityChiefBarbarian.createChiefBarbarianAttributes().build());

            // Pirates
            event.put(PIRATE.get(), EntityPirate.createPirateAttributes().build());
            event.put(ARCHER_PIRATE.get(), EntityArcherPirate.createArcherPirateAttributes().build());
            event.put(CAPTAIN_PIRATE.get(), EntityCaptainPirate.createCaptainPirateAttributes().build());

            // Egyptians
            event.put(MUMMY.get(), EntityMummy.createMummyAttributes().build());
            event.put(ARCHER_MUMMY.get(), EntityArcherMummy.createArcherMummyAttributes().build());
            event.put(PHARAO.get(), EntityPharao.createPharaoAttributes().build());

            // Norsemen
            event.put(SHIELDMAIDEN.get(), EntityShieldmaiden.createShieldmaidenAttributes().build());
            event.put(NORSEMEN_ARCHER.get(), EntityNorsemenArcher.createNorsemenArcherAttributes().build());
            event.put(NORSEMEN_CHIEF.get(), EntityNorsemenChief.createNorsemenChiefAttributes().build());

            // Amazons
            event.put(AMAZON_SPEARMAN.get(), EntityAmazonSpearman.createAmazonSpearmanAttributes().build());
            event.put(ARCHER_AMAZON.get(), EntityArcherAmazon.createArcherAmazonAttributes().build());
            event.put(AMAZON_CHIEF.get(), EntityAmazonChief.createAmazonChiefAttributes().build());

            // Drowned Pirates
            event.put(DROWNED_PIRATE.get(), EntityDrownedPirate.createDrownedPirateAttributes().build());
            event.put(DROWNED_ARCHER_PIRATE.get(), EntityDrownedArcherPirate.createDrownedArcherPirateAttributes().build());
            event.put(DROWNED_CAPTAIN_PIRATE.get(), EntityDrownedCaptainPirate.createDrownedCaptainPirateAttributes().build());
        });
    }
}

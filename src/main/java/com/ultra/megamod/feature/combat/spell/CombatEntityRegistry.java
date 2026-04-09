package com.ultra.megamod.feature.combat.spell;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entity type registry for spell combat entities (projectiles, clouds, etc.).
 */
public class CombatEntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, "megamod");

    // Resource keys
    private static final ResourceKey<EntityType<?>> SPELL_PROJECTILE_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath("megamod", "spell_projectile"));

    private static final ResourceKey<EntityType<?>> SPELL_CLOUD_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath("megamod", "spell_cloud"));

    // Entity types
    public static final Supplier<EntityType<SpellProjectileEntity>> SPELL_PROJECTILE =
            ENTITY_TYPES.register("spell_projectile", () -> EntityType.Builder
                    .<SpellProjectileEntity>of(SpellProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .fireImmune()
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(SPELL_PROJECTILE_KEY));

    public static final Supplier<EntityType<SpellCloudEntity>> SPELL_CLOUD =
            ENTITY_TYPES.register("spell_cloud", () -> EntityType.Builder
                    .<SpellCloudEntity>of(SpellCloudEntity::new, MobCategory.MISC)
                    .sized(1.0f, 0.5f)
                    .fireImmune()
                    .clientTrackingRange(64)
                    .updateInterval(20)
                    .build(SPELL_CLOUD_KEY));

    public static void init(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}

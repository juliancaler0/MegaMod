package com.ultra.megamod.lib.spellengine;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.entity.SpellCloud;
import com.ultra.megamod.lib.spellengine.entity.SpellProjectile;
import com.ultra.megamod.lib.spellengine.network.ServerNetwork;
import com.ultra.megamod.lib.spellengine.spellbinding.SpellBindingBlock;
import com.ultra.megamod.lib.spellengine.spellbinding.SpellBindingBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * NeoForge integration bridge for the ported SpellEngine library.
 *
 * Responsibilities:
 *   - Register entity types for {@link SpellProjectile} and {@link SpellCloud} via DeferredRegister.
 *   - Register the dynamic {@code megamod:spell} datapack registry so {@code data/megamod/spell/*.json}
 *     are loaded into the {@link SpellRegistry#KEY} registry.
 *   - Force-load {@link SpellDataComponents} so the spell container / spell choice data components
 *     are registered before items are built.
 *   - Run {@link SpellEngineMod#init()} during FMLCommonSetup to wire trigger/event dispatchers.
 *   - Relies on the existing {@code PlayerEntityEvents} mixin to forward melee attacks
 *     into the library's internal event bus for MELEE_IMPACT passive resolution.
 */
public final class SpellEngineNeoForge {
    private SpellEngineNeoForge() {}

    // --- Entity types ---------------------------------------------------

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MegaMod.MODID);

    private static final ResourceKey<EntityType<?>> SPELL_PROJECTILE_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(MegaMod.MODID, "spell_projectile_se"));

    private static final ResourceKey<EntityType<?>> SPELL_CLOUD_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(MegaMod.MODID, "spell_area_effect"));

    public static final Supplier<EntityType<SpellProjectile>> SPELL_PROJECTILE =
            ENTITY_TYPES.register("spell_projectile_se", () -> EntityType.Builder
                    .<SpellProjectile>of(SpellProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .fireImmune()
                    .clientTrackingRange(128)
                    .updateInterval(2)
                    .build(SPELL_PROJECTILE_KEY));

    public static final Supplier<EntityType<SpellCloud>> SPELL_CLOUD =
            ENTITY_TYPES.register("spell_area_effect", () -> EntityType.Builder
                    .<SpellCloud>of(SpellCloud::new, MobCategory.MISC)
                    .sized(6F, 0.5F)
                    .fireImmune()
                    .clientTrackingRange(128)
                    .updateInterval(20)
                    .build(SPELL_CLOUD_KEY));

    // --- Blocks / BlockEntities ----------------------------------------
    //
    // Port note (1.21.11): SpellBindingBlock.INSTANCE and SpellBindingBlockEntity.ENTITY_TYPE are
    // created as eager static singletons (legacy SpellEngine pattern). In 1.21.11 every block
    // instance comes with an "intrusive holder" that MUST be registered before mod-loading ends,
    // or startup fails with "Some intrusive holders were not registered". We pass the existing
    // static instances through a DeferredRegister so they get bound into the real registries.

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MegaMod.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MegaMod.MODID);

    public static final Supplier<SpellBindingBlock> SPELL_BINDING_BLOCK =
            BLOCKS.register("spell_binding", () -> SpellBindingBlock.INSTANCE);

    public static final Supplier<BlockEntityType<SpellBindingBlockEntity>> SPELL_BINDING_BE_TYPE =
            BLOCK_ENTITY_TYPES.register("spell_binding", () -> SpellBindingBlockEntity.ENTITY_TYPE);

    // --- Public init ----------------------------------------------------

    /**
     * Wire all SpellEngine NeoForge-side registrations. Must be called from the mod's main
     * constructor on the mod event bus.
     */
    public static void init(IEventBus modEventBus) {
        // Ensure data components are registered before any items reference them.
        SpellDataComponents.init();

        ENTITY_TYPES.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(SpellEngineNeoForge::onNewDataPackRegistry);
        modEventBus.addListener(SpellEngineNeoForge::onCommonSetup);

        // Network payload + configuration task registration
        modEventBus.addListener(ServerNetwork::registerPayloadHandlers);
        modEventBus.addListener(ServerNetwork::registerConfigurationTasks);

        // PlayerEntityEvents mixin already bridges Player.attack -> CombatEvents.PLAYER_MELEE_ATTACK
        // so MELEE_IMPACT triggers fire server-side when `SpellTriggers.init()` registers its listener
        // during FMLCommonSetupEvent below.
    }

    // --- Mod bus handlers ----------------------------------------------

    private static void onNewDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(SpellRegistry.KEY, SpellRegistry.LOCAL_CODEC, SpellRegistry.NETWORK_CODEC_V2);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        // Wire the common init (listeners on the library's internal Event objects).
        event.enqueueWork(SpellEngineMod::init);
        // Phase G.1: level-gate for relic abilities hooks SpellEvents.CASTING_ATTEMPT
        // after SpellEngineMod.init() so its predicate runs on every cast attempt.
        event.enqueueWork(com.ultra.megamod.feature.relics.RelicLevelGate::init);

        // Once entity types are resolved, back-fill the static ENTITY_TYPE fields on the
        // legacy SpellProjectile / SpellCloud classes so `new SpellProjectile(level, owner)`
        // continues to pick up the resolved EntityType<?>.
        event.enqueueWork(() -> {
            SpellProjectile.ENTITY_TYPE = SPELL_PROJECTILE.get();
            SpellCloud.ENTITY_TYPE = SPELL_CLOUD.get();
        });
    }

}

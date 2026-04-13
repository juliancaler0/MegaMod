package com.ultra.megamod.feature.combat.paladins.entity;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PaladinEntities {
    public static final Identifier BARRIER_ID = Identifier.fromNamespaceAndPath(MegaMod.MODID, "barrier");
    public static final Identifier BANNER_ID = Identifier.fromNamespaceAndPath(MegaMod.MODID, "battle_banner");

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MegaMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BarrierEntity>> BARRIER_TYPE =
            ENTITY_TYPES.register("barrier", () ->
                    EntityType.Builder.<BarrierEntity>of(BarrierEntity::new, MobCategory.MISC)
                            .sized(1F, 1F)
                            .fireImmune()
                            .clientTrackingRange(128)
                            .updateInterval(20)
                            .build(net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE, BARRIER_ID))
            );

    public static final DeferredHolder<EntityType<?>, EntityType<BannerEntity>> BANNER_TYPE =
            ENTITY_TYPES.register("battle_banner", () ->
                    EntityType.Builder.<BannerEntity>of(BannerEntity::new, MobCategory.MISC)
                            .sized(6F, 0.5F)
                            .fireImmune()
                            .clientTrackingRange(128)
                            .updateInterval(20)
                            .build(net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE, BANNER_ID))
            );

    public static void init(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}

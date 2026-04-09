package com.ultra.megamod.feature.citizen.visitor;

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
 * Registers the visitor entity type and its attributes.
 */
public class VisitorEntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
            (ResourceKey) Registries.ENTITY_TYPE, (String) "megamod"
    );

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create((ResourceKey) Registries.ENTITY_TYPE,
                (Identifier) Identifier.fromNamespaceAndPath("megamod", name));
    }

    public static final Supplier<EntityType<VisitorEntity>> VISITOR = ENTITY_TYPES.register("visitor",
            () -> EntityType.Builder.of(VisitorEntity::new, (MobCategory) MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(10)
                    .build(key("visitor")));

    /**
     * Register entity types and attribute creation event to the mod event bus.
     */
    public static void init(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);

        modBus.addListener((EntityAttributeCreationEvent event) -> {
            event.put(VISITOR.get(), VisitorEntity.createVisitorAttributes().build());
        });

        // Client renderer registration is handled in MegaModClient
    }
}

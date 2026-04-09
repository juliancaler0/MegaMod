package com.ultra.megamod.feature.citizen;

import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CitizenRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
        (ResourceKey) Registries.ENTITY_TYPE, (String) "megamod"
    );

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems((String) "megamod");

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(
        (ResourceKey) Registries.CREATIVE_MODE_TAB, (String) "megamod"
    );

    // Resource keys
    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create((ResourceKey) Registries.ENTITY_TYPE,
            (Identifier) Identifier.fromNamespaceAndPath("megamod", name));
    }

    // MineColonies-ported citizen entity (handler composition pattern)
    public static final Supplier<EntityType<MCEntityCitizen>> MC_CITIZEN = ENTITY_TYPES.register("mc_citizen",
        () -> EntityType.Builder.of(MCEntityCitizen::new, (MobCategory) MobCategory.CREATURE)
            .sized(0.6f, 1.95f).clientTrackingRange(10).build(key("mc_citizen")));

    // MC Citizen spawn egg
    public static final DeferredItem<SpawnEggItem> MC_CITIZEN_SPAWN_EGG = ITEMS.registerItem("mc_citizen_spawn_egg",
        p -> new SpawnEggItem(p.spawnEgg(MC_CITIZEN.get())));

    // Creative Tab
    public static final Supplier<CreativeModeTab> CITIZEN_TAB = CREATIVE_MODE_TABS.register("megamod_citizens_tab",
        () -> CreativeModeTab.builder()
            .title((Component) Component.literal((String) "MegaMod - Citizens"))
            .icon(() -> new ItemStack((ItemLike) Items.VILLAGER_SPAWN_EGG))
            .displayItems((parameters, output) -> {
                output.accept((ItemLike) MC_CITIZEN_SPAWN_EGG.get());
            }).build());

    public static void init(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
        ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        CitizenSoundRegistry.SOUNDS.register(modBus);

        modBus.addListener((EntityAttributeCreationEvent event) -> {
            // MC Citizen (MineColonies-ported)
            event.put(MC_CITIZEN.get(), MCEntityCitizen.createCitizenAttributes().build());
        });
    }

    public static net.minecraft.world.entity.EntityType<?> getEntityTypeForJob(com.ultra.megamod.feature.citizen.data.CitizenJob job) {
        // All jobs now use the unified MC_CITIZEN entity type
        return MC_CITIZEN.get();
    }
}

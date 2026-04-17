package com.ultra.megamod.feature.relics;

import com.ultra.megamod.MegaMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

/**
 * Stub relic registry — custom research-table relic system was scrapped (task #52).
 * Real source-pattern Relics-1.21.1 port will be re-implemented later.
 *
 * <p>This file kept alive to own the umbrella creative tabs ({@code megamod_weapons_tab},
 * {@code megamod_armor_tab}, {@code megamod_relics_tab}) that {@link com.ultra.megamod.feature.combat.CombatCreativeTab}
 * populates via {@link net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent}.</p>
 */
public final class RelicRegistry {
    private RelicRegistry() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MegaMod.MODID);

    public static final Supplier<CreativeModeTab> WEAPONS_TAB =
            CREATIVE_MODE_TABS.register("megamod_weapons_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.literal("MegaMod - Weapons"))
                            .icon(() -> new ItemStack(Items.NETHERITE_SWORD))
                            .displayItems((params, output) -> {})
                            .build());

    public static final Supplier<CreativeModeTab> ARMOR_TAB =
            CREATIVE_MODE_TABS.register("megamod_armor_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.literal("MegaMod - Armor"))
                            .icon(() -> new ItemStack(Items.NETHERITE_CHESTPLATE))
                            .displayItems((params, output) -> {})
                            .build());

    public static final Supplier<CreativeModeTab> RELICS_TAB =
            CREATIVE_MODE_TABS.register("megamod_relics_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.literal("MegaMod - Relics"))
                            .icon(() -> new ItemStack(Items.ENDER_EYE))
                            .displayItems((params, output) -> {})
                            .build());

    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        // CombatCreativeTab.onBuildContents listener is already registered from MegaMod.java
    }
}

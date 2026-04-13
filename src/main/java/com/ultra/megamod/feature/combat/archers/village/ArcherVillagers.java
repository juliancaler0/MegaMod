package com.ultra.megamod.feature.combat.archers.village;

import com.google.common.collect.ImmutableSet;
import com.ultra.megamod.feature.combat.archers.ArchersMod;
import com.ultra.megamod.feature.combat.archers.block.ArcherBlocks;
import com.ultra.megamod.feature.combat.archers.item.ArcherWeapons;
import com.ultra.megamod.feature.combat.archers.item.ArcherArmors;
import com.ultra.megamod.feature.combat.archers.content.ArcherSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.function.Supplier;

public class ArcherVillagers {
    public static final String ARCHERY_ARTISAN = "archery_artisan";
    public static final Identifier POI_ID = Identifier.fromNamespaceAndPath(ArchersMod.ID, ARCHERY_ARTISAN);

    private static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, ArchersMod.ID);
    private static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, ArchersMod.ID);

    private static final ResourceKey<PoiType> POI_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, POI_ID);
    private static final ResourceKey<VillagerProfession> PROFESSION_KEY = ResourceKey.create(Registries.VILLAGER_PROFESSION, Identifier.fromNamespaceAndPath(ArchersMod.ID, ARCHERY_ARTISAN));

    private static final Supplier<PoiType> ARCHERY_POI = POI_TYPES.register(ARCHERY_ARTISAN, () -> {
        var blockStates = ImmutableSet.copyOf(ArcherBlocks.WORKBENCH.block().getStateDefinition().getPossibleStates());
        return new PoiType(blockStates, 1, 10);
    });

    private static final Supplier<VillagerProfession> ARCHERY_PROFESSION = PROFESSIONS.register(ARCHERY_ARTISAN, () -> {
        var professionId = Identifier.fromNamespaceAndPath(ArchersMod.ID, ARCHERY_ARTISAN);
        return new VillagerProfession(
                net.minecraft.network.chat.Component.translatable("entity.minecraft.villager." + professionId.getNamespace() + "." + professionId.getPath()),
                (entry) -> entry.is(POI_KEY),
                (entry) -> entry.is(POI_KEY),
                ImmutableSet.of(),
                ImmutableSet.of(),
                ArcherSounds.WORKBENCH.soundEvent()
        );
    });

    public static void register(IEventBus modEventBus) {
        POI_TYPES.register(modEventBus);
        PROFESSIONS.register(modEventBus);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ArcherVillagers::onVillagerTrades);
    }

    private static void onVillagerTrades(VillagerTradesEvent event) {
        if (!event.getType().equals(PROFESSION_KEY)) return;

        // Level 1
        event.getTrades().get(1).add(new VillagerTrades.ItemsForEmeralds(Items.ARROW, 2, 8, 128, 3, 0.01f));
        event.getTrades().get(1).add(new VillagerTrades.EmeraldForItems(Items.LEATHER, 8, 12, 6));

        // Level 2
        event.getTrades().get(2).add(new VillagerTrades.ItemsForEmeralds(ArcherWeapons.composite_longbow.item(), 6, 1, 16));
        event.getTrades().get(2).add(new VillagerTrades.ItemsForEmeralds(ArcherArmors.archerArmorSet_T1.head, 15, 1, 18));
        event.getTrades().get(2).add(new VillagerTrades.EmeraldForItems(Items.STRING, 6, 12, 8));

        // Level 3
        event.getTrades().get(3).add(new VillagerTrades.ItemsForEmeralds(ArcherArmors.archerArmorSet_T1.feet, 15, 1, 18));
        event.getTrades().get(3).add(new VillagerTrades.EmeraldForItems(Items.REDSTONE, 12, 12, 5));
        event.getTrades().get(3).add(new VillagerTrades.ItemsForEmeralds(ArcherArmors.archerArmorSet_T1.legs, 15, 1, 18));

        // Level 4
        event.getTrades().get(4).add(new VillagerTrades.ItemsForEmeralds(ArcherArmors.archerArmorSet_T1.chest, 15, 1, 18));
        event.getTrades().get(4).add(new VillagerTrades.ItemsForEmeralds(Items.TURTLE_SCUTE, 20, 12, 10));

        // Level 5 - enchanted weapons
        event.getTrades().get(5).add(new VillagerTrades.EnchantedItemForEmeralds(ArcherWeapons.royal_longbow.item(), 40, 3, 30, 0F));
        event.getTrades().get(5).add(new VillagerTrades.EnchantedItemForEmeralds(ArcherWeapons.mechanic_shortbow.item(), 40, 3, 30, 0F));
        event.getTrades().get(5).add(new VillagerTrades.EnchantedItemForEmeralds(ArcherWeapons.rapid_crossbow.item(), 40, 3, 30, 0F));
        event.getTrades().get(5).add(new VillagerTrades.EnchantedItemForEmeralds(ArcherWeapons.heavy_crossbow.item(), 40, 3, 30, 0F));
    }
}

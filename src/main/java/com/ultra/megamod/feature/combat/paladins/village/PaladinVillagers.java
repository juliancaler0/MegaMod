package com.ultra.megamod.feature.combat.paladins.village;

import com.google.common.collect.ImmutableSet;
import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.paladins.block.PaladinBlocks;
import com.ultra.megamod.feature.combat.paladins.content.PaladinSounds;
import com.ultra.megamod.feature.combat.paladins.item.PaladinWeapons;
import com.ultra.megamod.feature.combat.paladins.item.armor.Armors;
import com.ultra.megamod.feature.combat.runes.RuneRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.List;

public class PaladinVillagers {
    public static final String PALADIN_MERCHANT = "monk";

    public static PoiType registerPOI(String name, net.minecraft.world.level.block.Block block) {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, name);
        var poiType = new PoiType(ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates()), 1, 10);
        return Registry.register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, id, poiType);
    }

    public static VillagerProfession registerProfession(String name, ResourceKey<PoiType> workStation) {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, name);
        return Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, id, new VillagerProfession(
                net.minecraft.network.chat.Component.translatable("entity.minecraft.villager." + id.getNamespace() + "." + id.getPath()),
                (entry) -> entry.is(workStation),
                (entry) -> entry.is(workStation),
                ImmutableSet.of(),
                ImmutableSet.of(),
                PaladinSounds.paladin_armor_equip.soundEvent())
        );
    }

    public static void registerPOI() {
        registerPOI(PALADIN_MERCHANT, PaladinBlocks.MONK_WORKBENCH.get());
    }

    public static void registerVillagers() {
        var profession = registerProfession(
                PALADIN_MERCHANT,
                ResourceKey.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(),
                        Identifier.fromNamespaceAndPath(MegaMod.MODID, PALADIN_MERCHANT)));

        LinkedHashMap<Integer, List<VillagerTrades.ItemListing>> trades = new LinkedHashMap<>();
        trades.put(1, List.of(
                new VillagerTrades.ItemsForEmeralds(RuneRegistry.HEALING_RUNE.get(), 2, 8, 128, 1, 0.01f),
                new VillagerTrades.ItemsForEmeralds(PaladinWeapons.acolyte_wand.item(), 4, 1, 12, 5),
                new VillagerTrades.ItemsForEmeralds(PaladinWeapons.wooden_great_hammer.item(), 8, 1, 12, 8)
        ));
        trades.put(2, List.of(
                new VillagerTrades.EmeraldForItems(Items.WHITE_WOOL, 5, 12, 5, 8),
                new VillagerTrades.EmeraldForItems(Items.IRON_INGOT, 6, 12, 5, 8),
                new VillagerTrades.EmeraldForItems(Items.IRON_NUGGET, 6, 12, 5, 8),
                new VillagerTrades.EmeraldForItems(Items.GOLD_INGOT, 6, 12, 5, 8)
        ));
        trades.put(3, List.of(
                new VillagerTrades.ItemsForEmeralds(Armors.paladinArmorSet_t1.head, 15, 1, 12, 13),
                new VillagerTrades.ItemsForEmeralds(Armors.paladinArmorSet_t1.feet, 15, 1, 12, 13),
                new VillagerTrades.ItemsForEmeralds(Armors.priestArmorSet_t1.head, 15, 1, 12, 13),
                new VillagerTrades.ItemsForEmeralds(Armors.priestArmorSet_t1.feet, 15, 1, 12, 13)
        ));
        trades.put(4, List.of(
                new VillagerTrades.ItemsForEmeralds(Armors.paladinArmorSet_t1.chest, 20, 1, 12, 15),
                new VillagerTrades.ItemsForEmeralds(Armors.paladinArmorSet_t1.legs, 20, 1, 12, 15),
                new VillagerTrades.ItemsForEmeralds(Armors.priestArmorSet_t1.chest, 20, 1, 12, 15),
                new VillagerTrades.ItemsForEmeralds(Armors.priestArmorSet_t1.legs, 20, 1, 12, 15)
        ));

        for (var entry : trades.entrySet()) {
            // NeoForge villager trade registration
            // These will be added via event handler
        }
    }

    public static void register() {
        registerPOI();
        registerVillagers();
    }
}

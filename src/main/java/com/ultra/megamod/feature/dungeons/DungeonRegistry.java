/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredItem
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.DeferredRegister$Items
 */
package com.ultra.megamod.feature.dungeons;

import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.items.DungeonKeyItem;
import com.ultra.megamod.feature.dungeons.items.SoulAnchorItem;
import com.ultra.megamod.feature.dungeons.loot.DungeonExclusiveItems;
import com.ultra.megamod.feature.dungeons.network.DungeonNetwork;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DungeonRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems((String)"megamod");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create((ResourceKey)Registries.CREATIVE_MODE_TAB, (String)"megamod");
    public static final DeferredItem<DungeonKeyItem> DUNGEON_KEY_NORMAL = ITEMS.registerItem("dungeon_key_normal", props -> new DungeonKeyItem(DungeonTier.NORMAL, (Item.Properties)props), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<DungeonKeyItem> DUNGEON_KEY_HARD = ITEMS.registerItem("dungeon_key_hard", props -> new DungeonKeyItem(DungeonTier.HARD, (Item.Properties)props), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<DungeonKeyItem> DUNGEON_KEY_NIGHTMARE = ITEMS.registerItem("dungeon_key_nightmare", props -> new DungeonKeyItem(DungeonTier.NIGHTMARE, (Item.Properties)props), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<DungeonKeyItem> DUNGEON_KEY_INFERNAL = ITEMS.registerItem("dungeon_key_infernal", props -> new DungeonKeyItem(DungeonTier.INFERNAL, (Item.Properties)props), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<DungeonKeyItem> DUNGEON_KEY_MYTHIC = ITEMS.registerItem("dungeon_key_mythic", props -> new DungeonKeyItem(DungeonTier.MYTHIC, (Item.Properties)props), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<DungeonKeyItem> DUNGEON_KEY_ETERNAL = ITEMS.registerItem("dungeon_key_eternal", props -> new DungeonKeyItem(DungeonTier.ETERNAL, (Item.Properties)props), () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<SoulAnchorItem> SOUL_ANCHOR = ITEMS.registerItem("soul_anchor", props -> new SoulAnchorItem((Item.Properties)props), () -> new Item.Properties().stacksTo(1));
    public static final ResourceKey<JukeboxSong> HOUSE_MONEY_SONG_KEY = ResourceKey.create((ResourceKey) Registries.JUKEBOX_SONG, (Identifier) Identifier.fromNamespaceAndPath((String) "megamod", (String) "house_money"));
    public static final DeferredItem<Item> MUSIC_DISC_HOUSE_MONEY = ITEMS.registerSimpleItem("music_disc_house_money", () -> new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(HOUSE_MONEY_SONG_KEY));
    public static final Supplier<CreativeModeTab> DUNGEONS_TAB = CREATIVE_MODE_TABS.register("megamod_dungeons_tab", () -> CreativeModeTab.builder().title((Component)Component.literal((String)"MegaMod - Dungeons")).icon(() -> new ItemStack((ItemLike)Items.TRIAL_KEY)).displayItems((parameters, output) -> {
        output.accept((ItemLike)DUNGEON_KEY_NORMAL.get());
        output.accept((ItemLike)DUNGEON_KEY_HARD.get());
        output.accept((ItemLike)DUNGEON_KEY_NIGHTMARE.get());
        output.accept((ItemLike)DUNGEON_KEY_INFERNAL.get());
        output.accept((ItemLike)DUNGEON_KEY_MYTHIC.get());
        output.accept((ItemLike)DUNGEON_KEY_ETERNAL.get());
        output.accept((ItemLike)SOUL_ANCHOR.get());
        output.accept((ItemLike)DungeonExclusiveItems.VOID_SHARD.get());
        output.accept((ItemLike)DungeonExclusiveItems.BOSS_TROPHY.get());
        output.accept((ItemLike)DungeonExclusiveItems.DUNGEON_MAP.get());
        output.accept((ItemLike)DungeonExclusiveItems.INFERNAL_ESSENCE.get());
        output.accept((ItemLike)DungeonExclusiveItems.WARP_STONE.get());
        output.accept((ItemLike)DungeonEntityRegistry.FOG_WALL_ITEM.get());
        output.accept((ItemLike)DungeonEntityRegistry.DUNGEON_ALTAR_ITEM.get());
        output.accept((ItemLike)DungeonEntityRegistry.DUNGEON_MINI_KEY.get());
        output.accept((ItemLike)DungeonExclusiveItems.WRAITH_TROPHY_ITEM.get());
        output.accept((ItemLike)DungeonExclusiveItems.OSSUKAGE_TROPHY_ITEM.get());
        output.accept((ItemLike)DungeonExclusiveItems.DUNGEON_KEEPER_TROPHY_ITEM.get());
        output.accept((ItemLike)DungeonExclusiveItems.FROSTMAW_TROPHY_ITEM.get());
        output.accept((ItemLike)DungeonExclusiveItems.WROUGHTNAUT_TROPHY_ITEM.get());
        output.accept((ItemLike)DungeonExclusiveItems.UMVUTHI_TROPHY_ITEM.get());
        output.accept((ItemLike)DungeonExclusiveItems.CHAOS_SPAWNER_TROPHY_ITEM.get());
        output.accept((ItemLike)DungeonExclusiveItems.SCULPTOR_TROPHY_ITEM.get());
        output.accept((ItemLike)DungeonExclusiveItems.STRANGE_MEAT.get());
        output.accept((ItemLike)DungeonExclusiveItems.LIVING_DIVINING_ROD.get());
        output.accept((ItemLike)DungeonExclusiveItems.ABSORPTION_ORB.get());
        output.accept((ItemLike)DungeonExclusiveItems.CERULEAN_INGOT.get());
        output.accept((ItemLike)DungeonExclusiveItems.CRYSTALLINE_SHARD.get());
        output.accept((ItemLike)DungeonExclusiveItems.SPECTRAL_SILK.get());
        output.accept((ItemLike)DungeonExclusiveItems.UMBRA_INGOT.get());
        output.accept((ItemLike)MUSIC_DISC_HOUSE_MONEY.get());
    }).build());

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        modBus.addListener(DungeonNetwork::registerPayloads);
    }
}


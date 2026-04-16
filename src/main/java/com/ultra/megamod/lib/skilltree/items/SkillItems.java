package com.ultra.megamod.lib.skilltree.items;

import com.ultra.megamod.lib.skilltree.SkillTreeMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SkillItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, SkillTreeMod.NAMESPACE);

    public record LoreLine(String text, ChatFormatting formatting) {
        public record Translatable(String translationKey, LoreLine line) { }
    }
    public record Entry(Identifier id, String title, List<LoreLine> lore,
                        Function<Item.Properties, Item> factory, Item.Properties settings,
                        Supplier<Item> itemSupplier) {
        public Entry(Identifier id, String title, List<LoreLine> lore, Item.Properties settings) {
            this(id, title, lore, Item::new, settings);
        }
        public Entry(Identifier id, String title, List<LoreLine> lore,
                     Function<Item.Properties, Item> factory, Item.Properties settings) {
            this(id, title, lore, factory, settings, null);
        }
        public Item item() {
            return itemSupplier != null ? itemSupplier.get() : null;
        }
        public List<LoreLine.Translatable> loreTranslation() {
            var keys = new ArrayList<LoreLine.Translatable>();
            int index = 0;
            for (var line : lore) {
                String key = "item." + id.getNamespace() + "." + id.getPath() + ".lore." + index++;
                keys.add(new LoreLine.Translatable(key, line));
            }
            return keys;
        }
    }
    public static final ArrayList<Entry> ENTRIES = new ArrayList<>();

    public static final Entry ORB_OF_OBLIVION;

    static {
        ORB_OF_OBLIVION = new Entry(
                Identifier.fromNamespaceAndPath(SkillTreeMod.NAMESPACE, "orb_of_oblivion"),
                "Orb of Oblivion",
                List.of(new LoreLine("Reset all skill points spend on the Class Skill Tree.", ChatFormatting.GRAY)),
                RespecItem::new,
                new Item.Properties().rarity(Rarity.UNCOMMON).durability(1)
        );
        ENTRIES.add(ORB_OF_OBLIVION);
    }

    public static void register(IEventBus modEventBus) {
        for (Entry entry : ENTRIES) {
            var holder = ITEMS.register(entry.id().getPath(), () -> entry.factory().apply(entry.settings()));
            var newEntry = new Entry(entry.id(), entry.title(), entry.lore(), entry.factory(), entry.settings(), holder);
            ENTRIES.set(ENTRIES.indexOf(entry), newEntry);
        }
        ITEMS.register(modEventBus);
    }
}

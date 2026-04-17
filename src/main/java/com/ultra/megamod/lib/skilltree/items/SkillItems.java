package com.ultra.megamod.lib.skilltree.items;

import com.ultra.megamod.lib.skilltree.SkillTreeMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class SkillItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SkillTreeMod.NAMESPACE);

    public record LoreLine(String text, ChatFormatting formatting) {
        public record Translatable(String translationKey, LoreLine line) { }
    }
    // `propsConfig` is applied to the Item.Properties handed in by DeferredRegister
    // (which has the registry id pre-set, mandatory in MC 1.21.2+). Callers should not
    // pre-build properties, or the item would register without its id and NeoForge
    // fails with "Item id not set".
    public record Entry(Identifier id, String title, List<LoreLine> lore,
                        Function<Item.Properties, Item> factory,
                        UnaryOperator<Item.Properties> propsConfig,
                        Supplier<Item> itemSupplier) {
        public Entry(Identifier id, String title, List<LoreLine> lore,
                     Function<Item.Properties, Item> factory,
                     UnaryOperator<Item.Properties> propsConfig) {
            this(id, title, lore, factory, propsConfig, null);
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
                props -> props.rarity(Rarity.UNCOMMON).durability(1)
        );
        ENTRIES.add(ORB_OF_OBLIVION);
    }

    public static void register(IEventBus modEventBus) {
        for (int i = 0; i < ENTRIES.size(); i++) {
            Entry entry = ENTRIES.get(i);
            var holder = ITEMS.registerItem(entry.id().getPath(),
                    props -> entry.factory().apply(entry.propsConfig().apply(props)));
            ENTRIES.set(i, new Entry(entry.id(), entry.title(), entry.lore(),
                    entry.factory(), entry.propsConfig(), holder));
        }
        ITEMS.register(modEventBus);
    }
}

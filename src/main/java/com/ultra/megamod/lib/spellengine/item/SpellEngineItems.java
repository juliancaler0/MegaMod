package com.ultra.megamod.lib.spellengine.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.api.tags.SpellTags;
import com.ultra.megamod.lib.spellengine.compat.SlotModCompat;
import com.ultra.megamod.lib.spellengine.spellbinding.SpellBinding;
import com.ultra.megamod.lib.spellengine.spellbinding.SpellBindingBlock;
import com.ultra.megamod.lib.spellengine.rpg_series.item.RPGItemRegistry;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Comparator;
import java.util.function.Supplier;

public class SpellEngineItems {

    public static class Group {
        public static Identifier ID = Identifier.fromNamespaceAndPath("megamod", "generic");
    }

    public static Item SCROLL;
    public static Item SPELL_BOOK;

    public static void register() {
        RPGItemRegistry.registerItem(SpellBinding.ID.getPath(), (props) -> SpellBindingBlock.ITEM);

        RPGItemRegistry.registerItem(ScrollItem.ID.getPath(), (props) -> {
            props.stacksTo(1);
            var scrollArgs = new SlotModCompat.SpellScrollArs(props);
            var scrollFactory = SlotModCompat.spellScrollFactory;
            SCROLL = scrollFactory != null ? scrollFactory.apply(scrollArgs) : new ScrollItem(scrollArgs.settings());
            return SCROLL;
        });

        RPGItemRegistry.registerItem(UniversalSpellBookItem.ID.getPath(), (props) -> {
            props.stacksTo(1);
            var bookArgs = new SlotModCompat.SpellBookArgs(props);
            var bookFactory = SlotModCompat.spellBookFactory;
            SPELL_BOOK = bookFactory != null ? bookFactory.apply(bookArgs) : new UniversalSpellBookItem(bookArgs.settings());
            return SPELL_BOOK;
        });
    }
}

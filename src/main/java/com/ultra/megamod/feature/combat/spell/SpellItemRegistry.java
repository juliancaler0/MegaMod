package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.MegaMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Registry for spell books (offhand items granting spell access).
 *
 * <p>Class-specific spell books (Paladin Libram, Archery Manual, Rogue Manual,
 * Warrior Codex) and spell scrolls (SCROLL_*) were retired alongside the
 * class-selection + spell-unlock systems. Only the four school books remain;
 * the new skill tree port will reintroduce unlock content via its own system.</p>
 */
public class SpellItemRegistry {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MegaMod.MODID);

    // ═══════════════════════════════════════════════════════════════
    // SPELL BINDING TABLE
    // ═══════════════════════════════════════════════════════════════

    public static final net.neoforged.neoforge.registries.DeferredBlock<SpellBindingTableBlock> SPELL_BINDING_TABLE_BLOCK =
            BLOCKS.registerBlock("spell_binding_table",
                    SpellBindingTableBlock::new,
                    () -> BlockBehaviour.Properties.of()
                            .strength(5.0f, 1200.0f).sound(SoundType.WOOD).lightLevel(s -> 7));

    public static final DeferredItem<BlockItem> SPELL_BINDING_TABLE_ITEM =
            ITEMS.registerSimpleBlockItem(SPELL_BINDING_TABLE_BLOCK);

    // ═══════════════════════════════════════════════════════════════
    // SPELL BOOKS — hold in offhand to gain access to all school spells
    // ═══════════════════════════════════════════════════════════════

    public static final DeferredItem<SpellBookItem> ARCANE_SPELL_BOOK = ITEMS.registerItem("arcane_spell_book",
        props -> new SpellBookItem((Item.Properties) props, "ARCANE", "Arcane", 0xFF7E3BFF),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellBookItem> FIRE_SPELL_BOOK = ITEMS.registerItem("fire_spell_book",
        props -> new SpellBookItem((Item.Properties) props, "FIRE", "Fire", 0xFFFF6B1A),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellBookItem> FROST_SPELL_BOOK = ITEMS.registerItem("frost_spell_book",
        props -> new SpellBookItem((Item.Properties) props, "FROST", "Frost", 0xFF4DA6FF),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellBookItem> HEALING_SPELL_BOOK = ITEMS.registerItem("healing_spell_book",
        props -> new SpellBookItem((Item.Properties) props, "HEALING", "Healing", 0xFFCCFF00),
        () -> new Item.Properties().stacksTo(1));

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }

    // ─── Loot tier helpers ───

    /** Spell scrolls — retired with the class-selection system. Empty list until the overhaul. */
    public static List<Item> getNormalTierScrolls() {
        return List.of();
    }

    /** Spell books for Hard dungeon tier loot tables. */
    public static List<Item> getHardTierBooks() {
        return List.of(
            ARCANE_SPELL_BOOK.get(), FIRE_SPELL_BOOK.get(),
            FROST_SPELL_BOOK.get(), HEALING_SPELL_BOOK.get()
        );
    }
}

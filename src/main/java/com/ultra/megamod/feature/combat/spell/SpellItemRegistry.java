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
 * <p>Four school books (Arcane, Fire, Frost, Healing) plus five class-parity
 * books (Archer, Rogue, Warrior, Paladin, Priest) matching source mod naming.
 * The class-specific books returned after the class-selection retirement
 * because the spell-books tag, recipes, and loot tables still reference them;
 * the skill tree port will wire them into its unlock flow.</p>
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

    // Class-parity spell books (match source mod naming for port compatibility)
    public static final DeferredItem<SpellBookItem> ARCHER_SPELL_BOOK = ITEMS.registerItem("archer_spell_book",
        props -> new SpellBookItem((Item.Properties) props, "RANGED", "Archer", 0xFF2D8B2D),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellBookItem> ROGUE_SPELL_BOOK = ITEMS.registerItem("rogue_spell_book",
        props -> new SpellBookItem((Item.Properties) props, "SHADOW", "Rogue", 0xFF4D004D),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellBookItem> WARRIOR_SPELL_BOOK = ITEMS.registerItem("warrior_spell_book",
        props -> new SpellBookItem((Item.Properties) props, "MELEE", "Warrior", 0xFF8B0000),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellBookItem> PALADIN_SPELL_BOOK = ITEMS.registerItem("paladin_spell_book",
        props -> new SpellBookItem((Item.Properties) props, "HOLY", "Paladin", 0xFFFFD700),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellBookItem> PRIEST_SPELL_BOOK = ITEMS.registerItem("priest_spell_book",
        props -> new SpellBookItem((Item.Properties) props, "HEALING", "Priest", 0xFFFFFAF0),
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
            FROST_SPELL_BOOK.get(), HEALING_SPELL_BOOK.get(),
            ARCHER_SPELL_BOOK.get(), ROGUE_SPELL_BOOK.get(),
            WARRIOR_SPELL_BOOK.get(), PALADIN_SPELL_BOOK.get(),
            PRIEST_SPELL_BOOK.get()
        );
    }
}

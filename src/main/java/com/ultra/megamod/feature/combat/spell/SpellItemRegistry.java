package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Registry for spell books (offhand items granting spell access) and
 * spell scrolls (consumable items that permanently teach spells).
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

    // Class-specific spell books (filter by classRequirement, not school)
    public static final DeferredItem<SpellBookItem> PALADIN_LIBRAM = ITEMS.registerItem("paladin_libram",
        props -> new SpellBookItem((Item.Properties) props, "HEALING", "Paladin", 0xFFFFDD44, "PALADIN"),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellBookItem> ARCHERY_MANUAL = ITEMS.registerItem("archery_manual",
        props -> new SpellBookItem((Item.Properties) props, "PHYSICAL_RANGED", "Archery", 0xFF88CC44, "RANGER"),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellBookItem> ROGUE_MANUAL = ITEMS.registerItem("rogue_manual",
        props -> new SpellBookItem((Item.Properties) props, "PHYSICAL_MELEE", "Rogue", 0xFF8844AA, "ROGUE"),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellBookItem> WARRIOR_CODEX = ITEMS.registerItem("warrior_codex",
        props -> new SpellBookItem((Item.Properties) props, "PHYSICAL_MELEE", "Warrior", 0xFFCC4422, "WARRIOR"),
        () -> new Item.Properties().stacksTo(1));

    // ═══════════════════════════════════════════════════════════════
    // SPELL SCROLLS — consumable items that permanently teach a spell
    // ═══════════════════════════════════════════════════════════════

    public static final DeferredItem<SpellScrollItem> SCROLL_FIREBALL = ITEMS.registerItem("scroll_fireball",
        props -> new SpellScrollItem((Item.Properties) props, "fireball"),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellScrollItem> SCROLL_FROSTBOLT = ITEMS.registerItem("scroll_frostbolt",
        props -> new SpellScrollItem((Item.Properties) props, "frostbolt"),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellScrollItem> SCROLL_ARCANE_BOLT = ITEMS.registerItem("scroll_arcane_bolt",
        props -> new SpellScrollItem((Item.Properties) props, "arcane_bolt"),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellScrollItem> SCROLL_HEAL = ITEMS.registerItem("scroll_heal",
        props -> new SpellScrollItem((Item.Properties) props, "heal"),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellScrollItem> SCROLL_FLASH_HEAL = ITEMS.registerItem("scroll_flash_heal",
        props -> new SpellScrollItem((Item.Properties) props, "flash_heal"),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellScrollItem> SCROLL_SHADOW_STEP = ITEMS.registerItem("scroll_shadow_step",
        props -> new SpellScrollItem((Item.Properties) props, "shadow_step"),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellScrollItem> SCROLL_POWER_SHOT = ITEMS.registerItem("scroll_power_shot",
        props -> new SpellScrollItem((Item.Properties) props, "power_shot"),
        () -> new Item.Properties().stacksTo(1));

    public static final DeferredItem<SpellScrollItem> SCROLL_CHARGE = ITEMS.registerItem("scroll_charge",
        props -> new SpellScrollItem((Item.Properties) props, "charge"),
        () -> new Item.Properties().stacksTo(1));

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }

    // ─── Loot tier helpers ───

    /**
     * Spell scrolls for Normal dungeon tier loot tables.
     */
    public static List<Item> getNormalTierScrolls() {
        return List.of(
            SCROLL_FIREBALL.get(), SCROLL_FROSTBOLT.get(), SCROLL_ARCANE_BOLT.get(),
            SCROLL_HEAL.get(), SCROLL_FLASH_HEAL.get(),
            SCROLL_SHADOW_STEP.get(), SCROLL_POWER_SHOT.get(), SCROLL_CHARGE.get()
        );
    }

    /**
     * Spell books for Hard dungeon tier loot tables.
     */
    public static List<Item> getHardTierBooks() {
        return List.of(
            ARCANE_SPELL_BOOK.get(), FIRE_SPELL_BOOK.get(),
            FROST_SPELL_BOOK.get(), HEALING_SPELL_BOOK.get(),
            PALADIN_LIBRAM.get(), ARCHERY_MANUAL.get(),
            ROGUE_MANUAL.get(), WARRIOR_CODEX.get()
        );
    }
}

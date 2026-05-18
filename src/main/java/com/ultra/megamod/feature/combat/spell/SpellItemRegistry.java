package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.spellengine.api.tags.SpellTags;
import com.ultra.megamod.lib.spellengine.item.SpellEngineItems;
import com.ultra.megamod.lib.spellengine.item.UniversalSpellBookItem;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Registry for the spell binding table block. Spell books themselves are
 * source-pattern: a single {@link UniversalSpellBookItem} registered as
 * {@code megamod:spell_book} via {@link SpellEngineItems}, with variants
 * distinguished by an applied {@code spell_book/<school>} tag carried in
 * the item's data components. Acquired via the SpellBindingTable or as
 * dungeon loot ({@link #randomSchoolBookStack}).
 */
public class SpellItemRegistry {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MegaMod.MODID);

    public static final net.neoforged.neoforge.registries.DeferredBlock<SpellBindingTableBlock> SPELL_BINDING_TABLE_BLOCK =
            BLOCKS.registerBlock("spell_binding_table",
                    SpellBindingTableBlock::new,
                    () -> BlockBehaviour.Properties.of()
                            .strength(5.0f, 1200.0f).sound(SoundType.WOOD).lightLevel(s -> 7));

    public static final DeferredItem<BlockItem> SPELL_BINDING_TABLE_ITEM =
            ITEMS.registerSimpleBlockItem(SPELL_BINDING_TABLE_BLOCK);

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
    }

    /**
     * Pools available as loot-dropped spell book variants. Matches the eight
     * spell_book/<pool> tags present under data/megamod/tags/spell/spell_book/.
     * "Healing" isn't a pool — paladin and priest cover that role per source.
     */
    private static final List<String> LOOT_SCHOOLS = List.of(
            "arcane", "fire", "frost",
            "paladin", "priest", "archer", "rogue", "warrior");

    /**
     * Returns a configured universal spell book stack for the given school
     * (e.g. {@code "arcane"}, {@code "fire"}). The applied tag is
     * {@code megamod:spell_book/<school>} which drives the item's model,
     * display name, and bound spell pool.
     */
    public static ItemStack bookStackForSchool(String school) {
        var stack = new ItemStack(SpellEngineItems.SPELL_BOOK);
        UniversalSpellBookItem.applyFromTag(stack, SpellTags.spellBook(MegaMod.MODID, school));
        return stack;
    }

    /** Loot-pool helper: random base-school spell book ItemStack. */
    public static ItemStack randomSchoolBookStack(RandomSource random) {
        return bookStackForSchool(LOOT_SCHOOLS.get(random.nextInt(LOOT_SCHOOLS.size())));
    }
}

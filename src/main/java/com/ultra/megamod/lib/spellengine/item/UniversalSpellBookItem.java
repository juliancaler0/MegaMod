package com.ultra.megamod.lib.spellengine.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.TagKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.locale.Language;
import net.minecraft.world.item.Rarity;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainer;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainerTemplates;
import com.ultra.megamod.lib.spellengine.api.tags.SpellTags;

/**
 * A universal spell book item that derives its configuration from data components.
 * Variants are created by applying different spell tags to the same item.
 */
public class UniversalSpellBookItem extends Item {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("megamod", "spell_book");

    public UniversalSpellBookItem(Item.Properties settings) {
        super(settings);
    }

    /**
     * Apply spell book configuration to an ItemStack based on a tag.
     * @param itemStack The stack to configure
     * @param tag The spell tag (e.g., wizards:spell_book/fire)
     * @return true if configuration was successful
     */
    public static boolean applyFromTag(ItemStack itemStack, TagKey<Spell> tag) {
        var tagPath = tag.location().getPath();
        if (!tagPath.startsWith(SpellTags.SPELL_BOOK_PREFIX)) {
            return false;
        }

        // Pool ID is the same as the tag ID (e.g., wizards:spell_book/fire)
        var poolId = tag.location();

        // Create spell container with binding pool
        var config = SpellContainerTemplates.getConfig();
        var baseContainer = config.spell_book != null
                ? config.spell_book
                : SpellContainerTemplates.defaults().spell_book;
        var container = baseContainer.withBindingPool(poolId);

        itemStack.set(SpellDataComponents.SPELL_CONTAINER, container);
        onConfigured(itemStack, tag);
        return true;
    }

    /**
     * Called after spell container is applied. Sets model, name, rarity.
     */
    private static void onConfigured(ItemStack itemStack, TagKey<Spell> tag) {
        // Set custom model ID
        var modelId = modelIdForPool(tag.location());
        itemStack.set(SpellDataComponents.ITEM_MODEL, modelId);

        // Set custom name if translation exists
        var key = translationKeyForPool(tag.location());
        if (Language.getInstance().has(key)) {
            itemStack.set(DataComponents.ITEM_NAME, Component.translatable(key));
        }
    }

    /**
     * Generate model ID for this spell book variant.
     * Tag: wizards:spell_book/fire -> Model: wizards:item/fire_spell_book
     */
    public static Identifier modelIdForPool(Identifier tagId) {
        return Identifier.fromNamespaceAndPath(tagId.getNamespace(), "item/" + tagId.getPath());
    }

    /**
     * Generate translation key for this spell book variant.
     * Tag: wizards:spell_book/fire -> Key: item.wizards.spell_book/fire
     */
    public static String translationKeyForPool(Identifier poolId) {
        return "item." + poolId.getNamespace() + "." + poolId.getPath();
    }

    /**
     * Get the description translation key from an ItemStack's spell container pool.
     * Returns the translation key with ".spell_binding.description" suffix.
     * @param itemStack The spell book ItemStack
     * @return The description translation key, or null if no pool is set
     */
    public static String descriptionKeyFromStack(ItemStack itemStack) {
        var container = itemStack.get(SpellDataComponents.SPELL_CONTAINER);
        if (container == null || container.pool() == null || container.pool().isEmpty()) {
            return null;
        }
        var poolId = Identifier.parse(container.pool());
        return translationKeyForPool(poolId) + ".spell_binding.description";
    }
}

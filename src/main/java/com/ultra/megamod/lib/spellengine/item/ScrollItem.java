package com.ultra.megamod.lib.spellengine.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.locale.Language;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.api.spell.*;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainers;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.api.tags.SpellTags;
import com.ultra.megamod.lib.spellengine.client.SpellEngineClient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScrollItem extends Item {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("megamod", "spell_scroll");

    public ScrollItem(Item.Properties settings) {
        super(settings);
    }

    public static void applySpell(ItemStack itemStack, Holder<Spell> spellEntry, @Nullable TagKey<Spell> pool) {
        itemStack.set(SpellDataComponents.SPELL_CONTAINER, SpellContainers.forScroll(spellEntry));
        onSpellAdded(itemStack, spellEntry, pool);
    }

    public static String translationKeyForPool(Identifier poolId) {
        return "item." + poolId.getNamespace() + "." + poolId.getPath();
    }

    public static Identifier modelIdForPool(Identifier poolId) {
        return Identifier.fromNamespaceAndPath(poolId.getNamespace(), "item/" + poolId.getPath());
    }

    public static void onSpellAdded(ItemStack itemStack, Holder<Spell> spellEntry, @Nullable TagKey<Spell> pool) {
        // Set rarity
        var spell = spellEntry.value();
        var ordinal = Math.max(spell.tier - 1, 0); // minimum 0
        var rarity = Rarity.values().length > ordinal ? Rarity.values()[ordinal] : Rarity.EPIC;
        itemStack.set(DataComponents.RARITY, rarity);

        if (pool != null) {
            // Set custom model override
            var modelId = modelIdForPool(pool.location());
            itemStack.set(SpellDataComponents.ITEM_MODEL, modelId);

            // Set custom name
            // - Example: "paladins:spell_scroll/paladin" -> "item.paladins.paladin_spell_scroll"
            var key = translationKeyForPool(pool.location());
            if (Language.getInstance().has(key)) {
                itemStack.set(DataComponents.ITEM_NAME, Component.translatable(key));
            }
        }
    }

    @Nullable public static TagKey<Spell> resolveSpellPool(Level world, Holder<Spell> spellEntry) {
        var wrapper = world.registryAccess().lookup(SpellRegistry.KEY);
        if (wrapper.isPresent()) {
            return resolveSpellPool(wrapper.get(), spellEntry);
        } else {
            return null;
        }
    }

    @Nullable public static TagKey<Spell> resolveSpellPool(HolderLookup.RegistryLookup<Spell> wrapper, Holder<Spell> spellEntry) {
        // Find the first tag in which spellEntry is contained
        var tag = wrapper.listTagIds()
                .filter(tagKey ->
                        tagKey.location().getPath().startsWith(SpellTags.SPELL_SCROLL_PREFIX)
                )
                .filter(tagKey -> {
                    var holderSet = wrapper.get(tagKey);
                    return holderSet.isPresent() && holderSet.get().stream().anyMatch(h -> h.equals(spellEntry));
                })
                .findFirst();
        if (tag.isPresent()) {
            return tag.get();
        } else {
            return null;
        }
    }

    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        if (SpellEngineClient.config.showSpellBindingTooltip) {
            tooltip.add(Component
                    .translatable("item.spell_engine.scroll.table_hint")
                    .withStyle(ChatFormatting.GRAY)
            );
        }
    }
}

package com.ultra.megamod.feature.relics;

import com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainer;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * Helper that wraps {@link Item.Properties} suppliers for relics, attaching a
 * {@link SpellContainer} data component whose {@code spell_ids} list is sourced
 * from {@link RelicSpellAssignments}. This is how relics plug into SpellEngine's
 * active-cast path (Phase G.1).
 *
 * <p>The container uses {@link SpellContainer.ContentType#CONTAINED} so that
 * only the exact spell IDs on the relic resolve for the player — relics do not
 * participate in pool-based spell browsing.
 */
public final class RelicSpellPropsHelper {
    private RelicSpellPropsHelper() {}

    /**
     * Builds an {@link Item.Properties} for a relic, attaching a SpellContainer
     * with the spell IDs mapped for the given item id.
     */
    public static Item.Properties relicProps(String itemId) {
        return withContainer(new Item.Properties().stacksTo(1), itemId);
    }

    /**
     * Variant used by held weapons (NONE slot) that need any extra base props
     * configured — caller still passes the base props they'd use otherwise.
     */
    public static Item.Properties withContainer(Item.Properties props, String itemId) {
        List<String> spellIds = RelicSpellAssignments.forItem(itemId);
        if (spellIds.isEmpty()) {
            return props;
        }
        SpellContainer container = new SpellContainer(
                SpellContainer.ContentType.CONTAINED,
                "",
                "",
                "",
                spellIds.size(),
                spellIds,
                0
        );
        return props.component(SpellDataComponents.SPELL_CONTAINER, container);
    }
}

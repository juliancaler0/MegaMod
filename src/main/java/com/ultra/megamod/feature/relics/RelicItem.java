package com.ultra.megamod.feature.relics;

import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.lib.accessories.api.core.AccessoryItem;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.List;

/**
 * Stub base class for relics. Custom research-table relic system was scrapped
 * (see task #52). Real source-pattern Relics-1.21.1 port will be re-implemented later.
 *
 * <p>Kept as an empty AccessoryItem subclass so legacy code compiles.</p>
 */
public class RelicItem extends AccessoryItem {
    private final AccessorySlotType slotType;
    private final String displayName;

    public RelicItem(Item.Properties properties) {
        super(properties);
        this.slotType = null;
        this.displayName = "";
    }

    /** Legacy 4-arg constructor used by JewelryItem + old relic subclasses. */
    public RelicItem(String displayName, AccessorySlotType slotType, List<?> abilities, Item.Properties properties) {
        super(properties);
        this.slotType = slotType;
        this.displayName = displayName != null ? displayName : "";
    }

    public AccessorySlotType getSlotType() { return slotType; }
    public String getDisplayName() { return displayName; }
    public List<?> getAbilities() { return Collections.emptyList(); }
}

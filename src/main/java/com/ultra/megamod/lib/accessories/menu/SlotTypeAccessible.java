package com.ultra.megamod.lib.accessories.menu;

import com.ultra.megamod.lib.accessories.api.AccessoriesContainer;
import com.ultra.megamod.lib.accessories.api.slot.SlotPath;
import com.ultra.megamod.lib.accessories.api.slot.SlotReference;
import com.ultra.megamod.lib.accessories.api.slot.SlotType;

public interface SlotTypeAccessible {

    AccessoriesContainer getContainer();

    int index();

    boolean isCosmeticSlot();

    default String slotName() {
        return getContainer().getSlotName();
    }

    default SlotType slotType() {
        return getContainer().slotType();
    }

    default SlotPath slotPath() {
        return getContainer().createPath(index());
    }

    default SlotReference slotReference() {
        return getContainer().createReference(index());
    }
}

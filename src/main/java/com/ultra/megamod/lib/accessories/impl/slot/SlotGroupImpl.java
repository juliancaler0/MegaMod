package com.ultra.megamod.lib.accessories.impl.slot;

import com.ultra.megamod.lib.accessories.api.slot.SlotGroup;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import com.ultra.megamod.lib.accessories.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public record SlotGroupImpl(String name, int order, Set<String> slots, Identifier icon) implements SlotGroup {

    public static final StructEndec<SlotGroup> ENDEC = StructEndecBuilder.of(
            Endec.STRING.fieldOf("name", SlotGroup::name),
            Endec.INT.fieldOf("order", SlotGroup::order),
            Endec.STRING.listOf().<Set<String>>xmap(LinkedHashSet::new, ArrayList::new).fieldOf("slots", SlotGroup::slots),
            MinecraftEndecs.IDENTIFIER.fieldOf("icon", SlotGroup::icon),
            SlotGroupImpl::new
    );

    @Override
    public String toString() {
        return "SlotGroup{" +
            "name='" + name + '\'' +
            ", order=" + order +
            ", slots=" + slots +
            ", icon=" + icon +
            '}';
    }
}

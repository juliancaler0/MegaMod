package com.ultra.megamod.lib.accessories.api.data.providers.group;

import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import com.ultra.megamod.lib.accessories.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record RawSlotGroup(String name, Optional<Boolean> replace, Optional<Identifier> icon, Optional<Integer> order, Optional<List<String>> slots) {
    public static final StructEndec<RawSlotGroup> ENDEC = StructEndecBuilder.of(
            Endec.STRING.fieldOf("name", RawSlotGroup::name),
            Endec.BOOLEAN.optionalOf().fieldOf("replace", RawSlotGroup::replace),
            MinecraftEndecs.IDENTIFIER.optionalOf().fieldOf("icon", RawSlotGroup::icon),
            Endec.INT.optionalOf().fieldOf("order", RawSlotGroup::order),
            Endec.STRING.listOf().optionalOf().fieldOf("slots", RawSlotGroup::slots),
            RawSlotGroup::new
    );
}

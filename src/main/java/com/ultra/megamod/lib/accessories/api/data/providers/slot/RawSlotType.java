package com.ultra.megamod.lib.accessories.api.data.providers.slot;

import com.ultra.megamod.lib.accessories.api.events.DropRule;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import com.ultra.megamod.lib.accessories.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.Set;

public record RawSlotType(String name, Optional<Boolean> replace, Optional<Identifier> icon, Optional<Integer> order, Optional<Integer> amount, Optional<Set<Identifier>> validators, Optional<DropRule> dropRule) {
    public static final StructEndec<RawSlotType> ENDEC = StructEndecBuilder.of(
            Endec.STRING.fieldOf("name", RawSlotType::name),
            Endec.BOOLEAN.optionalOf().fieldOf("replace", RawSlotType::replace),
            MinecraftEndecs.IDENTIFIER.optionalOf().fieldOf("icon", RawSlotType::icon),
            Endec.INT.optionalOf().fieldOf("order", RawSlotType::order),
            Endec.INT.optionalOf().fieldOf("amount", RawSlotType::amount),
            MinecraftEndecs.IDENTIFIER.setOf().optionalOf().fieldOf("validators", RawSlotType::validators),
            Endec.STRING.xmap(DropRule::valueOf, DropRule::name).optionalOf().fieldOf("dropRule", RawSlotType::dropRule),
            RawSlotType::new
    );
}

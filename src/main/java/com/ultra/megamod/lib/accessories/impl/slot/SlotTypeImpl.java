package com.ultra.megamod.lib.accessories.impl.slot;

import com.ultra.megamod.lib.accessories.Accessories;
import com.ultra.megamod.lib.accessories.api.events.DropRule;
import com.ultra.megamod.lib.accessories.api.slot.SlotType;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import com.ultra.megamod.lib.accessories.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.Set;

public record SlotTypeImpl(String name, Optional<String> alternativeTranslation, Identifier icon, int order, int amount, Set<Identifier> validators, DropRule dropRule) implements SlotType  {
    public SlotTypeImpl(String name, Identifier icon, int order, int amount, Set<Identifier> validators, DropRule dropRule) {
        this(name, Optional.empty(), icon, order, amount, validators, dropRule);
    }

    @Override
    public String translation() {
        return alternativeTranslation().orElseGet(SlotType.super::translation);
    }

    public static final StructEndec<SlotType> ENDEC = StructEndecBuilder.of(
            Endec.STRING.fieldOf("name", SlotType::name),
            Endec.STRING.optionalOf().fieldOf("alternativeTranslation", slotType -> {
                // NOTE: The endec adapter's optionalOf() encodes Optional.empty() as a literal
                // null string, which NPEs in NbtOps.createString and breaks the entire
                // SyncAllLoaderDataPacket on player join (see SyncedDataHelperManager). To keep
                // the codec well-formed we always hand it a non-null String — falling back to
                // the default translation key when no alternative translation is set. The
                // resulting Optional.of(defaultTranslation) is harmless: SlotType#translation()
                // would have returned that exact same string anyway.
                var translation = slotType.translation();
                if (translation == null) translation = "";

                return Optional.of(translation);
            }),
            MinecraftEndecs.IDENTIFIER.fieldOf("icon", SlotType::icon),
            Endec.INT.fieldOf("order", SlotType::order),
            Endec.INT.fieldOf("amount", SlotType::amount),
            MinecraftEndecs.IDENTIFIER.setOf().fieldOf("validators", SlotType::validators),
            Endec.STRING.xmap(DropRule::valueOf, DropRule::name).fieldOf("dropRule", SlotType::dropRule),
            SlotTypeImpl::new
    );
}

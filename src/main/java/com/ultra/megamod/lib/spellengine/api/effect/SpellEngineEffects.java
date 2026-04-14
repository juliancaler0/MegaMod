package com.ultra.megamod.lib.spellengine.api.effect;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.api.config.AttributeModifier;
import com.ultra.megamod.lib.spellengine.api.config.EffectConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpellEngineEffects {
    public static final List<Effects.Entry> entries = new ArrayList<>();
    private static Effects.Entry add(Effects.Entry entry) {
        entries.add(entry);
        return entry;
    }

    public static Effects.Entry STUN = add(new Effects.Entry(Identifier.fromNamespaceAndPath("megamod","spell_stun"),
            "Stunned",
            "Cannot move or act.",
            new CustomStatusEffect(MobEffectCategory.HARMFUL, 0x888800),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            Attributes.JUMP_STRENGTH.getRegisteredName(),
                            0,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            ))
    ));

    public static Effects.Entry IMMOBILIZE = add(new Effects.Entry(Identifier.fromNamespaceAndPath("megamod","immobilize"),
            "Immobilized",
            "Cannot move or jump.",
            new CustomStatusEffect(MobEffectCategory.HARMFUL, 0xcc0000),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            Attributes.JUMP_STRENGTH.getRegisteredName(),
                            -10,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    ),
                    new AttributeModifier(
                            Attributes.MOVEMENT_SPEED.getRegisteredName(),
                            -10,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
            ))
    ));

    public static void register() {
        ActionImpairing.configure(STUN.effect, EntityActionsAllowed.STUN);

        for (var entry: entries) {
            Synchronized.configure(entry.effect, true);
        }

        Effects.register(entries, new HashMap<>());
    }
}

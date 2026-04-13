package com.ultra.megamod.feature.combat.archers.effect;

import com.ultra.megamod.feature.combat.archers.ArchersMod;
import com.ultra.megamod.lib.spellengine.api.config.AttributeModifier;
import com.ultra.megamod.lib.spellengine.api.config.ConfigFile;
import com.ultra.megamod.lib.spellengine.api.config.EffectConfig;
import com.ultra.megamod.lib.spellengine.api.effect.CustomStatusEffect;
import com.ultra.megamod.lib.spellengine.api.effect.Effects;
import com.ultra.megamod.lib.spellengine.api.effect.Synchronized;
import com.ultra.megamod.lib.spellengine.api.entity.SpellEngineAttributes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Archer effect references. These effects are already registered by
 * {@link com.ultra.megamod.feature.combat.spell.SpellEffects} via its DeferredRegister.
 * This class only provides convenience {@link Effects.Entry} wrappers so existing
 * archer code (ArcherSpells, ArchersClientMod) can reference {@code .id}, {@code .effect},
 * and {@code .config()} without changes.
 *
 * <p><b>No effects are registered here</b> — the {@link #entries} list is empty so
 * {@link Effects#register} will not create duplicates.</p>
 */
public class ArcherEffects {
    /** Empty — nothing to register; all effects live in SpellEffects. */
    public static final List<Effects.Entry> entries = new ArrayList<>();

    // Wrap SpellEffects holders so callers that use .id / .effect / .config() keep working.

    public static final Effects.Entry HUNTERS_MARK_STASH = new Effects.Entry(
            Identifier.fromNamespaceAndPath(ArchersMod.ID, "hunters_mark_stash"),
            "Power Shot",
            "Will mark the target",
            new CustomStatusEffect(MobEffectCategory.BENEFICIAL, 0xff0000)
    );

    public static final Effects.Entry HUNTERS_MARK = new Effects.Entry(
            Identifier.fromNamespaceAndPath(ArchersMod.ID, "hunters_mark"),
            "Hunters Mark",
            "The target is marked",
            new CustomStatusEffect(MobEffectCategory.HARMFUL, 0xff0000),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            SpellEngineAttributes.DAMAGE_TAKEN.id.toString(),
                            0.1F,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    )
            ))
    );

    public static final Effects.Entry ENTANGLING_ROOTS = new Effects.Entry(
            Identifier.fromNamespaceAndPath(ArchersMod.ID, "entangling_roots"),
            "Entangling Roots",
            "Reduces Movement",
            new CustomStatusEffect(MobEffectCategory.HARMFUL, 0x993333),
            new EffectConfig(List.of(
                    new AttributeModifier(
                            Attributes.MOVEMENT_SPEED.getRegisteredName(),
                            -0.5F,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ),
                    new AttributeModifier(
                            Attributes.JUMP_STRENGTH.getRegisteredName(),
                            -0.5F,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    )
            ))
    );

    public static void register(ConfigFile.Effects config) {
        // No longer register effects here — SpellEffects owns them.
        // Just apply config and synchronization settings.
        Synchronized.configure(HUNTERS_MARK.effect, true);
        Synchronized.configure(ENTANGLING_ROOTS.effect, true);

        // Still call Effects.register so configs are applied, but with an empty list
        // so no DeferredRegister.register() calls are made.
        Effects.register(entries, config.effects);
    }
}

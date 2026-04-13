package com.ultra.megamod.lib.spellengine.internals.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.utils.PatternMatching;

import java.util.Optional;

public class SpellCastCriteria extends SimpleCriterionTrigger<SpellCastCriteria.Condition> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("megamod", "spell_cast");
    public static final SpellCastCriteria INSTANCE = new SpellCastCriteria();

    @Override
    public Codec<Condition> codec() {
        return Condition.CODEC;
    }

    public void trigger(ServerPlayer player, Holder<Spell> spell) {
        trigger(player, condition -> {
            return condition.matches(spell);
        });
    }

    public record Condition(Optional<ContextAwarePredicate> player, Optional<String> spell, Optional<String> other_spell) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Condition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Condition::player),
                                Codec.optionalField("spell", Codec.STRING, true).forGetter(Condition::spell),
                                Codec.optionalField("other_spell", Codec.STRING, true).forGetter(Condition::other_spell)
                        )
                        .apply(instance, Condition::new)
        );

        public boolean matches(Holder<Spell> spellEntry) {
            if (spell().isEmpty() && other_spell().isEmpty()) {
                return true;
            }
            if (spell().isPresent()) {
                var pattern = spell().get();
                if (PatternMatching.matches(spellEntry, SpellRegistry.KEY, pattern)) {
                    return true;
                }
            }
            if (other_spell().isPresent()) {
                var pattern = other_spell().get();
                if (PatternMatching.matches(spellEntry, SpellRegistry.KEY, pattern)) {
                    return true;
                }
            }
            return false;
        }
    }
}

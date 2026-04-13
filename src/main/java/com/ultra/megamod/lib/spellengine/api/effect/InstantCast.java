package com.ultra.megamod.lib.spellengine.api.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;

import java.util.HashMap;
import java.util.Map;

public class InstantCast {
    public enum Selection { NONE, SINGLE, TAG }
    public record Args(Selection selection, ResourceKey<Spell> spell, TagKey<Spell> tag) {
        public static Args spell(ResourceKey<Spell> spell) {
            return new Args(Selection.SINGLE, spell, null);
        }
        public static Args tag(TagKey<Spell> tag) {
            return new Args(Selection.TAG, null, tag);
        }
    }
    private static Map<ResourceKey<MobEffect>, Args> instantCastEffects = new HashMap<>();

    public static void register(Holder<MobEffect> effect, ResourceKey<Spell> spell) {
        register(effect.unwrapKey().get(), Args.spell(spell));
    }

    public static void register(Holder<MobEffect> effect, TagKey<Spell> tag) {
        register(effect.unwrapKey().get(), Args.tag(tag));
    }

    public static void register(ResourceKey<MobEffect> effect, Args args) {
        instantCastEffects.put(effect, args);
    }

    public static boolean instantify(Holder<Spell> spellEntry, LivingEntity caster) {
        for (var instance : caster.getActiveEffects()) {
            var effectKey = instance.getEffect().unwrapKey();
            if (effectKey.isEmpty()) { // Should never happen, added due to some incompatibility
                continue;
            }
            var args = instantCastEffects.get(effectKey.get());
            if (args != null) {
                switch (args.selection) {
                    case NONE -> {
                        return true;
                    }
                    case SINGLE -> {
                        if (args.spell.equals(spellEntry.unwrapKey().get())) {
                            return true;
                        }
                    }
                    case TAG -> {
                        if (spellEntry.is(args.tag)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}

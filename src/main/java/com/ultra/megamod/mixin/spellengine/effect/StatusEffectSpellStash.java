package com.ultra.megamod.mixin.spellengine.effect;

import net.minecraft.world.effect.MobEffect;
import com.ultra.megamod.lib.spellengine.internals.delivery.SpellStash;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(MobEffect.class)
public class StatusEffectSpellStash implements SpellStash {
    private ArrayList<SpellStash.Entry> stashedSpells = new ArrayList<>();

    @Override
    public void stashedSpell(SpellStash.Entry entry) {
        this.stashedSpells.add(entry);
    }

    @Override
    public List<Entry> getStashedSpells() {
        return this.stashedSpells;
    }
}

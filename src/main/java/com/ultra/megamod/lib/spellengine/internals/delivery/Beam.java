package com.ultra.megamod.lib.spellengine.internals.delivery;

import net.minecraft.world.phys.Vec3;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;

public class Beam {
    public record Position(Vec3 origin, Vec3 end, float length, boolean hitBlock) {  }
    public record Rendered(Position position, Spell.Target.Beam appearance) {  }
}

package com.ultra.megamod.mixin.spellengine.effect;

import net.minecraft.world.effect.MobEffect;
import com.ultra.megamod.lib.spellengine.api.effect.RemoveOnHit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MobEffect.class)
public class StatusEffectRemoveOnHit implements RemoveOnHit {
    @Unique
    private RemoveOnHit.Args SpellEngine_removalArgs = null;

    @Override
    public RemoveOnHit.Args getRemovalOnHit() {
        return SpellEngine_removalArgs;
    }

    @Override
    public MobEffect setRemovalOnHit(Args args) {
        SpellEngine_removalArgs = args;
        return (MobEffect)((Object)this);
    }
}

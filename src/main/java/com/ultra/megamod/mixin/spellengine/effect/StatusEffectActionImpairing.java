package com.ultra.megamod.mixin.spellengine.effect;

import net.minecraft.world.effect.MobEffect;
import com.ultra.megamod.lib.spellengine.api.effect.ActionImpairing;
import com.ultra.megamod.lib.spellengine.api.effect.EntityActionsAllowed;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MobEffect.class)
public class StatusEffectActionImpairing implements ActionImpairing {
    private EntityActionsAllowed entityActionsAllowed = null;
    @Override
    public @Nullable EntityActionsAllowed actionsAllowed() {
        return entityActionsAllowed;
    }

    @Override
    public void setAllowedEntityActions(EntityActionsAllowed actionsAllowed) {
        entityActionsAllowed = actionsAllowed;
    }
}

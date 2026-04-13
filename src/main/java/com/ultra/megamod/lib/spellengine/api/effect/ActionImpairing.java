package com.ultra.megamod.lib.spellengine.api.effect;

import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;

public interface ActionImpairing {
    @Nullable EntityActionsAllowed actionsAllowed();
    void setAllowedEntityActions(EntityActionsAllowed actionsAllowed);

    static void configure(MobEffect effect, EntityActionsAllowed actionsAllowed) {
        ((ActionImpairing)effect).setAllowedEntityActions(actionsAllowed);
    }
}

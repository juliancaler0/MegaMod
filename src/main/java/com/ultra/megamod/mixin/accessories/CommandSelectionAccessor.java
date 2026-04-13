package com.ultra.megamod.mixin.accessories;

import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Commands.CommandSelection.class)
public interface CommandSelectionAccessor {
    @Accessor("includeIntegrated")
    boolean accessories$includeIntegrated();
}

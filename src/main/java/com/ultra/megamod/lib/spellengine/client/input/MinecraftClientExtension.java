package com.ultra.megamod.lib.spellengine.client.input;

public interface MinecraftClientExtension {
    boolean isSpellCastLockActive();
    void onSpellHotbarInputHandled(SpellHotbar.Handle handled);
}

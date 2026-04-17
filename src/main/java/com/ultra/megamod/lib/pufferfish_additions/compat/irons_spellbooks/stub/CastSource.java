package com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub;

/**
 * Compile-time stub mirroring {@code io.redspace.ironsspellbooks.api.spells.CastSource}.
 */
public enum CastSource {
    SPELLBOOK(true),
    SWORD(true),
    SCROLL(false),
    COMMAND(false),
    NONE(false);

    private final boolean consumesMana;

    CastSource(boolean consumesMana) {
        this.consumesMana = consumesMana;
    }

    public boolean consumesMana() {
        return consumesMana;
    }
}

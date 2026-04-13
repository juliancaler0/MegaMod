package com.ultra.megamod.lib.spellengine.internals.casting;

import net.minecraft.world.entity.player.Player;

public class SpellCastSyncHelper {
    public static void setCasting(Player caster, SpellCast.Process process) {
        //System.out.println("Setting casting process to " + process);
        ((SpellCasterEntity)caster).setSpellCastProcess(process);
    }

    public static void clearCasting(Player caster) {
        //System.out.println("Clearing casting process");
        ((SpellCasterEntity)caster).setSpellCastProcess(null);
        ((SpellCasterEntity)caster).setChannelTickIndex(0);
    }
}

package com.ultra.megamod.lib.rangedweapon.internal;

public interface ArrowExtension {
    void rwa_markModified(boolean modified);
    boolean rwa_isModified();
    double rwa_getBaseDamage();
    void rwa_setBaseDamage(double damage);
}

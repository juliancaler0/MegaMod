package com.ultra.megamod.lib.rangedweapon.internal;

public interface RangedHasteEntity {
    void resetPartialHasteTicks();
    float getPartialHasteTick();
    void addPartialHasteTick(float tick);
}

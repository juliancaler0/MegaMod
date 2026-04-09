/*
 * Decompiled with CFR 0.152.
 */
package com.ultra.megamod.feature.relics.data;

public enum AccessorySlotType {
    BACK,
    BELT,
    HANDS_LEFT,
    HANDS_RIGHT,
    FEET,
    NECKLACE,
    RING_LEFT,
    RING_RIGHT,
    HEAD,
    FACE,
    NONE;

    public String getDisplayName() {
        return switch (this) {
            case BACK -> "Back";
            case BELT -> "Belt";
            case HANDS_LEFT -> "Left Hand";
            case HANDS_RIGHT -> "Right Hand";
            case FEET -> "Feet";
            case NECKLACE -> "Necklace";
            case RING_LEFT -> "Left Ring";
            case RING_RIGHT -> "Right Ring";
            case HEAD -> "Head";
            case FACE -> "Face";
            case NONE -> "None";
        };
    }
}


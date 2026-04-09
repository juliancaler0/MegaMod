package com.ultra.megamod.feature.citizen.ornament;

/**
 * Subtypes for timber frame ornamental blocks.
 * Controls the visual pattern of the frame planks.
 */
public enum TimberFrameType {
    PLAIN("plain", "Plain"),
    DOUBLE_CROSSED("double_crossed", "Double Crossed"),
    FRAMED("framed", "Framed"),
    SIDE_FRAMED("side_framed", "Side Framed"),
    UP_GATED("up_gated", "Up Gated"),
    DOWN_GATED("down_gated", "Down Gated"),
    ONE_CROSSED_LR("one_crossed_lr", "One Crossed LR"),
    ONE_CROSSED_RL("one_crossed_rl", "One Crossed RL"),
    HORIZONTAL_PLAIN("horizontal_plain", "Horizontal Plain"),
    SIDE_FRAMED_HORIZONTAL("side_framed_horizontal", "Side Framed Horizontal");

    private final String id;
    private final String displayName;

    TimberFrameType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}

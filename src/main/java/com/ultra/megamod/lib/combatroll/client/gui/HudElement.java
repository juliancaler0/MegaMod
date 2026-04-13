package com.ultra.megamod.lib.combatroll.client.gui;

import org.joml.Vector2f;

public class HudElement {
    public Origin origin;
    public Vector2f offset;

    public HudElement(Origin origin, Vector2f offset) {
        this.origin = origin;
        this.offset = offset;
    }

    public enum Origin {
        TOP, TOP_LEFT, TOP_RIGHT,
        BOTTOM, BOTTOM_LEFT, BOTTOM_RIGHT;

        public Vector2f getPoint(int screenWidth, int screenHeight) {
            switch (this) {
                case TOP -> {
                    return new Vector2f(screenWidth / 2F, 0);
                }
                case TOP_LEFT -> {
                    return new Vector2f(0, 0);
                }
                case TOP_RIGHT -> {
                    return new Vector2f(screenWidth, 0);
                }
                case BOTTOM -> {
                    return new Vector2f(screenWidth / 2F, screenHeight);
                }
                case BOTTOM_LEFT -> {
                    return new Vector2f(0, screenHeight);
                }
                case BOTTOM_RIGHT -> {
                    return new Vector2f(screenWidth, screenHeight);
                }
            }
            return new Vector2f(screenWidth / 2F, screenHeight / 2F); // Should never run
        }

        public Vector2f initialOffset() {
            int offset = 12;
            switch (this) {
                case TOP -> {
                    return new Vector2f(0, offset);
                }
                case TOP_LEFT -> {
                    return new Vector2f(offset, offset);
                }
                case TOP_RIGHT -> {
                    return new Vector2f((-1) * offset, offset);
                }
                case BOTTOM -> {
                    return new Vector2f(0, (-1) * offset);
                }
                case BOTTOM_LEFT -> {
                    return new Vector2f(offset, (-1) * offset);
                }
                case BOTTOM_RIGHT -> {
                    return new Vector2f((-1) * offset, (-1) * offset);
                }
            }
            return new Vector2f(0, 0); // Should never run
        }
    }
}

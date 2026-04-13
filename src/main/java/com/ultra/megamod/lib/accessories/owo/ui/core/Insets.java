package com.ultra.megamod.lib.accessories.owo.ui.core;

/**
 * Adapter stub for io.wispforest.owo.ui.core.Insets.
 */
public final class Insets {
    private final int top, bottom, left, right;

    public Insets(int top, int bottom, int left, int right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    public int top() { return top; }
    public int bottom() { return bottom; }
    public int left() { return left; }
    public int right() { return right; }

    public static Insets none() { return new Insets(0, 0, 0, 0); }
    public static Insets of(int value) { return new Insets(value, value, value, value); }
    public static Insets of(int top, int bottom, int left, int right) { return new Insets(top, bottom, left, right); }
    public static Insets left(int left) { return new Insets(0, 0, left, 0); }
    public static Insets right(int right) { return new Insets(0, 0, 0, right); }
    public static Insets top(int top) { return new Insets(top, 0, 0, 0); }
    public static Insets bottom(int bottom) { return new Insets(0, bottom, 0, 0); }
    public static Insets horizontal(int value) { return new Insets(0, 0, value, value); }
    public static Insets vertical(int value) { return new Insets(value, value, 0, 0); }

    public int horizontal() { return left + right; }
    public int vertical() { return top + bottom; }

    public Insets withTop(int top) { return new Insets(top, this.bottom, this.left, this.right); }
    public Insets withBottom(int bottom) { return new Insets(this.top, bottom, this.left, this.right); }
    public Insets withLeft(int left) { return new Insets(this.top, this.bottom, left, this.right); }
    public Insets withRight(int right) { return new Insets(this.top, this.bottom, this.left, right); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Insets other)) return false;
        return top == other.top && bottom == other.bottom && left == other.left && right == other.right;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(top, bottom, left, right);
    }
}

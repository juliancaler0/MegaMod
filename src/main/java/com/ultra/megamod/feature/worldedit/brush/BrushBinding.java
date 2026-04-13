package com.ultra.megamod.feature.worldedit.brush;

import com.ultra.megamod.feature.worldedit.mask.Mask;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;

/**
 * A brush binding is a brush + its size + active pattern/mask. When the
 * player right-clicks with a bound brush tool, this is what fires.
 */
public class BrushBinding {
    private Brush brush;
    private int size;
    private Pattern pattern;
    private Mask mask;
    private int range;

    public BrushBinding(Brush brush, int size, Pattern pattern) {
        this.brush = brush;
        this.size = Math.max(1, size);
        this.pattern = pattern;
        this.range = 300;
    }

    public Brush getBrush() { return brush; }
    public void setBrush(Brush b) { this.brush = b; }
    public int getSize() { return size; }
    public void setSize(int s) { this.size = Math.max(1, s); }
    public Pattern getPattern() { return pattern; }
    public void setPattern(Pattern p) { this.pattern = p; }
    public Mask getMask() { return mask; }
    public void setMask(Mask m) { this.mask = m; }
    public int getRange() { return range; }
    public void setRange(int r) { this.range = Math.max(1, r); }
}

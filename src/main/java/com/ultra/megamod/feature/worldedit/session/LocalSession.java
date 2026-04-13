package com.ultra.megamod.feature.worldedit.session;

import com.ultra.megamod.feature.worldedit.brush.BrushBinding;
import com.ultra.megamod.feature.worldedit.clipboard.Clipboard;
import com.ultra.megamod.feature.worldedit.history.UndoHistory;
import com.ultra.megamod.feature.worldedit.mask.Mask;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;
import com.ultra.megamod.feature.worldedit.region.CuboidRegion;
import com.ultra.megamod.feature.worldedit.region.Region;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.EnumMap;
import java.util.Map;

/**
 * Per-player WorldEdit session: selection, clipboard, history, brushes,
 * mask, pattern, and related flags.
 */
public class LocalSession {

    public enum SelectorType { CUBOID, EXTEND, POLY, ELLIPSOID, SPHERE, CYL, CONVEX }

    private final java.util.UUID playerId;
    private BlockPos pos1;
    private BlockPos pos2;
    private ResourceKey<Level> lastDimension;
    private Region selectionRegion;
    private SelectorType selectorType = SelectorType.CUBOID;

    private Clipboard clipboard;
    private final UndoHistory history = new UndoHistory();

    private Mask activeMask;
    private Pattern activePattern;

    private final Map<BrushSlot, BrushBinding> brushes = new EnumMap<>(BrushSlot.class);

    private boolean superPickaxe = false;
    private boolean weMode = false; // admin app toggle

    public enum BrushSlot { MAIN, OFFHAND }

    public LocalSession(java.util.UUID playerId) {
        this.playerId = playerId;
    }

    public java.util.UUID getPlayerId() { return playerId; }

    public BlockPos getPos1() { return pos1; }
    public BlockPos getPos2() { return pos2; }

    public void setPos1(BlockPos p, ResourceKey<Level> dim) {
        this.pos1 = p;
        this.lastDimension = dim;
        rebuildRegionIfCuboid();
    }

    public void setPos2(BlockPos p, ResourceKey<Level> dim) {
        this.pos2 = p;
        this.lastDimension = dim;
        rebuildRegionIfCuboid();
    }

    /** Updates pos1 without changing the cached dimension. */
    public void setPos1(BlockPos p) {
        this.pos1 = p;
        rebuildRegionIfCuboid();
    }

    public void setPos2(BlockPos p) {
        this.pos2 = p;
        rebuildRegionIfCuboid();
    }

    public ResourceKey<Level> getDimension() { return lastDimension; }

    public SelectorType getSelectorType() { return selectorType; }
    public void setSelectorType(SelectorType t) {
        this.selectorType = t;
        rebuildRegionIfCuboid();
    }

    public Region getSelectionRegion() { return selectionRegion; }
    public void setSelectionRegion(Region r) { this.selectionRegion = r; }

    private void rebuildRegionIfCuboid() {
        if (selectorType == SelectorType.CUBOID && pos1 != null && pos2 != null) {
            selectionRegion = new CuboidRegion(pos1, pos2);
        }
    }

    /** Throws if no valid selection is set for operations that require one. */
    public Region getRequiredSelection() throws IllegalStateException {
        if (selectionRegion == null) {
            throw new IllegalStateException("No region selected. Use /we_pos1 and /we_pos2 or the wand.");
        }
        return selectionRegion;
    }

    public Clipboard getClipboard() { return clipboard; }
    public void setClipboard(Clipboard c) { this.clipboard = c; }
    public boolean hasClipboard() { return clipboard != null; }

    public UndoHistory getHistory() { return history; }

    public Mask getActiveMask() { return activeMask; }
    public void setActiveMask(Mask m) { this.activeMask = m; }

    public Pattern getActivePattern() { return activePattern; }
    public void setActivePattern(Pattern p) { this.activePattern = p; }

    public BrushBinding getBrush(BrushSlot slot) { return brushes.get(slot); }
    public void setBrush(BrushSlot slot, BrushBinding b) { brushes.put(slot, b); }
    public void clearBrush(BrushSlot slot) { brushes.remove(slot); }

    public boolean isSuperPickaxe() { return superPickaxe; }
    public void setSuperPickaxe(boolean b) { this.superPickaxe = b; }

    public boolean isWeMode() { return weMode; }
    public void setWeMode(boolean b) { this.weMode = b; }

    public Map<BrushSlot, BrushBinding> getBrushes() { return brushes; }
}

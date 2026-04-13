package com.ultra.megamod.feature.worldedit.history;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** An ordered list of block changes that can be applied (redo) or reversed (undo). */
public class ChangeSet {
    private final List<BlockChange> changes = new ArrayList<>();
    private final ResourceKey<Level> dimension;
    private final long createdMillis = System.currentTimeMillis();
    private String description = "";

    public ChangeSet(ResourceKey<Level> dim) {
        this.dimension = dim;
    }

    public void add(BlockChange change) { changes.add(change); }
    public void addAll(List<BlockChange> list) { changes.addAll(list); }

    public int size() { return changes.size(); }
    public boolean isEmpty() { return changes.isEmpty(); }

    public ResourceKey<Level> dimension() { return dimension; }
    public String description() { return description; }
    public void setDescription(String d) { this.description = d; }
    public long createdMillis() { return createdMillis; }
    public List<BlockChange> changes() { return changes; }

    /** Reverse the changes (undo): set each old state. */
    public int undo(ServerLevel level) {
        int n = 0;
        for (int i = changes.size() - 1; i >= 0; i--) {
            BlockChange c = changes.get(i);
            BlockPos p = c.pos();
            BlockState cur = level.getBlockState(p);
            if (cur != c.oldState()) {
                level.setBlock(p, c.oldState(), 2);
                n++;
            }
        }
        return n;
    }

    /** Re-apply the changes (redo): set each new state. */
    public int redo(ServerLevel level) {
        int n = 0;
        for (BlockChange c : changes) {
            BlockPos p = c.pos();
            BlockState cur = level.getBlockState(p);
            if (cur != c.newState()) {
                level.setBlock(p, c.newState(), 2);
                n++;
            }
        }
        return n;
    }
}

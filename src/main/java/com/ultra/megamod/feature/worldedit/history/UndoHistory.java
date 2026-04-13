package com.ultra.megamod.feature.worldedit.history;

import java.util.ArrayDeque;
import java.util.Deque;

/** A bounded undo/redo stack. */
public class UndoHistory {
    public static final int DEFAULT_CAPACITY = 32;

    private final Deque<ChangeSet> undo = new ArrayDeque<>();
    private final Deque<ChangeSet> redo = new ArrayDeque<>();
    private final int capacity;

    public UndoHistory() { this(DEFAULT_CAPACITY); }
    public UndoHistory(int capacity) { this.capacity = Math.max(1, capacity); }

    public void record(ChangeSet set) {
        if (set == null || set.isEmpty()) return;
        undo.push(set);
        while (undo.size() > capacity) undo.pollLast();
        redo.clear();
    }

    public ChangeSet popUndo() {
        ChangeSet cs = undo.pollFirst();
        if (cs != null) redo.push(cs);
        return cs;
    }

    public ChangeSet popRedo() {
        ChangeSet cs = redo.pollFirst();
        if (cs != null) undo.push(cs);
        return cs;
    }

    public int undoDepth() { return undo.size(); }
    public int redoDepth() { return redo.size(); }

    public void clear() { undo.clear(); redo.clear(); }

    public int capacity() { return capacity; }
}

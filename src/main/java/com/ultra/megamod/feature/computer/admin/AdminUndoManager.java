package com.ultra.megamod.feature.computer.admin;

import java.util.*;

/**
 * Tracks undoable admin actions (economy changes, moderation actions).
 * In-memory only — clears on server restart.
 */
public class AdminUndoManager {

    private static final int MAX_HISTORY = 50;
    private static final List<UndoEntry> history = new ArrayList<>();

    public enum UndoType { ECO_MODIFY, ECO_SET, ECO_BULK, BAN, MUTE, WARN }

    public record UndoEntry(
            long timestamp, UndoType type, String adminName,
            String targetName, UUID targetUuid,
            String description, String undoCommand
    ) {}

    public static void record(UndoType type, String admin, String target, UUID targetUuid,
                              String desc, String undoCmd) {
        history.add(new UndoEntry(System.currentTimeMillis(), type, admin, target, targetUuid, desc, undoCmd));
        while (history.size() > MAX_HISTORY) history.remove(0);
    }

    public static List<UndoEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public static List<UndoEntry> getRecent(int count) {
        int start = Math.max(0, history.size() - count);
        return history.subList(start, history.size());
    }

    /**
     * Pop the most recent entry and return it. Returns null if empty.
     */
    public static UndoEntry popLast() {
        if (history.isEmpty()) return null;
        return history.remove(history.size() - 1);
    }

    public static void reset() { history.clear(); }
}

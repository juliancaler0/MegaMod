package com.ultra.megamod.util;

import com.ultra.megamod.MegaMod;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Provides async saving for heavy managers to avoid blocking the server tick.
 * Uses a single-threaded executor to ensure saves are sequential and thread-safe.
 * <p>
 * Usage: call {@link #saveAsync(Runnable)} for periodic saves during gameplay.
 * For server shutdown, call the manager's saveToDisk directly (synchronously)
 * to ensure all data is written before the process exits.
 * <p>
 * Call {@link #shutdown()} during server stop to cleanly drain pending saves.
 */
public class AsyncSaveHelper {

    private static volatile ExecutorService saveExecutor;

    private static ExecutorService getExecutor() {
        ExecutorService exec = saveExecutor;
        if (exec == null || exec.isShutdown()) {
            synchronized (AsyncSaveHelper.class) {
                exec = saveExecutor;
                if (exec == null || exec.isShutdown()) {
                    exec = Executors.newSingleThreadExecutor(r -> {
                        Thread t = new Thread(r, "MegaMod-Save");
                        t.setDaemon(true);
                        return t;
                    });
                    saveExecutor = exec;
                }
            }
        }
        return exec;
    }

    /**
     * Submits a save task to run asynchronously on the save thread.
     * The task should capture a snapshot of the data to save (or the data
     * should be safely readable from the save thread).
     */
    public static void saveAsync(Runnable saveTask) {
        try {
            getExecutor().execute(() -> {
                try {
                    saveTask.run();
                } catch (Exception e) {
                    MegaMod.LOGGER.error("Async save failed", e);
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException e) {
            // Executor was shut down between check and submit — run inline
            try {
                saveTask.run();
            } catch (Exception ex) {
                MegaMod.LOGGER.error("Fallback sync save failed", ex);
            }
        }
    }

    /**
     * Shuts down the save executor, waiting up to 10 seconds for pending saves to complete.
     * Call this during ServerStoppingEvent to ensure all queued saves finish.
     * A new executor will be created automatically on the next saveAsync call.
     */
    public static void shutdown() {
        ExecutorService exec;
        synchronized (AsyncSaveHelper.class) {
            exec = saveExecutor;
            saveExecutor = null;
        }
        if (exec == null) return;
        exec.shutdown();
        try {
            if (!exec.awaitTermination(10, TimeUnit.SECONDS)) {
                MegaMod.LOGGER.warn("Async save executor did not terminate in time, forcing shutdown");
                exec.shutdownNow();
            }
        } catch (InterruptedException e) {
            MegaMod.LOGGER.warn("Interrupted while waiting for async save executor shutdown");
            exec.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

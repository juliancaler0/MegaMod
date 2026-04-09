package com.ultra.megamod.feature.baritone.process;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Priority-based process coordinator.
 * Picks the highest-priority active process each tick.
 */
public class ProcessManager {
    private final List<BotProcess> processes = new ArrayList<>();
    private BotProcess activeProcess = null;

    public void register(BotProcess process) {
        processes.add(process);
    }

    public BotProcess getActiveProcess() {
        return activeProcess;
    }

    /**
     * Tick: select highest priority active process and get its command.
     */
    public BotProcess.PathingCommand tick(boolean calcFailed, boolean safeToCancel) {
        BotProcess best = processes.stream()
            .filter(BotProcess::isActive)
            .max(Comparator.comparingDouble(BotProcess::priority))
            .orElse(null);

        if (best != activeProcess) {
            if (activeProcess != null) {
                activeProcess.onLostControl();
            }
            activeProcess = best;
        }

        if (activeProcess == null) return null;
        return activeProcess.onTick(calcFailed, safeToCancel);
    }

    public void cancelAll() {
        for (BotProcess p : processes) {
            if (p.isActive()) p.cancel();
        }
        activeProcess = null;
    }

    public List<BotProcess> getProcesses() {
        return processes;
    }

    public String getStatus() {
        if (activeProcess != null && activeProcess.isActive()) {
            return activeProcess.name() + ": " + activeProcess.getStatus();
        }
        return "Idle";
    }
}

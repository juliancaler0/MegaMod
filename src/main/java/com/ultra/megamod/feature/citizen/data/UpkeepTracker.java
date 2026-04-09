package com.ultra.megamod.feature.citizen.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UpkeepTracker {
    private final Map<UUID, Long> lastPaymentTick = new HashMap<>();
    private final Map<UUID, Boolean> upkeepExempt = new HashMap<>();
    private final Map<UUID, Double> upkeepMultiplier = new HashMap<>();
    private final Map<UUID, Integer> missedPayments = new HashMap<>();
    private boolean globalUpkeepEnabled = true;

    public long getLastPaymentTick(UUID citizenId) {
        return lastPaymentTick.getOrDefault(citizenId, 0L);
    }

    public void setLastPaymentTick(UUID citizenId, long tick) {
        lastPaymentTick.put(citizenId, tick);
    }

    public boolean isExempt(UUID playerUuid) {
        return upkeepExempt.getOrDefault(playerUuid, false);
    }

    public void setExempt(UUID playerUuid, boolean exempt) {
        upkeepExempt.put(playerUuid, exempt);
    }

    public double getMultiplier(UUID playerUuid) {
        return upkeepMultiplier.getOrDefault(playerUuid, 1.0);
    }

    public void setMultiplier(UUID playerUuid, double mult) {
        upkeepMultiplier.put(playerUuid, mult);
    }

    public boolean isGlobalUpkeepEnabled() { return globalUpkeepEnabled; }
    public void setGlobalUpkeepEnabled(boolean enabled) { this.globalUpkeepEnabled = enabled; }

    public void removeCitizen(UUID citizenId) {
        lastPaymentTick.remove(citizenId);
    }

    // ---- Missed Payments Tracking ----

    public int getMissedPayments(UUID ownerUuid) {
        return missedPayments.getOrDefault(ownerUuid, 0);
    }

    public void incrementMissedPayments(UUID ownerUuid) {
        missedPayments.put(ownerUuid, getMissedPayments(ownerUuid) + 1);
    }

    public void resetMissedPayments(UUID ownerUuid) {
        missedPayments.remove(ownerUuid);
    }

    /**
     * Returns work efficiency based on consecutive missed payments.
     * 0 misses = 1.0 (full speed), 1 miss = 0.5 (half speed), 2+ misses = 0.0 (stopped).
     */
    public double getWorkEfficiency(UUID ownerUuid) {
        int missed = getMissedPayments(ownerUuid);
        if (missed <= 0) return 1.0;
        if (missed == 1) return 0.5;
        return 0.0;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("globalEnabled", globalUpkeepEnabled);
        CompoundTag payments = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : lastPaymentTick.entrySet()) {
            payments.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("payments", (Tag) payments);
        CompoundTag exempts = new CompoundTag();
        for (Map.Entry<UUID, Boolean> entry : upkeepExempt.entrySet()) {
            exempts.putBoolean(entry.getKey().toString(), entry.getValue());
        }
        tag.put("exempts", (Tag) exempts);
        CompoundTag multipliers = new CompoundTag();
        for (Map.Entry<UUID, Double> entry : upkeepMultiplier.entrySet()) {
            multipliers.putDouble(entry.getKey().toString(), entry.getValue());
        }
        tag.put("multipliers", (Tag) multipliers);
        CompoundTag missed = new CompoundTag();
        for (Map.Entry<UUID, Integer> entry : missedPayments.entrySet()) {
            missed.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("missedPayments", (Tag) missed);
        return tag;
    }

    public static UpkeepTracker load(CompoundTag tag) {
        UpkeepTracker tracker = new UpkeepTracker();
        tracker.globalUpkeepEnabled = tag.getBooleanOr("globalEnabled", true);
        CompoundTag payments = tag.getCompoundOrEmpty("payments");
        for (String key : payments.keySet()) {
            try {
                tracker.lastPaymentTick.put(UUID.fromString(key), payments.getLongOr(key, 0L));
            } catch (Exception ignored) {}
        }
        CompoundTag exempts = tag.getCompoundOrEmpty("exempts");
        for (String key : exempts.keySet()) {
            try {
                tracker.upkeepExempt.put(UUID.fromString(key), exempts.getBooleanOr(key, false));
            } catch (Exception ignored) {}
        }
        CompoundTag multipliers = tag.getCompoundOrEmpty("multipliers");
        for (String key : multipliers.keySet()) {
            try {
                tracker.upkeepMultiplier.put(UUID.fromString(key), multipliers.getDoubleOr(key, 1.0));
            } catch (Exception ignored) {}
        }
        CompoundTag missed = tag.getCompoundOrEmpty("missedPayments");
        for (String key : missed.keySet()) {
            try {
                tracker.missedPayments.put(UUID.fromString(key), missed.getIntOr(key, 0));
            } catch (Exception ignored) {}
        }
        return tracker;
    }
}

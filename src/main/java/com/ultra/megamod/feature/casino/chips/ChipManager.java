package com.ultra.megamod.feature.casino.chips;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.economy.EconomyManager;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

/**
 * Server-side chip inventory manager. Tracks per-player chip counts.
 * Persisted to disk via NbtIo so chips survive server restarts.
 */
public class ChipManager {
    private static ChipManager INSTANCE;
    private static final String FILE_NAME = "megamod_casino_chips.dat";

    // playerId -> denomination ordinal -> count
    private final Map<UUID, int[]> playerChips = new HashMap<>();
    private boolean dirty = false;

    public static ChipManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new ChipManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public int[] getChips(UUID playerId) {
        return playerChips.computeIfAbsent(playerId, k -> new int[ChipDenomination.values().length]);
    }

    public int getChipCount(UUID playerId, ChipDenomination denom) {
        return getChips(playerId)[denom.ordinal()];
    }

    public int getTotalValue(UUID playerId) {
        int[] chips = getChips(playerId);
        int total = 0;
        ChipDenomination[] denoms = ChipDenomination.values();
        for (int i = 0; i < denoms.length; i++) {
            total += chips[i] * denoms[i].value;
        }
        return total;
    }

    /**
     * Buy chips: deduct from wallet, add chips.
     * Returns true if successful.
     */
    public boolean buyChips(UUID playerId, ChipDenomination denom, int count, EconomyManager eco) {
        int cost = denom.value * count;
        if (eco.getWallet(playerId) < cost) return false;
        eco.spendWallet(playerId, cost);
        getChips(playerId)[denom.ordinal()] += count;
        markDirty();
        return true;
    }

    /**
     * Sell chips: remove chips, add to wallet.
     * Returns true if successful.
     */
    public boolean sellChips(UUID playerId, ChipDenomination denom, int count, EconomyManager eco) {
        int[] chips = getChips(playerId);
        if (chips[denom.ordinal()] < count) return false;
        chips[denom.ordinal()] -= count;
        eco.addWallet(playerId, denom.value * count);
        markDirty();
        return true;
    }

    /**
     * Spend chips for a bet. Removes chips from inventory.
     * Returns true if the player has enough chips of that denomination.
     */
    public boolean spendChip(UUID playerId, ChipDenomination denom, int count) {
        int[] chips = getChips(playerId);
        if (chips[denom.ordinal()] < count) return false;
        chips[denom.ordinal()] -= count;
        markDirty();
        return true;
    }

    /**
     * Add chips to player (from winning a bet).
     */
    public void addChips(UUID playerId, ChipDenomination denom, int count) {
        getChips(playerId)[denom.ordinal()] += count;
        markDirty();
    }

    /**
     * Add chips by total value (auto-breaks into best denominations).
     */
    public void addChipsByValue(UUID playerId, int totalValue) {
        int[] breakdown = ChipDenomination.breakdown(totalValue);
        int[] chips = getChips(playerId);
        for (int i = 0; i < breakdown.length; i++) {
            chips[i] += breakdown[i];
        }
        markDirty();
    }

    /**
     * Remove chips by total value (auto-selects from highest to lowest).
     * Returns actual amount removed (may be less if not enough chips).
     */
    public int removeChipsByValue(UUID playerId, int totalValue) {
        int[] chips = getChips(playerId);
        ChipDenomination[] denoms = ChipDenomination.values();
        int remaining = totalValue;

        // Remove from highest denomination first
        for (int i = denoms.length - 1; i >= 0 && remaining > 0; i--) {
            int canRemove = Math.min(chips[i], remaining / denoms[i].value);
            if (canRemove > 0) {
                chips[i] -= canRemove;
                remaining -= canRemove * denoms[i].value;
            }
        }
        // Handle remainder with smaller chips
        for (int i = denoms.length - 1; i >= 0 && remaining > 0; i--) {
            while (chips[i] > 0 && remaining > 0) {
                chips[i]--;
                remaining -= denoms[i].value;
            }
        }
        markDirty();
        return totalValue - Math.max(0, remaining);
    }

    /**
     * Cash out ALL chips back to wallet. Called when player leaves casino.
     */
    public int cashOutAll(UUID playerId, EconomyManager eco) {
        int total = getTotalValue(playerId);
        if (total > 0) {
            eco.addWallet(playerId, total);
        }
        playerChips.remove(playerId);
        markDirty();
        return total;
    }

    /**
     * Check if player has any chips at all.
     */
    public boolean hasAnyChips(UUID playerId) {
        return getTotalValue(playerId) > 0;
    }

    /**
     * Wallet-compatible API: spend chips by total value.
     * Returns true if player had enough chips.
     * Games call this instead of eco.spendWallet() when in casino.
     */
    public boolean spendChips(UUID playerId, int totalValue) {
        if (getTotalValue(playerId) < totalValue) return false;
        removeChipsByValue(playerId, totalValue); // markDirty() called inside removeChipsByValue
        return true;
    }

    /**
     * Wallet-compatible API: get total chip balance (like eco.getWallet).
     */
    public int getBalance(UUID playerId) {
        return getTotalValue(playerId);
    }

    /**
     * Serialize chip counts to JSON string for network sync.
     */
    public String toJson(UUID playerId) {
        int[] chips = getChips(playerId);
        ChipDenomination[] denoms = ChipDenomination.values();
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < denoms.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(denoms[i].value).append("\":").append(chips[i]);
        }
        sb.append(",\"total\":").append(getTotalValue(playerId));
        sb.append("}");
        return sb.toString();
    }

    private void markDirty() {
        this.dirty = true;
    }

    private Path getSavePath(ServerLevel level) {
        return level.getServer().getWorldPath(LevelResource.ROOT)
                .resolve("data").resolve(FILE_NAME);
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File dataFile = getSavePath(level).toFile();
            if (!dataFile.exists()) {
                MegaMod.LOGGER.info("No casino chips file found, starting fresh.");
                return;
            }
            CompoundTag root = NbtIo.readCompressed(dataFile.toPath(), NbtAccounter.unlimitedHeap());
            CompoundTag players = root.getCompoundOrEmpty("players");
            ChipDenomination[] denoms = ChipDenomination.values();
            for (String key : players.keySet()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    CompoundTag pData = players.getCompoundOrEmpty(key);
                    int[] chips = new int[denoms.length];
                    for (int i = 0; i < denoms.length; i++) {
                        chips[i] = pData.getIntOr(denoms[i].name(), 0);
                    }
                    playerChips.put(uuid, chips);
                } catch (IllegalArgumentException ignored) {
                }
            }
            MegaMod.LOGGER.info("Casino chips loaded for {} players.", playerChips.size());
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load casino chips!", e);
        }
    }

    public void saveToDisk(ServerLevel level) {
        if (!dirty) {
            return;
        }
        try {
            File dataFile = getSavePath(level).toFile();
            File dataDir = dataFile.getParentFile();
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            CompoundTag root = new CompoundTag();
            CompoundTag players = new CompoundTag();
            ChipDenomination[] denoms = ChipDenomination.values();
            for (Map.Entry<UUID, int[]> entry : playerChips.entrySet()) {
                CompoundTag pData = new CompoundTag();
                int[] chips = entry.getValue();
                for (int i = 0; i < denoms.length; i++) {
                    pData.putInt(denoms[i].name(), chips[i]);
                }
                players.put(entry.getKey().toString(), (Tag) pData);
            }
            root.put("players", (Tag) players);
            NbtIo.writeCompressed(root, dataFile.toPath());
            dirty = false;
            MegaMod.LOGGER.debug("Casino chips saved.");
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save casino chips!", e);
        }
    }
}

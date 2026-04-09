package com.ultra.megamod.feature.casino.slots;

import com.ultra.megamod.feature.casino.CasinoRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SlotMachineBlockEntity extends BlockEntity {
    // Static tracking: player UUID -> machine BlockPos (for disconnect cleanup)
    private static final Map<UUID, BlockPos> ACTIVE_PLAYERS = new HashMap<>();

    // Transient - not saved to disk, releases on server restart
    private UUID currentPlayer = null;

    // Persisted settings
    private int betIndex = 0;
    private int lineMode = 0;

    public SlotMachineBlockEntity(BlockPos pos, BlockState state) {
        super(CasinoRegistry.SLOT_MACHINE_BE.get(), pos, state);
    }

    /**
     * Returns true if any player is currently using this slot machine.
     */
    public boolean isOccupied() {
        return currentPlayer != null;
    }

    /**
     * Returns true if the given player UUID is the one using this slot machine.
     */
    public boolean isUsedBy(UUID playerId) {
        return currentPlayer != null && currentPlayer.equals(playerId);
    }

    /**
     * Locks this slot machine to the given player.
     */
    public void occupy(UUID playerId) {
        this.currentPlayer = playerId;
        ACTIVE_PLAYERS.put(playerId, this.worldPosition);
    }

    /**
     * Releases this slot machine so another player can use it.
     */
    public void release() {
        if (this.currentPlayer != null) {
            ACTIVE_PLAYERS.remove(this.currentPlayer);
        }
        this.currentPlayer = null;
    }

    /**
     * Gets the machine position for a player (for disconnect cleanup).
     */
    public static BlockPos getMachineForPlayer(UUID playerId) {
        return ACTIVE_PLAYERS.get(playerId);
    }

    /**
     * Removes a player from tracking (for disconnect cleanup).
     */
    public static void removePlayerTracking(UUID playerId) {
        ACTIVE_PLAYERS.remove(playerId);
    }

    /**
     * Gets the UUID of the player currently using this machine, or null if free.
     */
    public UUID getCurrentPlayer() {
        return currentPlayer;
    }

    public int getBetIndex() {
        return betIndex;
    }

    public void setBetIndex(int betIndex) {
        this.betIndex = betIndex;
        setChanged();
    }

    public int getLineMode() {
        return lineMode;
    }

    public void setLineMode(int lineMode) {
        this.lineMode = lineMode;
    }
}

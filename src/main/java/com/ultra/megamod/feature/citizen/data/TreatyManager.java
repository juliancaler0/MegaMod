package com.ultra.megamod.feature.citizen.data;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.util.AsyncSaveHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages treaty proposals between factions.
 * Persists to {@code world/data/megamod_treaties.dat}.
 */
public final class TreatyManager {

    private static final String SAVE_FILE = "megamod_treaties.dat";
    private static final long PROPOSAL_EXPIRY_TICKS = 24000 * 3; // 3 in-game days
    private static TreatyManager INSTANCE;

    /**
     * A treaty proposal between two factions.
     */
    public record TreatyProposal(
            String fromFaction,
            String toFaction,
            DiplomacyStatus proposedStatus,
            long createdTick,
            UUID proposalId
    ) {}

    private final List<TreatyProposal> proposals = new ArrayList<>();
    private boolean dirty = false;

    private TreatyManager() {}

    public static TreatyManager get(@NotNull ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new TreatyManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    // ==================== Proposals ====================

    public void addProposal(@NotNull String fromFaction, @NotNull String toFaction,
                            @NotNull DiplomacyStatus proposedStatus, long currentTick,
                            @NotNull UUID proposalId) {
        // Remove any existing proposal from same faction pair
        proposals.removeIf(p -> p.fromFaction().equals(fromFaction) && p.toFaction().equals(toFaction));
        proposals.add(new TreatyProposal(fromFaction, toFaction, proposedStatus, currentTick, proposalId));
        dirty = true;
    }

    public boolean acceptProposal(@NotNull String fromFaction, @NotNull String toFaction,
                                  @NotNull ServerLevel level) {
        Optional<TreatyProposal> found = proposals.stream()
                .filter(p -> p.fromFaction().equals(fromFaction) && p.toFaction().equals(toFaction))
                .findFirst();
        if (found.isEmpty()) return false;

        TreatyProposal proposal = found.get();
        proposals.remove(proposal);

        // Apply the proposed status via DiplomacyManager
        DiplomacyManager dm = DiplomacyManager.get(level);
        dm.setRelation(fromFaction, toFaction, proposal.proposedStatus());
        dm.saveToDisk(level);

        dirty = true;
        return true;
    }

    public boolean declineProposal(@NotNull String fromFaction, @NotNull String toFaction) {
        boolean removed = proposals.removeIf(
                p -> p.fromFaction().equals(fromFaction) && p.toFaction().equals(toFaction));
        if (removed) dirty = true;
        return removed;
    }

    @NotNull
    public List<TreatyProposal> getProposalsFor(@NotNull String factionId) {
        return proposals.stream()
                .filter(p -> p.toFaction().equals(factionId))
                .collect(Collectors.toList());
    }

    @NotNull
    public List<TreatyProposal> getProposalsFrom(@NotNull String factionId) {
        return proposals.stream()
                .filter(p -> p.fromFaction().equals(factionId))
                .collect(Collectors.toList());
    }

    /**
     * Removes expired proposals.
     */
    public void removeExpired(long currentTick) {
        boolean removed = proposals.removeIf(p -> (currentTick - p.createdTick()) > PROPOSAL_EXPIRY_TICKS);
        if (removed) dirty = true;
    }

    /**
     * Removes all proposals involving a faction (used when deleting a colony).
     */
    public void removeFaction(@NotNull String factionId) {
        boolean removed = proposals.removeIf(
                p -> p.fromFaction().equals(factionId) || p.toFaction().equals(factionId));
        if (removed) dirty = true;
    }

    // ==================== Persistence ====================

    public void saveToDisk(@NotNull ServerLevel level) {
        try {
            CompoundTag root = new CompoundTag();
            ListTag list = new ListTag();
            for (TreatyProposal p : proposals) {
                CompoundTag tag = new CompoundTag();
                tag.putString("from", p.fromFaction());
                tag.putString("to", p.toFaction());
                tag.putString("status", p.proposedStatus().name());
                tag.putLong("tick", p.createdTick());
                tag.putString("id", p.proposalId().toString());
                list.add(tag);
            }
            root.put("proposals", list);

            File dataDir = new File(level.getServer().getWorldPath(
                    net.minecraft.world.level.storage.LevelResource.ROOT).toFile(), "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File saveFile = new File(dataDir, SAVE_FILE);

            final File fSave = saveFile;
            final CompoundTag fRoot = root;
            try {
                AsyncSaveHelper.saveAsync(() -> {
                    try { NbtIo.writeCompressed(fRoot, fSave.toPath()); }
                    catch (Exception e) { MegaMod.LOGGER.error("Failed to save treaties", e); }
                });
            } catch (Exception e) {
                NbtIo.writeCompressed(root, saveFile.toPath());
            }
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save treaty data", e);
        }
    }

    public void loadFromDisk(@NotNull ServerLevel level) {
        proposals.clear();
        try {
            File dataDir = new File(level.getServer().getWorldPath(
                    net.minecraft.world.level.storage.LevelResource.ROOT).toFile(), "data");
            File saveFile = new File(dataDir, SAVE_FILE);
            if (!saveFile.exists()) return;

            CompoundTag root = NbtIo.readCompressed(saveFile.toPath(),
                    net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            if (root == null) return;

            if (root.contains("proposals")) {
                ListTag list = root.getListOrEmpty("proposals");
                for (int i = 0; i < list.size(); i++) {
                    if (!(list.get(i) instanceof CompoundTag tag)) continue;
                    String from = tag.getStringOr("from", "");
                    String to = tag.getStringOr("to", "");
                    String statusStr = tag.getStringOr("status", "NEUTRAL");
                    long tick = tag.getLongOr("tick", 0L);
                    String idStr = tag.getStringOr("id", "");
                    UUID id = idStr.isEmpty() ? UUID.randomUUID() : UUID.fromString(idStr);
                    if (!from.isEmpty() && !to.isEmpty()) {
                        proposals.add(new TreatyProposal(from, to,
                                DiplomacyStatus.fromString(statusStr), tick, id));
                    }
                }
            }
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load treaty data", e);
        }
    }
}

package com.ultra.megamod.feature.citizen.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;

public class ClaimData {
    public static final int MAX_CHUNKS = 50;

    private String ownerFactionId;
    private final Set<Long> claimedChunks = new HashSet<>();
    private int claimHealth = 100;
    private int maxClaimHealth = 100;
    private boolean underSiege = false;
    private String attackerFactionId = "";
    private boolean allowAllyBuild = true;
    private boolean allowAllyInteract = true;
    private boolean explosionProtection = true;

    public ClaimData(String ownerFactionId) {
        this.ownerFactionId = ownerFactionId;
    }

    public ClaimData() {}

    private static long encodeChunk(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    public String getOwnerFactionId() { return ownerFactionId; }
    public void setOwnerFactionId(String id) { this.ownerFactionId = id; }
    public int getClaimHealth() { return claimHealth; }
    public void setClaimHealth(int health) { this.claimHealth = Math.max(0, Math.min(health, maxClaimHealth)); }
    public int getMaxClaimHealth() { return maxClaimHealth; }
    public void setMaxClaimHealth(int max) { this.maxClaimHealth = max; }
    public boolean isUnderSiege() { return underSiege; }
    public void setUnderSiege(boolean siege) { this.underSiege = siege; }
    public String getAttackerFactionId() { return attackerFactionId; }
    public void setAttackerFactionId(String id) { this.attackerFactionId = id; }
    public boolean isAllowAllyBuild() { return allowAllyBuild; }
    public void setAllowAllyBuild(boolean allow) { this.allowAllyBuild = allow; }
    public boolean isAllowAllyInteract() { return allowAllyInteract; }
    public void setAllowAllyInteract(boolean allow) { this.allowAllyInteract = allow; }
    public boolean isExplosionProtection() { return explosionProtection; }
    public void setExplosionProtection(boolean explosionProtection) { this.explosionProtection = explosionProtection; }

    public boolean isChunkClaimed(int chunkX, int chunkZ) {
        return claimedChunks.contains(encodeChunk(chunkX, chunkZ));
    }

    public void claimChunk(int chunkX, int chunkZ) {
        claimedChunks.add(encodeChunk(chunkX, chunkZ));
    }

    public void unclaimChunk(int chunkX, int chunkZ) {
        claimedChunks.remove(encodeChunk(chunkX, chunkZ));
    }

    public Set<long[]> getClaimedChunks() {
        Set<long[]> result = new HashSet<>();
        for (long encoded : claimedChunks) {
            int x = (int) (encoded >> 32);
            int z = (int) encoded;
            result.add(new long[]{x, z});
        }
        return result;
    }

    public int getChunkCount() { return claimedChunks.size(); }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("faction", ownerFactionId);
        tag.putInt("health", claimHealth);
        tag.putInt("maxHealth", maxClaimHealth);
        tag.putBoolean("siege", underSiege);
        tag.putString("attacker", attackerFactionId);
        tag.putBoolean("allyBuild", allowAllyBuild);
        tag.putBoolean("allyInteract", allowAllyInteract);
        tag.putBoolean("explosionProtection", explosionProtection);
        ListTag chunks = new ListTag();
        for (long encoded : claimedChunks) {
            CompoundTag ct = new CompoundTag();
            ct.putLong("x", (int) (encoded >> 32));
            ct.putLong("z", (int) encoded);
            chunks.add(ct);
        }
        tag.put("chunks", (Tag) chunks);
        return tag;
    }

    public static ClaimData load(CompoundTag tag) {
        ClaimData data = new ClaimData();
        data.ownerFactionId = tag.getStringOr("faction", "");
        data.claimHealth = tag.getIntOr("health", 100);
        data.maxClaimHealth = tag.getIntOr("maxHealth", 100);
        data.underSiege = tag.getBooleanOr("siege", false);
        data.attackerFactionId = tag.getStringOr("attacker", "");
        data.allowAllyBuild = tag.getBooleanOr("allyBuild", true);
        data.allowAllyInteract = tag.getBooleanOr("allyInteract", true);
        data.explosionProtection = tag.getBooleanOr("explosionProtection", true);
        ListTag chunks = tag.getListOrEmpty("chunks");
        for (int i = 0; i < chunks.size(); i++) {
            CompoundTag ct = chunks.getCompoundOrEmpty(i);
            data.claimedChunks.add(encodeChunk(ct.getIntOr("x", 0), ct.getIntOr("z", 0)));
        }
        return data;
    }
}

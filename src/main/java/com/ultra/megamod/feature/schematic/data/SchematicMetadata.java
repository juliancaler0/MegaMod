package com.ultra.megamod.feature.schematic.data;

import net.minecraft.core.Vec3i;

/**
 * Metadata for a loaded schematic file.
 */
public class SchematicMetadata {

    private String name;
    private String author;
    private String description;
    private Vec3i enclosingSize;
    private long timeCreated;
    private long timeModified;
    private int totalBlocks;
    private int regionCount;

    public SchematicMetadata(String name, String author, String description,
                             Vec3i enclosingSize, long timeCreated, long timeModified,
                             int totalBlocks, int regionCount) {
        this.name = name;
        this.author = author;
        this.description = description;
        this.enclosingSize = enclosingSize;
        this.timeCreated = timeCreated;
        this.timeModified = timeModified;
        this.totalBlocks = totalBlocks;
        this.regionCount = regionCount;
    }

    public SchematicMetadata() {
        this("", "", "", Vec3i.ZERO, 0, 0, 0, 0);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public Vec3i getEnclosingSize() { return enclosingSize; }
    public void setEnclosingSize(Vec3i size) { this.enclosingSize = size; }
    public long getTimeCreated() { return timeCreated; }
    public long getTimeModified() { return timeModified; }
    public int getTotalBlocks() { return totalBlocks; }
    public void setTotalBlocks(int count) { this.totalBlocks = count; }
    public int getRegionCount() { return regionCount; }
    public void setRegionCount(int count) { this.regionCount = count; }
}

package com.ultra.megamod.feature.computer.screen.map;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.File;
import java.io.IOException;

/**
 * Groups 8x8 chunks into a single 128x128 pixel tile with DynamicTexture and disk persistence.
 * Ported from recruits ChunkTile.java, adapted for MegaMod with 8x8 tile size.
 */
public class MapChunkTile {
    private final int tileX, tileZ;
    private NativeImage image;
    private DynamicTexture texture;
    private Identifier textureId;
    private boolean needsUpdate = false;
    private boolean closed = false;
    // Per-chunk tracking: true = this chunk region has meaningful rendered pixel data
    private final boolean[][] chunkRendered = new boolean[TILE_SIZE][TILE_SIZE];

    public static final int TILE_SIZE = 8;           // chunks per tile axis
    public static final int PIXELS_PER_CHUNK = 16;
    public static final int TILE_PIXEL_SIZE = TILE_SIZE * PIXELS_PER_CHUNK; // 128

    public MapChunkTile(int tileX, int tileZ) {
        this.tileX = tileX;
        this.tileZ = tileZ;
    }

    public void loadOrCreate(File tileFile) {
        Minecraft mc = Minecraft.getInstance();

        try {
            if (tileFile.exists() && tileFile.length() > 0) {
                byte[] fileData = java.nio.file.Files.readAllBytes(tileFile.toPath());
                this.image = NativeImage.read(fileData);
                if (this.image.getWidth() != TILE_PIXEL_SIZE ||
                        this.image.getHeight() != TILE_PIXEL_SIZE) {
                    this.image.close();
                    this.image = null;
                } else {
                    scanRenderedChunks();
                }
            }
        } catch (IOException ignored) {
            this.image = null;
        }

        if (this.image == null) {
            try {
                this.image = new NativeImage(NativeImage.Format.RGBA, TILE_PIXEL_SIZE, TILE_PIXEL_SIZE, false);
                for (int i = 0; i < TILE_PIXEL_SIZE * TILE_PIXEL_SIZE; i++) {
                    this.image.setPixel(i % TILE_PIXEL_SIZE, i / TILE_PIXEL_SIZE, 0x00000000);
                }
            } catch (Exception e) {
                this.image = null;
                return;
            }
            this.needsUpdate = true;
        }

        try {
            // DynamicTexture(Supplier<String>, NativeImage) — the supplier is for debug naming
            this.texture = new DynamicTexture(() -> "megamod_maptile_" + tileX + "_" + tileZ, this.image);
            this.textureId = Identifier.fromNamespaceAndPath("megamod", "maptile_" + tileX + "_" + tileZ);
            mc.getTextureManager().register(this.textureId, this.texture);
            this.closed = false;
        } catch (Exception e) {
            // DynamicTexture creation failed — tile will render as unexplored
            this.texture = null;
            this.textureId = null;
        }
    }

    /**
     * Copy chunk pixel data into this tile's image.
     * Does NOT upload the GPU texture — call {@link #uploadTexture()} separately
     * after all chunks for this tick have been applied (batched upload).
     */
    public void updateFromChunkImage(MapChunkImage chunkImage, int chunkXInTile, int chunkZInTile) {
        if (this.image == null || chunkImage == null || !chunkImage.isMeaningful()) return;

        NativeImage chunkImg = chunkImage.getNativeImage();
        int startX = chunkXInTile * PIXELS_PER_CHUNK;
        int startZ = chunkZInTile * PIXELS_PER_CHUNK;

        for (int x = 0; x < PIXELS_PER_CHUNK; x++) {
            for (int z = 0; z < PIXELS_PER_CHUNK; z++) {
                this.image.setPixel(startX + x, startZ + z, chunkImg.getPixel(x, z));
            }
        }

        this.needsUpdate = true;
    }

    public void mergeWithExistingTile(File existingTileFile) {
        if (!existingTileFile.exists() || this.image == null) return;

        try {
            byte[] existingData = java.nio.file.Files.readAllBytes(existingTileFile.toPath());
            NativeImage existingImage = NativeImage.read(existingData);

            if (existingImage.getWidth() == TILE_PIXEL_SIZE &&
                    existingImage.getHeight() == TILE_PIXEL_SIZE) {
                for (int i = 0; i < TILE_PIXEL_SIZE * TILE_PIXEL_SIZE; i++) {
                    int x = i % TILE_PIXEL_SIZE;
                    int y = i / TILE_PIXEL_SIZE;
                    int currentPixel = this.image.getPixel(x, y);
                    if (((currentPixel >> 24) & 0xFF) == 0) {
                        this.image.setPixel(x, y, existingImage.getPixel(x, y));
                    }
                }
                this.needsUpdate = true;
                scanRenderedChunks();
            }
            existingImage.close();
        } catch (IOException ignored) {}
    }

    public void saveToFile(File tileFile) {
        if (this.closed || this.image == null || !this.needsUpdate) return;
        try {
            tileFile.getParentFile().mkdirs();
            this.image.writeToFile(tileFile);
            this.needsUpdate = false;
        } catch (IOException ignored) {}
    }

    public void render(GuiGraphics guiGraphics, int x, int y, int size) {
        render(guiGraphics, x, y, size, size);
    }

    public void render(GuiGraphics guiGraphics, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) return;
        if (this.closed || this.textureId == null || this.image == null || this.texture == null) return;

        // blit params: (pipeline, texture, x, y, u, v, blitW, blitH, texW, texH)
        // texW/texH are used for UV normalization: u1 = (u + blitW) / texW.
        // Setting texW=w and texH=h makes UV go 0..1, sampling the full 128x128 texture
        // and stretching it to the screen rectangle regardless of zoom level.
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.textureId,
                x, y, 0f, 0f, w, h, w, h);
    }

    /**
     * Scans the image to determine which chunk regions have meaningful rendered data.
     * Called after loading from disk so the async renderer can skip already-explored chunks.
     */
    private void scanRenderedChunks() {
        if (image == null) return;
        for (int cx = 0; cx < TILE_SIZE; cx++) {
            for (int cz = 0; cz < TILE_SIZE; cz++) {
                int startX = cx * PIXELS_PER_CHUNK;
                int startZ = cz * PIXELS_PER_CHUNK;
                int meaningful = 0;
                outer:
                for (int x = 0; x < PIXELS_PER_CHUNK; x++) {
                    for (int z = 0; z < PIXELS_PER_CHUNK; z++) {
                        int pixel = image.getPixel(startX + x, startZ + z);
                        if (((pixel >> 24) & 0xFF) > 0 && (pixel & 0x00FFFFFF) != 0) {
                            meaningful++;
                            if (meaningful >= 25) break outer;
                        }
                    }
                }
                chunkRendered[cx][cz] = meaningful >= 25;
            }
        }
    }

    public boolean isChunkRendered(int cx, int cz) {
        if (cx < 0 || cx >= TILE_SIZE || cz < 0 || cz >= TILE_SIZE) return false;
        return chunkRendered[cx][cz];
    }

    public void markChunkRendered(int cx, int cz) {
        if (cx >= 0 && cx < TILE_SIZE && cz >= 0 && cz < TILE_SIZE) {
            chunkRendered[cx][cz] = true;
        }
    }

    public void close() {
        this.closed = true;
        // DynamicTexture owns the NativeImage — releasing via TextureManager handles both.
        // Do NOT close image separately (double-close causes the Sampler0 crash).
        if (this.textureId != null) {
            try {
                Minecraft.getInstance().getTextureManager().release(this.textureId);
            } catch (Exception ignored) {}
            this.textureId = null;
        }
        this.texture = null;
        this.image = null;
    }

    /**
     * Re-upload the DynamicTexture from the current NativeImage data.
     * Must be called on the render/main thread after pixel data changes.
     */
    public void uploadTexture() {
        if (this.texture != null) {
            try {
                this.texture.upload();
            } catch (Exception ignored) {}
        }
    }

    public int getTileX() { return tileX; }
    public int getTileZ() { return tileZ; }
    public NativeImage getImage() { return image; }
    public Identifier getTextureId() { return textureId; }
    public void markAccessed() { }
    public void markNeedsUpdate() { this.needsUpdate = true; }

    /**
     * Serialize the tile image to PNG bytes for network transfer.
     */
    public byte[] toPngBytes() {
        if (this.image == null) return new byte[0];
        try {
            java.nio.file.Path tmp = java.nio.file.Files.createTempFile("maptile_", ".png");
            try {
                this.image.writeToFile(tmp);
                return java.nio.file.Files.readAllBytes(tmp);
            } finally {
                java.nio.file.Files.deleteIfExists(tmp);
            }
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     * Returns a 64-bit bitmask of which chunks (8x8 = 64) have been rendered.
     */
    public long getChunkBitmask() {
        long mask = 0;
        for (int cz = 0; cz < TILE_SIZE; cz++) {
            for (int cx = 0; cx < TILE_SIZE; cx++) {
                if (chunkRendered[cx][cz]) {
                    mask |= (1L << (cz * TILE_SIZE + cx));
                }
            }
        }
        return mask;
    }

    /**
     * Re-scan which chunks have meaningful rendered data.
     * Public wrapper for scanRenderedChunks().
     */
    public void rescanRenderedChunks() {
        scanRenderedChunks();
    }

    public static int chunkToTileCoord(int chunkCoord) {
        return Math.floorDiv(chunkCoord, TILE_SIZE);
    }

    public static int tileToChunkCoord(int tileCoord) {
        return tileCoord * TILE_SIZE;
    }
}

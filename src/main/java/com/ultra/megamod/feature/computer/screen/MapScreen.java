package com.ultra.megamod.feature.computer.screen;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.computer.screen.map.MapChunkTile;
import com.ultra.megamod.feature.computer.screen.map.MapChunkTileManager;
import com.ultra.megamod.feature.computer.screen.map.MapDrawingOverlay;
import com.ultra.megamod.feature.computer.screen.map.MapHighlightOverlay;
import com.ultra.megamod.feature.computer.screen.map.MapLayerRenderer;
import com.ultra.megamod.feature.computer.screen.map.MapStructureOverlay;
import com.ultra.megamod.feature.computer.screen.map.MapToolbar;
import com.ultra.megamod.feature.ui.UIHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import com.ultra.megamod.feature.map.MapWaypointSyncManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class MapScreen extends Screen {

    private final Screen parent;

    // Smooth zoom: blocks per pixel (0.5 = very close, 32.0 = very far)
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 32.0;
    private static final double ZOOM_FACTOR = 1.2;
    // Static so zoom/pan state persists across screen open/close in same session
    private static double zoomScale = 4.0;
    private static double panOffsetX = 0;
    private static double panOffsetZ = 0;
    private static String lastWorldId = "";
    private boolean dragging = false;
    private double dragStartX, dragStartY;
    private double panStartX, panStartZ;

    // Map area bounds
    private int mapLeft, mapTop, mapWidth, mapHeight;

    // Waypoint system
    private final List<Waypoint> waypoints = new ArrayList<>();
    private boolean waypointPanelOpen = true;
    private int wpScroll = 0;
    private int editingWaypoint = -1;
    private String wpNameInput = "";
    private int wpColorIndex = 0;
    private boolean wpNameActive = false;
    private int wpNameCursorTick = 0;

    // Title bar
    private int titleBarH;

    // Back button
    private int backX, backY, backW, backH;

    // Waypoint panel toggle button
    private int wpToggleX, wpToggleY, wpToggleW, wpToggleH;

    // Add waypoint button
    private int addWpX, addWpY, addWpW, addWpH;

    // Waypoint panel area
    private int wpPanelX, wpPanelY, wpPanelW, wpPanelH;
    private static final int WP_ROW_H = 42;
    private static final int WP_ROW_GAP = 2;

    // Mouse world position
    private int mouseWorldX, mouseWorldZ;
    private boolean mouseOnMap = false;

    // Coordinate display for hovered position
    private String hoveredCoords = "";

    // Waypoint colors
    private static final int[] WP_COLORS = {
        0xFFFF4444, // red
        0xFF4488FF, // blue
        0xFF44CC44, // green
        0xFFFFCC00, // yellow
        0xFFCC44CC, // purple
        0xFFFF8844, // orange
        0xFF44CCCC, // cyan
        0xFFFF88BB  // pink
    };
    private static final String[] WP_COLOR_NAMES = {
        "Red", "Blue", "Green", "Yellow", "Purple", "Orange", "Cyan", "Pink"
    };

    private static final int UNEXPLORED_COLOR = 0xFF1A1A2E;

    // Admin check (for teleport)
    private boolean isAdmin = false;

    // Waypoint data loaded flag
    private boolean waypointsLoaded = false;

    // Search field
    private String wpSearchText = "";
    private boolean wpSearchActive = false;
    private int wpSearchCursorTick = 0;

    // Sort mode: 0=Distance, 1=Name, 2=Date, 3=Category
    private int wpSortMode = 0;
    private static final String[] SORT_LABELS = {"Dist", "Name", "Date", "Cat"};

    // Collapsed categories (by name)
    private final Set<String> collapsedCategories = new HashSet<>();

    // Waypoint list cache (avoid rebuilding every frame)
    private List<Waypoint> cachedFilteredWaypoints = null;
    private int wpCacheFrameCounter = 0;
    private String lastCachedSearchText = null;
    private int lastCachedSortMode = -1;

    // Pre-built index map for O(1) original index lookup
    private Map<Waypoint, Integer> waypointOrigIndexMap = null;

    // Confirmation message
    private String confirmMessage = null;
    private int confirmTicks = 0;

    // Waypoint refresh after mutation (re-requests data to handle lost responses)
    private int wpRefreshCountdown = -1;

    // ===== Phase B & C: Toolbar, Layers, Drawing =====
    private MapToolbar toolbar;
    private final MapLayerRenderer layers = new MapLayerRenderer();

    // Layer toggle sidebar position
    private int layerToggleX, layerToggleY;

    // "Go To" coordinate popup
    private boolean gotoPopupOpen = false;
    private String gotoXInput = "";
    private String gotoZInput = "";
    private boolean gotoXActive = false;
    private boolean gotoZActive = false;
    private int gotoCursorTick = 0;

    // Drawing tool state
    private int[] drawLineStart = null; // world coords [x, z]
    private int[] measureStart = null;  // world coords [x, z]
    private String measureDistLabel = null;
    private boolean drawingsLoaded = false;

    // Text placement state
    private boolean placingText = false;
    private int placeTextWorldX, placeTextWorldZ;
    private String placeTextInput = "";
    private boolean placeTextActive = false;
    private int placeTextCursorTick = 0;

    // Drawing color for lines/text (cycle with right-click)
    private int drawingColorIndex = 0;
    private static final int[] DRAWING_COLORS = {
        0xFFFF4444, 0xFF4488FF, 0xFF44CC44, 0xFFFFCC00,
        0xFFCC44CC, 0xFFFF8844, 0xFF44CCCC, 0xFFFFFFFF
    };

    // ===== Phase D & E: Chunk Highlights & Structure Detection =====
    private boolean highlightsLoaded = false;
    private boolean structuresLoaded = false;
    private int highlightRequestCooldown = 0;

    public record Waypoint(String id, String name, int x, int y, int z, int colorIndex, long created,
                           String category, String dimension, boolean beaconEnabled) {}

    public MapScreen(Screen parent) {
        super((Component) Component.literal((String) "World Map"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;

        // Check admin status
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            String name = mc.player.getGameProfile().name();
            this.isAdmin = "NeverNotch".equals(name) || "Dev".equals(name);
            this.layers.isAdmin = this.isAdmin;
        }

        // Back button in title bar
        this.backW = 50;
        this.backH = 16;
        this.backX = 8;
        this.backY = (this.titleBarH - this.backH) / 2;

        // Waypoint panel dimensions (right side)
        this.wpPanelW = 140;
        this.wpPanelH = this.height - this.titleBarH - 30;
        this.wpPanelX = this.width - this.wpPanelW - 6;
        this.wpPanelY = this.titleBarH + 6;

        // Toggle waypoint panel button
        this.wpToggleW = 14;
        this.wpToggleH = 14;
        this.wpToggleX = this.wpPanelX - this.wpToggleW - 2;
        this.wpToggleY = this.wpPanelY;

        // Add waypoint button (below map)
        this.addWpW = 100;
        this.addWpH = 16;
        this.addWpX = 8;
        this.addWpY = this.height - this.addWpH - 6;

        // Initialize toolbar below title bar
        int toolbarY = this.titleBarH;
        int rightMargin = this.waypointPanelOpen ? (this.wpPanelW + 22) : 6;
        this.toolbar = new MapToolbar(0, toolbarY, this.width);

        // Map area (adjusted for toolbar)
        int toolbarH = this.toolbar.getBarHeight();
        int layerToggleW = this.layers.getToggleColumnWidth() + 4;
        this.mapLeft = 6 + layerToggleW;
        this.mapTop = this.titleBarH + toolbarH + 2;
        this.mapWidth = this.width - this.mapLeft - rightMargin;
        this.mapHeight = this.height - this.mapTop - 28;

        // Layer toggle sidebar position (left of map)
        this.layerToggleX = 4;
        this.layerToggleY = this.mapTop + 4;

        // Request waypoints from server
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("map_request_waypoints", ""),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );

        // Request drawings from server
        if (!this.drawingsLoaded) {
            String dim = "minecraft:overworld";
            if (mc.level != null) {
                dim = mc.level.dimension().identifier().toString();
            }
            sendToServer("map_request_drawings", "{\"dimension\":\"" + escapeJson(dim) + "\"}");
        }

        // Request chunk highlights and structures from server
        if (!this.highlightsLoaded) {
            sendToServer("map_request_highlights", "{}");
        }
        if (!this.structuresLoaded && this.isAdmin) {
            sendToServer("map_request_structures", "{}");
            sendToServer("map_locate_all_structures", "{}");
        }

        // Initialize chunk tile manager for GPU-accelerated map rendering
        MapChunkTileManager mgr = MapChunkTileManager.getInstance();
        mgr.initialize(mc.level);

        // Reset pan/zoom when switching worlds so we don't show stale offsets
        String worldId = mgr.getCurrentWorldId();
        if (!worldId.equals(lastWorldId)) {
            zoomScale = 4.0;
            panOffsetX = 0;
            panOffsetZ = 0;
            lastWorldId = worldId;
        }
    }

    private void recalculateLayout() {
        int rightMargin = this.waypointPanelOpen ? (this.wpPanelW + 22) : 6;
        int layerToggleW = this.layers.getToggleColumnWidth() + 4;
        this.mapLeft = 6 + layerToggleW;
        this.mapWidth = this.width - this.mapLeft - rightMargin;
        int toolbarH = this.toolbar != null ? this.toolbar.getBarHeight() : 18;
        this.mapTop = this.titleBarH + toolbarH + 2;
        this.mapHeight = this.height - this.mapTop - 28;
        if (this.toolbar != null) {
            this.toolbar.updatePosition(0, this.titleBarH, this.width);
        }
    }

    private void sendToServer(String action, String jsonData) {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload(action, jsonData),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (this.confirmTicks > 0) {
            this.confirmTicks--;
            if (this.confirmTicks <= 0) this.confirmMessage = null;
        }
        if (this.wpNameActive) {
            this.wpNameCursorTick++;
        }
        if (this.wpSearchActive) {
            this.wpSearchCursorTick++;
        }
        if (this.gotoXActive || this.gotoZActive) {
            this.gotoCursorTick++;
        }
        if (this.placeTextActive) {
            this.placeTextCursorTick++;
        }

        // Check for server responses
        ComputerDataPayload response = ComputerDataPayload.lastResponse;
        if (response != null) {
            if ("map_waypoint_data".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                parseWaypoints(response.jsonData());
                this.waypointsLoaded = true;
            } else if ("map_waypoint_saved".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                parseWaypoints(response.jsonData());
                this.confirmMessage = "Waypoint saved!";
                this.confirmTicks = 60;
            } else if ("map_waypoint_deleted".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                parseWaypoints(response.jsonData());
                this.editingWaypoint = -1;
                this.confirmMessage = "Waypoint deleted!";
                this.confirmTicks = 60;
            } else if ("map_waypoint_shared".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                this.confirmMessage = "Waypoint shared in chat!";
                this.confirmTicks = 60;
            } else if ("map_drawing_data".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                parseDrawings(response.jsonData());
                this.drawingsLoaded = true;
            } else if ("map_drawing_saved".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                parseDrawings(response.jsonData());
                this.confirmMessage = "Drawing saved!";
                this.confirmTicks = 40;
            } else if ("map_drawing_deleted".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                parseDrawings(response.jsonData());
                this.confirmMessage = "Drawing deleted!";
                this.confirmTicks = 40;
            } else if ("map_highlight_data".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                parseHighlights(response.jsonData());
                this.highlightsLoaded = true;
            } else if ("map_structure_data".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                parseStructures(response.jsonData());
                this.structuresLoaded = true;
            } else if ("map_bulk_result".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                parseWaypoints(response.jsonData());
                this.confirmMessage = "Bulk operation applied!";
                this.confirmTicks = 60;
            } else if ("map_share_mail_result".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                this.confirmMessage = "Waypoint shared via mail!";
                this.confirmTicks = 60;
            } else if ("error".equals(response.dataType())) {
                ComputerDataPayload.lastResponse = null;
                this.waypointsLoaded = true;
                this.drawingsLoaded = true;
                this.highlightsLoaded = true;
                if (this.isAdmin) {
                    this.structuresLoaded = true;
                }
            }
        }

        // Re-request waypoint data after mutations to handle lost responses
        if (this.wpRefreshCountdown > 0) {
            this.wpRefreshCountdown--;
        } else if (this.wpRefreshCountdown == 0) {
            this.wpRefreshCountdown = -1;
            sendToServer("map_request_waypoints", "");
        }

        // Retry loading waypoints/drawings if initial response was lost (race condition)
        if (!this.waypointsLoaded || !this.drawingsLoaded) {
            if (this.highlightRequestCooldown == 100) { // piggyback on existing timer, offset by 5s
                if (!this.waypointsLoaded) {
                    sendToServer("map_request_waypoints", "");
                }
                if (!this.drawingsLoaded && this.minecraft != null && this.minecraft.level != null) {
                    String dim = this.minecraft.level.dimension().identifier().toString();
                    sendToServer("map_request_drawings", "{\"dimension\":\"" + escapeJson(dim) + "\"}");
                }
            }
        }

        // Periodically re-request highlights as player moves
        if (this.highlightRequestCooldown > 0) {
            this.highlightRequestCooldown--;
        } else {
            this.highlightRequestCooldown = 200; // every 10 seconds
            sendToServer("map_request_highlights", "{}");
            if (this.isAdmin) {
                sendToServer("map_request_structures", "{}");
            }
        }

        // Update chunk tiles (renders loaded chunks incrementally)
        MapChunkTileManager.getInstance().updateCurrentTile();
    }

    private double getZoomScale() {
        return this.zoomScale;
    }

    private String getZoomLabel() {
        if (this.zoomScale < 1.0) return String.format("1:%.1f", 1.0 / this.zoomScale);
        if (this.zoomScale == 1.0) return "1:1";
        return String.format("%.1f:1", this.zoomScale);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Background
        g.fill(0, 0, this.width, this.height, 0xFF0E0E18);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);

        // Title bar
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, "World Map", this.width / 2, titleY);

        // Back button
        boolean backHover = mouseX >= this.backX && mouseX < this.backX + this.backW
                && mouseY >= this.backY && mouseY < this.backY + this.backH;
        UIHelper.drawButton(g, this.backX, this.backY, this.backW, this.backH, backHover);
        int backTextX = this.backX + (this.backW - this.font.width("< Back")) / 2;
        g.drawString(this.font, "< Back", backTextX, this.backY + (this.backH - 9) / 2, 0xFFCCCCDD, false);

        // Zoom label in title bar
        String zoomStr = "Zoom: " + getZoomLabel();
        int zoomW = this.font.width(zoomStr);
        g.drawString(this.font, zoomStr, this.width - zoomW - 10, titleY, 0xFF666677, false);

        // Render toolbar
        if (this.toolbar != null) {
            this.toolbar.render(g, this.font, mouseX, mouseY);
        }

        // Render map (before toggles so tooltip draws on top)
        renderMap(g, mouseX, mouseY);

        // Render layer toggle sidebar
        this.layers.renderToggles(g, this.font, this.layerToggleX, this.layerToggleY, mouseX, mouseY);

        // Render waypoint panel toggle
        boolean toggleHover = mouseX >= this.wpToggleX && mouseX < this.wpToggleX + this.wpToggleW
                && mouseY >= this.wpToggleY && mouseY < this.wpToggleY + this.wpToggleH;
        UIHelper.drawButton(g, this.wpToggleX, this.wpToggleY, this.wpToggleW, this.wpToggleH, toggleHover);
        String arrow = this.waypointPanelOpen ? ">" : "<";
        int arrowW = this.font.width(arrow);
        g.drawString(this.font, arrow, this.wpToggleX + (this.wpToggleW - arrowW) / 2,
                this.wpToggleY + (this.wpToggleH - 9) / 2, 0xFFCCCCDD, false);

        // Render waypoint panel
        if (this.waypointPanelOpen) {
            renderWaypointPanel(g, mouseX, mouseY);
        }

        // Add waypoint button
        boolean addHover = mouseX >= this.addWpX && mouseX < this.addWpX + this.addWpW
                && mouseY >= this.addWpY && mouseY < this.addWpY + this.addWpH;
        UIHelper.drawButton(g, this.addWpX, this.addWpY, this.addWpW, this.addWpH, addHover);
        String addStr = "+ Add Waypoint";
        int addTextX = this.addWpX + (this.addWpW - this.font.width(addStr)) / 2;
        g.drawString(this.font, addStr, addTextX, this.addWpY + (this.addWpH - 9) / 2, 0xFFCCCCDD, false);

        // Bottom info bar
        renderInfoBar(g, mouseX, mouseY);

        // Confirmation message
        if (this.confirmMessage != null && this.confirmTicks > 0) {
            int msgW = this.font.width(this.confirmMessage) + 20;
            int msgX = (this.width - msgW) / 2;
            int msgY = this.height / 2 - 15;
            UIHelper.drawCard(g, msgX, msgY, msgW, 20);
            UIHelper.drawCenteredLabel(g, this.font, this.confirmMessage, this.width / 2, msgY + 6);
        }

        // "Go To" coordinate popup
        if (this.gotoPopupOpen) {
            renderGotoPopup(g, mouseX, mouseY);
        }

        // Text placement input popup
        if (this.placingText) {
            renderTextPlacementPopup(g, mouseX, mouseY);
        }

        // Current tool label in bottom-left
        if (this.toolbar != null && this.toolbar.getCurrentTool() != MapToolbar.Tool.PAN) {
            String toolName = "Tool: " + this.toolbar.getCurrentTool().name();
            int toolLabelY = this.addWpY - 14;
            g.drawString(this.font, toolName, this.mapLeft + 2, toolLabelY, 0xFF58A6FF, false);
            // Drawing color indicator
            if (this.toolbar.getCurrentTool() == MapToolbar.Tool.DRAW_LINE
                    || this.toolbar.getCurrentTool() == MapToolbar.Tool.PLACE_TEXT) {
                int dcColor = DRAWING_COLORS[this.drawingColorIndex % DRAWING_COLORS.length];
                g.fill(this.mapLeft + 2 + this.font.width(toolName) + 4, toolLabelY,
                       this.mapLeft + 2 + this.font.width(toolName) + 14, toolLabelY + 9, dcColor);
                g.drawString(this.font, "[RMB=color]", this.mapLeft + 2 + this.font.width(toolName) + 18,
                        toolLabelY, 0xFF666688, false);
            }
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderMap(GuiGraphics g, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int playerX = (int) mc.player.getX();
        int playerZ = (int) mc.player.getZ();
        double zoomScale = getZoomScale();

        // Map background (unexplored)
        g.fill(this.mapLeft, this.mapTop, this.mapLeft + this.mapWidth, this.mapTop + this.mapHeight, UNEXPLORED_COLOR);

        // Scissor clip to map viewport — prevents tiles from rendering outside map bounds
        g.enableScissor(this.mapLeft, this.mapTop, this.mapLeft + this.mapWidth, this.mapTop + this.mapHeight);

        // Center of map in world coords (player + pan) — use double for precision
        double centerWorldX = playerX + this.panOffsetX;
        double centerWorldZ = playerZ + this.panOffsetZ;
        int centerWorldXi = (int) Math.floor(centerWorldX);
        int centerWorldZi = (int) Math.floor(centerWorldZ);

        // Draw terrain using cached chunk tiles (GPU-accelerated)
        MapChunkTileManager tileManager = MapChunkTileManager.getInstance();
        int halfViewBlocks = (int) (this.mapWidth * zoomScale / 2);
        int halfViewBlocksZ = (int) (this.mapHeight * zoomScale / 2);

        int minTileX = Math.floorDiv((centerWorldXi - halfViewBlocks) >> 4, MapChunkTile.TILE_SIZE);
        int maxTileX = Math.floorDiv((centerWorldXi + halfViewBlocks) >> 4, MapChunkTile.TILE_SIZE);
        int minTileZ = Math.floorDiv((centerWorldZi - halfViewBlocksZ) >> 4, MapChunkTile.TILE_SIZE);
        int maxTileZ = Math.floorDiv((centerWorldZi + halfViewBlocksZ) >> 4, MapChunkTile.TILE_SIZE);

        // Cap visible tile range to prevent tile explosion at extreme zoom/pan
        int maxTileRange = 10;
        int centerTileX = Math.floorDiv(centerWorldXi >> 4, MapChunkTile.TILE_SIZE);
        int centerTileZ = Math.floorDiv(centerWorldZi >> 4, MapChunkTile.TILE_SIZE);
        minTileX = Math.max(minTileX, centerTileX - maxTileRange);
        maxTileX = Math.min(maxTileX, centerTileX + maxTileRange);
        minTileZ = Math.max(minTileZ, centerTileZ - maxTileRange);
        maxTileZ = Math.min(maxTileZ, centerTileZ + maxTileRange);

        // Continuous zoom: compute tile positions using floating-point to avoid seams
        double tileWorldSize = MapChunkTile.TILE_SIZE * 16.0;
        double tileScreenSizeD = tileWorldSize / zoomScale;
        if (tileScreenSizeD >= 1) {
            // Reference point: map center corresponds to centerWorld (precise double)
            double refScreenX = this.mapLeft + this.mapWidth / 2.0 - centerWorldX / zoomScale;
            double refScreenY = this.mapTop + this.mapHeight / 2.0 - centerWorldZ / zoomScale;
            try {
                for (int tx = minTileX; tx <= maxTileX; tx++) {
                    for (int tz = minTileZ; tz <= maxTileZ; tz++) {
                        MapChunkTile tile = tileManager.getTileForRendering(tx, tz);
                        if (tile == null || tile.getTextureId() == null) continue;

                        // Compute exact screen bounds to eliminate seams between tiles
                        int screenX = (int) Math.floor(refScreenX + tx * tileWorldSize / zoomScale);
                        int screenY = (int) Math.floor(refScreenY + tz * tileWorldSize / zoomScale);
                        int nextScreenX = (int) Math.floor(refScreenX + (tx + 1) * tileWorldSize / zoomScale);
                        int nextScreenY = (int) Math.floor(refScreenY + (tz + 1) * tileWorldSize / zoomScale);

                        tile.render(g, screenX, screenY, nextScreenX - screenX, nextScreenY - screenY);
                    }
                }
            } catch (Exception e) {
                // Gracefully handle any rendering errors to prevent crash
            }
        }

        // Chunk grid overlay (controlled by layer toggle, only when chunks are visible)
        int chunkScreenSize = (int) (16.0 / zoomScale);
        if (this.layers.showChunkGrid && chunkScreenSize >= 4) {
            int halfMapWCg = this.mapWidth / 2;
            int halfMapHCg = this.mapHeight / 2;
            int maxGridLines = (this.mapWidth / chunkScreenSize) + 2;
            // Vertical chunk lines
            int startWorldX = (int) (centerWorldX - halfMapWCg * zoomScale);
            int firstChunkX = ((startWorldX / 16) * 16);
            if (startWorldX < 0 && startWorldX % 16 != 0) firstChunkX -= 16;
            int gridCount = 0;
            for (int cx = firstChunkX; gridCount < maxGridLines; cx += 16, gridCount++) {
                int screenX = (int) (this.mapLeft + halfMapWCg + (cx - centerWorldX) / zoomScale);
                if (screenX > this.mapLeft + this.mapWidth) break;
                if (screenX >= this.mapLeft && screenX < this.mapLeft + this.mapWidth) {
                    g.fill(screenX, this.mapTop, screenX + 1, this.mapTop + this.mapHeight, 0x10FFFFFF);
                }
            }
            // Horizontal chunk lines
            int startWorldZ = (int) (centerWorldZ - halfMapHCg * zoomScale);
            int firstChunkZ = ((startWorldZ / 16) * 16);
            if (startWorldZ < 0 && startWorldZ % 16 != 0) firstChunkZ -= 16;
            int maxGridLinesH = (this.mapHeight / chunkScreenSize) + 2;
            gridCount = 0;
            for (int cz = firstChunkZ; gridCount < maxGridLinesH; cz += 16, gridCount++) {
                int screenY = (int) (this.mapTop + halfMapHCg + (cz - centerWorldZ) / zoomScale);
                if (screenY > this.mapTop + this.mapHeight) break;
                if (screenY >= this.mapTop && screenY < this.mapTop + this.mapHeight) {
                    g.fill(this.mapLeft, screenY, this.mapLeft + this.mapWidth, screenY + 1, 0x10FFFFFF);
                }
            }
        }

        // Region grid overlay (controlled by layer toggle)
        if (this.layers.showRegionGrid) {
            MapLayerRenderer.renderRegionGrid(g, this.font, this.mapLeft, this.mapTop,
                this.mapWidth, this.mapHeight, centerWorldX, centerWorldZ, zoomScale);
        }

        // Drawing overlay (below waypoints/players, above terrain)
        if (this.layers.showDrawings) {
            MapDrawingOverlay.render(g, this.font, this.mapLeft, this.mapTop,
                this.mapWidth, this.mapHeight, centerWorldX, centerWorldZ, zoomScale);
        }

        int halfMapW = this.mapWidth / 2;
        int halfMapH = this.mapHeight / 2;

        // Draw chunk highlights (new chunks, portals, spawn chunks)
        if (this.layers.isLayerEnabled("highlights")) {
            MapHighlightOverlay.render(g, this.mapLeft, this.mapTop, this.mapWidth, this.mapHeight,
                centerWorldX, centerWorldZ, zoomScale);
        }

        // Draw structure markers (admin only)
        if (this.isAdmin && this.layers.isLayerEnabled("structures")) {
            MapStructureOverlay.render(g, this.font, this.mapLeft, this.mapTop, this.mapWidth, this.mapHeight,
                centerWorldX, centerWorldZ, zoomScale, mouseX, mouseY);
        }

        // Determine current dimension for cross-dimension waypoint scaling
        String currentDimension = "minecraft:overworld";
        if (mc.level != null) {
            currentDimension = mc.level.dimension().identifier().toString();
        }

        // Draw waypoint markers on map (controlled by layer toggle)
        if (this.layers.showWaypoints) for (Waypoint wp : this.waypoints) {
            // Cross-dimension coordinate scaling (Nether ↔ Overworld 8:1 ratio)
            int displayX = wp.x;
            int displayZ = wp.z;
            boolean crossDim = false;
            if (wp.dimension != null && !wp.dimension.equals(currentDimension)) {
                if (currentDimension.equals("minecraft:the_nether") && wp.dimension.equals("minecraft:overworld")) {
                    displayX /= 8;
                    displayZ /= 8;
                    crossDim = true;
                } else if (currentDimension.equals("minecraft:overworld") && wp.dimension.equals("minecraft:the_nether")) {
                    displayX *= 8;
                    displayZ *= 8;
                    crossDim = true;
                } else {
                    continue; // Don't show End waypoints in Overworld/Nether or vice versa
                }
            }
            int wpScreenX = (int) (this.mapLeft + halfMapW + (displayX - centerWorldX) / zoomScale);
            int wpScreenZ = (int) (this.mapTop + halfMapH + (displayZ - centerWorldZ) / zoomScale);

            if (wpScreenX >= this.mapLeft - 4 && wpScreenX <= this.mapLeft + this.mapWidth + 4
                    && wpScreenZ >= this.mapTop - 4 && wpScreenZ <= this.mapTop + this.mapHeight + 4) {
                int wpColor = WP_COLORS[wp.colorIndex % WP_COLORS.length];
                // Diamond shape (5x5)
                g.fill(wpScreenX, wpScreenZ - 3, wpScreenX + 1, wpScreenZ - 2, wpColor);
                g.fill(wpScreenX - 1, wpScreenZ - 2, wpScreenX + 2, wpScreenZ - 1, wpColor);
                g.fill(wpScreenX - 2, wpScreenZ - 1, wpScreenX + 3, wpScreenZ, wpColor);
                g.fill(wpScreenX - 1, wpScreenZ, wpScreenX + 2, wpScreenZ + 1, wpColor);
                g.fill(wpScreenX, wpScreenZ + 1, wpScreenX + 1, wpScreenZ + 2, wpColor);
                // Name label (with cross-dimension indicator)
                String label = crossDim ? wp.name + " [" + getDimensionAbbrev(wp.dimension) + "]" : wp.name;
                int labelW = this.font.width(label);
                int labelColor = crossDim ? ((wpColor & 0x00FFFFFF) | 0xAA000000) : wpColor; // dim if cross-dim
                g.fill(wpScreenX - labelW / 2 - 1, wpScreenZ - 13, wpScreenX + labelW / 2 + 1, wpScreenZ - 4, 0xAA000000);
                g.drawString(this.font, label, wpScreenX - labelW / 2, wpScreenZ - 12, labelColor, false);
            }
        }

        // Draw other players (controlled by layer toggle)
        if (this.layers.showPlayers && mc.getConnection() != null) {
            Collection<PlayerInfo> players = mc.getConnection().getOnlinePlayers();
            for (PlayerInfo info : players) {
                if (mc.player != null && info.getProfile().id().equals(mc.player.getUUID())) continue;
                net.minecraft.world.entity.player.Player otherPlayer = mc.level.getPlayerByUUID(info.getProfile().id());
                if (otherPlayer == null) continue;

                int opx = (int) otherPlayer.getX();
                int opz = (int) otherPlayer.getZ();
                int opScreenX = (int) (this.mapLeft + halfMapW + (opx - centerWorldX) / zoomScale);
                int opScreenZ = (int) (this.mapTop + halfMapH + (opz - centerWorldZ) / zoomScale);

                if (opScreenX >= this.mapLeft && opScreenX <= this.mapLeft + this.mapWidth
                        && opScreenZ >= this.mapTop && opScreenZ <= this.mapTop + this.mapHeight) {
                    g.fill(opScreenX - 2, opScreenZ - 2, opScreenX + 3, opScreenZ + 3, 0xFFFFFFFF);
                    g.fill(opScreenX - 1, opScreenZ - 1, opScreenX + 2, opScreenZ + 2, 0xFFDDDDDD);
                    String opName = info.getProfile().name();
                    int nameW = this.font.width(opName);
                    g.fill(opScreenX - nameW / 2 - 1, opScreenZ + 4, opScreenX + nameW / 2 + 1, opScreenZ + 14, 0xAA000000);
                    g.drawString(this.font, opName, opScreenX - nameW / 2, opScreenZ + 5, 0xFFFFFFFF, false);
                }
            }
        }

        // Draw mob entities on map (controlled by layer toggle)
        if (this.layers.showEntities) {
            for (net.minecraft.world.entity.Entity entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof net.minecraft.world.entity.LivingEntity living)) continue;
                if (entity instanceof net.minecraft.world.entity.player.Player) continue;
                if (!entity.isAlive()) continue;

                int ex = (int) entity.getX();
                int ez = (int) entity.getZ();
                int eScreenX = (int) (this.mapLeft + halfMapW + (ex - centerWorldX) / zoomScale);
                int eScreenZ = (int) (this.mapTop + halfMapH + (ez - centerWorldZ) / zoomScale);

                if (eScreenX < this.mapLeft || eScreenX > this.mapLeft + this.mapWidth) continue;
                if (eScreenZ < this.mapTop || eScreenZ > this.mapTop + this.mapHeight) continue;

                // Red for hostile, green for passive
                boolean hostile = entity instanceof net.minecraft.world.entity.monster.Monster;
                int dotColor = hostile ? 0xFFFF4444 : 0xFF44CC44;
                g.fill(eScreenX - 1, eScreenZ - 1, eScreenX + 2, eScreenZ + 2, dotColor);
            }
        }

        // Draw player marker (blue arrow at center)
        int playerScreenX = (int) (this.mapLeft + halfMapW + (playerX - centerWorldX) / zoomScale);
        int playerScreenZ = (int) (this.mapTop + halfMapH + (playerZ - centerWorldZ) / zoomScale);
        float yaw = mc.player.getYRot();
        drawPlayerArrow(g, playerScreenX, playerScreenZ, yaw);

        // Compass rose (top-left of map area)
        int compassX = this.mapLeft + 14;
        int compassY = this.mapTop + 14;
        g.drawString(this.font, "N", compassX - this.font.width("N") / 2, compassY - 12, 0xFFFF4444, false);
        g.drawString(this.font, "S", compassX - this.font.width("S") / 2, compassY + 4, 0xFFDDDDDD, false);
        g.drawString(this.font, "E", compassX + 6, compassY - 4, 0xFFDDDDDD, false);
        g.drawString(this.font, "W", compassX - 6 - this.font.width("W"), compassY - 4, 0xFFDDDDDD, false);
        // Cross lines
        g.fill(compassX, compassY - 6, compassX + 1, compassY + 6, 0x66FFFFFF);
        g.fill(compassX - 6, compassY, compassX + 6, compassY + 1, 0x66FFFFFF);

        // Disable scissor before drawing UI elements (border, tooltips, etc.)
        g.disableScissor();

        // Map border
        // Top
        g.fill(this.mapLeft, this.mapTop, this.mapLeft + this.mapWidth, this.mapTop + 1, 0xFF444466);
        // Bottom
        g.fill(this.mapLeft, this.mapTop + this.mapHeight - 1, this.mapLeft + this.mapWidth, this.mapTop + this.mapHeight, 0xFF444466);
        // Left
        g.fill(this.mapLeft, this.mapTop, this.mapLeft + 1, this.mapTop + this.mapHeight, 0xFF444466);
        // Right
        g.fill(this.mapLeft + this.mapWidth - 1, this.mapTop, this.mapLeft + this.mapWidth, this.mapTop + this.mapHeight, 0xFF444466);

        // Track mouse world position
        this.mouseOnMap = mouseX >= this.mapLeft && mouseX < this.mapLeft + this.mapWidth
                && mouseY >= this.mapTop && mouseY < this.mapTop + this.mapHeight;
        if (this.mouseOnMap) {
            int relX = mouseX - this.mapLeft - halfMapW;
            int relZ = mouseY - this.mapTop - halfMapH;
            this.mouseWorldX = (int) (centerWorldX + relX * zoomScale);
            this.mouseWorldZ = (int) (centerWorldZ + relZ * zoomScale);
            this.hoveredCoords = "X: " + this.mouseWorldX + "  Z: " + this.mouseWorldZ;

            // Crosshair at mouse
            g.fill(mouseX - 4, mouseY, mouseX - 1, mouseY + 1, 0x88FFFFFF);
            g.fill(mouseX + 2, mouseY, mouseX + 5, mouseY + 1, 0x88FFFFFF);
            g.fill(mouseX, mouseY - 4, mouseX + 1, mouseY - 1, 0x88FFFFFF);
            g.fill(mouseX, mouseY + 2, mouseX + 1, mouseY + 5, 0x88FFFFFF);

            // Draw tool-specific overlays
            if (this.toolbar != null) {
                MapToolbar.Tool tool = this.toolbar.getCurrentTool();

                // DRAW_LINE: preview line from start to cursor
                if (tool == MapToolbar.Tool.DRAW_LINE && this.drawLineStart != null) {
                    int sx1 = MapDrawingOverlay.worldToScreenX(this.drawLineStart[0], centerWorldX, this.mapLeft, this.mapWidth, zoomScale);
                    int sy1 = MapDrawingOverlay.worldToScreenY(this.drawLineStart[1], centerWorldZ, this.mapTop, this.mapHeight, zoomScale);
                    int drawColor = DRAWING_COLORS[this.drawingColorIndex % DRAWING_COLORS.length];
                    MapDrawingOverlay.renderDashedLine(g, sx1, sy1, mouseX, mouseY, drawColor,
                            this.mapLeft, this.mapTop, this.mapWidth, this.mapHeight);
                    // Start point marker
                    g.fill(sx1 - 2, sy1 - 2, sx1 + 3, sy1 + 3, drawColor);
                }

                // MEASURE: dashed line + distance label
                if (tool == MapToolbar.Tool.MEASURE && this.measureStart != null) {
                    int sx1 = MapDrawingOverlay.worldToScreenX(this.measureStart[0], centerWorldX, this.mapLeft, this.mapWidth, zoomScale);
                    int sy1 = MapDrawingOverlay.worldToScreenY(this.measureStart[1], centerWorldZ, this.mapTop, this.mapHeight, zoomScale);
                    MapDrawingOverlay.renderDashedLine(g, sx1, sy1, mouseX, mouseY, 0xFFFFCC00,
                            this.mapLeft, this.mapTop, this.mapWidth, this.mapHeight);
                    // Start point marker
                    g.fill(sx1 - 2, sy1 - 2, sx1 + 3, sy1 + 3, 0xFFFFCC00);
                    // Distance label
                    double dx = this.mouseWorldX - this.measureStart[0];
                    double dz = this.mouseWorldZ - this.measureStart[1];
                    int dist = (int) Math.sqrt(dx * dx + dz * dz);
                    String distLabel = dist + " blocks";
                    int labelX = (sx1 + mouseX) / 2;
                    int labelY = (sy1 + mouseY) / 2 - 10;
                    int labelW = this.font.width(distLabel);
                    g.fill(labelX - labelW / 2 - 2, labelY - 1, labelX + labelW / 2 + 2, labelY + 10, 0xCC000000);
                    g.drawString(this.font, distLabel, labelX - labelW / 2, labelY, 0xFFFFCC00, false);
                }

                // PLACE_WAYPOINT: crosshair with WP icon
                if (tool == MapToolbar.Tool.PLACE_WAYPOINT) {
                    g.fill(mouseX - 1, mouseY - 6, mouseX + 2, mouseY + 7, 0xAAFF8844);
                    g.fill(mouseX - 6, mouseY - 1, mouseX + 7, mouseY + 2, 0xAAFF8844);
                }
            }
        } else {
            this.hoveredCoords = "";
        }

        // Measure result (persisted after second click)
        if (this.measureDistLabel != null && this.toolbar != null
                && this.toolbar.getCurrentTool() == MapToolbar.Tool.MEASURE && this.measureStart == null) {
            int resultW = this.font.width(this.measureDistLabel) + 10;
            int resultX = this.mapLeft + this.mapWidth / 2 - resultW / 2;
            int resultY = this.mapTop + 4;
            g.fill(resultX, resultY, resultX + resultW, resultY + 12, 0xCC000000);
            UIHelper.drawCenteredLabel(g, this.font, this.measureDistLabel, this.mapLeft + this.mapWidth / 2, resultY + 2);
        }
    }

    private void drawPlayerArrow(GuiGraphics g, int cx, int cy, float yaw) {
        // Simplified directional arrow: 7x7 pixel area
        int color = 0xFF58A6FF;
        int outline = 0xFF1A3A66;

        // Normalize yaw to 0-360, then convert MC yaw to map direction:
        // MC yaw 0 = South (+Z = down on map), so add 180 to get map-north-up orientation
        float normYaw = (((yaw + 180) % 360) + 360) % 360;

        // 8-directional arrow (now in map coordinates: 0 = North/up)
        if (normYaw >= 337.5 || normYaw < 22.5) {
            // North (up)
            drawArrowNorth(g, cx, cy, color, outline);
        } else if (normYaw >= 22.5 && normYaw < 67.5) {
            // NE
            drawArrowNE(g, cx, cy, color, outline);
        } else if (normYaw >= 67.5 && normYaw < 112.5) {
            // East
            drawArrowEast(g, cx, cy, color, outline);
        } else if (normYaw >= 112.5 && normYaw < 157.5) {
            // SE
            drawArrowSE(g, cx, cy, color, outline);
        } else if (normYaw >= 157.5 && normYaw < 202.5) {
            // South
            drawArrowSouth(g, cx, cy, color, outline);
        } else if (normYaw >= 202.5 && normYaw < 247.5) {
            // SW
            drawArrowSW(g, cx, cy, color, outline);
        } else if (normYaw >= 247.5 && normYaw < 292.5) {
            // West
            drawArrowWest(g, cx, cy, color, outline);
        } else {
            // NW
            drawArrowNW(g, cx, cy, color, outline);
        }
    }

    private void drawArrowNorth(GuiGraphics g, int cx, int cy, int color, int outline) {
        // Upward arrow
        g.fill(cx - 1, cy - 4, cx + 2, cy - 3, outline);
        g.fill(cx, cy - 4, cx + 1, cy - 3, color);
        g.fill(cx - 2, cy - 3, cx + 3, cy - 2, outline);
        g.fill(cx - 1, cy - 3, cx + 2, cy - 2, color);
        g.fill(cx - 3, cy - 2, cx + 4, cy - 1, outline);
        g.fill(cx - 2, cy - 2, cx + 3, cy - 1, color);
        g.fill(cx - 1, cy - 1, cx + 2, cy + 3, outline);
        g.fill(cx, cy - 1, cx + 1, cy + 3, color);
    }

    private void drawArrowSouth(GuiGraphics g, int cx, int cy, int color, int outline) {
        g.fill(cx - 1, cy - 2, cx + 2, cy + 2, outline);
        g.fill(cx, cy - 2, cx + 1, cy + 2, color);
        g.fill(cx - 3, cy + 2, cx + 4, cy + 3, outline);
        g.fill(cx - 2, cy + 2, cx + 3, cy + 3, color);
        g.fill(cx - 2, cy + 3, cx + 3, cy + 4, outline);
        g.fill(cx - 1, cy + 3, cx + 2, cy + 4, color);
        g.fill(cx - 1, cy + 4, cx + 2, cy + 5, outline);
        g.fill(cx, cy + 4, cx + 1, cy + 5, color);
    }

    private void drawArrowEast(GuiGraphics g, int cx, int cy, int color, int outline) {
        g.fill(cx - 2, cy - 1, cx + 2, cy + 2, outline);
        g.fill(cx - 2, cy, cx + 2, cy + 1, color);
        g.fill(cx + 2, cy - 3, cx + 3, cy + 4, outline);
        g.fill(cx + 2, cy - 2, cx + 3, cy + 3, color);
        g.fill(cx + 3, cy - 2, cx + 4, cy + 3, outline);
        g.fill(cx + 3, cy - 1, cx + 4, cy + 2, color);
        g.fill(cx + 4, cy - 1, cx + 5, cy + 2, outline);
        g.fill(cx + 4, cy, cx + 5, cy + 1, color);
    }

    private void drawArrowWest(GuiGraphics g, int cx, int cy, int color, int outline) {
        g.fill(cx - 1, cy - 1, cx + 3, cy + 2, outline);
        g.fill(cx - 1, cy, cx + 3, cy + 1, color);
        g.fill(cx - 2, cy - 2, cx - 1, cy + 3, outline);
        g.fill(cx - 2, cy - 1, cx - 1, cy + 2, color);
        g.fill(cx - 3, cy - 2, cx - 2, cy + 3, outline);
        g.fill(cx - 3, cy - 1, cx - 2, cy + 2, color);
        g.fill(cx - 4, cy - 1, cx - 3, cy + 2, outline);
        g.fill(cx - 4, cy, cx - 3, cy + 1, color);
    }

    private void drawArrowNE(GuiGraphics g, int cx, int cy, int color, int outline) {
        g.fill(cx, cy - 3, cx + 3, cy - 2, outline);
        g.fill(cx + 1, cy - 3, cx + 3, cy - 2, color);
        g.fill(cx + 1, cy - 2, cx + 4, cy - 1, outline);
        g.fill(cx + 2, cy - 2, cx + 4, cy - 1, color);
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, outline);
        g.fill(cx, cy, cx + 1, cy + 1, color);
        g.fill(cx - 2, cy + 1, cx + 1, cy + 2, outline);
        g.fill(cx - 2, cy + 1, cx, cy + 2, color);
    }

    private void drawArrowSE(GuiGraphics g, int cx, int cy, int color, int outline) {
        g.fill(cx + 1, cy + 2, cx + 4, cy + 3, outline);
        g.fill(cx + 2, cy + 2, cx + 4, cy + 3, color);
        g.fill(cx, cy + 3, cx + 3, cy + 4, outline);
        g.fill(cx + 1, cy + 3, cx + 3, cy + 4, color);
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, outline);
        g.fill(cx, cy, cx + 1, cy + 1, color);
        g.fill(cx - 2, cy - 1, cx + 1, cy, outline);
        g.fill(cx - 2, cy - 1, cx, cy, color);
    }

    private void drawArrowSW(GuiGraphics g, int cx, int cy, int color, int outline) {
        g.fill(cx - 3, cy + 2, cx, cy + 3, outline);
        g.fill(cx - 3, cy + 2, cx - 1, cy + 3, color);
        g.fill(cx - 2, cy + 3, cx + 1, cy + 4, outline);
        g.fill(cx - 2, cy + 3, cx, cy + 4, color);
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, outline);
        g.fill(cx, cy, cx + 1, cy + 1, color);
        g.fill(cx, cy - 1, cx + 3, cy, outline);
        g.fill(cx + 1, cy - 1, cx + 3, cy, color);
    }

    private void drawArrowNW(GuiGraphics g, int cx, int cy, int color, int outline) {
        g.fill(cx - 3, cy - 2, cx, cy - 1, outline);
        g.fill(cx - 3, cy - 2, cx - 1, cy - 1, color);
        g.fill(cx - 2, cy - 3, cx + 1, cy - 2, outline);
        g.fill(cx - 2, cy - 3, cx, cy - 2, color);
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, outline);
        g.fill(cx, cy, cx + 1, cy + 1, color);
        g.fill(cx, cy + 1, cx + 3, cy + 2, outline);
        g.fill(cx + 1, cy + 1, cx + 3, cy + 2, color);
    }

    /**
     * Returns a short dimension abbreviation for display.
     */
    private String getDimensionAbbrev(String dimension) {
        if (dimension == null) return "OW";
        return switch (dimension) {
            case "minecraft:the_nether" -> "N";
            case "minecraft:the_end" -> "E";
            default -> "OW";
        };
    }

    /**
     * Build a filtered + sorted view of waypoints for the panel.
     */
    private List<Waypoint> getFilteredSortedWaypoints() {
        List<Waypoint> result = new ArrayList<>();
        String search = this.wpSearchText.toLowerCase().trim();

        for (Waypoint wp : this.waypoints) {
            if (!search.isEmpty() && !wp.name.toLowerCase().contains(search)) continue;
            result.add(wp);
        }

        Minecraft mc = Minecraft.getInstance();
        switch (this.wpSortMode) {
            case 0 -> { // Distance
                if (mc.player != null) {
                    double px = mc.player.getX();
                    double pz = mc.player.getZ();
                    result.sort(Comparator.comparingDouble(w -> {
                        double ddx = px - w.x;
                        double ddz = pz - w.z;
                        return ddx * ddx + ddz * ddz;
                    }));
                }
            }
            case 1 -> result.sort(Comparator.comparing(w -> w.name.toLowerCase())); // Name A-Z
            case 2 -> result.sort(Comparator.comparingLong(Waypoint::created).reversed()); // Date newest
            case 3 -> result.sort(Comparator.comparing(Waypoint::category).thenComparing(w -> w.name.toLowerCase())); // Category
        }

        return result;
    }

    /**
     * Group waypoints by category, maintaining order.
     */
    private LinkedHashMap<String, List<Waypoint>> groupByCategory(List<Waypoint> wps) {
        LinkedHashMap<String, List<Waypoint>> groups = new LinkedHashMap<>();
        for (Waypoint wp : wps) {
            groups.computeIfAbsent(wp.category, k -> new ArrayList<>()).add(wp);
        }
        return groups;
    }

    private void renderWaypointPanel(GuiGraphics g, int mouseX, int mouseY) {
        // Panel background
        UIHelper.drawInsetPanel(g, this.wpPanelX, this.wpPanelY, this.wpPanelW, this.wpPanelH);

        // Panel title
        UIHelper.drawCenteredLabel(g, this.font, "Waypoints", this.wpPanelX + this.wpPanelW / 2, this.wpPanelY + 4);
        UIHelper.drawHorizontalDivider(g, this.wpPanelX + 4, this.wpPanelY + 16, this.wpPanelW - 8);

        // Search field (below title)
        int searchX = this.wpPanelX + 4;
        int searchY = this.wpPanelY + 20;
        int searchW = this.wpPanelW - 46;
        int searchH = 12;
        g.fill(searchX, searchY, searchX + searchW, searchY + searchH, 0xFF111122);
        g.fill(searchX, searchY, searchX + searchW, searchY + 1, this.wpSearchActive ? 0xFF58A6FF : 0xFF333344);
        g.fill(searchX, searchY + searchH - 1, searchX + searchW, searchY + searchH, 0xFF333344);
        g.fill(searchX, searchY, searchX + 1, searchY + searchH, this.wpSearchActive ? 0xFF58A6FF : 0xFF333344);
        g.fill(searchX + searchW - 1, searchY, searchX + searchW, searchY + searchH, 0xFF333344);

        String searchDisplay = this.wpSearchText.isEmpty() && !this.wpSearchActive ? "Search..." : this.wpSearchText;
        int searchColor = this.wpSearchText.isEmpty() && !this.wpSearchActive ? 0xFF666688 : 0xFFCCCCDD;
        if (this.wpSearchActive && (this.wpSearchCursorTick / 10) % 2 == 0) {
            searchDisplay = this.wpSearchText + "_";
        }
        g.drawString(this.font, searchDisplay, searchX + 3, searchY + 2, searchColor, false);

        // Sort button (right of search)
        int sortBtnX = searchX + searchW + 3;
        int sortBtnW = this.wpPanelW - searchW - 11;
        boolean sortHover = mouseX >= sortBtnX && mouseX < sortBtnX + sortBtnW
                && mouseY >= searchY && mouseY < searchY + searchH;
        UIHelper.drawButton(g, sortBtnX, searchY, sortBtnW, searchH, sortHover);
        String sortLabel = SORT_LABELS[this.wpSortMode % SORT_LABELS.length];
        int sortLabelW = this.font.width(sortLabel);
        g.drawString(this.font, sortLabel, sortBtnX + (sortBtnW - sortLabelW) / 2, searchY + 2, 0xFFCCCCDD, false);

        if (this.waypoints.isEmpty()) {
            UIHelper.drawCenteredLabel(g, this.font, "No waypoints", this.wpPanelX + this.wpPanelW / 2, searchY + 20);
            return;
        }

        // Cache the filtered list: rebuild every 10 frames or when search/sort state changes
        this.wpCacheFrameCounter++;
        boolean cacheInvalid = this.cachedFilteredWaypoints == null
                || this.wpCacheFrameCounter >= 10
                || !Objects.equals(this.lastCachedSearchText, this.wpSearchText)
                || this.lastCachedSortMode != this.wpSortMode;
        if (cacheInvalid) {
            this.cachedFilteredWaypoints = getFilteredSortedWaypoints();
            this.lastCachedSearchText = this.wpSearchText;
            this.lastCachedSortMode = this.wpSortMode;
            this.wpCacheFrameCounter = 0;
            // Rebuild original index map for O(1) lookup
            this.waypointOrigIndexMap = new java.util.HashMap<>();
            for (int i = 0; i < this.waypoints.size(); i++) {
                this.waypointOrigIndexMap.put(this.waypoints.get(i), i);
            }
        }
        List<Waypoint> filtered = this.cachedFilteredWaypoints;
        if (filtered.isEmpty()) {
            UIHelper.drawCenteredLabel(g, this.font, "No results", this.wpPanelX + this.wpPanelW / 2, searchY + 20);
            return;
        }

        int listTop = searchY + searchH + 4;
        int listHeight = this.wpPanelY + this.wpPanelH - listTop - 4;

        // Build flat render list with category headers
        List<Object> renderItems = new ArrayList<>(); // String = category header, Waypoint = row
        LinkedHashMap<String, List<Waypoint>> groups = groupByCategory(filtered);
        boolean showCategoryHeaders = groups.size() > 1;

        for (Map.Entry<String, List<Waypoint>> entry : groups.entrySet()) {
            if (showCategoryHeaders) {
                renderItems.add(entry.getKey()); // category header
            }
            if (!showCategoryHeaders || !this.collapsedCategories.contains(entry.getKey())) {
                renderItems.addAll(entry.getValue());
            }
        }

        int CATEGORY_HEADER_H = 14;
        // Calculate total content height
        int totalContentH = 0;
        for (Object item : renderItems) {
            totalContentH += (item instanceof String) ? (CATEGORY_HEADER_H + WP_ROW_GAP) : (WP_ROW_H + WP_ROW_GAP);
        }

        int maxScroll = Math.max(0, totalContentH - listHeight);
        this.wpScroll = Math.max(0, Math.min(maxScroll, this.wpScroll));

        // Enable scissor for the list area
        g.enableScissor(this.wpPanelX + 1, listTop, this.wpPanelX + this.wpPanelW - 1, this.wpPanelY + this.wpPanelH - 1);

        int curY = listTop - this.wpScroll;
        for (Object item : renderItems) {
            if (item instanceof String categoryName) {
                // Category header row
                if (curY + CATEGORY_HEADER_H > listTop - CATEGORY_HEADER_H && curY < listTop + listHeight) {
                    int headerX = this.wpPanelX + 3;
                    int headerW = this.wpPanelW - 6;
                    boolean headerHover = mouseX >= headerX && mouseX < headerX + headerW
                            && mouseY >= curY && mouseY < curY + CATEGORY_HEADER_H;
                    g.fill(headerX, curY, headerX + headerW, curY + CATEGORY_HEADER_H,
                            headerHover ? 0xFF282848 : 0xFF202038);
                    boolean collapsed = this.collapsedCategories.contains(categoryName);
                    String arrow = collapsed ? "+" : "-";
                    g.drawString(this.font, arrow, headerX + 3, curY + 3, 0xFF888899, false);
                    g.drawString(this.font, categoryName, headerX + 12, curY + 3, 0xFFAAAACC, false);
                }
                curY += CATEGORY_HEADER_H + WP_ROW_GAP;
            } else {
                Waypoint wp = (Waypoint) item;
                // Use pre-built index map for O(1) lookup instead of indexOf scan
                int origIdx = this.waypointOrigIndexMap != null
                        ? this.waypointOrigIndexMap.getOrDefault(wp, -1) : this.waypoints.indexOf(wp);
                if (curY + WP_ROW_H > listTop - WP_ROW_H && curY < listTop + listHeight) {
                    renderWaypointRow(g, wp, origIdx, this.wpPanelX + 3, curY, this.wpPanelW - 6, mouseX, mouseY);
                }
                curY += WP_ROW_H + WP_ROW_GAP;
            }
        }

        g.disableScissor();

        // Scrollbar
        if (totalContentH > listHeight) {
            float scrollProgress = maxScroll > 0 ? (float) this.wpScroll / maxScroll : 0f;
            UIHelper.drawScrollbar(g, this.wpPanelX + this.wpPanelW - 7, listTop, listHeight, scrollProgress);
        }
    }

    private void renderWaypointRow(GuiGraphics g, Waypoint wp, int origIdx, int rowX, int rowY,
                                    int rowW, int mouseX, int mouseY) {
        boolean isEditing = (this.editingWaypoint == origIdx);
        boolean rowHover = mouseX >= rowX && mouseX < rowX + rowW && mouseY >= rowY && mouseY < rowY + WP_ROW_H;
        int rowBg = isEditing ? 0xFF2A2A4A : (rowHover ? 0xFF222244 : 0xFF1A1A2E);
        g.fill(rowX, rowY, rowX + rowW, rowY + WP_ROW_H, rowBg);

        int wpColor = WP_COLORS[wp.colorIndex % WP_COLORS.length];

        if (isEditing) {
            // Editing mode: show name input + color picker + buttons
            g.fill(rowX + 2, rowY + 2, rowX + rowW - 2, rowY + 13, 0xFF111122);
            String displayName = this.wpNameInput;
            if (this.wpNameActive && (this.wpNameCursorTick / 10) % 2 == 0) {
                displayName = displayName + "_";
            }
            g.drawString(this.font, displayName, rowX + 4, rowY + 3, 0xFFFFFFFF, false);

            // Color swatch (clickable)
            int swatchX = rowX + 2;
            int swatchY = rowY + 15;
            g.fill(swatchX, swatchY, swatchX + 10, swatchY + 10, WP_COLORS[this.wpColorIndex % WP_COLORS.length]);
            g.fill(swatchX, swatchY, swatchX + 10, swatchY + 1, 0xFFFFFFFF);
            g.fill(swatchX, swatchY, swatchX + 1, swatchY + 10, 0xFFFFFFFF);
            g.drawString(this.font, WP_COLOR_NAMES[this.wpColorIndex % WP_COLOR_NAMES.length],
                    swatchX + 13, swatchY + 1, 0xFFAAAAAA, false);

            // Save and Delete buttons
            int btnY = rowY + 28;
            int saveBtnX = rowX + 2;
            int saveBtnW = 30;
            int deleteBtnX = rowX + 35;
            int deleteBtnW = 30;
            int cancelBtnX = rowX + 68;
            int cancelBtnW = 30;

            boolean saveHover = mouseX >= saveBtnX && mouseX < saveBtnX + saveBtnW && mouseY >= btnY && mouseY < btnY + 12;
            boolean deleteHover = mouseX >= deleteBtnX && mouseX < deleteBtnX + deleteBtnW && mouseY >= btnY && mouseY < btnY + 12;
            boolean cancelHover = mouseX >= cancelBtnX && mouseX < cancelBtnX + cancelBtnW && mouseY >= btnY && mouseY < btnY + 12;

            UIHelper.drawButton(g, saveBtnX, btnY, saveBtnW, 12, saveHover);
            g.drawString(this.font, "Save", saveBtnX + 4, btnY + 2, 0xFFCCCCDD, false);

            UIHelper.drawButton(g, deleteBtnX, btnY, deleteBtnW, 12, deleteHover);
            g.drawString(this.font, "Del", deleteBtnX + 6, btnY + 2, 0xFFFF4444, false);

            UIHelper.drawButton(g, cancelBtnX, btnY, cancelBtnW, 12, cancelHover);
            g.drawString(this.font, "X", cancelBtnX + 11, btnY + 2, 0xFFCCCCDD, false);
        } else {
            // Normal display
            // Waypoint marker: skull for Death category, diamond otherwise
            boolean isDeath = "Death".equalsIgnoreCase(wp.category);
            if (isDeath) {
                g.drawString(this.font, "\u2620", rowX + 1, rowY + 1, wpColor, false);
            } else {
                // Color diamond dot
                g.fill(rowX + 3, rowY + 2, rowX + 5, rowY + 6, wpColor);
                g.fill(rowX + 2, rowY + 3, rowX + 6, rowY + 5, wpColor);
            }

            // Name (truncated to fit)
            String name = wp.name;
            int nameMaxW = rowW - 22;
            if (this.font.width(name) > nameMaxW) {
                while (this.font.width(name + "..") > nameMaxW && name.length() > 1) {
                    name = name.substring(0, name.length() - 1);
                }
                name = name + "..";
            }
            g.drawString(this.font, name, rowX + 9, rowY + 1, wpColor, false);

            // Beacon toggle icon (right side of name row)
            int beaconX = rowX + rowW - 10;
            int beaconY = rowY + 1;
            int beaconColor = wp.beaconEnabled ? 0xFF44FF44 : 0xFF555566;
            boolean beaconHover = mouseX >= beaconX - 1 && mouseX < beaconX + 9
                    && mouseY >= beaconY && mouseY < beaconY + 9;
            // Small beam icon: vertical line with a dot
            g.fill(beaconX + 3, beaconY, beaconX + 5, beaconY + 7, beaconHover ? 0xFFFFFFFF : beaconColor);
            g.fill(beaconX + 2, beaconY + 7, beaconX + 6, beaconY + 9, beaconHover ? 0xFFFFFFFF : beaconColor);

            // Coords + dimension tag
            String dimTag = getDimensionAbbrev(wp.dimension);
            String coordStr = wp.x + ", " + wp.y + ", " + wp.z;
            int dimTagW = this.font.width(dimTag) + 6;
            int coordMaxW = rowW - dimTagW - 8;
            if (this.font.width(coordStr) > coordMaxW) {
                coordStr = wp.x + "," + wp.z;
            }
            g.drawString(this.font, coordStr, rowX + 3, rowY + 12, 0xFFAAAAAA, false);

            // Dimension badge
            int dimBadgeX = rowX + rowW - dimTagW - 1;
            int dimColor = switch (dimTag) {
                case "N" -> 0xFFCC4444;
                case "E" -> 0xFFAA44CC;
                default -> 0xFF44AA44;
            };
            g.fill(dimBadgeX, rowY + 11, dimBadgeX + dimTagW, rowY + 21, 0xFF181828);
            g.drawString(this.font, dimTag, dimBadgeX + 3, rowY + 12, dimColor, false);

            // Distance
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                double dx = mc.player.getX() - wp.x;
                double dz = mc.player.getZ() - wp.z;
                int dist = (int) Math.sqrt(dx * dx + dz * dz);
                String distStr = dist + "m";
                int distW = this.font.width(distStr);
                g.drawString(this.font, distStr, rowX + 3, rowY + 23, 0xFF888888, false);
            }

            // Action buttons row
            int btnY = rowY + 30;
            int btnH = 10;
            int editBtnX = rowX + 2;
            int editBtnW = 24;
            int shareBtnX = rowX + 28;
            int shareBtnW = 32;

            boolean editHover = mouseX >= editBtnX && mouseX < editBtnX + editBtnW && mouseY >= btnY && mouseY < btnY + btnH;
            boolean shareHover = mouseX >= shareBtnX && mouseX < shareBtnX + shareBtnW && mouseY >= btnY && mouseY < btnY + btnH;

            UIHelper.drawButton(g, editBtnX, btnY, editBtnW, btnH, editHover);
            g.drawString(this.font, "Edit", editBtnX + 2, btnY + 1, 0xFFCCCCDD, false);

            UIHelper.drawButton(g, shareBtnX, btnY, shareBtnW, btnH, shareHover);
            g.drawString(this.font, "Share", shareBtnX + 3, btnY + 1, 0xFFCCCCDD, false);

            // Teleport button (admin only)
            if (this.isAdmin) {
                int tpBtnX = rowX + 63;
                int tpBtnW = 24;
                boolean tpHover = mouseX >= tpBtnX && mouseX < tpBtnX + tpBtnW && mouseY >= btnY && mouseY < btnY + btnH;
                UIHelper.drawButton(g, tpBtnX, btnY, tpBtnW, btnH, tpHover);
                g.drawString(this.font, "TP", tpBtnX + 5, btnY + 1, 0xFF44CCFF, false);
            }
        }
    }

    private void renderInfoBar(GuiGraphics g, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int barY = this.mapTop + this.mapHeight + 2;
        int barH = 14;

        // Player coords
        int px = (int) mc.player.getX();
        int py = (int) mc.player.getY();
        int pz = (int) mc.player.getZ();
        String posStr = "Pos: " + px + ", " + py + ", " + pz;
        g.drawString(this.font, posStr, this.mapLeft + 2, barY + 3, 0xFF58A6FF, false);

        // Facing direction
        float yaw = ((mc.player.getYRot() % 360) + 360) % 360;
        String facing;
        if (yaw >= 315 || yaw < 45) facing = "South";
        else if (yaw >= 45 && yaw < 135) facing = "West";
        else if (yaw >= 135 && yaw < 225) facing = "North";
        else facing = "East";
        String facingStr = "Facing: " + facing;
        int facingX = this.mapLeft + 2 + this.font.width(posStr) + 20;
        g.drawString(this.font, facingStr, facingX, barY + 3, 0xFFAAAAAA, false);

        // Current biome
        if (mc.level != null) {
            try {
                var biomeHolder = mc.level.getBiome(new BlockPos(px, py, pz));
                String biomeName = biomeHolder.unwrapKey()
                    .map(k -> {
                        String raw = k.identifier().getPath();
                        StringBuilder sb = new StringBuilder();
                        for (String word : raw.split("_")) {
                            if (!sb.isEmpty()) sb.append(" ");
                            if (!word.isEmpty()) {
                                sb.append(Character.toUpperCase(word.charAt(0)));
                                if (word.length() > 1) sb.append(word.substring(1));
                            }
                        }
                        return sb.toString();
                    })
                    .orElse("Unknown");
                String biomeStr = "Biome: " + biomeName;
                int biomeX = facingX + this.font.width(facingStr) + 20;
                g.drawString(this.font, biomeStr, biomeX, barY + 3, 0xFF81C784, false);
            } catch (Exception ignored) {}
        }

        // Mouse coords (right-aligned)
        if (this.mouseOnMap && !this.hoveredCoords.isEmpty()) {
            int hcW = this.font.width(this.hoveredCoords);
            g.drawString(this.font, this.hoveredCoords, this.mapLeft + this.mapWidth - hcW - 2, barY + 3, 0xFFCCCCCC, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) return super.mouseClicked(event, consumed);
        int mx = (int) event.x();
        int my = (int) event.y();
        int button = event.button();

        // GoTo popup (if open, handle clicks inside it first)
        if (this.gotoPopupOpen) {
            if (handleGotoPopupClick(mx, my)) return true;
            // Click outside popup closes it
            this.gotoPopupOpen = false;
            this.gotoXActive = false;
            this.gotoZActive = false;
            return true;
        }

        // Text placement popup (if open, handle it)
        if (this.placingText) {
            if (handleTextPlacementClick(mx, my)) return true;
            this.placingText = false;
            this.placeTextActive = false;
            return true;
        }

        // Back button
        if (mx >= this.backX && mx < this.backX + this.backW && my >= this.backY && my < this.backY + this.backH) {
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }

        // Toolbar clicks
        if (this.toolbar != null && this.toolbar.handleClick(mx, my)) {
            if (this.toolbar.wasGotoClicked()) {
                this.gotoPopupOpen = true;
                this.gotoXInput = "";
                this.gotoZInput = "";
                this.gotoXActive = true;
                this.gotoZActive = false;
                this.gotoCursorTick = 0;
            }
            // Reset tool state when switching tools
            this.drawLineStart = null;
            this.measureStart = null;
            this.measureDistLabel = null;
            return true;
        }

        // Layer toggle clicks
        if (this.layers.handleToggleClick(mx, my, this.layerToggleX, this.layerToggleY)) {
            MapChunkTileManager.getInstance().setCaveView(layers.caveView);
            return true;
        }

        // Waypoint panel toggle
        if (mx >= this.wpToggleX && mx < this.wpToggleX + this.wpToggleW
                && my >= this.wpToggleY && my < this.wpToggleY + this.wpToggleH) {
            this.waypointPanelOpen = !this.waypointPanelOpen;
            recalculateLayout();
            return true;
        }

        // Add waypoint button
        if (mx >= this.addWpX && mx < this.addWpX + this.addWpW
                && my >= this.addWpY && my < this.addWpY + this.addWpH) {
            addWaypointAtPlayer();
            return true;
        }

        // Waypoint panel clicks
        if (this.waypointPanelOpen && mx >= this.wpPanelX && mx < this.wpPanelX + this.wpPanelW
                && my >= this.wpPanelY && my < this.wpPanelY + this.wpPanelH) {
            handleWaypointPanelClick(mx, my);
            return true;
        }

        // Map area clicks (tool-dependent)
        if (mx >= this.mapLeft && mx < this.mapLeft + this.mapWidth
                && my >= this.mapTop && my < this.mapTop + this.mapHeight) {

            // Right-click on map: cycle drawing color
            if (button == 1 && this.toolbar != null) {
                MapToolbar.Tool tool = this.toolbar.getCurrentTool();
                if (tool == MapToolbar.Tool.DRAW_LINE || tool == MapToolbar.Tool.PLACE_TEXT) {
                    this.drawingColorIndex = (this.drawingColorIndex + 1) % DRAWING_COLORS.length;
                    return true;
                }
            }

            if (this.toolbar != null && button == 0) {
                MapToolbar.Tool tool = this.toolbar.getCurrentTool();

                if (tool == MapToolbar.Tool.DRAW_LINE) {
                    return handleDrawLineClick(mx, my);
                } else if (tool == MapToolbar.Tool.PLACE_TEXT) {
                    return handlePlaceTextClick(mx, my);
                } else if (tool == MapToolbar.Tool.PLACE_WAYPOINT) {
                    return handlePlaceWaypointClick(mx, my);
                } else if (tool == MapToolbar.Tool.MEASURE) {
                    return handleMeasureClick(mx, my);
                }
            }

            // PAN tool (default): start drag
            this.dragging = true;
            this.dragStartX = mx;
            this.dragStartY = my;
            this.panStartX = this.panOffsetX;
            this.panStartZ = this.panOffsetZ;
            return true;
        }

        return super.mouseClicked(event, consumed);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent e) {
        if (e.button() == 0) this.dragging = false;
        return super.mouseReleased(e);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (this.dragging && e.button() == 0) {
            this.panOffsetX = this.panStartX - (e.x() - this.dragStartX) * this.zoomScale;
            this.panOffsetZ = this.panStartZ - (e.y() - this.dragStartY) * this.zoomScale;
            return true;
        }
        return super.mouseDragged(e, dx, dy);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Scroll on waypoint panel
        if (this.waypointPanelOpen && mouseX >= this.wpPanelX && mouseX < this.wpPanelX + this.wpPanelW
                && mouseY >= this.wpPanelY && mouseY < this.wpPanelY + this.wpPanelH) {
            this.wpScroll -= (int) scrollY;
            this.wpScroll = Math.max(0, this.wpScroll);
            return true;
        }

        // Scroll on map = smooth zoom toward cursor
        if (mouseX >= this.mapLeft && mouseX < this.mapLeft + this.mapWidth
                && mouseY >= this.mapTop && mouseY < this.mapTop + this.mapHeight) {
            double oldZoom = this.zoomScale;
            if (scrollY > 0) {
                this.zoomScale = Math.max(MIN_ZOOM, this.zoomScale / ZOOM_FACTOR);
            } else if (scrollY < 0) {
                this.zoomScale = Math.min(MAX_ZOOM, this.zoomScale * ZOOM_FACTOR);
            }
            // Zoom toward cursor: adjust pan so the world point under the cursor stays put
            if (oldZoom != this.zoomScale) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    int playerX = (int) mc.player.getX();
                    int playerZ = (int) mc.player.getZ();
                    // World coord under cursor before zoom
                    double relX = mouseX - (this.mapLeft + this.mapWidth / 2.0);
                    double relZ = mouseY - (this.mapTop + this.mapHeight / 2.0);
                    double worldXBefore = playerX + this.panOffsetX + relX * oldZoom;
                    double worldZBefore = playerZ + this.panOffsetZ + relZ * oldZoom;
                    // After zoom, that same screen pixel maps to a different world coord
                    // We want: playerX + newPanX + relX * newZoom == worldXBefore
                    this.panOffsetX = worldXBefore - relX * this.zoomScale - playerX;
                    this.panOffsetZ = worldZBefore - relZ * this.zoomScale - playerZ;
                }
            }
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        int keyCode = event.key();

        // GoTo popup input
        if (this.gotoPopupOpen) {
            if (keyCode == 256) {
                this.gotoPopupOpen = false;
                this.gotoXActive = false;
                this.gotoZActive = false;
                return true;
            }
            if (keyCode == 258) { // Tab key
                if (this.gotoXActive) {
                    this.gotoXActive = false;
                    this.gotoZActive = true;
                } else {
                    this.gotoZActive = false;
                    this.gotoXActive = true;
                }
                this.gotoCursorTick = 0;
                return true;
            }
            if (keyCode == 257 || keyCode == 335) { // Enter
                executeGoto();
                return true;
            }
            if (keyCode == 259) { // Backspace
                if (this.gotoXActive && !this.gotoXInput.isEmpty()) {
                    this.gotoXInput = this.gotoXInput.substring(0, this.gotoXInput.length() - 1);
                } else if (this.gotoZActive && !this.gotoZInput.isEmpty()) {
                    this.gotoZInput = this.gotoZInput.substring(0, this.gotoZInput.length() - 1);
                }
                return true;
            }
            String keyName = org.lwjgl.glfw.GLFW.glfwGetKeyName(keyCode, event.scancode());
            if (keyName != null) {
                // Only allow digits and minus sign
                if (keyName.matches("[0-9\\-]")) {
                    if (this.gotoXActive && this.gotoXInput.length() < 8) {
                        this.gotoXInput += keyName;
                    } else if (this.gotoZActive && this.gotoZInput.length() < 8) {
                        this.gotoZInput += keyName;
                    }
                }
            }
            return true;
        }

        // Text placement input
        if (this.placeTextActive) {
            if (keyCode == 256) {
                this.placingText = false;
                this.placeTextActive = false;
                return true;
            }
            if (keyCode == 257 || keyCode == 335) {
                submitPlacedText();
                return true;
            }
            if (keyCode == 259) {
                if (!this.placeTextInput.isEmpty()) {
                    this.placeTextInput = this.placeTextInput.substring(0, this.placeTextInput.length() - 1);
                }
                return true;
            }
            String keyName = org.lwjgl.glfw.GLFW.glfwGetKeyName(keyCode, event.scancode());
            if (keyName != null && this.placeTextInput.length() < 32) {
                this.placeTextInput += keyName;
            }
            return true;
        }

        // Search field input
        if (this.wpSearchActive) {
            if (keyCode == 256) {
                this.wpSearchActive = false;
                return true;
            }
            if (keyCode == 259) {
                if (!this.wpSearchText.isEmpty()) {
                    this.wpSearchText = this.wpSearchText.substring(0, this.wpSearchText.length() - 1);
                    this.wpScroll = 0;
                }
                return true;
            }
            String keyName = org.lwjgl.glfw.GLFW.glfwGetKeyName(keyCode, event.scancode());
            if (keyName != null && this.wpSearchText.length() < 20) {
                this.wpSearchText += keyName;
                this.wpScroll = 0;
            }
            return true;
        }

        // Waypoint name input
        if (this.wpNameActive) {
            if (keyCode == 256) {
                this.wpNameActive = false;
                this.editingWaypoint = -1;
                return true;
            }
            if (keyCode == 257 || keyCode == 335) {
                saveEditingWaypoint();
                return true;
            }
            if (keyCode == 259) {
                if (!this.wpNameInput.isEmpty()) {
                    this.wpNameInput = this.wpNameInput.substring(0, this.wpNameInput.length() - 1);
                }
                return true;
            }
            // Handle printable characters for waypoint name
            String keyName = org.lwjgl.glfw.GLFW.glfwGetKeyName(keyCode, event.scancode());
            if (keyName != null && this.wpNameInput.length() < 24) {
                this.wpNameInput += keyName;
            }
            return true;
        }

        if (keyCode == 256) {
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }

        if (keyCode == 82) {
            this.panOffsetX = 0;
            this.panOffsetZ = 0;
            return true;
        }

        return super.keyPressed(event);
    }

    private void handleWaypointPanelClick(int mx, int my) {
        // Search field
        int searchX = this.wpPanelX + 4;
        int searchY = this.wpPanelY + 20;
        int searchW = this.wpPanelW - 46;
        int searchH = 12;

        if (mx >= searchX && mx < searchX + searchW && my >= searchY && my < searchY + searchH) {
            this.wpSearchActive = true;
            this.wpSearchCursorTick = 0;
            this.wpNameActive = false;
            return;
        } else if (my >= searchY && my < searchY + searchH) {
            // Sort button
            int sortBtnX = searchX + searchW + 3;
            int sortBtnW = this.wpPanelW - searchW - 11;
            if (mx >= sortBtnX && mx < sortBtnX + sortBtnW) {
                this.wpSortMode = (this.wpSortMode + 1) % SORT_LABELS.length;
                this.wpScroll = 0;
                return;
            }
        }

        // Deactivate search if clicking elsewhere
        this.wpSearchActive = false;

        // Compute list layout matching renderWaypointPanel
        int listTop = searchY + searchH + 4;
        int CATEGORY_HEADER_H = 14;

        List<Waypoint> filtered = getFilteredSortedWaypoints();
        LinkedHashMap<String, List<Waypoint>> groups = groupByCategory(filtered);
        boolean showCategoryHeaders = groups.size() > 1;

        List<Object> renderItems = new ArrayList<>();
        for (Map.Entry<String, List<Waypoint>> entry : groups.entrySet()) {
            if (showCategoryHeaders) {
                renderItems.add(entry.getKey());
            }
            if (!showCategoryHeaders || !this.collapsedCategories.contains(entry.getKey())) {
                renderItems.addAll(entry.getValue());
            }
        }

        int curY = listTop - this.wpScroll;
        int rowX = this.wpPanelX + 3;
        int rowW = this.wpPanelW - 6;

        for (Object item : renderItems) {
            if (item instanceof String categoryName) {
                // Category header click = toggle collapse
                if (mx >= rowX && mx < rowX + rowW && my >= curY && my < curY + CATEGORY_HEADER_H) {
                    if (this.collapsedCategories.contains(categoryName)) {
                        this.collapsedCategories.remove(categoryName);
                    } else {
                        this.collapsedCategories.add(categoryName);
                    }
                    return;
                }
                curY += CATEGORY_HEADER_H + WP_ROW_GAP;
            } else {
                Waypoint wp = (Waypoint) item;
                int origIdx = this.waypoints.indexOf(wp);

                if (mx >= rowX && mx < rowX + rowW && my >= curY && my < curY + WP_ROW_H) {
                    handleWaypointRowClick(wp, origIdx, rowX, curY, rowW, mx, my);
                    return;
                }
                curY += WP_ROW_H + WP_ROW_GAP;
            }
        }
    }

    private void handleWaypointRowClick(Waypoint wp, int origIdx, int rowX, int rowY, int rowW, int mx, int my) {
        if (this.editingWaypoint == origIdx) {
            // Editing mode clicks
            if (my >= rowY + 2 && my < rowY + 13) {
                this.wpNameActive = true;
                this.wpNameCursorTick = 0;
                return;
            }

            int swatchX = rowX + 2;
            int swatchY = rowY + 15;
            if (mx >= swatchX && mx < swatchX + 60 && my >= swatchY && my < swatchY + 10) {
                this.wpColorIndex = (this.wpColorIndex + 1) % WP_COLORS.length;
                return;
            }

            int btnY = rowY + 28;
            int saveBtnX = rowX + 2;
            if (mx >= saveBtnX && mx < saveBtnX + 30 && my >= btnY && my < btnY + 12) {
                saveEditingWaypoint();
                return;
            }

            int deleteBtnX = rowX + 35;
            if (mx >= deleteBtnX && mx < deleteBtnX + 30 && my >= btnY && my < btnY + 12) {
                deleteWaypoint(wp.id);
                return;
            }

            int cancelBtnX = rowX + 68;
            if (mx >= cancelBtnX && mx < cancelBtnX + 30 && my >= btnY && my < btnY + 12) {
                this.editingWaypoint = -1;
                this.wpNameActive = false;
                return;
            }
        } else {
            // Beacon toggle (top-right of row)
            int beaconX = rowX + rowW - 10;
            int beaconY = rowY + 1;
            if (mx >= beaconX - 1 && mx < beaconX + 9 && my >= beaconY && my < beaconY + 9) {
                toggleWaypointBeacon(wp);
                return;
            }

            // Action buttons row
            int btnY = rowY + 30;
            int btnH = 10;

            int editBtnX = rowX + 2;
            if (mx >= editBtnX && mx < editBtnX + 24 && my >= btnY && my < btnY + btnH) {
                this.editingWaypoint = origIdx;
                this.wpNameInput = wp.name;
                this.wpColorIndex = wp.colorIndex;
                this.wpNameActive = true;
                this.wpSearchActive = false;
                this.wpNameCursorTick = 0;
                return;
            }

            int shareBtnX = rowX + 28;
            if (mx >= shareBtnX && mx < shareBtnX + 32 && my >= btnY && my < btnY + btnH) {
                shareWaypoint(wp.id);
                return;
            }

            if (this.isAdmin) {
                int tpBtnX = rowX + 63;
                if (mx >= tpBtnX && mx < tpBtnX + 24 && my >= btnY && my < btnY + btnH) {
                    teleportToWaypoint(wp);
                    return;
                }
            }

            // Click on row body (not buttons) = center map on waypoint
            if (my < btnY) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    this.panOffsetX = wp.x - mc.player.getX();
                    this.panOffsetZ = wp.z - mc.player.getZ();
                }
            }
        }
    }

    private void toggleWaypointBeacon(Waypoint wp) {
        // Send updated waypoint with toggled beacon
        boolean newBeacon = !wp.beaconEnabled;
        String json = "{\"id\":\"" + escapeJson(wp.id) + "\""
                + ",\"name\":\"" + escapeJson(wp.name) + "\""
                + ",\"x\":" + wp.x + ",\"y\":" + wp.y + ",\"z\":" + wp.z
                + ",\"colorIndex\":" + wp.colorIndex
                + ",\"category\":\"" + escapeJson(wp.category) + "\""
                + ",\"dimension\":\"" + escapeJson(wp.dimension) + "\""
                + ",\"beaconEnabled\":" + newBeacon + "}";

        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("map_save_waypoint", json),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
        this.wpRefreshCountdown = 10;
    }

    private void addWaypointAtPlayer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (this.waypoints.size() >= 100) {
            this.confirmMessage = "Max 100 waypoints!";
            this.confirmTicks = 60;
            return;
        }

        int x = (int) mc.player.getX();
        int y = (int) mc.player.getY();
        int z = (int) mc.player.getZ();
        String name = "Waypoint " + (this.waypoints.size() + 1);
        int colorIdx = this.waypoints.size() % WP_COLORS.length;
        String dim = mc.level.dimension().identifier().toString();

        String json = "{\"name\":\"" + escapeJson(name) + "\""
                + ",\"x\":" + x + ",\"y\":" + y + ",\"z\":" + z
                + ",\"colorIndex\":" + colorIdx
                + ",\"category\":\"General\""
                + ",\"dimension\":\"" + escapeJson(dim) + "\""
                + ",\"beaconEnabled\":true}";

        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("map_save_waypoint", json),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
        this.wpRefreshCountdown = 10;
    }

    private void saveEditingWaypoint() {
        if (this.editingWaypoint < 0 || this.editingWaypoint >= this.waypoints.size()) return;
        Waypoint wp = this.waypoints.get(this.editingWaypoint);
        String name = this.wpNameInput.trim();
        if (name.isEmpty()) name = "Waypoint";

        String json = "{\"id\":\"" + escapeJson(wp.id) + "\""
                + ",\"name\":\"" + escapeJson(name) + "\""
                + ",\"x\":" + wp.x + ",\"y\":" + wp.y + ",\"z\":" + wp.z
                + ",\"colorIndex\":" + this.wpColorIndex
                + ",\"category\":\"" + escapeJson(wp.category) + "\""
                + ",\"dimension\":\"" + escapeJson(wp.dimension) + "\""
                + ",\"beaconEnabled\":" + wp.beaconEnabled + "}";

        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("map_save_waypoint", json),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
        this.wpRefreshCountdown = 10;

        this.editingWaypoint = -1;
        this.wpNameActive = false;
    }

    private void deleteWaypoint(String wpId) {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("map_delete_waypoint", wpId),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
        this.wpRefreshCountdown = 10;
    }

    private void shareWaypoint(String wpId) {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("map_share_waypoint", wpId),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
    }

    private void teleportToWaypoint(Waypoint wp) {
        ClientPacketDistributor.sendToServer(
            (CustomPacketPayload) new ComputerActionPayload("map_teleport", wp.x + ":" + wp.y + ":" + wp.z),
            (CustomPacketPayload[]) new CustomPacketPayload[0]
        );
        this.confirmMessage = "Teleporting...";
        this.confirmTicks = 40;
    }

    // ===== GoTo Popup =====

    private void renderGotoPopup(GuiGraphics g, int mouseX, int mouseY) {
        int popW = 140;
        int popH = 60;
        int popX = this.width / 2 - popW / 2;
        int popY = this.height / 2 - popH / 2;

        UIHelper.drawCard(g, popX, popY, popW, popH);
        UIHelper.drawCenteredLabel(g, this.font, "Go To Coordinates", popX + popW / 2, popY + 4);

        // X input
        int inputW = 55;
        int inputH = 12;
        int xInputX = popX + 8;
        int zInputX = popX + popW / 2 + 4;
        int inputY = popY + 18;

        g.drawString(this.font, "X:", xInputX, inputY + 2, 0xFFCCCCDD, false);
        int xFieldX = xInputX + 14;
        g.fill(xFieldX, inputY, xFieldX + inputW, inputY + inputH, 0xFF111122);
        int xBorderColor = this.gotoXActive ? 0xFF58A6FF : 0xFF333344;
        g.fill(xFieldX, inputY, xFieldX + inputW, inputY + 1, xBorderColor);
        g.fill(xFieldX, inputY + inputH - 1, xFieldX + inputW, inputY + inputH, 0xFF333344);
        String xDisplay = this.gotoXInput;
        if (this.gotoXActive && (this.gotoCursorTick / 10) % 2 == 0) xDisplay += "_";
        g.drawString(this.font, xDisplay, xFieldX + 3, inputY + 2, 0xFFFFFFFF, false);

        g.drawString(this.font, "Z:", zInputX, inputY + 2, 0xFFCCCCDD, false);
        int zFieldX = zInputX + 14;
        g.fill(zFieldX, inputY, zFieldX + inputW, inputY + inputH, 0xFF111122);
        int zBorderColor = this.gotoZActive ? 0xFF58A6FF : 0xFF333344;
        g.fill(zFieldX, inputY, zFieldX + inputW, inputY + 1, zBorderColor);
        g.fill(zFieldX, inputY + inputH - 1, zFieldX + inputW, inputY + inputH, 0xFF333344);
        String zDisplay = this.gotoZInput;
        if (this.gotoZActive && (this.gotoCursorTick / 10) % 2 == 0) zDisplay += "_";
        g.drawString(this.font, zDisplay, zFieldX + 3, inputY + 2, 0xFFFFFFFF, false);

        // Go button
        int goBtnW = 40;
        int goBtnH = 14;
        int goBtnX = popX + popW / 2 - goBtnW / 2;
        int goBtnY = popY + popH - goBtnH - 6;
        boolean goHover = mouseX >= goBtnX && mouseX < goBtnX + goBtnW
                && mouseY >= goBtnY && mouseY < goBtnY + goBtnH;
        UIHelper.drawButton(g, goBtnX, goBtnY, goBtnW, goBtnH, goHover);
        String goLabel = "Go";
        int goLabelW = this.font.width(goLabel);
        g.drawString(this.font, goLabel, goBtnX + (goBtnW - goLabelW) / 2, goBtnY + 3, 0xFFCCCCDD, false);
    }

    private boolean handleGotoPopupClick(int mx, int my) {
        int popW = 140;
        int popH = 60;
        int popX = this.width / 2 - popW / 2;
        int popY = this.height / 2 - popH / 2;

        // Check if click is inside popup
        if (mx < popX || mx >= popX + popW || my < popY || my >= popY + popH) {
            return false;
        }

        int inputW = 55;
        int inputH = 12;
        int xFieldX = popX + 8 + 14;
        int zFieldX = popX + popW / 2 + 4 + 14;
        int inputY = popY + 18;

        // X field click
        if (mx >= xFieldX && mx < xFieldX + inputW && my >= inputY && my < inputY + inputH) {
            this.gotoXActive = true;
            this.gotoZActive = false;
            this.gotoCursorTick = 0;
            return true;
        }

        // Z field click
        if (mx >= zFieldX && mx < zFieldX + inputW && my >= inputY && my < inputY + inputH) {
            this.gotoXActive = false;
            this.gotoZActive = true;
            this.gotoCursorTick = 0;
            return true;
        }

        // Go button
        int goBtnW = 40;
        int goBtnH = 14;
        int goBtnX = popX + popW / 2 - goBtnW / 2;
        int goBtnY = popY + popH - goBtnH - 6;
        if (mx >= goBtnX && mx < goBtnX + goBtnW && my >= goBtnY && my < goBtnY + goBtnH) {
            executeGoto();
            return true;
        }

        return true; // consume click inside popup
    }

    private void executeGoto() {
        try {
            int gotoX = Integer.parseInt(this.gotoXInput.trim());
            int gotoZ = Integer.parseInt(this.gotoZInput.trim());
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                this.panOffsetX = gotoX - mc.player.getX();
                this.panOffsetZ = gotoZ - mc.player.getZ();
            }
            this.gotoPopupOpen = false;
            this.gotoXActive = false;
            this.gotoZActive = false;
        } catch (NumberFormatException e) {
            this.confirmMessage = "Invalid coordinates!";
            this.confirmTicks = 40;
        }
    }

    // ===== Text Placement Popup =====

    private void renderTextPlacementPopup(GuiGraphics g, int mouseX, int mouseY) {
        int popW = 160;
        int popH = 44;
        int popX = this.width / 2 - popW / 2;
        int popY = this.height / 2 - popH / 2;

        UIHelper.drawCard(g, popX, popY, popW, popH);
        g.drawString(this.font, "Label text:", popX + 6, popY + 4, 0xFFCCCCDD, false);

        // Text input field
        int fieldX = popX + 6;
        int fieldY = popY + 14;
        int fieldW = popW - 12;
        int fieldH = 12;
        g.fill(fieldX, fieldY, fieldX + fieldW, fieldY + fieldH, 0xFF111122);
        g.fill(fieldX, fieldY, fieldX + fieldW, fieldY + 1, 0xFF58A6FF);
        String display = this.placeTextInput;
        if (this.placeTextActive && (this.placeTextCursorTick / 10) % 2 == 0) display += "_";
        g.drawString(this.font, display, fieldX + 3, fieldY + 2, 0xFFFFFFFF, false);

        // OK button
        int okBtnW = 30;
        int okBtnH = 12;
        int okBtnX = popX + popW / 2 - okBtnW / 2;
        int okBtnY = popY + popH - okBtnH - 4;
        boolean okHover = mouseX >= okBtnX && mouseX < okBtnX + okBtnW
                && mouseY >= okBtnY && mouseY < okBtnY + okBtnH;
        UIHelper.drawButton(g, okBtnX, okBtnY, okBtnW, okBtnH, okHover);
        g.drawString(this.font, "OK", okBtnX + 9, okBtnY + 2, 0xFFCCCCDD, false);
    }

    private boolean handleTextPlacementClick(int mx, int my) {
        int popW = 160;
        int popH = 44;
        int popX = this.width / 2 - popW / 2;
        int popY = this.height / 2 - popH / 2;

        if (mx < popX || mx >= popX + popW || my < popY || my >= popY + popH) {
            return false;
        }

        // Text field click
        int fieldX = popX + 6;
        int fieldY = popY + 14;
        int fieldW = popW - 12;
        int fieldH = 12;
        if (mx >= fieldX && mx < fieldX + fieldW && my >= fieldY && my < fieldY + fieldH) {
            this.placeTextActive = true;
            this.placeTextCursorTick = 0;
            return true;
        }

        // OK button
        int okBtnW = 30;
        int okBtnH = 12;
        int okBtnX = popX + popW / 2 - okBtnW / 2;
        int okBtnY = popY + popH - okBtnH - 4;
        if (mx >= okBtnX && mx < okBtnX + okBtnW && my >= okBtnY && my < okBtnY + okBtnH) {
            submitPlacedText();
            return true;
        }

        return true;
    }

    private void submitPlacedText() {
        String text = this.placeTextInput.trim();
        if (text.isEmpty()) text = "Label";

        Minecraft mc = Minecraft.getInstance();
        String dim = "minecraft:overworld";
        if (mc.level != null) {
            dim = mc.level.dimension().identifier().toString();
        }
        int color = DRAWING_COLORS[this.drawingColorIndex % DRAWING_COLORS.length];

        String json = "{\"text\":\"" + escapeJson(text) + "\""
                + ",\"x\":" + this.placeTextWorldX
                + ",\"z\":" + this.placeTextWorldZ
                + ",\"color\":" + color
                + ",\"dimension\":\"" + escapeJson(dim) + "\"}";
        sendToServer("map_save_drawing_text", json);

        this.placingText = false;
        this.placeTextActive = false;
    }

    // ===== Tool Click Handlers =====

    private boolean handleDrawLineClick(int mx, int my) {
        if (this.drawLineStart == null) {
            // First click: set start point
            this.drawLineStart = new int[]{this.mouseWorldX, this.mouseWorldZ};
        } else {
            // Second click: save line to server
            Minecraft mc = Minecraft.getInstance();
            String dim = "minecraft:overworld";
            if (mc.level != null) {
                dim = mc.level.dimension().identifier().toString();
            }
            int color = DRAWING_COLORS[this.drawingColorIndex % DRAWING_COLORS.length];

            String json = "{\"x1\":" + this.drawLineStart[0]
                    + ",\"z1\":" + this.drawLineStart[1]
                    + ",\"x2\":" + this.mouseWorldX
                    + ",\"z2\":" + this.mouseWorldZ
                    + ",\"color\":" + color
                    + ",\"dimension\":\"" + escapeJson(dim) + "\"}";
            sendToServer("map_save_drawing_line", json);

            // Add locally for immediate feedback
            String localId = "line_local_" + System.currentTimeMillis();
            MapDrawingOverlay.addLineLocal(new MapDrawingOverlay.DrawingLine(
                localId, this.drawLineStart[0], this.drawLineStart[1],
                this.mouseWorldX, this.mouseWorldZ, color, false));

            this.drawLineStart = null;
        }
        return true;
    }

    private boolean handlePlaceTextClick(int mx, int my) {
        // Open text input popup at clicked world position
        this.placeTextWorldX = this.mouseWorldX;
        this.placeTextWorldZ = this.mouseWorldZ;
        this.placeTextInput = "";
        this.placingText = true;
        this.placeTextActive = true;
        this.placeTextCursorTick = 0;
        return true;
    }

    private boolean handlePlaceWaypointClick(int mx, int my) {
        // Create waypoint at the clicked map position
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return true;
        if (this.waypoints.size() >= 100) {
            this.confirmMessage = "Max 100 waypoints!";
            this.confirmTicks = 60;
            return true;
        }

        int x = this.mouseWorldX;
        int z = this.mouseWorldZ;
        int y = mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        String name = "WP " + (this.waypoints.size() + 1);
        int colorIdx = this.waypoints.size() % WP_COLORS.length;
        String dim = mc.level.dimension().identifier().toString();

        String json = "{\"name\":\"" + escapeJson(name) + "\""
                + ",\"x\":" + x + ",\"y\":" + y + ",\"z\":" + z
                + ",\"colorIndex\":" + colorIdx
                + ",\"category\":\"General\""
                + ",\"dimension\":\"" + escapeJson(dim) + "\""
                + ",\"beaconEnabled\":true}";
        sendToServer("map_save_waypoint", json);
        this.wpRefreshCountdown = 10;
        return true;
    }

    private boolean handleMeasureClick(int mx, int my) {
        if (this.measureStart == null) {
            // First click: set start
            this.measureStart = new int[]{this.mouseWorldX, this.mouseWorldZ};
            this.measureDistLabel = null;
        } else {
            // Second click: calculate + display distance
            double dx = this.mouseWorldX - this.measureStart[0];
            double dz = this.mouseWorldZ - this.measureStart[1];
            int dist = (int) Math.sqrt(dx * dx + dz * dz);
            this.measureDistLabel = "Distance: " + dist + " blocks (" +
                    this.measureStart[0] + "," + this.measureStart[1] + " -> " +
                    this.mouseWorldX + "," + this.mouseWorldZ + ")";
            this.measureStart = null;
        }
        return true;
    }

    // ===== Drawing Data Parsing =====

    private void parseDrawings(String json) {
        if (json == null || json.length() < 3) return;
        try {
            List<MapDrawingOverlay.DrawingLine> lines = new ArrayList<>();
            List<MapDrawingOverlay.DrawingText> texts = new ArrayList<>();

            // Parse lines array
            int linesStart = json.indexOf("\"lines\":[");
            if (linesStart >= 0) {
                int arrStart = json.indexOf('[', linesStart);
                int arrEnd = findMatchingBracket(json, arrStart);
                if (arrEnd > arrStart) {
                    String arrStr = json.substring(arrStart + 1, arrEnd);
                    String[] entries = splitJsonArray(arrStr);
                    for (String entry : entries) {
                        String e = entry.trim();
                        if (e.isEmpty()) continue;
                        String id = extractJsonString(e, "id");
                        int x1 = extractJsonInt(e, "x1");
                        int z1 = extractJsonInt(e, "z1");
                        int x2 = extractJsonInt(e, "x2");
                        int z2 = extractJsonInt(e, "z2");
                        int color = extractJsonInt(e, "color");
                        boolean shared = extractJsonBoolean(e, "shared");
                        if (id != null) {
                            lines.add(new MapDrawingOverlay.DrawingLine(id, x1, z1, x2, z2, color, shared));
                        }
                    }
                }
            }

            // Parse texts array
            int textsStart = json.indexOf("\"texts\":[");
            if (textsStart >= 0) {
                int arrStart = json.indexOf('[', textsStart);
                int arrEnd = findMatchingBracket(json, arrStart);
                if (arrEnd > arrStart) {
                    String arrStr = json.substring(arrStart + 1, arrEnd);
                    String[] entries = splitJsonArray(arrStr);
                    for (String entry : entries) {
                        String e = entry.trim();
                        if (e.isEmpty()) continue;
                        String id = extractJsonString(e, "id");
                        String text = extractJsonString(e, "text");
                        int x = extractJsonInt(e, "x");
                        int z = extractJsonInt(e, "z");
                        int color = extractJsonInt(e, "color");
                        boolean shared = extractJsonBoolean(e, "shared");
                        if (id != null && text != null) {
                            texts.add(new MapDrawingOverlay.DrawingText(id, text, x, z, color, shared));
                        }
                    }
                }
            }

            MapDrawingOverlay.updateFromServer(lines, texts);
        } catch (Exception ignored) {}
    }

    private int findMatchingBracket(String json, int openPos) {
        if (openPos < 0 || openPos >= json.length()) return -1;
        int depth = 0;
        for (int i = openPos; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private void parseWaypoints(String json) {
        this.waypoints.clear();
        if (json == null || json.length() < 3) return;

        // Parse JSON array of waypoints
        // Format: {"waypoints":[{"id":"...","name":"...","x":0,"y":0,"z":0,"colorIndex":0,"created":0,
        //          "category":"Base","dimension":"minecraft:overworld","beaconEnabled":true}, ...]}
        try {
            // Remove outer braces for the waypoints wrapper
            int arrStart = json.indexOf('[');
            int arrEnd = json.lastIndexOf(']');
            if (arrStart < 0 || arrEnd < 0) return;

            String arrStr = json.substring(arrStart + 1, arrEnd);
            if (arrStr.trim().isEmpty()) return;

            List<MapWaypointSyncManager.BeaconWaypoint> beaconWaypoints = new ArrayList<>();

            // Split on },{
            String[] entries = splitJsonArray(arrStr);
            for (String entry : entries) {
                String e = entry.trim();
                if (e.isEmpty()) continue;

                String id = extractJsonString(e, "id");
                String name = extractJsonString(e, "name");
                int x = extractJsonInt(e, "x");
                int y = extractJsonInt(e, "y");
                int z = extractJsonInt(e, "z");
                int colorIndex = extractJsonInt(e, "colorIndex");
                long created = extractJsonLong(e, "created");
                String category = extractJsonString(e, "category");
                String dimension = extractJsonString(e, "dimension");
                boolean beaconEnabled = extractJsonBoolean(e, "beaconEnabled");

                if (category == null || category.isEmpty()) category = "General";
                if (dimension == null || dimension.isEmpty()) dimension = "minecraft:overworld";

                if (id != null && name != null) {
                    this.waypoints.add(new Waypoint(id, name, x, y, z, colorIndex, created,
                            category, dimension, beaconEnabled));
                    beaconWaypoints.add(new MapWaypointSyncManager.BeaconWaypoint(
                            x, y, z, colorIndex, beaconEnabled, dimension));
                }
            }

            // Feed the sync manager for 3D beacon rendering
            MapWaypointSyncManager.updateWaypoints(beaconWaypoints);
        } catch (Exception ignored) {}
    }

    private String[] splitJsonArray(String arrStr) {
        List<String> results = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < arrStr.length(); i++) {
            char c = arrStr.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') depth--;
            else if (c == ',' && depth == 0) {
                results.add(arrStr.substring(start, i));
                start = i + 1;
            }
        }
        if (start < arrStr.length()) {
            results.add(arrStr.substring(start));
        }
        return results.toArray(new String[0]);
    }

    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int valStart = idx + search.length();
        int valEnd = json.indexOf('"', valStart);
        if (valEnd < 0) return null;
        return json.substring(valStart, valEnd).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private int extractJsonInt(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return 0;
        int valStart = idx + search.length();
        int valEnd = valStart;
        while (valEnd < json.length() && (Character.isDigit(json.charAt(valEnd)) || json.charAt(valEnd) == '-')) {
            valEnd++;
        }
        try {
            return Integer.parseInt(json.substring(valStart, valEnd));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean extractJsonBoolean(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return false;
        int valStart = idx + search.length();
        String rest = json.substring(valStart).trim();
        return rest.startsWith("true");
    }

    private long extractJsonLong(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return 0L;
        int valStart = idx + search.length();
        int valEnd = valStart;
        while (valEnd < json.length() && (Character.isDigit(json.charAt(valEnd)) || json.charAt(valEnd) == '-')) {
            valEnd++;
        }
        try {
            return Long.parseLong(json.substring(valStart, valEnd));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private void parseHighlights(String json) {
        if (json == null || json.length() < 3) return;
        try {
            // Extract spawn position from top-level JSON
            int serverSpawnX = extractJsonInt(json, "spawnX");
            int serverSpawnZ = extractJsonInt(json, "spawnZ");

            List<MapHighlightOverlay.ChunkHighlight> highlights = new ArrayList<>();
            int arrStart = json.indexOf('[');
            int arrEnd = json.lastIndexOf(']');
            if (arrStart < 0 || arrEnd < 0) return;
            String arrStr = json.substring(arrStart + 1, arrEnd);
            if (arrStr.trim().isEmpty()) {
                MapHighlightOverlay.updateFromServer(highlights, serverSpawnX, serverSpawnZ);
                return;
            }
            String[] entries = splitJsonArray(arrStr);
            for (String e : entries) {
                String t = e.trim();
                if (t.isEmpty()) continue;
                int cx = extractJsonInt(t, "cx");
                int cz = extractJsonInt(t, "cz");
                String type = extractJsonString(t, "type");
                if (type != null) {
                    highlights.add(new MapHighlightOverlay.ChunkHighlight(cx, cz, type));
                }
            }
            MapHighlightOverlay.updateFromServer(highlights, serverSpawnX, serverSpawnZ);
        } catch (Exception ignored) {}
    }

    private void parseStructures(String json) {
        if (json == null || json.length() < 3) return;
        try {
            List<MapStructureOverlay.StructureMarker> structures = new ArrayList<>();
            int arrStart = json.indexOf('[');
            int arrEnd = json.lastIndexOf(']');
            if (arrStart < 0 || arrEnd < 0) return;
            String arrStr = json.substring(arrStart + 1, arrEnd);
            if (arrStr.trim().isEmpty()) {
                MapStructureOverlay.updateFromServer(structures);
                return;
            }
            String[] entries = splitJsonArray(arrStr);
            for (String e : entries) {
                String t = e.trim();
                if (t.isEmpty()) continue;
                String type = extractJsonString(t, "type");
                String name = extractJsonString(t, "name");
                int x = extractJsonInt(t, "x");
                int z = extractJsonInt(t, "z");
                if (type != null && name != null) {
                    structures.add(new MapStructureOverlay.StructureMarker(type, name, x, z));
                }
            }
            MapStructureOverlay.updateFromServer(structures);
        } catch (Exception ignored) {}
    }

    @Override
    public void removed() {
        super.removed();
        // Save tile cache to disk (but keep tiles in memory for background rendering)
        MapChunkTileManager.getInstance().saveAll();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

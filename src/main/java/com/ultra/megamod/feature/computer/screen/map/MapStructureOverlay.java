package com.ultra.megamod.feature.computer.screen.map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import java.util.*;

public class MapStructureOverlay {

    public record StructureMarker(String type, String displayName, int x, int z) {}

    private static final List<StructureMarker> structures = new ArrayList<>();

    // Structure type -> color
    private static final Map<String, Integer> STRUCTURE_COLORS = new HashMap<>();
    static {
        STRUCTURE_COLORS.put("Village", 0xFF8B4513);       // brown
        STRUCTURE_COLORS.put("Stronghold", 0xFF00CED1);    // dark cyan
        STRUCTURE_COLORS.put("Monument", 0xFF000080);      // navy
        STRUCTURE_COLORS.put("Nether Fortress", 0xFFFF4500); // orange-red
        STRUCTURE_COLORS.put("Desert Temple", 0xFFFFD700);   // gold
        STRUCTURE_COLORS.put("Jungle Temple", 0xFF228B22);   // forest green
        STRUCTURE_COLORS.put("Shipwreck", 0xFF8B6914);     // dark goldenrod
        STRUCTURE_COLORS.put("Pillager Outpost", 0xFF696969); // dim gray
        STRUCTURE_COLORS.put("Woodland Mansion", 0xFF8B0000); // dark red
        STRUCTURE_COLORS.put("Ancient City", 0xFF2F4F4F);    // dark slate gray
    }

    public static void updateFromServer(List<StructureMarker> newStructures) {
        structures.clear();
        structures.addAll(newStructures);
    }

    public static void render(GuiGraphics g, Font font, int mapLeft, int mapTop, int mapWidth, int mapHeight,
                              double centerWorldX, double centerWorldZ, double zoomScale, int mouseX, int mouseY) {
        for (StructureMarker s : structures) {
            int sx = (int) (mapLeft + mapWidth / 2.0 + (s.x() - centerWorldX) / zoomScale);
            int sy = (int) (mapTop + mapHeight / 2.0 + (s.z() - centerWorldZ) / zoomScale);

            if (sx < mapLeft - 5 || sx > mapLeft + mapWidth + 5) continue;
            if (sy < mapTop - 5 || sy > mapTop + mapHeight + 5) continue;

            int color = STRUCTURE_COLORS.getOrDefault(s.displayName(), 0xFFAAAAAA);

            // Draw a small icon (6x6 square with inner pattern)
            g.fill(sx - 3, sy - 3, sx + 3, sy + 3, color);
            g.fill(sx - 2, sy - 2, sx + 2, sy + 2, brighten(color));
            g.fill(sx - 1, sy - 1, sx + 1, sy + 1, color);

            // Hover tooltip
            if (mouseX >= sx - 4 && mouseX <= sx + 4 && mouseY >= sy - 4 && mouseY <= sy + 4) {
                // Draw tooltip background
                String label = s.displayName() + " (" + s.x() + ", " + s.z() + ")";
                int tw = font.width(label) + 6;
                g.fill(mouseX + 6, mouseY - 2, mouseX + 8 + tw, mouseY + 12, 0xEE000000);
                g.drawString(font, label, mouseX + 9, mouseY, 0xFFFFFFFF, false);
            }
        }
    }

    private static int brighten(int color) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, ((color >> 16) & 0xFF) + 40);
        int gVal = Math.min(255, ((color >> 8) & 0xFF) + 40);
        int b = Math.min(255, (color & 0xFF) + 40);
        return (a << 24) | (r << 16) | (gVal << 8) | b;
    }
}

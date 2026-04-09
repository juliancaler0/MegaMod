package com.ultra.megamod.feature.computer.screen;

import com.ultra.megamod.feature.computer.network.ComputerActionPayload;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.ui.UIHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.ultra.megamod.feature.dungeons.DungeonSoundRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class MusicPlayerScreen extends Screen {
    private final Screen parent;
    private int scroll = 0;
    private boolean playing = false;
    private boolean paused = false;
    private boolean shuffle = false;
    private boolean repeat = false;
    private int currentDisc = -1;
    private int playTicks = 0;
    private Set<String> unlockedDiscs = new HashSet<>();
    private SimpleSoundInstance currentSound = null;
    private List<DiscEntry> allDiscs = new ArrayList<>();
    private List<Integer> playlist = new ArrayList<>();
    private int playlistIndex = 0;
    private float[] eqBars = new float[12];
    private float[] eqTargets = new float[12];
    private boolean dataLoaded = false;
    private int titleBarH;
    private int maxScroll = 0;
    private Random eqRandom = new Random();

    // Layout constants
    private static final int MARGIN = 8;
    private static final int NOW_PLAYING_H = 68;
    private static final int DISC_ROW_H = 18;
    private static final int DISC_GAP = 2;
    private static final int BUTTON_SIZE = 16;
    private static final int EQ_BAR_COUNT = 12;
    private static final int EQ_BAR_W = 3;
    private static final int EQ_BAR_GAP = 2;
    private static final int EQ_MAX_H = 20;

    // Button positions (calculated in init)
    private int backX, backY, backW, backH;
    private int prevX, prevY;
    private int playPauseX, playPauseY;
    private int stopX, stopY;
    private int nextX, nextY;
    private int shuffleX, shuffleY;
    private int repeatX, repeatY;
    private int libraryTop, libraryBottom, libraryLeft, libraryRight;

    public record DiscEntry(String itemId, String name, String author, int color, SoundEvent soundEvent, int durationTicks, boolean adminOnly) {}
    // Convenience constructor for normal discs
    private static DiscEntry disc(String itemId, String name, String author, int color, SoundEvent soundEvent, int durationTicks) {
        return new DiscEntry(itemId, name, author, color, soundEvent, durationTicks, false);
    }

    public MusicPlayerScreen(Screen parent) {
        super((Component) Component.literal((String) "Music Player"));
        this.parent = parent;
        initDiscList();
    }

    // Custom sound event for House Money — uses registered SoundEvent from DungeonSoundRegistry
    private static final SoundEvent HOUSE_MONEY_SOUND = DungeonSoundRegistry.MUSIC_DISC_HOUSE_MONEY.get();

    private void initDiscList() {
        allDiscs.add(disc("minecraft:music_disc_13", "13", "C418", 0xFFB347, SoundEvents.MUSIC_DISC_13.value(), 3560));
        allDiscs.add(disc("minecraft:music_disc_cat", "Cat", "C418", 0x7FFF00, SoundEvents.MUSIC_DISC_CAT.value(), 3700));
        allDiscs.add(disc("minecraft:music_disc_blocks", "Blocks", "C418", 0xFF8C00, SoundEvents.MUSIC_DISC_BLOCKS.value(), 6900));
        allDiscs.add(disc("minecraft:music_disc_chirp", "Chirp", "C418", 0xFF4444, SoundEvents.MUSIC_DISC_CHIRP.value(), 3700));
        allDiscs.add(disc("minecraft:music_disc_far", "Far", "C418", 0x44FF88, SoundEvents.MUSIC_DISC_FAR.value(), 3480));
        allDiscs.add(disc("minecraft:music_disc_mall", "Mall", "C418", 0xBB77FF, SoundEvents.MUSIC_DISC_MALL.value(), 3940));
        allDiscs.add(disc("minecraft:music_disc_mellohi", "Mellohi", "C418", 0xFF77DD, SoundEvents.MUSIC_DISC_MELLOHI.value(), 1920));
        allDiscs.add(disc("minecraft:music_disc_stal", "Stal", "C418", 0x555555, SoundEvents.MUSIC_DISC_STAL.value(), 3000));
        allDiscs.add(disc("minecraft:music_disc_strad", "Strad", "C418", 0xFFFFFF, SoundEvents.MUSIC_DISC_STRAD.value(), 3760));
        allDiscs.add(disc("minecraft:music_disc_ward", "Ward", "C418", 0x44BBFF, SoundEvents.MUSIC_DISC_WARD.value(), 5020));
        allDiscs.add(disc("minecraft:music_disc_11", "11", "C418", 0x222222, SoundEvents.MUSIC_DISC_11.value(), 1420));
        allDiscs.add(disc("minecraft:music_disc_wait", "Wait", "C418", 0x44DDDD, SoundEvents.MUSIC_DISC_WAIT.value(), 4700));
        allDiscs.add(disc("minecraft:music_disc_otherside", "Otherside", "Lena Raine", 0x3399FF, SoundEvents.MUSIC_DISC_OTHERSIDE.value(), 3900));
        allDiscs.add(disc("minecraft:music_disc_pigstep", "Pigstep", "Lena Raine", 0xFF5500, SoundEvents.MUSIC_DISC_PIGSTEP.value(), 2960));
        allDiscs.add(disc("minecraft:music_disc_5", "5", "Samuel Aberg", 0x66CCCC, SoundEvents.MUSIC_DISC_5.value(), 3560));
        allDiscs.add(disc("minecraft:music_disc_relic", "Relic", "Aaron Cherof", 0xCCAA55, SoundEvents.MUSIC_DISC_RELIC.value(), 3620));
        // Custom MegaMod disc — admin only (NeverNotch, Dev)
        allDiscs.add(new DiscEntry("megamod:house_money", "House Money", "Baby Keem", 0xFFD700, HOUSE_MONEY_SOUND, 3915, true));
    }

    protected void init() {
        super.init();
        Objects.requireNonNull(this.font);
        this.titleBarH = 9 + 16;

        // Back button
        this.backW = 50;
        this.backH = 16;
        this.backX = 8;
        this.backY = (this.titleBarH - this.backH) / 2;

        // Control buttons centered in Now Playing area
        int controlsY = this.titleBarH + MARGIN + 46;
        int controlsCenterX = this.width / 2;
        int totalControlsW = BUTTON_SIZE * 5 + 4 * 4; // 5 buttons + 4 gaps of 4px
        int controlsStartX = controlsCenterX - totalControlsW / 2;

        this.prevX = controlsStartX;
        this.prevY = controlsY;
        this.playPauseX = controlsStartX + BUTTON_SIZE + 4;
        this.playPauseY = controlsY;
        this.stopX = controlsStartX + (BUTTON_SIZE + 4) * 2;
        this.stopY = controlsY;
        this.nextX = controlsStartX + (BUTTON_SIZE + 4) * 3;
        this.nextY = controlsY;
        this.shuffleX = controlsStartX + (BUTTON_SIZE + 4) * 4;
        this.shuffleY = controlsY;
        this.repeatX = controlsStartX + (BUTTON_SIZE + 4) * 4 + BUTTON_SIZE + 8;
        this.repeatY = controlsY;

        // Library area
        this.libraryTop = this.titleBarH + MARGIN + NOW_PLAYING_H + 6;
        this.libraryBottom = this.height - MARGIN - 4;
        this.libraryLeft = MARGIN + 4;
        this.libraryRight = this.width - MARGIN - 4 - 8; // 8 for scrollbar

        // Request unlocked discs from server
        ClientPacketDistributor.sendToServer((CustomPacketPayload) new ComputerActionPayload("music_request_discs", ""), (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }

    public void tick() {
        super.tick();

        // Check for server response
        ComputerDataPayload response = ComputerDataPayload.lastResponse;
        if (response != null && "music_disc_data".equals(response.dataType())) {
            ComputerDataPayload.lastResponse = null;
            parseDiscData(response.jsonData());
            this.dataLoaded = true;
        }

        // Consume error responses so the screen doesn't stay stuck
        if (response != null && "error".equals(response.dataType())) {
            ComputerDataPayload.lastResponse = null;
            this.dataLoaded = true;
        }

        // Advance play timer
        if (playing && !paused && currentDisc >= 0 && currentDisc < allDiscs.size()) {
            playTicks++;
            DiscEntry disc = allDiscs.get(currentDisc);
            if (playTicks >= disc.durationTicks()) {
                advanceToNext();
            }
        }

        // Animate equalizer
        for (int i = 0; i < EQ_BAR_COUNT; i++) {
            if (playing && !paused) {
                // Periodically set new random targets
                if (eqRandom.nextInt(4) == 0) {
                    eqTargets[i] = 0.2f + eqRandom.nextFloat() * 0.8f;
                }
                // Lerp toward target
                eqBars[i] += (eqTargets[i] - eqBars[i]) * 0.3f;
            } else {
                // Fade out when not playing
                eqBars[i] *= 0.85f;
                eqTargets[i] = 0;
            }
        }

        // Recalculate max scroll
        int visibleRows = (libraryBottom - libraryTop) / (DISC_ROW_H + DISC_GAP);
        maxScroll = Math.max(0, allDiscs.size() - visibleRows);
    }

    private void parseDiscData(String json) {
        unlockedDiscs.clear();
        if (json == null || json.length() < 3) return;
        // Parse {"unlockedDiscs":["minecraft:music_disc_cat",...]}
        int arrStart = json.indexOf('[');
        int arrEnd = json.lastIndexOf(']');
        if (arrStart < 0 || arrEnd < 0 || arrEnd <= arrStart) return;
        String arrContent = json.substring(arrStart + 1, arrEnd).trim();
        if (arrContent.isEmpty()) return;
        String[] entries = arrContent.split(",");
        for (String entry : entries) {
            String trimmed = entry.trim();
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                unlockedDiscs.add(trimmed.substring(1, trimmed.length() - 1));
            }
        }
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Background
        g.fill(0, 0, this.width, this.height, 0xFF0A0A14);
        UIHelper.drawPanel(g, 0, 0, this.width, this.height);

        // Title bar
        UIHelper.drawTitleBar(g, 0, 0, this.width, this.titleBarH);
        Objects.requireNonNull(this.font);
        int titleY = (this.titleBarH - 9) / 2;
        UIHelper.drawCenteredTitle(g, this.font, "Music Player", this.width / 2, titleY);

        // Back button
        boolean backHover = mouseX >= backX && mouseX < backX + backW && mouseY >= backY && mouseY < backY + backH;
        UIHelper.drawButton(g, backX, backY, backW, backH, backHover);
        int backTextX = backX + (backW - this.font.width("< Back")) / 2;
        g.drawString(this.font, "< Back", backTextX, backY + (backH - 9) / 2, UIHelper.CREAM_TEXT, false);

        // Disc count in title bar
        int unlockedCount = unlockedDiscs.size();
        int totalCount = allDiscs.size();
        String countStr = unlockedCount + "/" + totalCount + " Unlocked";
        int countW = this.font.width(countStr);
        g.drawString(this.font, countStr, this.width - countW - 10, titleY, UIHelper.GOLD_MID, false);

        // Now Playing area
        int npX = MARGIN;
        int npY = this.titleBarH + MARGIN;
        int npW = this.width - MARGIN * 2;
        int npH = NOW_PLAYING_H;
        renderNowPlaying(g, npX, npY, npW, npH, mouseX, mouseY);

        // Divider
        UIHelper.drawHorizontalDivider(g, MARGIN + 4, libraryTop - 3, this.width - MARGIN * 2 - 8);

        // Library header
        g.drawString(this.font, "Disc Library", libraryLeft, libraryTop + 1, UIHelper.GOLD_BRIGHT, false);
        int headerH = 12;

        // Library list
        int listTop = libraryTop + headerH + 2;
        int listH = libraryBottom - listTop;

        // Clip and render disc rows
        g.enableScissor(libraryLeft, listTop, libraryRight, libraryBottom);
        renderDiscLibrary(g, listTop, listH, mouseX, mouseY);
        g.disableScissor();

        // Scrollbar
        if (maxScroll > 0) {
            float scrollProgress = (float) scroll / (float) maxScroll;
            UIHelper.drawScrollbar(g, this.width - MARGIN - 4 - 6, listTop, listH, scrollProgress);
        }

        // Playlist indicator
        if (!playlist.isEmpty()) {
            String plStr = "Queue: " + playlist.size() + " disc" + (playlist.size() != 1 ? "s" : "");
            int plW = this.font.width(plStr);
            g.drawString(this.font, plStr, libraryRight - plW, libraryTop + 1, UIHelper.GOLD_DARK, false);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderNowPlaying(GuiGraphics g, int x, int y, int w, int h, int mouseX, int mouseY) {
        // Dark background card for now playing
        int accentColor = (currentDisc >= 0 && currentDisc < allDiscs.size()) ? allDiscs.get(currentDisc).color() : 0x555555;
        int bgColor = darkenColor(accentColor, 200);
        UIHelper.drawCard(g, x, y, w, h, false);

        // Accent strip at top of card
        g.fill(x + 2, y + 1, x + w - 2, y + 3, accentColor);

        if (currentDisc >= 0 && currentDisc < allDiscs.size()) {
            DiscEntry disc = allDiscs.get(currentDisc);

            // Disc name and author
            String nowLabel = playing ? (paused ? "PAUSED" : "NOW PLAYING") : "STOPPED";
            g.drawString(this.font, nowLabel, x + 8, y + 6, playing ? (paused ? 0xFFAAAA44 : accentColor) : 0xFF888888, false);

            String discTitle = disc.name() + " - " + disc.author();
            g.drawString(this.font, discTitle, x + 8, y + 18, UIHelper.CREAM_TEXT, false);

            // Progress bar
            int progX = x + 8;
            int progY = y + 32;
            int progW = w - 16 - (EQ_BAR_COUNT * (EQ_BAR_W + EQ_BAR_GAP)) - 12;
            int progH = 8;
            float progress = (float) playTicks / (float) Math.max(1, disc.durationTicks());
            UIHelper.drawProgressBar(g, progX, progY, progW, progH, progress, accentColor);

            // Time display
            int elapsedSec = playTicks / 20;
            int totalSec = disc.durationTicks() / 20;
            String timeStr = formatTime(elapsedSec) + " / " + formatTime(totalSec);
            int timeW = this.font.width(timeStr);
            g.drawString(this.font, timeStr, progX + (progW - timeW) / 2, progY + progH + 2, UIHelper.GOLD_DARK, false);

            // Equalizer bars (right side of progress bar area)
            int eqX = x + w - 8 - (EQ_BAR_COUNT * (EQ_BAR_W + EQ_BAR_GAP));
            int eqY = y + 8;
            renderEqualizer(g, eqX, eqY, accentColor);
        } else {
            // No disc selected
            String noDisc = "No disc selected";
            int ndW = this.font.width(noDisc);
            g.drawString(this.font, noDisc, x + (w - ndW) / 2, y + 12, 0xFF666666, false);

            String hint = "Select a disc from the library below";
            int hW = this.font.width(hint);
            g.drawString(this.font, hint, x + (w - hW) / 2, y + 24, 0xFF444444, false);
        }

        // Control buttons
        boolean prevHover = isHovered(mouseX, mouseY, prevX, prevY, BUTTON_SIZE, BUTTON_SIZE);
        boolean playHover = isHovered(mouseX, mouseY, playPauseX, playPauseY, BUTTON_SIZE, BUTTON_SIZE);
        boolean stopHover = isHovered(mouseX, mouseY, stopX, stopY, BUTTON_SIZE, BUTTON_SIZE);
        boolean nextHover = isHovered(mouseX, mouseY, nextX, nextY, BUTTON_SIZE, BUTTON_SIZE);
        boolean shuffHover = isHovered(mouseX, mouseY, shuffleX, shuffleY, BUTTON_SIZE, BUTTON_SIZE);
        boolean repHover = isHovered(mouseX, mouseY, repeatX, repeatY, BUTTON_SIZE, BUTTON_SIZE);

        UIHelper.drawIconButton(g, this.font, prevX, prevY, BUTTON_SIZE, "|<", UIHelper.CREAM_TEXT, prevHover);
        String playIcon = (playing && !paused) ? "||" : ">";
        UIHelper.drawIconButton(g, this.font, playPauseX, playPauseY, BUTTON_SIZE, playIcon, UIHelper.CREAM_TEXT, playHover);
        UIHelper.drawIconButton(g, this.font, stopX, stopY, BUTTON_SIZE, "#", UIHelper.CREAM_TEXT, stopHover);
        UIHelper.drawIconButton(g, this.font, nextX, nextY, BUTTON_SIZE, ">|", UIHelper.CREAM_TEXT, nextHover);
        UIHelper.drawIconButton(g, this.font, shuffleX, shuffleY, BUTTON_SIZE, "S", shuffle ? 0xFF44FF44 : UIHelper.CREAM_TEXT, shuffHover);
        UIHelper.drawIconButton(g, this.font, repeatX, repeatY, BUTTON_SIZE, "R", repeat ? 0xFF44FF44 : UIHelper.CREAM_TEXT, repHover);
    }

    private void renderEqualizer(GuiGraphics g, int x, int y, int accentColor) {
        for (int i = 0; i < EQ_BAR_COUNT; i++) {
            int barX = x + i * (EQ_BAR_W + EQ_BAR_GAP);
            int barH = Math.max(1, (int) (eqBars[i] * EQ_MAX_H));
            int barY = y + EQ_MAX_H - barH;

            // Gradient effect: brighter at top
            int barColor = accentColor;
            g.fill(barX, barY, barX + EQ_BAR_W, y + EQ_MAX_H, barColor);
            // Highlight top pixel
            if (barH > 2) {
                int bright = brightenColor(barColor, 80);
                g.fill(barX, barY, barX + EQ_BAR_W, barY + 1, bright);
            }
        }
    }

    private void renderDiscLibrary(GuiGraphics g, int listTop, int listH, int mouseX, int mouseY) {
        int rowW = libraryRight - libraryLeft;

        for (int i = 0; i < allDiscs.size(); i++) {
            int displayIndex = i - scroll;
            int rowY = listTop + displayIndex * (DISC_ROW_H + DISC_GAP);

            // Skip rows outside visible area
            if (rowY + DISC_ROW_H < listTop || rowY > libraryBottom) continue;

            DiscEntry disc = allDiscs.get(i);
            boolean unlocked = unlockedDiscs.contains(disc.itemId());
            boolean hovered = mouseX >= libraryLeft && mouseX < libraryRight && mouseY >= rowY && mouseY < rowY + DISC_ROW_H;
            boolean isPlaying = (i == currentDisc && playing);

            // Row background
            if (isPlaying) {
                int playBg = (disc.color() & 0x00FFFFFF) | 0x33000000;
                g.fill(libraryLeft, rowY, libraryLeft + rowW, rowY + DISC_ROW_H, playBg);
                // Left accent bar
                g.fill(libraryLeft, rowY, libraryLeft + 2, rowY + DISC_ROW_H, disc.color());
            } else {
                UIHelper.drawRowBg(g, libraryLeft, rowY, rowW, DISC_ROW_H, i % 2 == 0);
            }

            if (hovered && unlocked) {
                g.fill(libraryLeft, rowY, libraryLeft + rowW, rowY + DISC_ROW_H, 0x22FFFFFF);
            }

            int textY = rowY + (DISC_ROW_H - 9) / 2;

            if (unlocked) {
                // Disc number
                String numStr = String.format("%02d", i + 1);
                g.drawString(this.font, numStr, libraryLeft + 4, textY, UIHelper.GOLD_DARK, false);

                // Disc color dot
                g.fill(libraryLeft + 20, rowY + 5, libraryLeft + 24, rowY + DISC_ROW_H - 5, disc.color());

                // Disc name
                g.drawString(this.font, disc.name(), libraryLeft + 28, textY, isPlaying ? disc.color() : UIHelper.CREAM_TEXT, false);

                // Author (right-aligned)
                String authorStr = disc.author();
                int authorW = this.font.width(authorStr);
                g.drawString(this.font, authorStr, libraryLeft + rowW - authorW - 30, textY, UIHelper.GOLD_DARK, false);

                // Queue button [+]
                int qBtnX = libraryLeft + rowW - 24;
                int qBtnY = rowY + 2;
                int qBtnS = DISC_ROW_H - 4;
                boolean qHover = mouseX >= qBtnX && mouseX < qBtnX + qBtnS && mouseY >= qBtnY && mouseY < qBtnY + qBtnS;
                boolean inQueue = playlist.contains(i);
                UIHelper.drawButton(g, qBtnX, qBtnY, qBtnS, qBtnS, qHover);
                String qIcon = inQueue ? "-" : "+";
                int qIconW = this.font.width(qIcon);
                g.drawString(this.font, qIcon, qBtnX + (qBtnS - qIconW) / 2, qBtnY + (qBtnS - 9) / 2, inQueue ? 0xFFFF4444 : 0xFF44FF44, false);
            } else {
                // Locked disc
                String numStr = String.format("%02d", i + 1);
                g.drawString(this.font, numStr, libraryLeft + 4, textY, 0xFF444444, false);

                // Grey dot
                g.fill(libraryLeft + 20, rowY + 5, libraryLeft + 24, rowY + DISC_ROW_H - 5, 0xFF444444);

                // Locked name
                g.drawString(this.font, "???", libraryLeft + 28, textY, 0xFF555555, false);

                // Hint
                String hint = "Donate to Museum";
                int hintW = this.font.width(hint);
                g.drawString(this.font, hint, libraryLeft + rowW - hintW - 6, textY, 0xFF444444, false);
            }
        }
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        if (consumed) {
            return super.mouseClicked(event, consumed);
        }
        int mx = (int) event.x();
        int my = (int) event.y();

        // Back button
        if (isHovered(mx, my, backX, backY, backW, backH)) {
            stopMusic();
            Minecraft.getInstance().setScreen(this.parent);
            return true;
        }

        // Previous
        if (isHovered(mx, my, prevX, prevY, BUTTON_SIZE, BUTTON_SIZE)) {
            previousDisc();
            return true;
        }

        // Play/Pause
        if (isHovered(mx, my, playPauseX, playPauseY, BUTTON_SIZE, BUTTON_SIZE)) {
            if (playing && !paused) {
                pauseMusic();
            } else if (playing && paused) {
                resumeMusic();
            } else if (currentDisc >= 0) {
                playDisc(currentDisc);
            } else if (!playlist.isEmpty()) {
                playlistIndex = 0;
                playDisc(playlist.get(0));
            }
            return true;
        }

        // Stop
        if (isHovered(mx, my, stopX, stopY, BUTTON_SIZE, BUTTON_SIZE)) {
            stopMusic();
            return true;
        }

        // Next
        if (isHovered(mx, my, nextX, nextY, BUTTON_SIZE, BUTTON_SIZE)) {
            advanceToNext();
            return true;
        }

        // Shuffle
        if (isHovered(mx, my, shuffleX, shuffleY, BUTTON_SIZE, BUTTON_SIZE)) {
            shuffle = !shuffle;
            return true;
        }

        // Repeat
        if (isHovered(mx, my, repeatX, repeatY, BUTTON_SIZE, BUTTON_SIZE)) {
            repeat = !repeat;
            return true;
        }

        // Library disc clicks
        int headerH = 12;
        int listTop = libraryTop + headerH + 2;
        if (mx >= libraryLeft && mx < libraryRight && my >= listTop && my < libraryBottom) {
            int rowW = libraryRight - libraryLeft;
            for (int i = 0; i < allDiscs.size(); i++) {
                int displayIndex = i - scroll;
                int rowY = listTop + displayIndex * (DISC_ROW_H + DISC_GAP);
                if (my >= rowY && my < rowY + DISC_ROW_H) {
                    boolean unlocked = unlockedDiscs.contains(allDiscs.get(i).itemId());
                    if (!unlocked) return true;

                    // Check queue button [+/-]
                    int qBtnX = libraryLeft + rowW - 24;
                    int qBtnY = rowY + 2;
                    int qBtnS = DISC_ROW_H - 4;
                    if (mx >= qBtnX && mx < qBtnX + qBtnS && my >= qBtnY && my < qBtnY + qBtnS) {
                        // Toggle in playlist
                        if (playlist.contains(i)) {
                            playlist.remove(Integer.valueOf(i));
                        } else {
                            playlist.add(i);
                        }
                        return true;
                    }

                    // Click to play immediately
                    playDisc(i);
                    return true;
                }
            }
        }

        return super.mouseClicked(event, consumed);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll -= (int) scrollY;
        this.scroll = Math.max(0, Math.min(maxScroll, this.scroll));
        return true;
    }

    private void playDisc(int index) {
        if (index < 0 || index >= allDiscs.size()) return;
        if (!unlockedDiscs.contains(allDiscs.get(index).itemId())) return;

        stopSound();

        currentDisc = index;
        playTicks = 0;
        playing = true;
        paused = false;

        DiscEntry disc = allDiscs.get(index);
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().stop(null, SoundSource.RECORDS);
        mc.getMusicManager().stopPlaying();

        currentSound = createMusicInstance(disc.soundEvent());
        mc.getSoundManager().play((SoundInstance) currentSound);
    }

    private static SimpleSoundInstance createMusicInstance(SoundEvent event) {
        // Use forUI (proven to work in this codebase) — plays non-positionally
        return SimpleSoundInstance.forUI(event, 1.0f, 1.0f);
    }

    private void pauseMusic() {
        if (playing && currentSound != null) {
            paused = true;
            Minecraft.getInstance().getSoundManager().stop((SoundInstance) currentSound);
        }
    }

    private void resumeMusic() {
        if (playing && paused && currentDisc >= 0) {
            paused = false;
            DiscEntry disc = allDiscs.get(currentDisc);
            Minecraft mc = Minecraft.getInstance();
            mc.getMusicManager().stopPlaying();
            currentSound = createMusicInstance(disc.soundEvent());
            mc.getSoundManager().play((SoundInstance) currentSound);
        }
    }

    private void stopMusic() {
        stopSound();
        playing = false;
        paused = false;
        playTicks = 0;
    }

    private void stopSound() {
        if (currentSound != null) {
            Minecraft.getInstance().getSoundManager().stop((SoundInstance) currentSound);
            currentSound = null;
        }
    }

    private void advanceToNext() {
        if (repeat && currentDisc >= 0) {
            // Repeat current
            playDisc(currentDisc);
            return;
        }

        if (!playlist.isEmpty()) {
            // Advance through playlist
            if (shuffle) {
                int nextIdx = eqRandom.nextInt(playlist.size());
                playlistIndex = nextIdx;
            } else {
                playlistIndex++;
                if (playlistIndex >= playlist.size()) {
                    playlistIndex = 0;
                }
            }
            playDisc(playlist.get(playlistIndex));
            return;
        }

        // No playlist, advance through unlocked discs
        List<Integer> available = getUnlockedIndices();
        if (available.isEmpty()) {
            stopMusic();
            return;
        }

        if (shuffle) {
            int nextIdx = eqRandom.nextInt(available.size());
            playDisc(available.get(nextIdx));
        } else {
            int currentPos = available.indexOf(currentDisc);
            int nextPos = (currentPos + 1) % available.size();
            playDisc(available.get(nextPos));
        }
    }

    private void previousDisc() {
        if (!playlist.isEmpty()) {
            playlistIndex--;
            if (playlistIndex < 0) {
                playlistIndex = playlist.size() - 1;
            }
            playDisc(playlist.get(playlistIndex));
            return;
        }

        List<Integer> available = getUnlockedIndices();
        if (available.isEmpty()) return;

        int currentPos = available.indexOf(currentDisc);
        int prevPos = (currentPos - 1 + available.size()) % available.size();
        playDisc(available.get(prevPos));
    }

    private List<Integer> getUnlockedIndices() {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < allDiscs.size(); i++) {
            if (unlockedDiscs.contains(allDiscs.get(i).itemId())) {
                indices.add(i);
            }
        }
        return indices;
    }

    private boolean isHovered(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private String formatTime(int totalSeconds) {
        int min = totalSeconds / 60;
        int sec = totalSeconds % 60;
        return min + ":" + (sec < 10 ? "0" : "") + sec;
    }

    private static int darkenColor(int color, int amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.max(0, ((color >> 16) & 0xFF) - amount);
        int gr = Math.max(0, ((color >> 8) & 0xFF) - amount);
        int b = Math.max(0, (color & 0xFF) - amount);
        return (a << 24) | (r << 16) | (gr << 8) | b;
    }

    private static int brightenColor(int color, int amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, ((color >> 16) & 0xFF) + amount);
        int gr = Math.min(255, ((color >> 8) & 0xFF) + amount);
        int b = Math.min(255, (color & 0xFF) + amount);
        return (a << 24) | (r << 16) | (gr << 8) | b;
    }

    public void removed() {
        super.removed();
        stopSound();
    }

    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    public boolean isPauseScreen() {
        return false;
    }
}

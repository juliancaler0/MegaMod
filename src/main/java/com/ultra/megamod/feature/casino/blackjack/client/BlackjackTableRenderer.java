package com.ultra.megamod.feature.casino.blackjack.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ultra.megamod.feature.casino.blackjack.BlackjackTableBlockEntity;
import com.ultra.megamod.feature.casino.network.BlackjackSyncPayload;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/**
 * Renders playing cards flat on the blackjack table surface.
 * <p>
 * Dealer cards are rendered at the north side of the block (low Z),
 * player cards at the south side (high Z). Face-up cards are white
 * rectangles; face-down cards are dark blue rectangles.
 * <p>
 * The detailed card view (rank, suit, values) is handled by
 * {@code BlackjackScreen}. This BER provides visual world-space feedback
 * that a game is active and cards exist on the table.
 */
public class BlackjackTableRenderer implements BlockEntityRenderer<BlackjackTableBlockEntity, BlackjackRenderState> {

    /** 1x1 white pixel texture used as a canvas for vertex-colored quads. */
    private static final Identifier WHITE_TEXTURE =
            Identifier.fromNamespaceAndPath("megamod", "textures/gui/casino/white.png");

    /** Card dimensions in block units. */
    private static final float CARD_W = 0.13f;
    private static final float CARD_H = 0.18f;

    /** Horizontal spacing between cards. */
    private static final float CARD_SPACING = 0.16f;

    /** Y offset above block surface (cards sit on top). */
    private static final float Y_OFFSET = 1.01f;

    /** Dealer card row Z position (north side of block). */
    private static final float DEALER_Z = 0.25f;

    /** Player card row base Z position (south side of block). */
    private static final float PLAYER_Z_BASE = 0.60f;

    /** Z spacing between player seat rows. */
    private static final float SEAT_Z_SPACING = 0.18f;

    // Card colors (RGBA)
    private static final int FACE_UP_R = 240, FACE_UP_G = 240, FACE_UP_B = 235;
    private static final int FACE_DOWN_R = 30, FACE_DOWN_G = 50, FACE_DOWN_B = 120;
    private static final int RED_ACCENT_R = 200, RED_ACCENT_G = 40, RED_ACCENT_B = 40;
    private static final int BLACK_ACCENT_R = 30, BLACK_ACCENT_G = 30, BLACK_ACCENT_B = 30;

    public BlackjackTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    // ── Render state lifecycle ──────────────────────────────────────────────

    @Override
    public BlackjackRenderState createRenderState() {
        return new BlackjackRenderState();
    }

    @Override
    public void extractRenderState(BlackjackTableBlockEntity blockEntity, BlackjackRenderState state, float partialTick,
                                   Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumbling);

        BlackjackSyncPayload sync = BlackjackSyncPayload.lastSync;
        if (sync == null || sync.gameStateJson() == null || sync.gameStateJson().isEmpty()) {
            state.hasGame = false;
            return;
        }

        try {
            JsonObject json = JsonParser.parseString(sync.gameStateJson()).getAsJsonObject();
            String phase = json.has("phase") ? json.get("phase").getAsString() : "WAITING";

            if ("WAITING".equals(phase)) {
                state.hasGame = false;
                return;
            }

            state.hasGame = true;

            // Dealer
            JsonObject dealer = json.has("dealer") ? json.getAsJsonObject("dealer") : null;
            if (dealer != null && dealer.has("cards")) {
                JsonArray dealerCards = dealer.getAsJsonArray("cards");
                state.dealerCardCount = dealerCards.size();
                state.dealerHoleRevealed = dealer.has("holeRevealed") && dealer.get("holeRevealed").getAsBoolean();
            } else {
                state.dealerCardCount = 0;
                state.dealerHoleRevealed = false;
            }

            // Seats
            JsonArray seats = json.has("seats") ? json.getAsJsonArray("seats") : null;
            state.seatCount = 0;
            for (int i = 0; i < 4; i++) {
                state.seatOccupied[i] = false;
                state.seatCardCounts[i] = 0;
                state.seatHasRed[i] = false;
            }

            if (seats != null) {
                int count = Math.min(seats.size(), 4);
                for (int i = 0; i < count; i++) {
                    JsonObject seat = seats.get(i).getAsJsonObject();
                    boolean occupied = seat.has("occupied") && seat.get("occupied").getAsBoolean();
                    state.seatOccupied[i] = occupied;
                    if (occupied) {
                        state.seatCount++;
                        // Count cards in first hand
                        if (seat.has("hands")) {
                            JsonArray hands = seat.getAsJsonArray("hands");
                            if (!hands.isEmpty()) {
                                JsonObject firstHand = hands.get(0).getAsJsonObject();
                                if (firstHand.has("cards")) {
                                    JsonArray cards = firstHand.getAsJsonArray("cards");
                                    state.seatCardCounts[i] = cards.size();
                                    // Check for any red suit cards (Hearts/Diamonds)
                                    for (JsonElement cardElem : cards) {
                                        String cardStr = cardElem.getAsString();
                                        if (cardStr.endsWith("H") || cardStr.endsWith("D")) {
                                            state.seatHasRed[i] = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            state.hasGame = false;
        }
    }

    // ── Rendering ───────────────────────────────────────────────────────────

    @Override
    public void submit(BlackjackRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (!state.hasGame) return;

        final int dealerCards = state.dealerCardCount;
        final boolean holeRevealed = state.dealerHoleRevealed;
        final boolean[] seatOccupied = state.seatOccupied;
        final int[] seatCardCounts = state.seatCardCounts;
        final boolean[] seatHasRed = state.seatHasRed;

        poseStack.pushPose();

        collector.submitCustomGeometry(poseStack, RenderTypes.entitySolid(WHITE_TEXTURE), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();

            // ── Dealer cards ────────────────────────────────────────────
            if (dealerCards > 0) {
                float dealerStartX = 0.5f - (dealerCards * CARD_SPACING) / 2f + CARD_SPACING / 2f;
                for (int i = 0; i < dealerCards; i++) {
                    float cx = dealerStartX + i * CARD_SPACING;
                    boolean faceUp = (i == 0) || holeRevealed;
                    drawCard(matrix, pose, consumer, cx, Y_OFFSET, DEALER_Z, faceUp, false);
                }
            }

            // ── Player cards ────────────────────────────────────────────
            for (int seat = 0; seat < 4; seat++) {
                if (!seatOccupied[seat] || seatCardCounts[seat] == 0) continue;

                int cardCount = seatCardCounts[seat];
                float seatZ = PLAYER_Z_BASE + seat * SEAT_Z_SPACING;
                float startX = 0.5f - (cardCount * CARD_SPACING) / 2f + CARD_SPACING / 2f;

                for (int i = 0; i < cardCount; i++) {
                    float cx = startX + i * CARD_SPACING;
                    drawCard(matrix, pose, consumer, cx, Y_OFFSET, seatZ, true, seatHasRed[seat]);
                }
            }
        });

        poseStack.popPose();
    }

    /**
     * Draws a single card as a flat colored quad on the Y plane (table surface).
     *
     * @param cx          Card center X
     * @param cy          Card Y (height above block origin)
     * @param cz          Card center Z
     * @param faceUp      true for face-up (white), false for face-down (dark blue)
     * @param hasRed      hint for face-up cards: if true, add a red accent corner
     */
    private static void drawCard(Matrix4f matrix, PoseStack.Pose pose, VertexConsumer consumer,
                                 float cx, float cy, float cz, boolean faceUp, boolean hasRed) {
        float halfW = CARD_W / 2f;
        float halfH = CARD_H / 2f;

        int r, g, b;
        if (faceUp) {
            r = FACE_UP_R;
            g = FACE_UP_G;
            b = FACE_UP_B;
        } else {
            r = FACE_DOWN_R;
            g = FACE_DOWN_G;
            b = FACE_DOWN_B;
        }

        // Main card body -- quad on the Y plane (facing up)
        consumer.addVertex(matrix, cx - halfW, cy, cz - halfH)
                .setColor(r, g, b, 255)
                .setUv(0, 0)
                .setOverlay(655360)
                .setLight(0x00F000F0)
                .setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, cx - halfW, cy, cz + halfH)
                .setColor(r, g, b, 255)
                .setUv(0, 1)
                .setOverlay(655360)
                .setLight(0x00F000F0)
                .setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, cx + halfW, cy, cz + halfH)
                .setColor(r, g, b, 255)
                .setUv(1, 1)
                .setOverlay(655360)
                .setLight(0x00F000F0)
                .setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, cx + halfW, cy, cz - halfH)
                .setColor(r, g, b, 255)
                .setUv(1, 0)
                .setOverlay(655360)
                .setLight(0x00F000F0)
                .setNormal(pose, 0, 1, 0);

        // Thin border around the card (slightly larger, dark)
        float bw = halfW + 0.008f;
        float bh = halfH + 0.008f;
        float by = cy - 0.001f; // just below card surface

        consumer.addVertex(matrix, cx - bw, by, cz - bh)
                .setColor(50, 50, 50, 255)
                .setUv(0, 0)
                .setOverlay(655360)
                .setLight(0x00F000F0)
                .setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, cx - bw, by, cz + bh)
                .setColor(50, 50, 50, 255)
                .setUv(0, 1)
                .setOverlay(655360)
                .setLight(0x00F000F0)
                .setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, cx + bw, by, cz + bh)
                .setColor(50, 50, 50, 255)
                .setUv(1, 1)
                .setOverlay(655360)
                .setLight(0x00F000F0)
                .setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, cx + bw, by, cz - bh)
                .setColor(50, 50, 50, 255)
                .setUv(1, 0)
                .setOverlay(655360)
                .setLight(0x00F000F0)
                .setNormal(pose, 0, 1, 0);

        // Small colored accent in the top-left corner of face-up cards
        if (faceUp) {
            float accR, accG, accB;
            if (hasRed) {
                accR = RED_ACCENT_R;
                accG = RED_ACCENT_G;
                accB = RED_ACCENT_B;
            } else {
                accR = BLACK_ACCENT_R;
                accG = BLACK_ACCENT_G;
                accB = BLACK_ACCENT_B;
            }

            float dotSize = 0.025f;
            float dotX = cx - halfW + 0.03f;
            float dotZ = cz - halfH + 0.03f;
            float dotY = cy + 0.001f;

            consumer.addVertex(matrix, dotX - dotSize, dotY, dotZ - dotSize)
                    .setColor((int) accR, (int) accG, (int) accB, 255)
                    .setUv(0, 0)
                    .setOverlay(655360)
                    .setLight(0x00F000F0)
                    .setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, dotX - dotSize, dotY, dotZ + dotSize)
                    .setColor((int) accR, (int) accG, (int) accB, 255)
                    .setUv(0, 1)
                    .setOverlay(655360)
                    .setLight(0x00F000F0)
                    .setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, dotX + dotSize, dotY, dotZ + dotSize)
                    .setColor((int) accR, (int) accG, (int) accB, 255)
                    .setUv(1, 1)
                    .setOverlay(655360)
                    .setLight(0x00F000F0)
                    .setNormal(pose, 0, 1, 0);
            consumer.addVertex(matrix, dotX + dotSize, dotY, dotZ - dotSize)
                    .setColor((int) accR, (int) accG, (int) accB, 255)
                    .setUv(1, 0)
                    .setOverlay(655360)
                    .setLight(0x00F000F0)
                    .setNormal(pose, 0, 1, 0);
        }

        // Face-down pattern: small diamond pattern on card back
        if (!faceUp) {
            float patternSize = 0.02f;
            float patternY = cy + 0.001f;
            int pr = 60, pg = 80, pb = 160;

            // Draw a 3x4 grid of small diamonds
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 3; col++) {
                    float px = cx - halfW + 0.025f + col * (CARD_W / 3.5f);
                    float pz = cz - halfH + 0.025f + row * (CARD_H / 4.5f);

                    consumer.addVertex(matrix, px - patternSize, patternY, pz)
                            .setColor(pr, pg, pb, 255)
                            .setUv(0, 0.5f)
                            .setOverlay(655360)
                            .setLight(0x00F000F0)
                            .setNormal(pose, 0, 1, 0);
                    consumer.addVertex(matrix, px, patternY, pz - patternSize)
                            .setColor(pr, pg, pb, 255)
                            .setUv(0.5f, 0)
                            .setOverlay(655360)
                            .setLight(0x00F000F0)
                            .setNormal(pose, 0, 1, 0);
                    consumer.addVertex(matrix, px + patternSize, patternY, pz)
                            .setColor(pr, pg, pb, 255)
                            .setUv(1, 0.5f)
                            .setOverlay(655360)
                            .setLight(0x00F000F0)
                            .setNormal(pose, 0, 1, 0);
                    consumer.addVertex(matrix, px, patternY, pz + patternSize)
                            .setColor(pr, pg, pb, 255)
                            .setUv(0.5f, 1)
                            .setOverlay(655360)
                            .setLight(0x00F000F0)
                            .setNormal(pose, 0, 1, 0);
                }
            }
        }
    }
}

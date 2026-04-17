package com.ultra.megamod.lib.pufferfish_skills.client.rendering;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.joml.Vector2f;

/**
 * Renders skill connections (lines and arrows) between skill nodes.
 * In 1.21.11, GuiGraphics uses a Matrix3x2fStack for 2D transforms. We draw each connection
 * directly while the transform is active using fill() calls for line segments.
 * The connections are drawn as a series of small axis-aligned rectangles that approximate
 * the thick line.
 */
public class ConnectionBatchedRenderer {

	private GuiGraphics context;
	private boolean hasStrokes = false;
	private boolean hasFills = false;

	// We draw connections in two passes: strokes first, then fills on top
	private static final int MAX_CONNECTIONS = 512;
	private float[] strokeStartX = new float[MAX_CONNECTIONS];
	private float[] strokeStartY = new float[MAX_CONNECTIONS];
	private float[] strokeEndX = new float[MAX_CONNECTIONS];
	private float[] strokeEndY = new float[MAX_CONNECTIONS];
	private boolean[] strokeBidirectional = new boolean[MAX_CONNECTIONS];
	private int[] strokeColor = new int[MAX_CONNECTIONS];
	private int strokeCount = 0;

	private float[] fillStartX = new float[MAX_CONNECTIONS];
	private float[] fillStartY = new float[MAX_CONNECTIONS];
	private float[] fillEndX = new float[MAX_CONNECTIONS];
	private float[] fillEndY = new float[MAX_CONNECTIONS];
	private boolean[] fillBidirectional = new boolean[MAX_CONNECTIONS];
	private int[] fillColor = new int[MAX_CONNECTIONS];
	private int fillCount = 0;

	public void emitConnection(
			GuiGraphics context,
			float startX,
			float startY,
			float endX,
			float endY,
			boolean bidirectional,
			int fillColorVal,
			int strokeColorVal
	) {
		this.context = context;

		if (strokeCount < MAX_CONNECTIONS) {
			strokeStartX[strokeCount] = startX;
			strokeStartY[strokeCount] = startY;
			strokeEndX[strokeCount] = endX;
			strokeEndY[strokeCount] = endY;
			strokeBidirectional[strokeCount] = bidirectional;
			strokeColor[strokeCount] = strokeColorVal;
			strokeCount++;
			hasStrokes = true;
		}

		if (fillCount < MAX_CONNECTIONS) {
			fillStartX[fillCount] = startX;
			fillStartY[fillCount] = startY;
			fillEndX[fillCount] = endX;
			fillEndY[fillCount] = endY;
			fillBidirectional[fillCount] = bidirectional;
			fillColor[fillCount] = fillColorVal;
			fillCount++;
			hasFills = true;
		}
	}

	public void draw(GuiGraphics context) {
		this.context = context;

		// Draw strokes first (thicker, behind fills)
		for (int i = 0; i < strokeCount; i++) {
			drawLine(strokeStartX[i], strokeStartY[i], strokeEndX[i], strokeEndY[i], 3f, strokeColor[i]);
			if (!strokeBidirectional[i]) {
				drawArrow(strokeStartX[i], strokeStartY[i], strokeEndX[i], strokeEndY[i], 8f, strokeColor[i]);
			}
		}

		// Draw fills on top (thinner, on top of strokes)
		for (int i = 0; i < fillCount; i++) {
			drawLine(fillStartX[i], fillStartY[i], fillEndX[i], fillEndY[i], 1f, fillColor[i]);
			if (!fillBidirectional[i]) {
				drawArrow(fillStartX[i], fillStartY[i], fillEndX[i], fillEndY[i], 6f, fillColor[i]);
			}
		}

		strokeCount = 0;
		fillCount = 0;
		hasStrokes = false;
		hasFills = false;
	}

	private void drawLine(float startX, float startY, float endX, float endY, float thickness, int color) {
		float dx = endX - startX;
		float dy = endY - startY;
		float len = (float) Math.sqrt(dx * dx + dy * dy);
		if (len < 0.001f) return;

		// Draw as a single rotated rectangle using the Matrix3x2f transform stack. This replaces
		// the old scanline fill (which did O(rows * connections) per-pixel fill() calls — ~30k
		// draw calls per frame with ~150 connections — and caused heavy panning lag).
		float halfThickness = thickness / 2f;
		float angle = (float) Math.atan2(dy, dx);
		var pose = context.pose();
		pose.pushMatrix();
		pose.translate(startX, startY);
		pose.rotate(angle);
		context.fill(0, -Mth.ceil(halfThickness), Mth.ceil(len), Mth.ceil(halfThickness), color);
		pose.popMatrix();
	}

	private void drawArrow(float startX, float startY, float endX, float endY, float size, int color) {
		var center = new Vector2f((endX + startX) / 2f, (endY + startY) / 2f);
		var normal = new Vector2f(endX - startX, endY - startY);
		float len = normal.length();
		if (len < 0.001f) return;
		normal.normalize();

		var forward = new Vector2f(normal).mul(size);
		var backward = new Vector2f(forward).div(-2f);
		var back = new Vector2f(center).add(backward);
		var side = new Vector2f(backward).perpendicular().mul(Mth.sqrt(3f));

		float tipX = center.x + forward.x;
		float tipY = center.y + forward.y;
		float leftX = back.x - side.x;
		float leftY = back.y - side.y;
		float rightX = back.x + side.x;
		float rightY = back.y + side.y;

		// Draw arrow as a filled triangle approximation
		drawFilledTriangle(tipX, tipY, leftX, leftY, rightX, rightY, color);
	}

	/**
	 * Draw a thick line quad by scan-line filling between its edges.
	 * The quad has corners (x1,y1), (x2,y2), (x3,y3), (x4,y4) forming a parallelogram.
	 */
	private void drawThickLineSegments(
			float x1, float y1, float x2, float y2,
			float x3, float y3, float x4, float y4,
			int color
	) {
		// Find bounding box
		float minY = Math.min(Math.min(y1, y2), Math.min(y3, y4));
		float maxY = Math.max(Math.max(y1, y2), Math.max(y3, y4));

		int startRow = Mth.floor(minY);
		int endRow = Mth.ceil(maxY);

		// For each scanline row, find the x extent of the quad
		float[] xs = {x1, x2, x3, x4};
		float[] ys = {y1, y2, y3, y4};
		// Edges: 0-1, 1-2, 2-3, 3-0
		int[][] edges = {{0,1}, {1,2}, {2,3}, {3,0}};

		for (int row = startRow; row < endRow; row++) {
			float scanY = row + 0.5f;
			float minX = Float.POSITIVE_INFINITY;
			float maxX = Float.NEGATIVE_INFINITY;

			for (int[] edge : edges) {
				float ey1 = ys[edge[0]];
				float ey2 = ys[edge[1]];
				float ex1 = xs[edge[0]];
				float ex2 = xs[edge[1]];

				if ((ey1 <= scanY && ey2 > scanY) || (ey2 <= scanY && ey1 > scanY)) {
					float t = (scanY - ey1) / (ey2 - ey1);
					float ix = ex1 + t * (ex2 - ex1);
					minX = Math.min(minX, ix);
					maxX = Math.max(maxX, ix);
				}
			}

			if (minX <= maxX) {
				context.fill(Mth.floor(minX), row, Mth.ceil(maxX), row + 1, color);
			}
		}
	}

	/**
	 * Draw a filled triangle using scanline approach.
	 */
	private void drawFilledTriangle(
			float x1, float y1, float x2, float y2, float x3, float y3,
			int color
	) {
		float minY = Math.min(Math.min(y1, y2), y3);
		float maxY = Math.max(Math.max(y1, y2), y3);

		int startRow = Mth.floor(minY);
		int endRow = Mth.ceil(maxY);

		float[] xs = {x1, x2, x3};
		float[] ys = {y1, y2, y3};
		int[][] edges = {{0,1}, {1,2}, {2,0}};

		for (int row = startRow; row < endRow; row++) {
			float scanY = row + 0.5f;
			float minX = Float.POSITIVE_INFINITY;
			float maxX = Float.NEGATIVE_INFINITY;

			for (int[] edge : edges) {
				float ey1 = ys[edge[0]];
				float ey2 = ys[edge[1]];
				float ex1 = xs[edge[0]];
				float ex2 = xs[edge[1]];

				if ((ey1 <= scanY && ey2 > scanY) || (ey2 <= scanY && ey1 > scanY)) {
					float t = (scanY - ey1) / (ey2 - ey1);
					float ix = ex1 + t * (ex2 - ex1);
					minX = Math.min(minX, ix);
					maxX = Math.max(maxX, ix);
				}
			}

			if (minX <= maxX) {
				context.fill(Mth.floor(minX), row, Mth.ceil(maxX), row + 1, color);
			}
		}
	}
}

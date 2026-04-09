package com.ultra.megamod.feature.dungeons.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Block state properties for DNL Chaos Spawner blocks.
 */
public class ChaosProperties {

    public static final EnumProperty<AllSides> ALL_SIDES = EnumProperty.create("all_sides", AllSides.class);
    public static final EnumProperty<BarrierEdges> BARRIER_EDGES = EnumProperty.create("barrier_edge", BarrierEdges.class);
    public static final EnumProperty<BarrierVertexs> BARRIER_VERTEXS = EnumProperty.create("barrier_vertex", BarrierVertexs.class);

    public enum AllSides implements StringRepresentable {
        TOP("top"), BOTTOM("bottom"), VERTICAL("vertical");
        private final String name;
        AllSides(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }

    public enum BarrierEdges implements StringRepresentable {
        TOP("top"), BOTTOM("bottom"), UP("up"), RIGHT("right"), DOWN("down"), LEFT("left");
        private final String name;
        BarrierEdges(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }

    public enum BarrierVertexs implements StringRepresentable {
        TOP("top"), BOTTOM("bottom"), TOP_RIGHT("top_right"), TOP_LEFT("top_left"),
        BOTTOM_RIGHT("bottom_right"), BOTTOM_LEFT("bottom_left");
        private final String name;
        BarrierVertexs(String name) { this.name = name; }
        @Override public String getSerializedName() { return name; }
    }
}

package com.ultra.megamod.feature.citizen.ornament;

/**
 * Comprehensive enum of ALL decorative ornament block variants craftable via the Architects Cutter.
 * Each entry maps to a specific DO model and block shape.
 *
 * Organized by category:
 * - Timber Frames (10 variants, 2 components, directional full blocks)
 * - Framed Lights (7 variants, 2 components, directional full blocks)
 * - Shingles (15 variants = 5 heights x 3 shapes, 3 components, directional)
 * - Shingle Slabs (6 variants, 2 components, slab shape)
 * - Pillars (3 shapes x 4 parts = 12 variants, 1 component, full blocks)
 * - Panels (15 variants, 2 components, full blocks)
 * - Posts (6 variants, 1 component, full blocks)
 * - Paper Walls (2 types, 2 components, four-way connectable)
 * - Doors (4 variants, 1 component, door shape)
 * - Fancy Doors (2 variants, 2 components, door shape)
 * - Trapdoors (15 variants, 1 component, trapdoor shape)
 * - Fancy Trapdoors (2 variants, 2 components, trapdoor shape)
 * - Extra Blocks (27 variants, 1 component, full blocks)
 * - Barrels (1, 1 component, full block)
 * - All Brick (2 variants, 1 component, full blocks)
 * - All Brick Stairs (2 variants, 1 component, stair shape)
 * - Fences (1, 1 component, fence shape)
 * - Fence Gates (1, 1 component, fence gate shape)
 * - Slabs (1, 1 component, slab shape)
 * - Walls (1, 1 component, wall shape)
 * - Stairs (1, 1 component, stair shape)
 */
public enum OrnamentBlockType {

    // ==================== Timber Frames (2 components, directional) ====================
    TIMBER_FRAME_PLAIN("timber_frame_plain", 2, "Plain Timber Frame", BlockShape.FULL, "timber_frame/plain_spec"),
    TIMBER_FRAME_DOUBLE_CROSSED("timber_frame_double_crossed", 2, "Double Crossed Timber Frame", BlockShape.FULL, "timber_frame/double_crossed_spec"),
    TIMBER_FRAME_FRAMED("timber_frame_framed", 2, "Framed Timber Frame", BlockShape.FULL, "timber_frame/framed_spec"),
    TIMBER_FRAME_SIDE_FRAMED("timber_frame_side_framed", 2, "Side Framed Timber Frame", BlockShape.FULL, "timber_frame/side_framed_spec"),
    TIMBER_FRAME_UP_GATED("timber_frame_up_gated", 2, "Up Gated Timber Frame", BlockShape.FULL, "timber_frame/up_gated_spec"),
    TIMBER_FRAME_DOWN_GATED("timber_frame_down_gated", 2, "Down Gated Timber Frame", BlockShape.FULL, "timber_frame/down_gated_spec"),
    TIMBER_FRAME_ONE_CROSSED_LR("timber_frame_one_crossed_lr", 2, "One Crossed LR Timber Frame", BlockShape.FULL, "timber_frame/one_crossed_lr_spec"),
    TIMBER_FRAME_ONE_CROSSED_RL("timber_frame_one_crossed_rl", 2, "One Crossed RL Timber Frame", BlockShape.FULL, "timber_frame/one_crossed_rl_spec"),
    TIMBER_FRAME_HORIZONTAL_PLAIN("timber_frame_horizontal_plain", 2, "Horizontal Plain Timber Frame", BlockShape.FULL, "timber_frame/horizontal_plain_spec"),
    TIMBER_FRAME_SIDE_FRAMED_HORIZONTAL("timber_frame_side_framed_horizontal", 2, "Side Framed Horizontal Timber Frame", BlockShape.FULL, "timber_frame/side_framed_horizontal_spec"),

    // ==================== Framed Lights (2 components) ====================
    FRAMED_LIGHT_VERTICAL("framed_light_vertical", 2, "Vertical Framed Light", BlockShape.FULL, "framed_light/vertical_light_spec"),
    FRAMED_LIGHT_CROSSED("framed_light_crossed", 2, "Crossed Framed Light", BlockShape.FULL, "framed_light/crossed_light_spec"),
    FRAMED_LIGHT_FRAMED("framed_light_framed", 2, "Framed Light", BlockShape.FULL, "framed_light/framed_light_spec"),
    FRAMED_LIGHT_HORIZONTAL("framed_light_horizontal", 2, "Horizontal Framed Light", BlockShape.FULL, "framed_light/horizontal_light_spec"),
    FRAMED_LIGHT_FANCY("framed_light_fancy", 2, "Fancy Framed Light", BlockShape.FULL, "framed_light/fancy_light_spec"),
    FRAMED_LIGHT_FOUR("framed_light_four", 2, "Four Framed Light", BlockShape.FULL, "framed_light/four_light_spec"),
    FRAMED_LIGHT_CENTER("framed_light_center", 2, "Center Framed Light", BlockShape.FULL, "framed_light/center_light_spec"),

    // ==================== Shingles (3 components, directional) ====================
    // Default height
    SHINGLE_DEFAULT_STRAIGHT("shingle_default_straight", 3, "Shingle", BlockShape.FULL, "shingle/straight_spec"),
    SHINGLE_DEFAULT_CONCAVE("shingle_default_concave", 3, "Concave Shingle", BlockShape.FULL, "shingle/concave_spec"),
    SHINGLE_DEFAULT_CONVEX("shingle_default_convex", 3, "Convex Shingle", BlockShape.FULL, "shingle/convex_spec"),
    // Flat height
    SHINGLE_FLAT_STRAIGHT("shingle_flat_straight", 3, "Flat Shingle", BlockShape.FULL, "shingle/flat_straight_spec"),
    SHINGLE_FLAT_CONCAVE("shingle_flat_concave", 3, "Flat Concave Shingle", BlockShape.FULL, "shingle/flat_concave_spec"),
    SHINGLE_FLAT_CONVEX("shingle_flat_convex", 3, "Flat Convex Shingle", BlockShape.FULL, "shingle/flat_convex_spec"),
    // Flat Lower height
    SHINGLE_FLAT_LOWER_STRAIGHT("shingle_flat_lower_straight", 3, "Flat Lower Shingle", BlockShape.FULL, "shingle/flat_lower_straight_spec"),
    SHINGLE_FLAT_LOWER_CONCAVE("shingle_flat_lower_concave", 3, "Flat Lower Concave Shingle", BlockShape.FULL, "shingle/flat_lower_concave_spec"),
    SHINGLE_FLAT_LOWER_CONVEX("shingle_flat_lower_convex", 3, "Flat Lower Convex Shingle", BlockShape.FULL, "shingle/flat_lower_convex_spec"),
    // Steep height
    SHINGLE_STEEP_STRAIGHT("shingle_steep_straight", 3, "Steep Shingle", BlockShape.FULL, "shingle/steep_straight_spec"),
    SHINGLE_STEEP_CONCAVE("shingle_steep_concave", 3, "Steep Concave Shingle", BlockShape.FULL, "shingle/steep_concave_spec"),
    SHINGLE_STEEP_CONVEX("shingle_steep_convex", 3, "Steep Convex Shingle", BlockShape.FULL, "shingle/steep_convex_spec"),
    // Steep Lower height
    SHINGLE_STEEP_LOWER_STRAIGHT("shingle_steep_lower_straight", 3, "Steep Lower Shingle", BlockShape.FULL, "shingle/steep_lower_straight_spec"),
    SHINGLE_STEEP_LOWER_CONCAVE("shingle_steep_lower_concave", 3, "Steep Lower Concave Shingle", BlockShape.FULL, "shingle/steep_lower_concave_spec"),
    SHINGLE_STEEP_LOWER_CONVEX("shingle_steep_lower_convex", 3, "Steep Lower Convex Shingle", BlockShape.FULL, "shingle/steep_lower_convex_spec"),

    // ==================== Shingle Slabs (2 components) ====================
    SHINGLE_SLAB_TOP("shingle_slab_top", 2, "Shingle Slab Top", BlockShape.SLAB, "shingle_slab/shingle_slab_top_spec"),
    SHINGLE_SLAB_ONE_WAY("shingle_slab_one_way", 2, "Shingle Slab One Way", BlockShape.SLAB, "shingle_slab/shingle_slab_one_way_spec"),
    SHINGLE_SLAB_TWO_WAY("shingle_slab_two_way", 2, "Shingle Slab Two Way", BlockShape.SLAB, "shingle_slab/shingle_slab_two_way_spec"),
    SHINGLE_SLAB_THREE_WAY("shingle_slab_three_way", 2, "Shingle Slab Three Way", BlockShape.SLAB, "shingle_slab/shingle_slab_three_way_spec"),
    SHINGLE_SLAB_FOUR_WAY("shingle_slab_four_way", 2, "Shingle Slab Four Way", BlockShape.SLAB, "shingle_slab/shingle_slab_four_way_spec"),
    SHINGLE_SLAB_CURVED("shingle_slab_curved", 2, "Shingle Slab Curved", BlockShape.SLAB, "shingle_slab/shingle_slab_curved_spec"),

    // ==================== Pillars (1 component) ====================
    // Round pillars
    PILLAR_ROUND_FULL("pillar_round_full", 1, "Round Full Pillar", BlockShape.FULL, "pillar/blockpillar_full_pillar_spec"),
    PILLAR_ROUND_COLUMN("pillar_round_column", 1, "Round Pillar Column", BlockShape.FULL, "pillar/blockpillar_pillar_column_spec"),
    PILLAR_ROUND_BASE("pillar_round_base", 1, "Round Pillar Base", BlockShape.FULL, "pillar/blockpillar_pillar_base_spec"),
    PILLAR_ROUND_CAPITAL("pillar_round_capital", 1, "Round Pillar Capital", BlockShape.FULL, "pillar/blockpillar_pillar_capital_spec"),
    // Voxel (blocky) pillars
    PILLAR_VOXEL_FULL("pillar_voxel_full", 1, "Voxel Full Pillar", BlockShape.FULL, "pillar/blockypillar_full_pillar_spec"),
    PILLAR_VOXEL_COLUMN("pillar_voxel_column", 1, "Voxel Pillar Column", BlockShape.FULL, "pillar/blockypillar_pillar_column_spec"),
    PILLAR_VOXEL_BASE("pillar_voxel_base", 1, "Voxel Pillar Base", BlockShape.FULL, "pillar/blockypillar_pillar_base_spec"),
    PILLAR_VOXEL_CAPITAL("pillar_voxel_capital", 1, "Voxel Pillar Capital", BlockShape.FULL, "pillar/blockypillar_pillar_capital_spec"),
    // Square pillars
    PILLAR_SQUARE_FULL("pillar_square_full", 1, "Square Full Pillar", BlockShape.FULL, "pillar/squarepillar_full_pillar_spec"),
    PILLAR_SQUARE_COLUMN("pillar_square_column", 1, "Square Pillar Column", BlockShape.FULL, "pillar/squarepillar_pillar_column_spec"),
    PILLAR_SQUARE_BASE("pillar_square_base", 1, "Square Pillar Base", BlockShape.FULL, "pillar/squarepillar_pillar_base_spec"),
    PILLAR_SQUARE_CAPITAL("pillar_square_capital", 1, "Square Pillar Capital", BlockShape.FULL, "pillar/squarepillar_pillar_capital_spec"),

    // ==================== Panels (2 components) ====================
    PANEL_FULL("panel_full", 2, "Full Panel", BlockShape.FULL, "panel/panel_full_spec"),
    PANEL_BOSS("panel_boss", 2, "Boss Panel", BlockShape.FULL, "panel/panel_boss_spec"),
    PANEL_COFFER("panel_coffer", 2, "Coffer Panel", BlockShape.FULL, "panel/panel_coffer_spec"),
    PANEL_HORIZONTAL_BARS("panel_horizontal_bars", 2, "Horizontal Bars Panel", BlockShape.FULL, "panel/panel_horizontal_bars_spec"),
    PANEL_HORIZONTALLY_SQUIGGLY_STRIPED("panel_horizontally_squiggly_striped", 2, "Squiggly Horizontal Panel", BlockShape.FULL, "panel/panel_horizontally_squiggly_striped_spec"),
    PANEL_HORIZONTALLY_STRIPED("panel_horizontally_striped", 2, "Horizontally Striped Panel", BlockShape.FULL, "panel/panel_horizontally_striped_spec"),
    PANEL_MOULDING("panel_moulding", 2, "Moulding Panel", BlockShape.FULL, "panel/panel_moulding_spec"),
    PANEL_PORT_MANTEAU("panel_port_manteau", 2, "Port Manteau Panel", BlockShape.FULL, "panel/panel_port_manteau_spec"),
    PANEL_PORTHOLE("panel_porthole", 2, "Porthole Panel", BlockShape.FULL, "panel/panel_porthole_spec"),
    PANEL_ROUNDEL("panel_roundel", 2, "Roundel Panel", BlockShape.FULL, "panel/panel_roundel_spec"),
    PANEL_SLOT("panel_slot", 2, "Slot Panel", BlockShape.FULL, "panel/panel_slot_spec"),
    PANEL_VERTICAL_BARS("panel_vertical_bars", 2, "Vertical Bars Panel", BlockShape.FULL, "panel/panel_vertical_bars_spec"),
    PANEL_VERTICALLY_SQUIGGLY_STRIPED("panel_vertically_squiggly_striped", 2, "Squiggly Vertical Panel", BlockShape.FULL, "panel/panel_vertically_squiggly_striped_spec"),
    PANEL_VERTICALLY_STRIPED("panel_vertically_striped", 2, "Vertically Striped Panel", BlockShape.FULL, "panel/panel_vertically_striped_spec"),
    PANEL_WAFFLE("panel_waffle", 2, "Waffle Panel", BlockShape.FULL, "panel/panel_waffle_spec"),

    // ==================== Posts (1 component) ====================
    POST_PLAIN("post_plain", 1, "Plain Post", BlockShape.FULL, "post/post_plain_spec"),
    POST_HEAVY("post_heavy", 1, "Heavy Post", BlockShape.FULL, "post/post_heavy_spec"),
    POST_TURNED("post_turned", 1, "Turned Post", BlockShape.FULL, "post/post_turned_spec"),
    POST_PINCHED("post_pinched", 1, "Pinched Post", BlockShape.FULL, "post/post_pinched_spec"),
    POST_DOUBLE("post_double", 1, "Double Post", BlockShape.FULL, "post/post_double_spec"),
    POST_QUAD("post_quad", 1, "Quad Post", BlockShape.FULL, "post/post_quad_spec"),

    // ==================== Paper Walls (2 components) ====================
    PAPER_WALL("paper_wall", 2, "Paper Wall", BlockShape.FULL, "paperwall/blockpaperwall_post_spec"),
    PAPER_WALL_TILED("paper_wall_tiled", 2, "Tiled Paper Wall", BlockShape.FULL, "tiledpaperwall/blockpaperwall_post_spec"),

    // ==================== Doors (1 component, door shape) ====================
    DOOR_FULL("door_full", 1, "Full Door", BlockShape.DOOR, "door/door_full_spec"),
    DOOR_PORT_MANTEAU("door_port_manteau", 1, "Port Manteau Door", BlockShape.DOOR, "door/door_port_manteau_spec"),
    DOOR_VERTICALLY_STRIPED("door_vertically_striped", 1, "Vertically Striped Door", BlockShape.DOOR, "door/door_vertically_striped_spec"),
    DOOR_WAFFLE("door_waffle", 1, "Waffle Door", BlockShape.DOOR, "door/door_waffle_spec"),

    // ==================== Fancy Doors (2 components, door shape) ====================
    FANCY_DOOR_FULL("fancy_door_full", 2, "Fancy Full Door", BlockShape.DOOR, "door/fancy/door_full_spec"),
    FANCY_DOOR_CREEPER("fancy_door_creeper", 2, "Fancy Creeper Door", BlockShape.DOOR, "door/fancy/door_creeper_spec"),

    // ==================== Trapdoors (1 component, trapdoor shape) ====================
    TRAPDOOR_FULL("trapdoor_full", 1, "Full Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_full_spec"),
    TRAPDOOR_BOSS("trapdoor_boss", 1, "Boss Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_boss_spec"),
    TRAPDOOR_COFFER("trapdoor_coffer", 1, "Coffer Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_coffer_spec"),
    TRAPDOOR_HORIZONTAL_BARS("trapdoor_horizontal_bars", 1, "Horizontal Bars Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_horizontal_bars_spec"),
    TRAPDOOR_HORIZONTALLY_SQUIGGLY_STRIPED("trapdoor_horiz_squiggly", 1, "Squiggly Horizontal Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_horizontally_squiggly_striped_spec"),
    TRAPDOOR_HORIZONTALLY_STRIPED("trapdoor_horiz_striped", 1, "Horizontally Striped Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_horizontally_striped_spec"),
    TRAPDOOR_MOULDING("trapdoor_moulding", 1, "Moulding Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_moulding_spec"),
    TRAPDOOR_PORT_MANTEAU("trapdoor_port_manteau", 1, "Port Manteau Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_port_manteau_spec"),
    TRAPDOOR_PORTHOLE("trapdoor_porthole", 1, "Porthole Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_porthole_spec"),
    TRAPDOOR_ROUNDEL("trapdoor_roundel", 1, "Roundel Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_roundel_spec"),
    TRAPDOOR_SLOT("trapdoor_slot", 1, "Slot Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_slot_spec"),
    TRAPDOOR_VERTICAL_BARS("trapdoor_vertical_bars", 1, "Vertical Bars Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_vertical_bars_spec"),
    TRAPDOOR_VERTICALLY_SQUIGGLY_STRIPED("trapdoor_vert_squiggly", 1, "Squiggly Vertical Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_vertically_squiggly_striped_spec"),
    TRAPDOOR_VERTICALLY_STRIPED("trapdoor_vert_striped", 1, "Vertically Striped Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_vertically_striped_spec"),
    TRAPDOOR_WAFFLE("trapdoor_waffle", 1, "Waffle Trapdoor", BlockShape.TRAPDOOR, "trapdoor/trapdoor_waffle_spec"),

    // ==================== Fancy Trapdoors (2 components, trapdoor shape) ====================
    FANCY_TRAPDOOR_FULL("fancy_trapdoor_full", 2, "Fancy Full Trapdoor", BlockShape.TRAPDOOR, "trapdoor/fancy/trapdoor_full_spec"),
    FANCY_TRAPDOOR_CREEPER("fancy_trapdoor_creeper", 2, "Fancy Creeper Trapdoor", BlockShape.TRAPDOOR, "trapdoor/fancy/trapdoor_creeper_spec"),

    // ==================== Extra Blocks (1 component, full block) ====================
    EXTRA_BLACK_BRICK("extra_black_brick", 1, "Black Brick", BlockShape.FULL, null),
    EXTRA_BLUE_BRICK("extra_blue_brick", 1, "Blue Brick", BlockShape.FULL, null),
    EXTRA_BLUE_SLATE("extra_blue_slate", 1, "Blue Slate", BlockShape.FULL, null),
    EXTRA_BROWN_BRICK("extra_brown_brick", 1, "Brown Brick", BlockShape.FULL, null),
    EXTRA_BASE_BRICK("extra_base_brick", 1, "Brick", BlockShape.FULL, null),
    EXTRA_CYAN_BRICK("extra_cyan_brick", 1, "Cyan Brick", BlockShape.FULL, null),
    EXTRA_GRAY_BRICK("extra_gray_brick", 1, "Gray Brick", BlockShape.FULL, null),
    EXTRA_GREEN_BRICK("extra_green_brick", 1, "Green Brick", BlockShape.FULL, null),
    EXTRA_GREEN_SLATE("extra_green_slate", 1, "Green Slate", BlockShape.FULL, null),
    EXTRA_LIGHT_BLUE_BRICK("extra_light_blue_brick", 1, "Light Blue Brick", BlockShape.FULL, null),
    EXTRA_LIGHT_GRAY_BRICK("extra_light_gray_brick", 1, "Light Gray Brick", BlockShape.FULL, null),
    EXTRA_LIME_BRICK("extra_lime_brick", 1, "Lime Brick", BlockShape.FULL, null),
    EXTRA_MAGENTA_BRICK("extra_magenta_brick", 1, "Magenta Brick", BlockShape.FULL, null),
    EXTRA_MOSS_SLATE("extra_moss_slate", 1, "Moss Slate", BlockShape.FULL, null),
    EXTRA_ORANGE_BRICK("extra_orange_brick", 1, "Orange Brick", BlockShape.FULL, null),
    EXTRA_PINK_BRICK("extra_pink_brick", 1, "Pink Brick", BlockShape.FULL, null),
    EXTRA_PURPLE_BRICK("extra_purple_brick", 1, "Purple Brick", BlockShape.FULL, null),
    EXTRA_PURPLE_SLATE("extra_purple_slate", 1, "Purple Slate", BlockShape.FULL, null),
    EXTRA_RED_BRICK("extra_red_brick", 1, "Red Brick", BlockShape.FULL, null),
    EXTRA_BASE_SLATE("extra_base_slate", 1, "Slate", BlockShape.FULL, null),
    EXTRA_BASE_THATCHED("extra_base_thatched", 1, "Thatched", BlockShape.FULL, null),
    EXTRA_WHITE_BRICK("extra_white_brick", 1, "White Brick", BlockShape.FULL, null),
    EXTRA_YELLOW_BRICK("extra_yellow_brick", 1, "Yellow Brick", BlockShape.FULL, null),
    EXTRA_BASE_PAPER("extra_base_paper", 1, "Paper Block", BlockShape.FULL, null),
    EXTRA_BASE_CACTUS("extra_base_cactus", 1, "Cactus Plank", BlockShape.FULL, null),
    EXTRA_GREEN_CACTUS("extra_green_cactus", 1, "Green Cactus Plank", BlockShape.FULL, null),
    EXTRA_LIGHT_PAPER("extra_light_paper", 1, "Light Paper Block", BlockShape.FULL, null),

    // ==================== All Brick (1 component, full block) ====================
    ALL_BRICK_LIGHT("all_brick_light", 1, "Light All Brick", BlockShape.FULL, "allbrick/light_brick_spec"),
    ALL_BRICK_DARK("all_brick_dark", 1, "Dark All Brick", BlockShape.FULL, "allbrick/dark_brick_spec"),

    // ==================== All Brick Stairs (1 component, stair shape) ====================
    ALL_BRICK_STAIR_LIGHT("all_brick_stair_light", 1, "Light All Brick Stair", BlockShape.STAIR, "allbrick/light_brick_stair_spec"),
    ALL_BRICK_STAIR_DARK("all_brick_stair_dark", 1, "Dark All Brick Stair", BlockShape.STAIR, "allbrick/dark_brick_stair_spec"),

    // ==================== Barrel (1 component, full block) ====================
    BARREL("barrel", 1, "Ornament Barrel", BlockShape.FULL, null),

    // ==================== Fence (1 component, fence shape) ====================
    FENCE("ornament_fence", 1, "Ornament Fence", BlockShape.FENCE, "fence/fence_post_spec"),

    // ==================== Fence Gate (1 component, fence gate shape) ====================
    FENCE_GATE("ornament_fence_gate", 1, "Ornament Fence Gate", BlockShape.FENCE_GATE, "fence_gate/fence_gate_spec"),

    // ==================== Slab (1 component, slab shape) ====================
    SLAB("ornament_slab", 1, "Ornament Slab", BlockShape.SLAB, "slab/slab_bottom_spec"),

    // ==================== Wall (1 component, wall shape) ====================
    WALL("ornament_wall", 1, "Ornament Wall", BlockShape.WALL, "wall/wall_post_spec"),

    // ==================== Stair (1 component, stair shape) ====================
    STAIR("ornament_stair", 1, "Ornament Stair", BlockShape.STAIR, "stair/stairs_spec");

    private final String id;
    private final int componentCount;
    private final String displayName;
    private final BlockShape shape;
    private final String modelPath; // relative path under models/block/ without .json, or null for cube_all fallback

    OrnamentBlockType(String id, int componentCount, String displayName, BlockShape shape, String modelPath) {
        this.id = id;
        this.componentCount = componentCount;
        this.displayName = displayName;
        this.shape = shape;
        this.modelPath = modelPath;
    }

    public String getId() {
        return id;
    }

    public int getComponentCount() {
        return componentCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BlockShape getShape() {
        return shape;
    }

    /**
     * Returns the model path relative to models/block/, or null if using a generic cube_all model.
     */
    public String getModelPath() {
        return modelPath;
    }

    /**
     * Returns the block registry name. This IS the id directly (no prefix).
     */
    public String getRegistryName() {
        return id;
    }

    /**
     * Block shape categories that determine which Block subclass to use.
     */
    public enum BlockShape {
        FULL,       // Standard full cube (OrnamentBlock / BaseEntityBlock)
        SLAB,       // OrnamentSlabBlock
        STAIR,      // OrnamentStairBlock
        FENCE,      // OrnamentFenceBlock
        FENCE_GATE, // OrnamentFenceGateBlock
        WALL,       // OrnamentWallBlock
        DOOR,       // OrnamentDoorBlock
        TRAPDOOR    // OrnamentTrapdoorBlock
    }
}

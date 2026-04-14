package com.ultra.megamod.feature.worldedit.wiki;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Static wiki for every WorldEdit command implemented in MegaMod.
 * Used by the admin app's WorldEdit tab to render a searchable help list.
 */
public final class WorldEditWikiEntries {

    /** One entry per command. */
    public record Entry(String category, String name, String syntax, String description, String[] examples) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();

    static {
        // --- Selection ---
        add("Selection", "/we_wand", "/we_wand", "Gives you the selection wand. Left-click = pos1, right-click = pos2.",
            "/we_wand");
        add("Selection", "/we_pos1", "/we_pos1 [x y z]",
            "Sets the first position (defaults to your feet).",
            "/we_pos1", "/we_pos1 100 64 200");
        add("Selection", "/we_pos2", "/we_pos2 [x y z]",
            "Sets the second position.",
            "/we_pos2");
        add("Selection", "/we_hpos1", "/we_hpos1",
            "Sets pos1 to the block you are looking at.");
        add("Selection", "/we_hpos2", "/we_hpos2",
            "Sets pos2 to the block you are looking at.");
        add("Selection", "/we_sel", "/we_sel <type>",
            "Changes the selector type: cuboid, extend, poly, ellipsoid, sphere, cyl, convex.",
            "/we_sel cuboid", "/we_sel poly");
        add("Selection", "/we_size", "/we_size",
            "Displays width/height/length/volume of the current selection.");
        add("Selection", "/we_count", "/we_count <block>",
            "Counts how many of a given block exist in the selection.",
            "/we_count stone");
        add("Selection", "/we_distr", "/we_distr",
            "Shows the block type distribution within the selection.");
        add("Selection", "/we_chunk", "/we_chunk",
            "Selects the entire chunk you are standing in.");
        add("Selection", "/we_expand", "/we_expand <n> [dir]",
            "Expands the selection by N blocks on the given direction (n/s/e/w/u/d/vert).",
            "/we_expand 5 up", "/we_expand 10 vert");
        add("Selection", "/we_contract", "/we_contract <n> [dir]",
            "Contracts the selection.",
            "/we_contract 3 down");
        add("Selection", "/we_shift", "/we_shift <n> [dir]",
            "Shifts the selection boundary by N in the given direction.",
            "/we_shift 5 east");
        add("Selection", "/we_outset", "/we_outset <n>",
            "Expands the selection symmetrically on all six sides.");
        add("Selection", "/we_inset", "/we_inset <n>",
            "Contracts symmetrically on all six sides.");

        // --- Region ---
        add("Region", "/we_set", "/we_set <pattern>",
            "Replaces every block in the selection with the given pattern.",
            "/we_set stone", "/we_set 50%stone,50%cobblestone");
        add("Region", "/we_replace", "/we_replace <from> <to>",
            "Replaces all blocks matching the 'from' mask with the 'to' pattern.",
            "/we_replace stone diamond_block", "/we_replace #existing air");
        add("Region", "/we_walls", "/we_walls <pattern>",
            "Fills the outer vertical walls of the selection.",
            "/we_walls stone_bricks");
        add("Region", "/we_faces", "/we_faces <pattern>",
            "Fills all six outer faces of the selection.",
            "/we_faces oak_planks");
        add("Region", "/we_overlay", "/we_overlay <pattern>",
            "Places a pattern on top of every top-most solid block in the selection.",
            "/we_overlay grass_block");
        add("Region", "/we_center", "/we_center <pattern>",
            "Sets the exact centre of the selection to the given pattern.");
        add("Region", "/we_smooth", "/we_smooth [iterations]",
            "Lightly smooths the terrain within the selection.",
            "/we_smooth", "/we_smooth 3");
        add("Region", "/we_hollow", "/we_hollow [thickness]",
            "Carves the interior of the selection, leaving a shell of the given thickness.",
            "/we_hollow 1");
        add("Region", "/we_stack", "/we_stack <n> [dir]",
            "Stacks the selection N times in the given direction.",
            "/we_stack 5 up");
        add("Region", "/we_move", "/we_move <n> [dir]",
            "Moves the contents of the selection N blocks in the given direction.",
            "/we_move 10 forward");
        add("Region", "/we_forest", "/we_forest [density]",
            "Plants trees randomly on grass blocks in the selection.",
            "/we_forest 10");
        add("Region", "/we_flora", "/we_flora [density]",
            "Places flowers / short grass on grass blocks in the selection.",
            "/we_flora 15");
        add("Region", "/we_naturalize", "/we_naturalize",
            "Applies a grass/dirt/stone layering to every column in the selection.");
        add("Region", "/we_regen", "/we_regen",
            "Placeholder — MegaMod does not currently regenerate chunks.");

        // --- Clipboard ---
        add("Clipboard", "/we_copy", "/we_copy",
            "Copies the selection into your clipboard.");
        add("Clipboard", "/we_cut", "/we_cut",
            "Copies, then clears, the selection.");
        add("Clipboard", "/we_paste", "/we_paste [skipAir]",
            "Pastes the clipboard at your current location.",
            "/we_paste", "/we_paste false");
        add("Clipboard", "/we_rotate", "/we_rotate <degrees>",
            "Rotates the clipboard by 90/180/270 degrees.",
            "/we_rotate 90");
        add("Clipboard", "/we_flip", "/we_flip",
            "Flips the clipboard left-to-right.");
        add("Clipboard", "/we_clearclipboard", "/we_clearclipboard",
            "Empties the clipboard.");

        // --- Schematic ---
        add("Schematic", "/we_schem save", "/we_schem save <name>",
            "Saves the clipboard to a .litematic file in blueprints/worldedit_schematics/.",
            "/we_schem save my_castle");
        add("Schematic", "/we_schem load", "/we_schem load <name>",
            "Loads a .litematic into the clipboard.",
            "/we_schem load my_castle");
        add("Schematic", "/we_schem list", "/we_schem list",
            "Lists all saved schematics.");
        add("Schematic", "/we_schem delete", "/we_schem delete <name>",
            "Deletes a saved schematic.");

        // --- Generation ---
        add("Generation", "/we_sphere", "/we_sphere <pattern> <radius>",
            "Creates a solid sphere at your position.",
            "/we_sphere stone 8");
        add("Generation", "/we_hsphere", "/we_hsphere <pattern> <radius>",
            "Creates a hollow sphere.",
            "/we_hsphere glass 10");
        add("Generation", "/we_cyl", "/we_cyl <pattern> <radius> [height]",
            "Creates a solid cylinder.",
            "/we_cyl stone 5 10");
        add("Generation", "/we_hcyl", "/we_hcyl <pattern> <radius> <height>",
            "Creates a hollow cylinder.");
        add("Generation", "/we_pyramid", "/we_pyramid <pattern> <size>",
            "Creates a solid square pyramid.",
            "/we_pyramid sandstone 10");
        add("Generation", "/we_hpyramid", "/we_hpyramid <pattern> <size>",
            "Creates a hollow square pyramid.");
        add("Generation", "/we_generate", "/we_generate <pattern> <expr>",
            "Expression-language shape generation is not ported in MegaMod.");

        // --- History ---
        add("History", "/we_undo", "/we_undo [n]",
            "Undoes your last N edits (default 1).",
            "/we_undo", "/we_undo 5");
        add("History", "/we_redo", "/we_redo [n]",
            "Redoes your last N undone edits.",
            "/we_redo");
        add("History", "/we_clearhistory", "/we_clearhistory",
            "Clears your undo and redo stacks.");

        // --- Brush ---
        add("Brush", "/we_brush sphere", "/we_brush sphere <size> <pattern>",
            "Binds a sphere brush. Right-click while holding the brush item to paint.",
            "/we_brush sphere 4 stone");
        add("Brush", "/we_brush hsphere", "/we_brush hsphere <size> <pattern>",
            "Binds a hollow sphere brush.");
        add("Brush", "/we_brush cyl", "/we_brush cyl <size> <height> <pattern>",
            "Binds a cylinder brush.",
            "/we_brush cyl 3 5 glass");
        add("Brush", "/we_brush cube", "/we_brush cube <size> <pattern>",
            "Binds a cube brush.",
            "/we_brush cube 2 obsidian");
        add("Brush", "/we_brush smooth", "/we_brush smooth <size>",
            "Binds a smoothing brush.");
        add("Brush", "/we_brush paste", "/we_brush paste",
            "Binds a brush that pastes your clipboard.");
        add("Brush", "/we_brush none", "/we_brush none",
            "Unbinds the current brush.");
        add("Brush", "/we_brush list", "/we_brush list",
            "Shows the currently bound brush.");
        add("Brush", "/we_mask", "/we_mask <mask>",
            "Sets a mask on the active brush. Use 'none' to clear.",
            "/we_mask !air", "/we_mask #existing");
        add("Brush", "/we_gmask", "/we_gmask <mask>",
            "Sets a global mask applied to every edit.",
            "/we_gmask !bedrock");

        // --- Tool ---
        add("Tool", "/we_tool wand", "/we_tool wand", "Gives you a wand.");
        add("Tool", "/we_tool farwand", "/we_tool farwand", "Gives you a long-range wand.");
        add("Tool", "/we_tool brush", "/we_tool brush", "Gives you the brush item.");
        add("Tool", "/we_tool info", "/we_tool info", "Gives you an info tool — left-click a block to inspect it.");
        add("Tool", "/we_tool tree", "/we_tool tree", "Gives you a tree planter — right-click grass to grow a tree.");
        add("Tool", "/we_tool superpickaxe", "/we_tool superpickaxe",
            "Gives you a super pickaxe — left click obliterates a block instantly.");
        add("Tool", "/we_super", "/we_super", "Toggles super pickaxe mode on/off.");

        // --- Utility ---
        add("Utility", "/we_drain", "/we_drain <r>", "Removes nearby fluids within radius R.");
        add("Utility", "/we_fixwater", "/we_fixwater <r>", "Fills partial water blocks within R.");
        add("Utility", "/we_fixlava", "/we_fixlava <r>", "Fills partial lava blocks within R.");
        add("Utility", "/we_snow", "/we_snow <r>", "Places snow layers on open ground within R.");
        add("Utility", "/we_thaw", "/we_thaw <r>", "Removes snow and ice within R.");
        add("Utility", "/we_ex", "/we_ex <r>", "Extinguishes any fire within R blocks.");
        add("Utility", "/we_removeabove", "/we_removeabove [size] [h]",
            "Removes blocks above your head within a size x height column.",
            "/we_removeabove 3 50");
        add("Utility", "/we_removebelow", "/we_removebelow [size]",
            "Removes blocks below your feet.");
        add("Utility", "/we_removenear", "/we_removenear <block> <r>",
            "Removes occurrences of a block within radius R.");
        add("Utility", "/we_replacenear", "/we_replacenear <r> <from> <to>",
            "Replaces blocks near you.");
        add("Utility", "/we_butcher", "/we_butcher [r]", "Kills hostile mobs near you.");
        add("Utility", "/we_remove", "/we_remove <type> <r>",
            "Removes entities by type (items, arrows, xp, paintings, itemframes, boats, minecarts).");
        add("Utility", "/we_help", "/we_help", "Refers you to the in-game wiki tab.");

        // --- Navigation ---
        add("Navigation", "/we_unstuck", "/we_unstuck", "Pops you up to the next air pocket above.");
        add("Navigation", "/we_ascend", "/we_ascend [n]", "Teleports up N floors.");
        add("Navigation", "/we_descend", "/we_descend [n]", "Teleports down N floors.");
        add("Navigation", "/we_ceil", "/we_ceil [offset]", "Teleports to just below the ceiling.");
        add("Navigation", "/we_thru", "/we_thru", "Passes through a nearby wall.");
        add("Navigation", "/we_up", "/we_up <n>", "Teleports straight up N blocks.");
        add("Navigation", "/we_jumpto", "/we_jumpto", "Teleports you to where you are looking.");

        // --- Chunk ---
        add("Chunk", "/we_chunkinfo", "/we_chunkinfo", "Shows your current chunk coords.");
        add("Chunk", "/we_listchunks", "/we_listchunks", "Lists every chunk that overlaps your selection.");

        // --- Biome ---
        add("Biome", "/we_biomeinfo", "/we_biomeinfo", "Shows the biome at your location.");
        add("Biome", "/we_setbiome", "/we_setbiome <id>",
            "Biome mutation support varies by Minecraft version; see notes.");
        add("Biome", "/we_biomelist", "/we_biomelist", "Lists known biomes in this world.");

        // --- Schematic alias ---
        add("Schematic", "/we_schematic", "/we_schematic <save|load|list|delete> [name]",
            "Long-form alias for /we_schem.",
            "/we_schematic save my_castle");

        // --- Presets ---
        add("Presets", "/we_clear", "/we_clear",
            "Sets the entire selection to air. Same as /we_set air.",
            "/we_clear");
        add("Presets", "/we_flatten", "/we_flatten [pattern]",
            "Keeps only the bottom layer of the selection (filled with the pattern, default grass_block); everything above becomes air. Useful for quickly preparing a build site.",
            "/we_flatten",
            "/we_flatten stone",
            "/we_flatten 70%grass_block,30%dirt");
        add("Presets", "/we_hill", "/we_hill [maxHeight] [pattern]",
            "Sculpts a natural-looking hill inside the selection footprint using a cosine falloff plus per-column noise. Interior is dirt, surface is the pattern (default grass_block). maxHeight defaults to 8.",
            "/we_hill",
            "/we_hill 12",
            "/we_hill 20 grass_block",
            "/we_hill 6 sand");
        add("Presets", "/we_terraform", "/we_terraform [pattern]",
            "Clears the entire selection then overlays the pattern on the new floor. Equivalent to /we_clear plus /we_overlay <pattern>.",
            "/we_terraform grass_block",
            "/we_terraform sandstone");

        // --- Block IDs reference ---
        addRef("Building basics",
            "stone, cobblestone, smooth_stone, granite, andesite, diorite, deepslate, polished_deepslate, mossy_cobblestone, calcite, tuff");
        addRef("Wood logs / planks",
            "oak_log, oak_planks, spruce_log, spruce_planks, birch_log, jungle_log, acacia_log, dark_oak_log, mangrove_log, cherry_log, crimson_stem, warped_stem");
        addRef("Dirt / grass / sand",
            "grass_block, dirt, coarse_dirt, podzol, mycelium, rooted_dirt, mud, packed_mud, sand, red_sand, gravel, clay");
        addRef("Concrete / wool (color prefix)",
            "white_concrete, orange_concrete, ... (16 vanilla colors). Same prefixes work for: wool, terracotta, glazed_terracotta, stained_glass, candle, bed, banner, carpet, shulker_box");
        addRef("Glass",
            "glass, tinted_glass, glass_pane, white_stained_glass (...all colors)");
        addRef("Metals / minerals",
            "iron_block, gold_block, diamond_block, emerald_block, netherite_block, copper_block, raw_iron_block, raw_gold_block, raw_copper_block, redstone_block, lapis_block");
        addRef("Lights",
            "torch, lantern, soul_torch, soul_lantern, glowstone, sea_lantern, shroomlight, ochre_froglight, verdant_froglight, pearlescent_froglight, redstone_lamp, jack_o_lantern");
        addRef("Liquids / utility",
            "water, lava, ice, packed_ice, blue_ice, snow_block, snow, air, cave_air, void_air, barrier, structure_void");
        addRef("Plants / decoration",
            "oak_leaves, grass (small plant), tall_grass, fern, large_fern, dandelion, poppy, sunflower, vine, lily_pad, sea_pickle, kelp, cactus, sugar_cane, bamboo");
        addRef("Nether",
            "netherrack, nether_bricks, soul_sand, soul_soil, glowstone, magma_block, blackstone, basalt, polished_basalt, smooth_basalt, crying_obsidian");
        addRef("End",
            "end_stone, end_stone_bricks, purpur_block, end_rod, chorus_plant, chorus_flower, dragon_egg, obsidian");
        addRef("Pattern + mask syntax",
            "Single block: stone. Weighted mix: 50pct_stone,50pct_cobblestone (use percent signs). Namespaced: minecraft:dirt. Percentages must sum to 100. Masks: hash-existing (any non-air), bang-stone (anything except stone), pipe = OR, ampersand = AND, dollar-plains = biome.");
    }

    private static void addRef(String name, String examples) {
        ENTRIES.add(new Entry("Block IDs", name, "(reference)", examples, new String[0]));
    }

    private static void add(String cat, String name, String syntax, String desc, String... examples) {
        ENTRIES.add(new Entry(cat, name, syntax, desc, examples));
    }

    public static List<Entry> all() { return Collections.unmodifiableList(ENTRIES); }

    /** Filtered by category or free-text search. */
    public static List<Entry> filter(String category, String search) {
        String needle = search == null ? "" : search.trim().toLowerCase();
        List<Entry> out = new ArrayList<>();
        for (Entry e : ENTRIES) {
            if (category != null && !category.isEmpty() && !category.equals("All") && !category.equalsIgnoreCase(e.category())) continue;
            if (!needle.isEmpty()) {
                boolean match = e.name().toLowerCase().contains(needle)
                             || e.description().toLowerCase().contains(needle)
                             || e.syntax().toLowerCase().contains(needle);
                if (!match) continue;
            }
            out.add(e);
        }
        return out;
    }
}

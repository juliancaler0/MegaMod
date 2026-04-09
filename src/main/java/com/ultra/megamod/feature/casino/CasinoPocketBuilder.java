package com.ultra.megamod.feature.casino;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.dimensions.DimensionRegistry;
import com.ultra.megamod.feature.furniture.FurnitureRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Builds a detailed casino in the pocket dimension with all decoration furniture.
 *
 * Layout (50 wide x 60 deep):
 *   Grand Entrance:     x=10-40, z=0-12  (reception, signs, barriers)
 *   Slot Floor:         x=2-22,  z=14-38 (themed slot aisles with decorative machines)
 *   Card Zone:          x=28-48, z=14-38 (blackjack, craps, roulette, baccarat)
 *   Wheel Zone:         x=5-45,  z=40-55 (wheel + lounge seating)
 *   VIP Lounge:         x=5-45,  z=55-60 (sofas, vending, ashtrays)
 */
public class CasinoPocketBuilder {

    private static final int WALL_H = 8;
    private static final int CEIL_Y = 8;
    private static final int W = 50;  // total width
    private static final int D = 60;  // total depth

    public static void buildCasino(ServerLevel level, BlockPos origin) {
        long startTime = System.currentTimeMillis();
        int ox = origin.getX(), oy = origin.getY(), oz = origin.getZ();

        BlockState floor = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        BlockState floorAccent = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        BlockState wall = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState wallAccent = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState ceiling = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState glow = Blocks.GLOWSTONE.defaultBlockState();
        BlockState carpet = Blocks.RED_CARPET.defaultBlockState();
        BlockState goldCarpet = Blocks.YELLOW_CARPET.defaultBlockState();
        BlockState purpleCarpet = Blocks.PURPLE_CARPET.defaultBlockState();
        BlockState gold = Blocks.GOLD_BLOCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState portal = DimensionRegistry.PORTAL_BLOCK.get().defaultBlockState();

        // === SHELL: floor, walls, ceiling for entire area ===
        for (int x = 0; x < W; x++) {
            for (int z = 0; z < D; z++) {
                // Foundation
                set(level, ox + x, oy - 1, oz + z, Blocks.DEEPSLATE.defaultBlockState());
                // Floor: checkerboard blackstone pattern
                boolean accent = (x + z) % 6 == 0;
                set(level, ox + x, oy, oz + z, accent ? floorAccent : floor);

                // Ceiling with glowstone lighting grid
                boolean isGlow = (x % 3 == 0) && (z % 3 == 0);
                set(level, ox + x, oy + CEIL_Y, oz + z, isGlow ? glow : ceiling);

                // Walls and interior
                boolean isWall = (x == 0 || x == W - 1 || z == 0 || z == D - 1);
                for (int y = 1; y < CEIL_Y; y++) {
                    if (isWall) {
                        set(level, ox + x, oy + y, oz + z, y == 3 || y == 5 ? wallAccent : wall);
                    } else {
                        set(level, ox + x, oy + y, oz + z, air);
                    }
                }
            }
        }

        // === CARPET ===
        // Center aisle
        for (int z = 1; z < D - 1; z++) {
            for (int x = 23; x <= 27; x++) set(level, ox + x, oy + 1, oz + z, carpet);
        }
        // Entrance red carpet
        for (int x = 15; x <= 35; x++) {
            for (int z = 1; z <= 10; z++) set(level, ox + x, oy + 1, oz + z, carpet);
        }
        // Slot zone purple carpet aisles
        for (int z = 14; z <= 37; z++) {
            for (int x = 5; x <= 6; x++) set(level, ox + x, oy + 1, oz + z, purpleCarpet);
            for (int x = 11; x <= 12; x++) set(level, ox + x, oy + 1, oz + z, purpleCarpet);
            for (int x = 17; x <= 18; x++) set(level, ox + x, oy + 1, oz + z, purpleCarpet);
        }
        // VIP lounge gold carpet
        for (int x = 6; x <= 44; x++) {
            for (int z = 55; z <= 58; z++) set(level, ox + x, oy + 1, oz + z, goldCarpet);
        }

        // === PORTAL (entrance, z=1) ===
        for (int y = 1; y <= 4; y++) { set(level, ox + 23, oy + y, oz + 1, gold); set(level, ox + 27, oy + y, oz + 1, gold); }
        for (int x = 24; x <= 26; x++) { set(level, ox + x, oy + 4, oz + 1, gold); }
        for (int x = 24; x <= 26; x++) { for (int y = 1; y <= 3; y++) set(level, ox + x, oy + y, oz + 1, portal); }

        // === CASHIER AREA ===
        // Barriers flanking each cashier (clean look, no ropes)
        placeDecor(level, ox + 17, oy + 1, oz + 5, FurnitureRegistry.CASINO_BARRIER.get(), Direction.EAST);
        placeDecor(level, ox + 21, oy + 1, oz + 5, FurnitureRegistry.CASINO_BARRIER.get(), Direction.WEST);
        placeDecor(level, ox + 29, oy + 1, oz + 5, FurnitureRegistry.CASINO_BARRIER.get(), Direction.EAST);
        placeDecor(level, ox + 33, oy + 1, oz + 5, FurnitureRegistry.CASINO_BARRIER.get(), Direction.WEST);
        spawnCashier(level, new BlockPos(ox + 19, oy, oz + 5), "Cashier Anna");
        spawnCashier(level, new BlockPos(ox + 31, oy, oz + 5), "Cashier Mark");
        // Cashier tables rotated to face NORTH toward approaching players
        placeDecor(level, ox + 19, oy + 1, oz + 6, FurnitureRegistry.CASINO_TABLE_BLANK.get(), Direction.NORTH);
        placeDecor(level, ox + 31, oy + 1, oz + 6, FurnitureRegistry.CASINO_TABLE_BLANK.get(), Direction.NORTH);
        // Chip sets beside cashiers (not on the table — doesn't block interaction)
        placeDecor(level, ox + 21, oy + 1, oz + 6, FurnitureRegistry.CASINO2_CHIP_SET.get(), Direction.NORTH);
        placeDecor(level, ox + 29, oy + 1, oz + 6, FurnitureRegistry.CASINO2_CHIP_SET.get(), Direction.NORTH);

        // === ATM AREA (near entrance, flanking the cashiers) — functional ATMs ===
        placeDecor(level, ox + 15, oy + 1, oz + 5, com.ultra.megamod.feature.computer.ComputerRegistry.ATM_BLOCK.get(), Direction.SOUTH);
        placeDecor(level, ox + 35, oy + 1, oz + 5, com.ultra.megamod.feature.computer.ComputerRegistry.ATM_BLOCK.get(), Direction.SOUTH);
        placeDecor(level, ox + 15, oy + 3, oz + 5, FurnitureRegistry.BANK_SIGN.get(), Direction.SOUTH);
        placeDecor(level, ox + 35, oy + 3, oz + 5, FurnitureRegistry.BANK_SIGN.get(), Direction.SOUTH);

        // === ENTRANCE DECORATIONS ===
        placeDecor(level, ox + 25, oy + 5, oz + 2, FurnitureRegistry.CASINO_MONITOR.get(), Direction.SOUTH);
        for (int x = 16; x <= 22; x += 2) placeDecor(level, ox + x, oy + 1, oz + 3, FurnitureRegistry.CASINO_BARRIER.get(), Direction.EAST);
        for (int x = 28; x <= 34; x += 2) placeDecor(level, ox + x, oy + 1, oz + 3, FurnitureRegistry.CASINO_BARRIER.get(), Direction.WEST);
        placeDecor(level, ox + 14, oy + 1, oz + 2, FurnitureRegistry.CASINO_POT_TREE.get(), Direction.SOUTH);
        placeDecor(level, ox + 36, oy + 1, oz + 2, FurnitureRegistry.CASINO_POT_TREE.get(), Direction.SOUTH);
        placeDecor(level, ox + 18, oy + 4, oz + 1, FurnitureRegistry.CASINO2_LADY_LED_SIGN.get(), Direction.SOUTH);
        placeDecor(level, ox + 32, oy + 4, oz + 1, FurnitureRegistry.CASINO2_LED_SIGN.get(), Direction.SOUTH);
        placeDecor(level, ox + 25, oy + 6, oz + 2, FurnitureRegistry.CASINO2_POKER_SIGN.get(), Direction.SOUTH);

        // ================================================================
        //  SLOT FLOOR (x=2-22, z=14-38)
        //  Themed aisles with real + decorative machines, LED signs, barriers
        // ================================================================
        // Slot states facing into each aisle (walkway)
        BlockState slotEast = CasinoRegistry.SLOT_MACHINE.get().defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, Direction.EAST);
        BlockState slotWest = CasinoRegistry.SLOT_MACHINE.get().defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, Direction.WEST);

        // --- Aisle 1 (x=2): Left wall row facing EAST into walkway ---
        for (int z = 15; z <= 35; z += 4) {
            set(level, ox + 2, oy + 1, oz + z, slotEast);
        }
        placeDecor(level, ox + 2, oy + 2, oz + 17, FurnitureRegistry.CASINO2_BUFFALO_SLOT_MACHINE.get(), Direction.EAST);
        placeDecor(level, ox + 2, oy + 2, oz + 21, FurnitureRegistry.CASINO2_VENEZIA_SLOT.get(), Direction.EAST);
        placeDecor(level, ox + 2, oy + 2, oz + 25, FurnitureRegistry.CASINO_GAME_BIGWIN.get(), Direction.EAST);
        placeDecor(level, ox + 2, oy + 2, oz + 29, FurnitureRegistry.CASINO2_MADONNA_GAMBLING_MACHINE.get(), Direction.EAST);
        placeDecor(level, ox + 2, oy + 2, oz + 33, FurnitureRegistry.CASINO2_GAMBLING_GAME_MACHINE.get(), Direction.EAST);

        // LED sign above aisle 1
        placeDecor(level, ox + 3, oy + 5, oz + 14, FurnitureRegistry.CASINO2_LED_SIGN.get(), Direction.SOUTH);

        // --- Aisle 2 (x=7-9): Back-to-back row ---
        // x=7 faces WEST into walkway between aisle 1 and 2
        for (int z = 15; z <= 35; z += 4) {
            set(level, ox + 7, oy + 1, oz + z, slotWest);
        }
        // x=9 faces EAST into walkway between aisle 2 and 3
        for (int z = 15; z <= 35; z += 4) {
            set(level, ox + 9, oy + 1, oz + z, slotEast);
        }
        placeDecor(level, ox + 8, oy + 1, oz + 16, FurnitureRegistry.CASINO_POT_TREE.get(), Direction.SOUTH);
        placeDecor(level, ox + 8, oy + 1, oz + 24, FurnitureRegistry.CASINO_BARRIER.get(), Direction.SOUTH);
        placeDecor(level, ox + 8, oy + 1, oz + 32, FurnitureRegistry.CASINO_POT_TREE.get(), Direction.SOUTH);

        // --- Aisle 3 (x=13-15): Back-to-back row ---
        // x=13 faces WEST into walkway between aisle 2 and 3
        for (int z = 15; z <= 35; z += 4) {
            set(level, ox + 13, oy + 1, oz + z, slotWest);
        }
        // x=15 faces EAST into walkway between aisle 3 and 4
        for (int z = 15; z <= 35; z += 4) {
            set(level, ox + 15, oy + 1, oz + z, slotEast);
        }
        placeDecor(level, ox + 14, oy + 1, oz + 16, FurnitureRegistry.CASINO_BARRIER.get(), Direction.SOUTH);
        placeDecor(level, ox + 14, oy + 1, oz + 24, FurnitureRegistry.CASINO_POT_TREE.get(), Direction.SOUTH);
        placeDecor(level, ox + 14, oy + 1, oz + 32, FurnitureRegistry.CASINO_BARRIER.get(), Direction.SOUTH);

        // --- Aisle 4 (x=19): Right row facing WEST into walkway ---
        for (int z = 15; z <= 35; z += 4) {
            set(level, ox + 19, oy + 1, oz + z, slotWest);
        }
        placeDecor(level, ox + 21, oy + 2, oz + 17, FurnitureRegistry.CASINO2_BUFFALO_SLOT_MACHINE.get(), Direction.WEST);
        placeDecor(level, ox + 21, oy + 2, oz + 21, FurnitureRegistry.CASINO_GAME_SLOT.get(), Direction.WEST);
        placeDecor(level, ox + 21, oy + 2, oz + 25, FurnitureRegistry.CASINO2_VENEZIA_SLOT.get(), Direction.WEST);
        placeDecor(level, ox + 21, oy + 2, oz + 29, FurnitureRegistry.CASINO2_GAMBLING_GAME_MACHINE.get(), Direction.WEST);
        placeDecor(level, ox + 21, oy + 2, oz + 33, FurnitureRegistry.CASINO_GAME_BIGWIN.get(), Direction.WEST);

        // LED signs above slot floor
        placeDecor(level, ox + 10, oy + 5, oz + 14, FurnitureRegistry.CASINO2_LADY_LED_SIGN.get(), Direction.SOUTH);
        placeDecor(level, ox + 19, oy + 5, oz + 14, FurnitureRegistry.CASINO2_LED_SIGN.get(), Direction.SOUTH);

        // Slot floor end caps: barriers + vending machines (on top of floor block = oy+2)
        placeDecor(level, ox + 1, oy + 1, oz + 14, FurnitureRegistry.CASINO_POT_TREE.get(), Direction.SOUTH);
        placeDecor(level, ox + 22, oy + 1, oz + 14, FurnitureRegistry.CASINO_POT_TREE.get(), Direction.SOUTH);
        placeDecor(level, ox + 1, oy + 2, oz + 37, FurnitureRegistry.CASINO_VENDING_MACHINE.get(), Direction.EAST);
        placeDecor(level, ox + 22, oy + 2, oz + 37, FurnitureRegistry.CASINO_VENDING_MACHINE.get(), Direction.WEST);

        // Barrier between slot zone and center aisle
        for (int z = 13; z <= 13; z++) {
            placeDecor(level, ox + 22, oy + 1, oz + z, FurnitureRegistry.CASINO_BARRIER.get(), Direction.SOUTH);
        }

        // ================================================================
        //  CARD ZONE (x=29-48, z=14-38)
        // ================================================================
        BlockState bjTable = CasinoRegistry.BLACKJACK_TABLE.get().defaultBlockState();
        BlockState bjModel = FurnitureRegistry.CASINO_TABLE_BLACKJACK.get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH);
        BlockState bjChair = CasinoRegistry.BLACKJACK_CHAIR.get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);

        // -- Blackjack Table 1 --
        int bj1X = 32, bj1Z = 16;
        set(level, ox + bj1X, oy, oz + bj1Z, bjTable);
        set(level, ox + bj1X, oy + 1, oz + bj1Z, bjModel);
        for (int dx = -2; dx <= 2; dx++) set(level, ox + bj1X + dx, oy + 1, oz + bj1Z + 2, bjChair);
        placeDecor(level, ox + bj1X - 1, oy + 2, oz + bj1Z, FurnitureRegistry.CASINO2_CHIP_SET.get(), Direction.SOUTH);
        spawnDealer(level, new BlockPos(ox + bj1X, oy, oz + bj1Z - 1), "Dealer Brian");
        spawnLabel(level, new BlockPos(ox + bj1X, oy, oz + bj1Z), "BLACKJACK");

        // -- Blackjack Table 2 --
        int bj2X = 42, bj2Z = 16;
        set(level, ox + bj2X, oy, oz + bj2Z, bjTable);
        set(level, ox + bj2X, oy + 1, oz + bj2Z, bjModel);
        for (int dx = -2; dx <= 2; dx++) set(level, ox + bj2X + dx, oy + 1, oz + bj2Z + 2, bjChair);
        placeDecor(level, ox + bj2X + 1, oy + 2, oz + bj2Z, FurnitureRegistry.CASINO2_STACK_CARD.get(), Direction.SOUTH);
        spawnDealer(level, new BlockPos(ox + bj2X, oy, oz + bj2Z - 1), "Dealer Tom");
        spawnLabel(level, new BlockPos(ox + bj2X, oy, oz + bj2Z), "BLACKJACK");

        // -- Craps Table --
        int crapsX = 32, crapsZ = 24;
        placeDecor(level, ox + crapsX, oy + 1, oz + crapsZ, FurnitureRegistry.CASINO_TABLE_CRAPS.get(), Direction.SOUTH);
        BlockState crapsChair = FurnitureRegistry.CASINO2_RED_ARMREST_CHAIR.get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
        for (int dx = -2; dx <= 2; dx++) set(level, ox + crapsX + dx, oy + 1, oz + crapsZ + 2, crapsChair);
        placeDecor(level, ox + crapsX + 2, oy + 2, oz + crapsZ, FurnitureRegistry.CASINO2_CHIP_SET.get(), Direction.SOUTH);
        spawnDealer(level, new BlockPos(ox + crapsX, oy, oz + crapsZ - 1), "Dealer Dice");
        spawnLabel(level, new BlockPos(ox + crapsX, oy, oz + crapsZ), "CRAPS");

        // -- Roulette Table --
        int rouletteX = 42, rouletteZ = 24;
        placeDecor(level, ox + rouletteX, oy + 1, oz + rouletteZ, FurnitureRegistry.CASINO_TABLE_ROULETTE.get(), Direction.SOUTH);
        BlockState rouletteChair = FurnitureRegistry.CASINO2_YELLOW_ARMREST_CHAIR.get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
        for (int dx = -2; dx <= 2; dx++) set(level, ox + rouletteX + dx, oy + 1, oz + rouletteZ + 2, rouletteChair);
        placeDecor(level, ox + rouletteX - 1, oy + 2, oz + rouletteZ, FurnitureRegistry.CASINO2_ROULETTE.get(), Direction.SOUTH);
        spawnDealer(level, new BlockPos(ox + rouletteX, oy, oz + rouletteZ - 1), "Dealer Roulette");
        spawnLabel(level, new BlockPos(ox + rouletteX, oy, oz + rouletteZ), "ROULETTE");

        // -- Baccarat Table --
        int baccaratX = 37, baccaratZ = 32;
        placeDecor(level, ox + baccaratX, oy + 1, oz + baccaratZ, FurnitureRegistry.CASINO_TABLE_BLANK.get(), Direction.SOUTH);
        BlockState bacChair = FurnitureRegistry.CASINO2_CHAIR_YELLOW.get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
        for (int dx = -2; dx <= 2; dx++) set(level, ox + baccaratX + dx, oy + 1, oz + baccaratZ + 2, bacChair);
        placeDecor(level, ox + baccaratX + 1, oy + 2, oz + baccaratZ, FurnitureRegistry.CASINO2_STACK_CARD.get(), Direction.SOUTH);
        spawnDealer(level, new BlockPos(ox + baccaratX, oy, oz + baccaratZ - 1), "Dealer Baccarat");
        spawnLabel(level, new BlockPos(ox + baccaratX, oy, oz + baccaratZ), "BACCARAT");

        // Card zone decoration
        placeDecor(level, ox + 48, oy + 1, oz + 14, FurnitureRegistry.CASINO_POT_TREE.get(), Direction.SOUTH);
        placeDecor(level, ox + 48, oy + 1, oz + 35, FurnitureRegistry.CASINO_POT_TREE.get(), Direction.SOUTH);
        placeDecor(level, ox + 29, oy + 1, oz + 14, FurnitureRegistry.CASINO_BARRIER.get(), Direction.SOUTH);

        // === WHEEL ZONE (z=40-55, centered) ===
        BlockState wheelState = CasinoRegistry.WHEEL.get().defaultBlockState();
        set(level, ox + 25, oy + 4, oz + 54, wheelState);
        BlockState wChair = CasinoRegistry.WHEEL_CHAIR.get().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH);
        for (int x = 18; x <= 32; x += 2) {
            set(level, ox + x, oy + 1, oz + 48, wChair);
            set(level, ox + x, oy + 1, oz + 46, wChair);
        }
        placeDecor(level, ox + 16, oy + 1, oz + 53, FurnitureRegistry.CASINO_POT_TREE.get(), Direction.SOUTH);
        placeDecor(level, ox + 34, oy + 1, oz + 53, FurnitureRegistry.CASINO_POT_TREE.get(), Direction.SOUTH);
        placeDecor(level, ox + 20, oy + 5, oz + 54, FurnitureRegistry.CASINO2_LED_SIGN.get(), Direction.SOUTH);
        placeDecor(level, ox + 30, oy + 5, oz + 54, FurnitureRegistry.CASINO2_LADY_LED_SIGN.get(), Direction.SOUTH);

        // === VIP LOUNGE (z=55-58) ===
        for (int x = 6; x <= 12; x += 3) placeDecor(level, ox + x, oy + 1, oz + 57, FurnitureRegistry.CASINO_SOFA_RED.get(), Direction.NORTH);
        for (int x = 38; x <= 44; x += 3) placeDecor(level, ox + x, oy + 1, oz + 57, FurnitureRegistry.CASINO_SOFA_RED.get(), Direction.NORTH);
        placeDecor(level, ox + 16, oy + 1, oz + 57, FurnitureRegistry.CASINO_SOFA_RED_SINGLE.get(), Direction.NORTH);
        placeDecor(level, ox + 34, oy + 1, oz + 57, FurnitureRegistry.CASINO_SOFA_RED_SINGLE.get(), Direction.NORTH);
        placeDecor(level, ox + 9, oy + 1, oz + 56, FurnitureRegistry.CASINO_TABLE_WOOD.get(), Direction.SOUTH);
        placeDecor(level, ox + 41, oy + 1, oz + 56, FurnitureRegistry.CASINO_TABLE_WOOD.get(), Direction.SOUTH);
        // Ashtrays beside tables on the floor (not on top — avoids floating)
        placeDecor(level, ox + 10, oy + 1, oz + 56, FurnitureRegistry.CASINO2_ASHTRAY.get(), Direction.SOUTH);
        placeDecor(level, ox + 40, oy + 1, oz + 56, FurnitureRegistry.CASINO2_ASHTRAY.get(), Direction.SOUTH);
        placeDecor(level, ox + 1, oy + 2, oz + 57, FurnitureRegistry.CASINO_VENDING_MACHINE.get(), Direction.EAST);
        placeDecor(level, ox + 48, oy + 2, oz + 57, FurnitureRegistry.CASINO_VENDING_MACHINE.get(), Direction.WEST);

        // === WALL DECORATIONS ===
        placeDecor(level, ox + 8, oy + 4, oz + 0, FurnitureRegistry.CASINO2_WOMAN_PAINTING.get(), Direction.SOUTH);
        placeDecor(level, ox + 42, oy + 4, oz + 0, FurnitureRegistry.CASINO2_WOMAN_PAINTING.get(), Direction.SOUTH);
        placeDecor(level, ox + 1, oy + 1, oz + 35, FurnitureRegistry.CASINO_POT_TREE.get(), Direction.SOUTH);
        placeDecor(level, ox + 48, oy + 1, oz + 45, FurnitureRegistry.CASINO_POT_TREE.get(), Direction.SOUTH);

        long elapsed = System.currentTimeMillis() - startTime;
        MegaMod.LOGGER.info("Casino built in {}ms at origin ({}, {}, {})", elapsed, ox, oy, oz);
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, 3);
    }

    private static void placeDecor(ServerLevel level, int x, int y, int z, net.minecraft.world.level.block.Block block, Direction facing) {
        BlockState state = block.defaultBlockState();
        try {
            state = state.setValue(HorizontalDirectionalBlock.FACING, facing);
        } catch (Exception ignored) {}
        level.setBlock(new BlockPos(x, y, z), state, 3);
    }

    private static void spawnLabel(ServerLevel level, BlockPos tablePos, String gameName) {
        net.minecraft.world.entity.decoration.ArmorStand label =
                new net.minecraft.world.entity.decoration.ArmorStand(net.minecraft.world.entity.EntityType.ARMOR_STAND, level);
        label.setPos(tablePos.getX() + 0.5, tablePos.getY() + 1.5, tablePos.getZ() + 0.5);
        label.setInvisible(true);
        label.setNoGravity(true);
        label.setInvulnerable(true);
        label.setNoBasePlate(true);
        label.setSilent(true);
        label.setCustomName(Component.literal(gameName).withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD));
        label.setCustomNameVisible(true);
        level.addFreshEntity(label);
    }

    private static void spawnCashier(ServerLevel level, BlockPos pos, String name) {
        CashierEntity cashier = new CashierEntity(CasinoRegistry.CASHIER.get(), level);
        cashier.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        cashier.setCustomName(Component.literal(name).withStyle(net.minecraft.ChatFormatting.AQUA));
        cashier.setCustomNameVisible(true);
        cashier.setSilent(true);
        level.addFreshEntity(cashier);
    }

    private static void spawnDealer(ServerLevel level, BlockPos pos, String name) {
        DealerEntity dealer = new DealerEntity(CasinoRegistry.DEALER.get(), level);
        dealer.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        dealer.setCustomName(Component.literal(name).withStyle(net.minecraft.ChatFormatting.GOLD));
        dealer.setCustomNameVisible(true);
        dealer.setSilent(true);
        level.addFreshEntity(dealer);
    }
}

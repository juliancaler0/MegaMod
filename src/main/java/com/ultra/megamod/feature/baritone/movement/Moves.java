package com.ultra.megamod.feature.baritone.movement;

import com.ultra.megamod.feature.baritone.pathfinding.BetterBlockPos;
import com.ultra.megamod.feature.baritone.pathfinding.CalculationContext;
import com.ultra.megamod.feature.baritone.movement.movements.*;

/**
 * Enum of all possible movement types from a given position.
 * Each generates candidate moves for A* expansion.
 */
public enum Moves {
    TRAVERSE_NORTH(0, 0, -1) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementTraverse(ctx, src, src.offset(0, 0, -1));
        }
    },
    TRAVERSE_SOUTH(0, 0, 1) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementTraverse(ctx, src, src.offset(0, 0, 1));
        }
    },
    TRAVERSE_EAST(1, 0, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementTraverse(ctx, src, src.offset(1, 0, 0));
        }
    },
    TRAVERSE_WEST(-1, 0, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementTraverse(ctx, src, src.offset(-1, 0, 0));
        }
    },
    ASCEND_NORTH(0, 1, -1) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementAscend(ctx, src, src.offset(0, 1, -1));
        }
    },
    ASCEND_SOUTH(0, 1, 1) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementAscend(ctx, src, src.offset(0, 1, 1));
        }
    },
    ASCEND_EAST(1, 1, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementAscend(ctx, src, src.offset(1, 1, 0));
        }
    },
    ASCEND_WEST(-1, 1, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementAscend(ctx, src, src.offset(-1, 1, 0));
        }
    },
    DESCEND_NORTH(0, -1, -1) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementDescend(ctx, src, src.offset(0, -1, -1));
        }
    },
    DESCEND_SOUTH(0, -1, 1) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementDescend(ctx, src, src.offset(0, -1, 1));
        }
    },
    DESCEND_EAST(1, -1, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementDescend(ctx, src, src.offset(1, -1, 0));
        }
    },
    DESCEND_WEST(-1, -1, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementDescend(ctx, src, src.offset(-1, -1, 0));
        }
    },
    DIAGONAL_NE(1, 0, -1) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementDiagonal(ctx, src, 1, -1);
        }
    },
    DIAGONAL_NW(-1, 0, -1) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementDiagonal(ctx, src, -1, -1);
        }
    },
    DIAGONAL_SE(1, 0, 1) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementDiagonal(ctx, src, 1, 1);
        }
    },
    DIAGONAL_SW(-1, 0, 1) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementDiagonal(ctx, src, -1, 1);
        }
    },
    PILLAR_UP(0, 1, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementPillar(ctx, src);
        }
    },
    DOWNWARD(0, -1, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementDownward(ctx, src);
        }
    },
    PARKOUR_NORTH(0, 0, -2) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementParkour(ctx, src, 0, -1);
        }
    },
    PARKOUR_SOUTH(0, 0, 2) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementParkour(ctx, src, 0, 1);
        }
    },
    PARKOUR_EAST(2, 0, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementParkour(ctx, src, 1, 0);
        }
    },
    PARKOUR_WEST(-2, 0, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementParkour(ctx, src, -1, 0);
        }
    },
    FALL_NORTH(0, -2, -1) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementFall(ctx, src, findLanding(ctx, src, 0, -1));
        }
    },
    FALL_SOUTH(0, -2, 1) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementFall(ctx, src, findLanding(ctx, src, 0, 1));
        }
    },
    FALL_EAST(1, -2, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementFall(ctx, src, findLanding(ctx, src, 1, 0));
        }
    },
    FALL_WEST(-1, -2, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementFall(ctx, src, findLanding(ctx, src, -1, 0));
        }
    },

    // === New movement types ===

    SWIM_NORTH(0, 0, -1) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementSwim(ctx, src, src.offset(0, 0, -1));
        }
    },
    SWIM_SOUTH(0, 0, 1) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementSwim(ctx, src, src.offset(0, 0, 1));
        }
    },
    SWIM_EAST(1, 0, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementSwim(ctx, src, src.offset(1, 0, 0));
        }
    },
    SWIM_WEST(-1, 0, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementSwim(ctx, src, src.offset(-1, 0, 0));
        }
    },
    CLIMB_UP(0, 1, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementClimb(ctx, src, src.offset(0, 1, 0), true);
        }
    },
    CLIMB_DOWN(0, -1, 0) {
        @Override public Movement apply(CalculationContext ctx, BetterBlockPos src) {
            return new MovementClimb(ctx, src, src.offset(0, -1, 0), false);
        }
    };

    public final int dx, dy, dz;

    Moves(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public abstract Movement apply(CalculationContext ctx, BetterBlockPos src);

    /** Find the landing position for a fall in a given horizontal direction. Scans down up to 4 blocks. */
    private static BetterBlockPos findLanding(CalculationContext ctx, BetterBlockPos src, int dx, int dz) {
        int x = src.x + dx;
        int z = src.z + dz;
        for (int dy = 1; dy <= 4; dy++) {
            int y = src.y - dy;
            net.minecraft.core.BlockPos floor = new net.minecraft.core.BlockPos(x, y - 1, z);
            if (MovementHelper.canWalkOn(ctx, floor)) {
                return new BetterBlockPos(x, y, z);
            }
        }
        // Default: 2 blocks down
        return new BetterBlockPos(x, src.y - 2, z);
    }
}

package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.ETFApi;
import com.ultra.megamod.lib.etf.features.property_reading.properties.optifine_properties.BlocksProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code blockBelowSolid} — nearest solid-render block scanning downward.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class BlockBelowSolidProperty extends BlocksProperty {


    protected BlockBelowSolidProperty(final Properties properties, final int propertyNum, final String[] ids) throws RandomPropertyException {
        super(properties, propertyNum, ids);
    }

    public static BlocksProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new BlockBelowSolidProperty(properties, propertyNum, new String[]{"blockBelowSolid"});
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Override
    protected @Nullable BlockState[] getTestingBlocks(final ETFEntityRenderState entity) {
        if (entity.uuid().getLeastSignificantBits() == ETFApi.ETF_SPAWNER_MARKER) {
            return new BlockState[]{Blocks.SPAWNER.defaultBlockState()};
        } else {
            if (entity.world() == null || entity.blockPos() == null) {
                return null;
            }
            Level world = entity.world();
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            mutablePos.set(entity.blockPos());

            int minBuildHeight = world.getMinY();

            while (minBuildHeight <= mutablePos.getY() && !world.getBlockState(mutablePos).isSolidRender()) {
                mutablePos.move(0, -1, 0);
            }
            if (minBuildHeight > mutablePos.getY()) {
                return null;
            }
            return new BlockState[]{world.getBlockState(mutablePos)};
        }
    }

    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"blockBelowSolid"};
    }
}

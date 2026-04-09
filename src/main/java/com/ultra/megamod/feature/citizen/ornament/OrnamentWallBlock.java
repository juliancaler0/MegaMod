package com.ultra.megamod.feature.citizen.ornament;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Ornament wall block -- uses vanilla WallBlock shape/behavior but supports retexturing.
 */
public class OrnamentWallBlock extends WallBlock implements IMateriallyTexturedBlock, EntityBlock {

    @SuppressWarnings("unchecked")
    private static final MapCodec<WallBlock> CODEC =
            (MapCodec<WallBlock>) (MapCodec<?>) OrnamentWallBlock.simpleCodec(OrnamentWallBlock::new);

    private final OrnamentBlockType ornamentType;
    private List<IMateriallyTexturedBlockComponent> components;

    public OrnamentWallBlock(OrnamentBlockType type, BlockBehaviour.Properties props) {
        super(props);
        this.ornamentType = type;
        buildComponents();
    }

    public OrnamentWallBlock(BlockBehaviour.Properties props) {
        super(props);
        this.ornamentType = null;
    }

    @Override
    public MapCodec<WallBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MateriallyTexturedBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        OrnamentBlockHelper.transferOrnamentData(level, pos, stack);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return OrnamentBlockHelper.getDropsWithData(super.getDrops(state, builder), builder, this);
    }

    @Override
    public List<IMateriallyTexturedBlockComponent> getComponents() {
        if (components == null) buildComponents();
        return components;
    }

    private void buildComponents() {
        components = OrnamentBlockHelper.buildComponents(ornamentType);
    }
}

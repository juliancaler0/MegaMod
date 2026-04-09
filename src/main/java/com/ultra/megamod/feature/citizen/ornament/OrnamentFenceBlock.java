package com.ultra.megamod.feature.citizen.ornament;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Ornament fence block -- uses vanilla FenceBlock shape/behavior but supports retexturing.
 */
public class OrnamentFenceBlock extends FenceBlock implements IMateriallyTexturedBlock, EntityBlock {

    @SuppressWarnings("unchecked")
    private static final MapCodec<FenceBlock> CODEC =
            (MapCodec<FenceBlock>) (MapCodec<?>) OrnamentFenceBlock.simpleCodec(OrnamentFenceBlock::new);

    private final OrnamentBlockType ornamentType;
    private List<IMateriallyTexturedBlockComponent> components;

    public OrnamentFenceBlock(OrnamentBlockType type, BlockBehaviour.Properties props) {
        super(props);
        this.ornamentType = type;
        buildComponents();
    }

    public OrnamentFenceBlock(BlockBehaviour.Properties props) {
        super(props);
        this.ornamentType = null;
    }

    @Override
    public MapCodec<FenceBlock> codec() {
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

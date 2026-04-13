package com.ultra.megamod.feature.combat.archers.block;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.combat.archers.ArchersMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ArcherWorkbenchBlock extends Block {
    public static final MapCodec<ArcherWorkbenchBlock> CODEC = simpleCodec(ArcherWorkbenchBlock::new);
    public static Identifier BLOCK_ID = Identifier.fromNamespaceAndPath(ArchersMod.ID, "archers_workbench");

    public ArcherWorkbenchBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends ArcherWorkbenchBlock> codec() {
        return CODEC;
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag options) {
        tooltip.accept(Component.translatable("block." + BLOCK_ID.getNamespace() + "." + BLOCK_ID.getPath() + ".hint")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    // MARK: Facing

    private static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // MARK: Partial transparency

    public boolean propagatesSkylightDown(BlockState state) {
        return true;
    }
}

package com.ultra.megamod.feature.citizen.colonyblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Block entity for the compost barrel. Takes organic input, processes over 24000 ticks (1 day),
 * progresses through stages 0-9, then produces compost output.
 */
public class TileEntityBarrel extends BlockEntity {

    private static final int TOTAL_TICKS = 24000; // 1 Minecraft day
    private static final int MAX_STAGE = 9;

    private ItemStack input = ItemStack.EMPTY;
    private int timer = 0;
    private boolean composting = false;
    private boolean done = false;

    public TileEntityBarrel(BlockPos pos, BlockState state) {
        super(ColonyBlockRegistry.BARREL_BE.get(), pos, state);
    }

    public void addInput(ItemStack stack) {
        if (!composting && !done) {
            this.input = stack.copyWithCount(1);
            this.composting = true;
            this.timer = 0;
            this.done = false;
            setChanged();
            updateStage(0);
        }
    }

    public boolean isComposting() {
        return composting;
    }

    public boolean isDone() {
        return done;
    }

    public int getStage() {
        if (!composting && !done) return 0;
        if (done) return MAX_STAGE;
        return Math.min(MAX_STAGE, (int) ((float) timer / TOTAL_TICKS * MAX_STAGE));
    }

    public ItemStack collectOutput() {
        if (!done) return ItemStack.EMPTY;
        ItemStack result = ColonyBlockRegistry.COMPOST_ITEM != null
            ? new ItemStack(ColonyBlockRegistry.COMPOST_ITEM.get(), 1)
            : new ItemStack(Items.BONE_MEAL, 2);
        reset();
        return result;
    }

    private void reset() {
        input = ItemStack.EMPTY;
        timer = 0;
        composting = false;
        done = false;
        setChanged();
        updateStage(0);
    }

    private void updateStage(int stage) {
        if (level != null && !level.isClientSide()) {
            BlockState current = level.getBlockState(worldPosition);
            if (current.getBlock() instanceof BlockBarrel) {
                int currentStage = current.getValue(BlockBarrel.STAGE);
                if (currentStage != stage) {
                    level.setBlock(worldPosition, current.setValue(BlockBarrel.STAGE, stage), 3);
                }
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileEntityBarrel be) {
        if (!be.composting || be.done) return;

        be.timer++;
        int newStage = Math.min(MAX_STAGE, (int) ((float) be.timer / TOTAL_TICKS * MAX_STAGE));
        int currentStage = state.getValue(BlockBarrel.STAGE);
        if (newStage != currentStage) {
            be.updateStage(newStage);
        }

        if (be.timer >= TOTAL_TICKS) {
            be.composting = false;
            be.done = true;
            be.updateStage(MAX_STAGE);
            be.setChanged();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Timer", timer);
        output.putBoolean("Composting", composting);
        output.putBoolean("Done", done);
        if (!input.isEmpty()) {
            output.store("Input", ItemStack.CODEC, input);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.timer = input.getIntOr("Timer", 0);
        this.composting = input.getBooleanOr("Composting", false);
        this.done = input.getBooleanOr("Done", false);
        this.input = input.read("Input", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }
}

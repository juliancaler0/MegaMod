package com.ultra.megamod.feature.alchemy.block;

import com.ultra.megamod.feature.alchemy.AlchemyRecipeRegistry;
import com.ultra.megamod.feature.alchemy.AlchemyRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AlchemyGrindstoneBlockEntity extends BlockEntity {

    private String inputItemId = "";
    private String outputItemId = "";
    private int outputCount = 0;
    private int grindingProgress = 0;
    private int grindingTotal = 0;
    private boolean grinding = false;
    private boolean outputReady = false;

    public AlchemyGrindstoneBlockEntity(BlockPos pos, BlockState state) {
        super(AlchemyRegistry.ALCHEMY_GRINDSTONE_BE.get(), pos, state);
    }

    // ==================== Public API ====================

    public boolean insertItem(String itemId, ItemStack stack) {
        if (grinding || outputReady || !inputItemId.isEmpty()) {
            return false;
        }

        AlchemyRecipeRegistry.GrindingRecipe recipe = AlchemyRecipeRegistry.findGrindingRecipe(itemId);
        if (recipe == null) {
            return false;
        }

        this.inputItemId = itemId;
        this.outputItemId = recipe.output();
        this.outputCount = recipe.outputCount();
        this.grindingTotal = recipe.grindTicks();
        this.grindingProgress = 0;
        this.grinding = true;
        setChanged();
        return true;
    }

    public boolean isGrinding() {
        return grinding;
    }

    public int getGrindingProgress() {
        return grindingProgress;
    }

    public int getGrindingTotal() {
        return grindingTotal > 0 ? grindingTotal : 100;
    }

    public boolean hasOutput() {
        return outputReady;
    }

    public ItemStack collectOutput() {
        if (!outputReady || outputItemId.isEmpty()) {
            reset();
            return ItemStack.EMPTY;
        }

        Identifier id = Identifier.tryParse(outputItemId);
        if (id == null) {
            reset();
            return ItemStack.EMPTY;
        }

        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == null) {
            reset();
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(item, outputCount);
        reset();
        return result;
    }

    private void reset() {
        inputItemId = "";
        outputItemId = "";
        outputCount = 0;
        grindingProgress = 0;
        grindingTotal = 0;
        grinding = false;
        outputReady = false;
        setChanged();
    }

    // ==================== Server Tick ====================

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlchemyGrindstoneBlockEntity be) {
        if (level.isClientSide()) return;
        if (!be.grinding) return;

        be.grindingProgress++;

        if (be.grindingProgress >= be.grindingTotal) {
            be.grinding = false;
            be.outputReady = true;
            be.inputItemId = "";
            be.setChanged();
        }
    }

    // ==================== NBT Persistence ====================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("InputItem", inputItemId);
        output.putString("OutputItem", outputItemId);
        output.putInt("OutputCount", outputCount);
        output.putInt("GrindProgress", grindingProgress);
        output.putInt("GrindTotal", grindingTotal);
        output.putBoolean("Grinding", grinding);
        output.putBoolean("OutputReady", outputReady);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inputItemId = input.getStringOr("InputItem", "");
        outputItemId = input.getStringOr("OutputItem", "");
        outputCount = input.getIntOr("OutputCount", 0);
        grindingProgress = input.getIntOr("GrindProgress", 0);
        grindingTotal = input.getIntOr("GrindTotal", 0);
        grinding = input.getBooleanOr("Grinding", false);
        outputReady = input.getBooleanOr("OutputReady", false);
    }
}

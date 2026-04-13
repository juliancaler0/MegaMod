package com.ultra.megamod.feature.combat.runes;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Smithing-style menu for the Rune Crafting Altar.
 * Two input slots (base item + reagent) produce one output via RuneCraftingRecipe.
 * Ported 1:1 from the Runes mod's RuneCraftingScreenHandler.
 */
public class RuneCraftingMenu extends ItemCombinerMenu {

    private final Level level;
    @Nullable
    private RecipeHolder<RuneCraftingRecipe> currentRecipe;

    public RuneCraftingMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, ContainerLevelAccess.NULL);
    }

    public RuneCraftingMenu(int syncId, Inventory playerInventory, ContainerLevelAccess access) {
        super(RuneWorkbenchRegistry.RUNE_CRAFTING_MENU.get(), syncId, playerInventory, access,
                createInputSlotDefinitions());
        this.level = playerInventory.player.level();
    }

    private static ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create()
                .withSlot(0, 27, 47, stack -> true)  // base item
                .withSlot(1, 76, 47, stack -> true)  // reagent/addition
                .withResultSlot(2, 134, 47)
                .build();
    }

    @Override
    protected boolean mayPickup(Player player, boolean hasStack) {
        return this.currentRecipe != null
                && this.currentRecipe.value().matches(this.createRecipeInput(), this.level);
    }

    @Override
    protected void onTake(Player player, ItemStack stack) {
        stack.onCraftedBy(player, stack.getCount());
        this.resultSlots.awardUsedRecipes(player, this.getRelevantItems());

        shrinkInput(0);
        shrinkInput(1);

        // Play rune crafting sound with timing control
        var runeCrafter = (RuneCrafter) player;
        if (runeCrafter.shouldPlayRuneCraftingSound(player.tickCount)) {
            this.access.execute((level, pos) -> {
                level.playSound(null, pos, RuneCrafting.SOUND.get(), SoundSource.BLOCKS,
                        level.random.nextFloat() * 0.1F + 0.9F, 1.0F);
            });
            runeCrafter.onPlayedRuneCraftingSound(player.tickCount);
        }
    }

    private RuneCraftingRecipeInput createRecipeInput() {
        return new RuneCraftingRecipeInput(this.inputSlots.getItem(0), this.inputSlots.getItem(1));
    }

    private List<ItemStack> getRelevantItems() {
        return List.of(this.inputSlots.getItem(0), this.inputSlots.getItem(1));
    }

    private void shrinkInput(int slot) {
        ItemStack stack = this.inputSlots.getItem(slot);
        if (!stack.isEmpty()) {
            stack.shrink(1);
            this.inputSlots.setItem(slot, stack);
        }
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() instanceof RuneCraftingBlock;
    }

    @Override
    public void createResult() {
        var recipeInput = this.createRecipeInput();
        if (!(this.level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        var result = serverLevel.recipeAccess()
                .getRecipeFor(RuneCrafting.RECIPE_TYPE.get(), recipeInput, this.level);

        if (result.isPresent()) {
            var recipeHolder = result.get();
            ItemStack itemStack = recipeHolder.value().assemble(recipeInput, this.level.registryAccess());
            this.currentRecipe = recipeHolder;
            this.resultSlots.setRecipeUsed(recipeHolder);
            this.resultSlots.setItem(0, itemStack);
        } else {
            this.currentRecipe = null;
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(stack, slot);
    }
}

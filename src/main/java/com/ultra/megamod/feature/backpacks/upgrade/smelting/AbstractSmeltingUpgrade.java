package com.ultra.megamod.feature.backpacks.upgrade.smelting;

import com.ultra.megamod.feature.backpacks.upgrade.BackpackUpgrade;
import com.ultra.megamod.feature.backpacks.upgrade.ITickableUpgrade;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract smelting upgrade for backpacks.
 * Provides input (slot 0), fuel (slot 1), and result (slot 2) slots.
 * Subclasses define id, display name, and cook time.
 */
public abstract class AbstractSmeltingUpgrade extends BackpackUpgrade implements ITickableUpgrade {

    /** Slot indices */
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int RESULT_SLOT = 2;

    /** Hardcoded fuel burn durations (in ticks). Covers common fuels. */
    private static final Map<Item, Integer> FUEL_DURATIONS = new HashMap<>();
    static {
        FUEL_DURATIONS.put(Items.COAL, 1600);
        FUEL_DURATIONS.put(Items.CHARCOAL, 1600);
        FUEL_DURATIONS.put(Items.COAL_BLOCK, 16000);
        FUEL_DURATIONS.put(Items.OAK_LOG, 300);
        FUEL_DURATIONS.put(Items.SPRUCE_LOG, 300);
        FUEL_DURATIONS.put(Items.BIRCH_LOG, 300);
        FUEL_DURATIONS.put(Items.JUNGLE_LOG, 300);
        FUEL_DURATIONS.put(Items.ACACIA_LOG, 300);
        FUEL_DURATIONS.put(Items.DARK_OAK_LOG, 300);
        FUEL_DURATIONS.put(Items.MANGROVE_LOG, 300);
        FUEL_DURATIONS.put(Items.CHERRY_LOG, 300);
        FUEL_DURATIONS.put(Items.OAK_PLANKS, 300);
        FUEL_DURATIONS.put(Items.SPRUCE_PLANKS, 300);
        FUEL_DURATIONS.put(Items.BIRCH_PLANKS, 300);
        FUEL_DURATIONS.put(Items.JUNGLE_PLANKS, 300);
        FUEL_DURATIONS.put(Items.ACACIA_PLANKS, 300);
        FUEL_DURATIONS.put(Items.DARK_OAK_PLANKS, 300);
        FUEL_DURATIONS.put(Items.MANGROVE_PLANKS, 300);
        FUEL_DURATIONS.put(Items.CHERRY_PLANKS, 300);
        FUEL_DURATIONS.put(Items.BAMBOO_PLANKS, 300);
        FUEL_DURATIONS.put(Items.STICK, 100);
        FUEL_DURATIONS.put(Items.LAVA_BUCKET, 20000);
        FUEL_DURATIONS.put(Items.BLAZE_ROD, 2400);
        FUEL_DURATIONS.put(Items.DRIED_KELP_BLOCK, 4001);
        FUEL_DURATIONS.put(Items.BAMBOO, 50);
    }

    protected int burnTime = 0;
    protected int cookTime = 0;
    protected int totalCookTime = 200;

    /** Internal container holding input, fuel, and result items */
    private final SimpleContainer smeltingContainer = new SimpleContainer(3);

    /**
     * Return the RecipeType this upgrade uses for lookups.
     * SMELTING for furnace, SMOKING for smoker, BLASTING for blast furnace.
     */
    public abstract RecipeType<? extends AbstractCookingRecipe> getRecipeType();

    @Override
    public int getSlotCount() {
        return 3;
    }

    @Override
    public SimpleContainer createContainer() {
        return new SimpleContainer(getSlotCount());
    }

    @Override
    public List<Slot> createSlots(SimpleContainer container, int baseX, int baseY) {
        List<Slot> slots = new ArrayList<>();
        // Input slot
        slots.add(new Slot(container, INPUT_SLOT, baseX, baseY));
        // Fuel slot
        slots.add(new Slot(container, FUEL_SLOT, baseX, baseY + 36));
        // Result slot
        slots.add(new Slot(container, RESULT_SLOT, baseX + 56, baseY + 18));
        return slots;
    }

    @Override
    public int getTickRate() {
        return 1;
    }

    @Override
    public void tick(ServerPlayer player, ServerLevel level) {
        if (!isActive()) return;

        ItemStack input = smeltingContainer.getItem(INPUT_SLOT);
        ItemStack fuel = smeltingContainer.getItem(FUEL_SLOT);
        ItemStack result = smeltingContainer.getItem(RESULT_SLOT);

        // Decrement burn time
        if (burnTime > 0) {
            burnTime--;
        }

        // Look up what the input would smelt into
        ItemStack recipeResult = getRecipeResult(input, level);

        // If not burning and we have a valid smeltable input, try to consume fuel
        if (burnTime <= 0 && !recipeResult.isEmpty()) {
            if (!fuel.isEmpty() && isFuel(fuel)) {
                burnTime = getBurnDuration(fuel);
                if (burnTime > 0) {
                    fuel.shrink(1);
                    if (fuel.isEmpty()) {
                        smeltingContainer.setItem(FUEL_SLOT, ItemStack.EMPTY);
                    }
                }
            }
        }

        // If burning and has a valid smeltable input, increment cook time
        if (burnTime > 0 && !recipeResult.isEmpty()) {
            // Check result slot can accept the recipe output
            if (canSmelt(recipeResult, result)) {
                cookTime++;
                if (cookTime >= totalCookTime) {
                    cookTime = 0;
                    // Perform the smelt with the looked-up recipe result
                    performSmelt(input, result, recipeResult);
                }
            } else {
                // Can't smelt (result full or incompatible), reset cook progress
                cookTime = 0;
            }
        } else {
            // Not burning or no valid recipe, reset cook time
            if (cookTime > 0) {
                cookTime = 0;
            }
        }
    }

    /**
     * Look up the smelting recipe result for the given input using vanilla's RecipeManager.
     * Returns ItemStack.EMPTY if no matching recipe exists.
     */
    @SuppressWarnings("unchecked")
    private ItemStack getRecipeResult(ItemStack input, ServerLevel level) {
        if (input.isEmpty()) return ItemStack.EMPTY;

        SingleRecipeInput singleInput = new SingleRecipeInput(input);
        Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> recipe =
                level.getServer().getRecipeManager().getRecipeFor(
                        (RecipeType) getRecipeType(), singleInput, level);

        if (recipe.isPresent()) {
            return recipe.get().value().assemble(singleInput, level.registryAccess()).copy();
        }
        return ItemStack.EMPTY;
    }

    /**
     * Check if the recipe result can be placed into the result slot.
     * Result slot must be empty, or contain the same item with room for more.
     */
    private boolean canSmelt(ItemStack recipeResult, ItemStack currentResult) {
        if (recipeResult.isEmpty()) return false;
        if (currentResult.isEmpty()) return true;
        return ItemStack.isSameItem(recipeResult, currentResult)
                && currentResult.getCount() + recipeResult.getCount() <= currentResult.getMaxStackSize();
    }

    /**
     * Perform the smelt operation: shrink input by 1 and place the recipe result into the result slot.
     */
    private void performSmelt(ItemStack input, ItemStack currentResult, ItemStack recipeResult) {
        if (currentResult.isEmpty()) {
            smeltingContainer.setItem(RESULT_SLOT, recipeResult.copy());
        } else {
            currentResult.grow(recipeResult.getCount());
        }
        input.shrink(1);
        if (input.isEmpty()) {
            smeltingContainer.setItem(INPUT_SLOT, ItemStack.EMPTY);
        }
    }

    /**
     * Check if an item is valid fuel.
     */
    private boolean isFuel(ItemStack stack) {
        return FUEL_DURATIONS.containsKey(stack.getItem());
    }

    /**
     * Get the burn duration for a fuel item in ticks.
     */
    private int getBurnDuration(ItemStack stack) {
        return FUEL_DURATIONS.getOrDefault(stack.getItem(), 0);
    }

    @Override
    public List<ItemStack> onRemoved() {
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < smeltingContainer.getContainerSize(); i++) {
            ItemStack stack = smeltingContainer.getItem(i);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        smeltingContainer.clearContent();
        burnTime = 0;
        cookTime = 0;
        return drops;
    }

    @Override
    public void saveToTag(CompoundTag tag) {
        super.saveToTag(tag);

        tag.putInt("BurnTime", burnTime);
        tag.putInt("CookTime", cookTime);
        tag.putInt("TotalCookTime", totalCookTime);

        ListTag slotItems = new ListTag();
        for (int i = 0; i < smeltingContainer.getContainerSize(); i++) {
            ItemStack stack = smeltingContainer.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemTag.putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                itemTag.putInt("count", stack.getCount());
                slotItems.add(itemTag);
            }
        }
        tag.put("SmeltingSlots", slotItems);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        super.loadFromTag(tag);

        this.burnTime = tag.getIntOr("BurnTime", 0);
        this.cookTime = tag.getIntOr("CookTime", 0);
        this.totalCookTime = tag.getIntOr("TotalCookTime", totalCookTime);

        smeltingContainer.clearContent();
        if (tag.contains("SmeltingSlots")) {
            ListTag slotItems = tag.getListOrEmpty("SmeltingSlots");
            for (int i = 0; i < slotItems.size(); i++) {
                net.minecraft.nbt.Tag entry = slotItems.get(i);
                if (entry instanceof CompoundTag itemTag) {
                    int slot = itemTag.getIntOr("Slot", -1);
                    if (slot >= 0 && slot < 3) {
                        String itemId = itemTag.getStringOr("id", "");
                        int count = itemTag.getIntOr("count", 1);
                        if (!itemId.isEmpty()) {
                            Identifier id = Identifier.parse(itemId);
                            net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.getValue(id);
                            if (item != null && item != Items.AIR) {
                                smeltingContainer.setItem(slot, new ItemStack(item, count));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the internal smelting container for direct access.
     */
    public SimpleContainer getSmeltingContainer() {
        return smeltingContainer;
    }

    /** Current burn time remaining. */
    public int getBurnTime() {
        return burnTime;
    }

    /** Current cook progress. */
    public int getCookTime() {
        return cookTime;
    }

    /** Total ticks needed to smelt one item. */
    public int getTotalCookTime() {
        return totalCookTime;
    }

    /** Whether the furnace is currently burning fuel. */
    public boolean isBurning() {
        return burnTime > 0;
    }
}

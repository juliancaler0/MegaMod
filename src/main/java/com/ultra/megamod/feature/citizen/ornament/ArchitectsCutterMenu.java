package com.ultra.megamod.feature.citizen.ornament;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Menu for the Architects Cutter crafting station.
 * <p>
 * Layout:
 * - 3 input slots (materials — must be BlockItems)
 * - scrollable output list of matching ornament blocks
 * - 1 output slot
 * <p>
 * When input materials change, the menu recalculates which OrnamentBlockType entries
 * match (component count <= number of non-empty inputs). The player selects from the
 * list, and the output item gets MaterialTextureData attached via NBT on the item's
 * custom tag.
 */
public class ArchitectsCutterMenu extends AbstractContainerMenu {

    public static final int INPUT_SLOT_COUNT = 3;
    public static final int OUTPUT_SLOT_INDEX = 3;

    private final SimpleContainer inputContainer = new SimpleContainer(INPUT_SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            ArchitectsCutterMenu.this.onInputChanged();
        }
    };
    private final ResultContainer outputContainer = new ResultContainer();

    /** All valid output recipes based on current input materials. */
    private final List<OrnamentRecipe> availableRecipes = new ArrayList<>();

    /** Currently selected recipe index. */
    private final DataSlot selectedRecipe = DataSlot.standalone();

    /** Tracks whether the output slot is accessible. */
    private boolean hasValidOutput = false;

    // Server constructor
    public ArchitectsCutterMenu(int containerId, Inventory playerInv) {
        super(OrnamentRegistry.CUTTER_MENU.get(), containerId);
        selectedRecipe.set(-1);
        addDataSlot(selectedRecipe);

        // Input slots — left side, stacked vertically
        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            this.addSlot(new MaterialInputSlot(inputContainer, i, 20, 17 + i * 22));
        }

        // Output slot — right side
        this.addSlot(new OutputSlot(outputContainer, 0, 143, 38));

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 95 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 153));
        }
    }

    // Client constructor (from network)
    public ArchitectsCutterMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv);
        buf.readBlockPos(); // consume the block pos written by server
    }

    // ==================== Recipe Calculation ====================

    /**
     * Called when any input slot changes. Recalculates the available output recipes.
     */
    private void onInputChanged() {
        availableRecipes.clear();
        selectedRecipe.set(-1);
        outputContainer.setItem(0, ItemStack.EMPTY);
        hasValidOutput = false;

        // Collect non-empty input blocks
        List<Block> inputBlocks = new ArrayList<>();
        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            ItemStack stack = inputContainer.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
                inputBlocks.add(blockItem.getBlock());
            }
        }

        if (inputBlocks.isEmpty()) return;

        // Find all ornament types whose component count <= available materials
        int materialCount = inputBlocks.size();
        for (OrnamentBlockType type : OrnamentBlockType.values()) {
            if (type.getComponentCount() <= materialCount) {
                availableRecipes.add(new OrnamentRecipe(type, inputBlocks));
            }
        }
    }

    /**
     * Returns the list of available recipes for the screen to display.
     */
    public List<OrnamentRecipe> getAvailableRecipes() {
        return availableRecipes;
    }

    /**
     * Returns the currently selected recipe index.
     */
    public int getSelectedRecipeIndex() {
        return selectedRecipe.get();
    }

    /**
     * Called when the player clicks on a recipe in the output list.
     */
    public void selectRecipe(int index) {
        if (index < 0 || index >= availableRecipes.size()) {
            selectedRecipe.set(-1);
            outputContainer.setItem(0, ItemStack.EMPTY);
            hasValidOutput = false;
            return;
        }
        selectedRecipe.set(index);
        OrnamentRecipe recipe = availableRecipes.get(index);
        ItemStack result = createOutputStack(recipe);
        outputContainer.setItem(0, result);
        hasValidOutput = !result.isEmpty();
    }

    /**
     * Creates the output ItemStack with MaterialTextureData embedded in its custom tag.
     */
    private ItemStack createOutputStack(OrnamentRecipe recipe) {
        OrnamentBlockType type = recipe.type();
        // Look up the registered block for this type
        Block ornamentBlock = OrnamentRegistry.getOrnamentBlock(type);
        if (ornamentBlock == null || ornamentBlock == Blocks.AIR) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(ornamentBlock);

        // Build texture data from input materials mapped to components
        if (ornamentBlock instanceof IMateriallyTexturedBlock texturedBlock) {
            List<IMateriallyTexturedBlockComponent> components = texturedBlock.getComponents();
            Map<Identifier, Block> textureMap = new HashMap<>();
            for (int i = 0; i < components.size() && i < recipe.materials().size(); i++) {
                textureMap.put(components.get(i).getId(), recipe.materials().get(i));
            }
            MaterialTextureData data = new MaterialTextureData(textureMap);
            if (!data.isEmpty()) {
                CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = customData.copyTag();
                tag.put("OrnamentData", data.toNbt());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }

        return stack;
    }

    // ==================== Slot Handling ====================

    @Override
    public boolean clickMenuButton(Player player, int id) {
        // id is the recipe index clicked by the client
        if (id >= 0 && id < availableRecipes.size()) {
            selectRecipe(id);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            if (index == OUTPUT_SLOT_INDEX) {
                // Moving output to player inventory
                if (!this.moveItemStackTo(slotStack, OUTPUT_SLOT_INDEX + 1, OUTPUT_SLOT_INDEX + 37, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, result);
            } else if (index < INPUT_SLOT_COUNT) {
                // Moving input to player inventory
                if (!this.moveItemStackTo(slotStack, OUTPUT_SLOT_INDEX + 1, OUTPUT_SLOT_INDEX + 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player to input slots
                if (slotStack.getItem() instanceof BlockItem) {
                    if (!this.moveItemStackTo(slotStack, 0, INPUT_SLOT_COUNT, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Return input items to player
        if (!player.level().isClientSide()) {
            for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
                ItemStack stack = inputContainer.getItem(i);
                if (!stack.isEmpty()) {
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                }
            }
        }
    }

    // ==================== Slot Types ====================

    /**
     * Input slot that only accepts BlockItems.
     */
    private static class MaterialInputSlot extends Slot {
        public MaterialInputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof BlockItem;
        }

        @Override
        public int getMaxStackSize() {
            return 1; // one material block per slot
        }
    }

    /**
     * Output slot — takes only, cannot place.
     */
    private class OutputSlot extends Slot {
        public OutputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false; // output only
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            // Consume one of each input material
            for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
                ItemStack input = inputContainer.getItem(i);
                if (!input.isEmpty()) {
                    input.shrink(1);
                    if (input.isEmpty()) {
                        inputContainer.setItem(i, ItemStack.EMPTY);
                    }
                }
            }
            super.onTake(player, stack);
        }
    }

    // ==================== Recipe Data ====================

    /**
     * Represents a potential output: an ornament type with the supplied materials.
     */
    public record OrnamentRecipe(OrnamentBlockType type, List<Block> materials) {
        public String getDisplayName() {
            return type.getDisplayName();
        }
    }
}

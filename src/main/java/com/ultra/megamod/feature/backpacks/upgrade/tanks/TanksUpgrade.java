package com.ultra.megamod.feature.backpacks.upgrade.tanks;

import com.ultra.megamod.feature.backpacks.upgrade.BackpackUpgrade;
import net.minecraft.nbt.CompoundTag;

/**
 * Backpack upgrade that adds two fluid tanks (left and right).
 * Each tank can hold up to 4000 mB of a single fluid type.
 */
public class TanksUpgrade extends BackpackUpgrade {

    public static final int MAX_CAPACITY = 4000;

    private String leftFluidId = "";
    private int leftFluidAmount = 0;
    private String rightFluidId = "";
    private int rightFluidAmount = 0;

    @Override
    public String getId() {
        return "tanks";
    }

    @Override
    public String getDisplayName() {
        return "Tanks";
    }

    // --- Left tank ---

    public String getLeftFluidId() {
        return leftFluidId;
    }

    public int getLeftFluidAmount() {
        return leftFluidAmount;
    }

    /**
     * Fill the left tank with the given fluid.
     * If the tank is empty, sets the fluid type. If the tank already contains a different fluid, does nothing.
     *
     * @param fluidId the registry ID of the fluid (e.g. "minecraft:water")
     * @param amount  the amount in mB to add
     * @return the amount actually inserted
     */
    public int fillLeft(String fluidId, int amount) {
        if (fluidId == null || fluidId.isEmpty() || amount <= 0) return 0;

        if (leftFluidAmount == 0) {
            leftFluidId = fluidId;
        } else if (!leftFluidId.equals(fluidId)) {
            return 0; // different fluid already stored
        }

        int space = MAX_CAPACITY - leftFluidAmount;
        int toInsert = Math.min(amount, space);
        leftFluidAmount += toInsert;
        return toInsert;
    }

    /**
     * Drain from the left tank.
     *
     * @param amount the maximum amount in mB to drain
     * @return the amount actually drained
     */
    public int drainLeft(int amount) {
        if (amount <= 0 || leftFluidAmount <= 0) return 0;

        int toDrain = Math.min(amount, leftFluidAmount);
        leftFluidAmount -= toDrain;

        if (leftFluidAmount == 0) {
            leftFluidId = "";
        }

        return toDrain;
    }

    // --- Right tank ---

    public String getRightFluidId() {
        return rightFluidId;
    }

    public int getRightFluidAmount() {
        return rightFluidAmount;
    }

    /**
     * Fill the right tank with the given fluid.
     * If the tank is empty, sets the fluid type. If the tank already contains a different fluid, does nothing.
     *
     * @param fluidId the registry ID of the fluid (e.g. "minecraft:lava")
     * @param amount  the amount in mB to add
     * @return the amount actually inserted
     */
    public int fillRight(String fluidId, int amount) {
        if (fluidId == null || fluidId.isEmpty() || amount <= 0) return 0;

        if (rightFluidAmount == 0) {
            rightFluidId = fluidId;
        } else if (!rightFluidId.equals(fluidId)) {
            return 0; // different fluid already stored
        }

        int space = MAX_CAPACITY - rightFluidAmount;
        int toInsert = Math.min(amount, space);
        rightFluidAmount += toInsert;
        return toInsert;
    }

    /**
     * Drain from the right tank.
     *
     * @param amount the maximum amount in mB to drain
     * @return the amount actually drained
     */
    public int drainRight(int amount) {
        if (amount <= 0 || rightFluidAmount <= 0) return 0;

        int toDrain = Math.min(amount, rightFluidAmount);
        rightFluidAmount -= toDrain;

        if (rightFluidAmount == 0) {
            rightFluidId = "";
        }

        return toDrain;
    }

    // --- Persistence ---

    @Override
    public void saveToTag(CompoundTag tag) {
        super.saveToTag(tag);
        tag.putString("LeftFluidId", leftFluidId);
        tag.putInt("LeftFluidAmount", leftFluidAmount);
        tag.putString("RightFluidId", rightFluidId);
        tag.putInt("RightFluidAmount", rightFluidAmount);
    }

    @Override
    public void loadFromTag(CompoundTag tag) {
        super.loadFromTag(tag);
        this.leftFluidId = tag.getStringOr("LeftFluidId", "");
        this.leftFluidAmount = tag.getIntOr("LeftFluidAmount", 0);
        this.rightFluidId = tag.getStringOr("RightFluidId", "");
        this.rightFluidAmount = tag.getIntOr("RightFluidAmount", 0);
    }
}

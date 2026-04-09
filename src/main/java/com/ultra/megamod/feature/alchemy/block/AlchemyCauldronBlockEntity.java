package com.ultra.megamod.feature.alchemy.block;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.alchemy.AlchemyManager;
import com.ultra.megamod.feature.alchemy.AlchemyRecipeRegistry;
import com.ultra.megamod.feature.alchemy.AlchemyRegistry;
import com.ultra.megamod.feature.skills.SkillManager;
import com.ultra.megamod.feature.skills.SkillTreeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlchemyCauldronBlockEntity extends BlockEntity {

    private int waterLevel = 0; // 0 = empty, 1 = full
    private final List<String> ingredients = new ArrayList<>(); // up to 3 reagent IDs
    private int brewingProgress = 0; // 0-200 ticks
    private boolean brewing = false;
    private boolean resultReady = false;
    private String outputPotionId = "";
    private UUID lastBrewerUuid = null;

    public AlchemyCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(AlchemyRegistry.ALCHEMY_CAULDRON_BE.get(), pos, state);
    }

    // ==================== Public API ====================

    public int getWaterLevel() {
        return waterLevel;
    }

    public void fillWater() {
        this.waterLevel = 1;
        setChanged();
    }

    public boolean addIngredient(String reagentId) {
        if (ingredients.size() >= 3 || waterLevel <= 0 || brewing || resultReady) {
            return false;
        }
        ingredients.add(reagentId);
        setChanged();
        // If we now have 3 ingredients, start brewing automatically
        if (ingredients.size() == 3) {
            startBrewing();
        }
        return true;
    }

    public int getIngredientCount() {
        return ingredients.size();
    }

    public List<String> getIngredients() {
        return new ArrayList<>(ingredients);
    }

    public boolean isBrewing() {
        return brewing;
    }

    public int getBrewingProgress() {
        return brewingProgress;
    }

    public boolean hasResult() {
        return resultReady;
    }

    public String getOutputPotionId() {
        return outputPotionId;
    }

    public ItemStack collectResult(ServerPlayer player) {
        if (!resultReady || outputPotionId.isEmpty()) {
            clearCauldron();
            return ItemStack.EMPTY;
        }

        // Look up the potion item
        Identifier id = Identifier.tryParse(outputPotionId);
        if (id == null) {
            clearCauldron();
            return ItemStack.EMPTY;
        }

        Item potionItem = BuiltInRegistries.ITEM.getValue(id);
        if (potionItem == null) {
            clearCauldron();
            return ItemStack.EMPTY;
        }

        // Record the discovery
        if (player != null && level instanceof ServerLevel serverLevel) {
            AlchemyManager mgr = AlchemyManager.get(serverLevel);
            AlchemyRecipeRegistry.BrewingRecipe recipe = AlchemyRecipeRegistry.getBrewingByOutput(outputPotionId);
            if (recipe != null) {
                mgr.discoverRecipe(player.getUUID(), recipe.id());
                mgr.incrementBrewCount(player.getUUID(), outputPotionId);
                mgr.saveToDisk(serverLevel);
            }

            // Alchemy level bonus: chance for double output
            int alchLevel = mgr.getAlchemyLevel(player.getUUID());
            int count = 1;
            if (alchLevel >= 30 && level.random.nextFloat() < 0.15f) {
                count = 2;
            } else if (alchLevel >= 15 && level.random.nextFloat() < 0.08f) {
                count = 2;
            }

            ItemStack result = new ItemStack(potionItem, count);
            clearCauldron();
            return result;
        }

        ItemStack result = new ItemStack(potionItem, 1);
        clearCauldron();
        return result;
    }

    public void clearCauldron() {
        waterLevel = 0;
        ingredients.clear();
        brewingProgress = 0;
        brewing = false;
        resultReady = false;
        outputPotionId = "";
        lastBrewerUuid = null;
        setChanged();
        updateBlockState(false);
    }

    // ==================== Internal ====================

    private void startBrewing() {
        if (ingredients.size() != 3 || waterLevel <= 0) return;

        // Look up the recipe
        AlchemyRecipeRegistry.BrewingRecipe recipe = AlchemyRecipeRegistry.findBrewingRecipe(ingredients);
        if (recipe != null) {
            outputPotionId = recipe.output();
        } else {
            outputPotionId = ""; // Will produce nothing
        }

        brewing = true;
        brewingProgress = 0;
        setChanged();
        updateBlockState(true);
    }

    private boolean hasHeatSource() {
        if (level == null) return false;
        BlockPos below = worldPosition.below();
        BlockState belowState = level.getBlockState(below);
        Block belowBlock = belowState.getBlock();

        // Check for fire, lava, campfire, soul campfire, magma
        if (belowBlock == Blocks.FIRE || belowBlock == Blocks.SOUL_FIRE) return true;
        if (belowBlock == Blocks.LAVA) return true;
        if (belowBlock == Blocks.MAGMA_BLOCK) return true;
        if (belowState.getBlock() instanceof CampfireBlock) {
            return belowState.getValue(CampfireBlock.LIT);
        }
        return false;
    }

    private void updateBlockState(boolean brewingState) {
        if (level != null && !level.isClientSide()) {
            BlockState current = level.getBlockState(worldPosition);
            if (current.getBlock() instanceof AlchemyCauldronBlock) {
                if (current.getValue(AlchemyCauldronBlock.BREWING) != brewingState) {
                    level.setBlock(worldPosition, current.setValue(AlchemyCauldronBlock.BREWING, brewingState), 3);
                }
            }
        }
    }

    /**
     * Check if the player meets the skill requirements for this recipe tier.
     */
    public boolean checkSkillRequirements(ServerPlayer player, AlchemyRecipeRegistry.BrewingRecipe recipe) {
        if (player == null || recipe == null) return false;
        ServerLevel serverLevel = (ServerLevel) player.level();
        SkillManager skills = SkillManager.get(serverLevel);
        UUID uuid = player.getUUID();
        int arcaneLevel = skills.getLevel(uuid, SkillTreeType.ARCANE);

        return switch (recipe.tier()) {
            case 1 -> arcaneLevel >= 5;
            case 2 -> arcaneLevel >= 10;
            case 3 -> skills.isNodeUnlocked(uuid, "mana_weaver_1");
            case 4 -> skills.isNodeUnlocked(uuid, "mana_weaver_3");
            case 5 -> skills.isNodeUnlocked(uuid, "mana_weaver_5");
            default -> false;
        };
    }

    // ==================== Server Tick ====================

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlchemyCauldronBlockEntity be) {
        if (level.isClientSide()) return;

        if (!be.brewing) return;

        // Need heat source to brew
        if (!be.hasHeatSource()) {
            return;
        }

        // Advance brewing
        be.brewingProgress++;

        // Higher alchemy level = faster brewing (checked via last brewer)
        // Speed boost every 10 ticks if brewer has high alchemy level
        if (be.lastBrewerUuid != null && be.brewingProgress % 10 == 0) {
            AlchemyManager mgr = AlchemyManager.get((ServerLevel) level);
            int alchLevel = mgr.getAlchemyLevel(be.lastBrewerUuid);
            if (alchLevel >= 40) {
                be.brewingProgress += 3; // 30% faster
            } else if (alchLevel >= 20) {
                be.brewingProgress += 2; // 20% faster
            } else if (alchLevel >= 10) {
                be.brewingProgress += 1; // 10% faster
            }
        }

        if (be.brewingProgress >= 200) {
            // Brewing complete
            be.brewing = false;
            be.resultReady = true;
            be.setChanged();
            be.updateBlockState(false);
        }
    }

    // ==================== NBT Persistence ====================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("WaterLevel", waterLevel);
        output.putInt("BrewingProgress", brewingProgress);
        output.putBoolean("Brewing", brewing);
        output.putBoolean("ResultReady", resultReady);
        output.putString("OutputPotion", outputPotionId);
        if (lastBrewerUuid != null) {
            output.putString("LastBrewer", lastBrewerUuid.toString());
        }
        // Store ingredients as comma-separated string for simplicity
        output.putString("IngredientsStr", String.join(",", ingredients));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        waterLevel = input.getIntOr("WaterLevel", 0);
        brewingProgress = input.getIntOr("BrewingProgress", 0);
        brewing = input.getBooleanOr("Brewing", false);
        resultReady = input.getBooleanOr("ResultReady", false);
        outputPotionId = input.getStringOr("OutputPotion", "");
        String brewerStr = input.getStringOr("LastBrewer", "");
        if (!brewerStr.isEmpty()) {
            try {
                lastBrewerUuid = UUID.fromString(brewerStr);
            } catch (IllegalArgumentException e) {
                lastBrewerUuid = null;
            }
        }
        ingredients.clear();
        String ingredientsStr = input.getStringOr("IngredientsStr", "");
        if (!ingredientsStr.isEmpty()) {
            for (String s : ingredientsStr.split(",")) {
                if (!s.isEmpty()) ingredients.add(s);
            }
        }
    }

    public void setLastBrewer(UUID uuid) {
        this.lastBrewerUuid = uuid;
    }
}

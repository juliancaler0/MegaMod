package com.ultra.megamod.feature.furniture;

import com.ultra.megamod.feature.attributes.MegaModAttributes;
import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.generation.RoomTemplate;
import com.ultra.megamod.feature.dungeons.loot.DungeonChestLoot;
import com.ultra.megamod.feature.dungeons.loot.DungeonLootGenerator;
import com.ultra.megamod.feature.dungeons.loot.LootQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class DungeonChestBlockEntity extends BlockEntity implements Container {
    private static final int SIZE = 54; // double chest
    private final ItemStack[] items = new ItemStack[SIZE];

    // Lazy loot generation: store tier + room type, generate on first open
    private String pendingTier = null;
    private String pendingRoomType = null;
    private int bossChestCount = 1; // how many boss chests share the loot (for splitting)
    private boolean lootGenerated = false;

    public DungeonChestBlockEntity(BlockPos pos, BlockState state) {
        super(FurnitureRegistry.DUNGEON_CHEST_BE.get(), pos, state);
        for (int i = 0; i < SIZE; i++) {
            items[i] = ItemStack.EMPTY;
        }
    }

    /**
     * Mark this chest for lazy loot generation. Loot will be created when a player first opens it,
     * using their Luck and loot_fortune attributes to influence quality rolls.
     */
    public void setPendingLoot(DungeonTier tier, RoomTemplate.RoomType roomType) {
        setPendingLoot(tier, roomType, 1);
    }

    public void setPendingLoot(DungeonTier tier, RoomTemplate.RoomType roomType, int bossChestCount) {
        this.pendingTier = tier.name();
        this.pendingRoomType = roomType.name();
        this.bossChestCount = Math.max(1, bossChestCount);
        this.lootGenerated = false;
        setChanged();
    }

    /**
     * Generate loot using the opening player's attributes. Called once on first open.
     */
    public void generateLootForPlayer(Player player) {
        if (lootGenerated || pendingTier == null || pendingRoomType == null) return;
        lootGenerated = true;

        DungeonTier tier = DungeonTier.fromName(pendingTier);
        RoomTemplate.RoomType roomType;
        try {
            roomType = RoomTemplate.RoomType.valueOf(pendingRoomType);
        } catch (IllegalArgumentException e) {
            roomType = RoomTemplate.RoomType.TREASURE;
        }

        // Read player's luck and loot_fortune for quality bonus
        double lootFortune = 0.0;
        double luck = 0.0;
        AttributeInstance fortuneInst = player.getAttribute((Holder<Attribute>) MegaModAttributes.LOOT_FORTUNE);
        if (fortuneInst != null) lootFortune = fortuneInst.getValue();
        AttributeInstance luckInst = player.getAttribute(Attributes.LUCK);
        if (luckInst != null) luck = luckInst.getValue();

        // Combined bonus: each point shifts quality roll favorably
        double totalBonus = lootFortune + (luck * 3.0);

        // Treasure Alchemist synergy: upgrade dungeon loot tier by one
        if (player instanceof net.minecraft.server.level.ServerPlayer sp2
                && com.ultra.megamod.feature.skills.synergy.SynergyEffects.hasTreasureAlchemist(sp2)) {
            tier = switch (tier) {
                case NORMAL -> DungeonTier.HARD;
                case HARD -> DungeonTier.NIGHTMARE;
                case NIGHTMARE, INFERNAL -> DungeonTier.INFERNAL;
                default -> tier;
            };
        }

        // Generate loot with the bonus applied
        DungeonChestLoot.activeFortuneBonus = totalBonus;
        DungeonChestLoot.activeLootFortune = lootFortune;
        DungeonLootGenerator.adminLootBoost = player instanceof net.minecraft.server.level.ServerPlayer sp
                && com.ultra.megamod.feature.computer.admin.AdminSystem.isAdmin(sp);
        try {
            // Generate full loot then take this chest's share if split across multiple boss chests
            List<ItemStack> fullLoot = DungeonChestLoot.generateChestLoot(roomType, tier, level.getRandom());
            List<ItemStack> loot;
            if (bossChestCount > 1) {
                // Distribute items round-robin across chests, pick our share by position hash
                int chestIndex = Math.abs(worldPosition.hashCode()) % bossChestCount;
                loot = new java.util.ArrayList<>();
                for (int i = 0; i < fullLoot.size(); i++) {
                    if (i % bossChestCount == chestIndex) {
                        loot.add(fullLoot.get(i));
                    }
                }
            } else {
                loot = fullLoot;
            }
            for (int i = 0; i < loot.size() && i < SIZE; i++) {
                items[i] = loot.get(i);
            }
        } finally {
            DungeonChestLoot.activeFortuneBonus = 0.0;
            DungeonChestLoot.activeLootFortune = 0.0;
            DungeonLootGenerator.adminLootBoost = false;
        }

        // Clear pending state
        pendingTier = null;
        pendingRoomType = null;
        setChanged();
    }

    public boolean hasPendingLoot() {
        return !lootGenerated && pendingTier != null && pendingRoomType != null;
    }

    public int getContainerSize() {
        return SIZE;
    }

    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SIZE ? items[slot] : ItemStack.EMPTY;
    }

    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= SIZE || items[slot].isEmpty()) return ItemStack.EMPTY;
        ItemStack result = items[slot].split(amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= SIZE) return ItemStack.EMPTY;
        ItemStack stack = items[slot];
        items[slot] = ItemStack.EMPTY;
        return stack;
    }

    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < SIZE) {
            items[slot] = stack;
            setChanged();
        }
    }

    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    public void clearContent() {
        for (int i = 0; i < SIZE; i++) {
            items[i] = ItemStack.EMPTY;
        }
        setChanged();
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < SIZE; i++) {
            if (!items[i].isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), items[i]);
                items[i] = ItemStack.EMPTY;
            }
        }
    }
}

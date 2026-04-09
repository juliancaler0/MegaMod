package com.ultra.megamod.feature.adminmodules.modules.player;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ChestStealerModule extends AdminModule {
    private ModuleSetting.IntSetting range;
    private ModuleSetting.IntSetting delay;
    private int tick = 0;

    public ChestStealerModule() {
        super("chest_stealer", "ChestStealer", "Quick-loots nearby containers", ModuleCategory.PLAYER);
    }

    @Override
    protected void initSettings() {
        range = integer("Range", 4, 1, 8, "Search range for containers");
        delay = integer("Delay", 10, 1, 40, "Ticks between loot cycles");
    }

    @Override
    public void onServerTick(ServerPlayer player, ServerLevel level) {
        // Scale scan interval with range: use configured delay, but enforce minimum 20 ticks for range 5+
        int effectiveDelay = delay.getValue();
        if (range.getValue() >= 5 && effectiveDelay < 20) {
            effectiveDelay = 20;
        }
        if (++tick % effectiveDelay != 0) return;

        int r = range.getValue();
        BlockPos center = player.blockPosition();
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);
                    // Use Container interface to catch ALL container types:
                    // chests, barrels, shulker boxes, hoppers, dispensers, droppers, etc.
                    if (be instanceof Container container) {
                        for (int i = 0; i < container.getContainerSize(); i++) {
                            ItemStack stack = container.getItem(i);
                            if (!stack.isEmpty()) {
                                if (player.getInventory().add(stack.copy())) {
                                    container.setItem(i, ItemStack.EMPTY);
                                }
                            }
                        }
                        // Mark block entity dirty so changes persist
                        be.setChanged();
                    }
                }
            }
        }
    }
}

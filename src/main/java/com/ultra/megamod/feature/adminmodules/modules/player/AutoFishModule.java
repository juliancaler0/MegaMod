package com.ultra.megamod.feature.adminmodules.modules.player;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class AutoFishModule extends AdminModule {
    private ModuleSetting.IntSetting interval;
    private ModuleSetting.BoolSetting giveXP;
    private ModuleSetting.EnumSetting mode;
    private int tick = 0;

    public AutoFishModule() {
        super("auto_fish", "AutoFish", "Auto-fishes when near water with rod", ModuleCategory.PLAYER);
    }

    @Override
    protected void initSettings() {
        interval = integer("Interval", 100, 20, 200, "Ticks between catches");
        giveXP = bool("GiveXP", true, "Award XP per catch");
        mode = enumVal("Mode", "Direct", List.of("Direct", "Realistic"), "Direct=instant, Realistic=requires water nearby");
    }

    @Override
    public void onServerTick(ServerPlayer player, ServerLevel level) {
        if (++tick % interval.getValue() != 0) return;
        // Require fishing rod in hand
        if (!player.getMainHandItem().is(Items.FISHING_ROD)) return;

        // In Realistic mode, check for water within 5 blocks
        if ("Realistic".equals(mode.getValue())) {
            boolean waterNearby = false;
            BlockPos center = player.blockPosition();
            outer:
            for (int x = -5; x <= 5; x++) {
                for (int y = -3; y <= 1; y++) {
                    for (int z = -5; z <= 5; z++) {
                        if (level.getFluidState(center.offset(x, y, z)).is(Fluids.WATER)
                                || level.getFluidState(center.offset(x, y, z)).is(Fluids.FLOWING_WATER)) {
                            waterNearby = true;
                            break outer;
                        }
                    }
                }
            }
            if (!waterNearby) return;
        }

        // Give a random fish type
        ItemStack fish;
        int roll = level.getRandom().nextInt(100);
        if (roll < 60) fish = new ItemStack(Items.COD, 1);
        else if (roll < 85) fish = new ItemStack(Items.SALMON, 1);
        else if (roll < 95) fish = new ItemStack(Items.TROPICAL_FISH, 1);
        else fish = new ItemStack(Items.PUFFERFISH, 1);

        player.getInventory().add(fish);
        if (giveXP.getValue()) {
            player.giveExperiencePoints(level.getRandom().nextIntBetweenInclusive(1, 6));
        }
    }
}

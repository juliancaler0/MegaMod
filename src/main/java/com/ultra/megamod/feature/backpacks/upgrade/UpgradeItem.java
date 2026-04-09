package com.ultra.megamod.feature.backpacks.upgrade;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Base item class for backpack upgrades.
 * Each upgrade item knows which BackpackUpgrade class it creates.
 */
public class UpgradeItem extends Item {

    private final String upgradeId;
    private final String displayName;
    private final Supplier<BackpackUpgrade> upgradeFactory;

    public UpgradeItem(Properties props, String upgradeId, String displayName,
                       Supplier<BackpackUpgrade> upgradeFactory) {
        super(props.stacksTo(1));
        this.upgradeId = upgradeId;
        this.displayName = displayName;
        this.upgradeFactory = upgradeFactory;
    }

    public String getUpgradeId() { return upgradeId; }
    public String getUpgradeDisplayName() { return displayName; }

    /** Create a new instance of the associated BackpackUpgrade. */
    public BackpackUpgrade createUpgrade() {
        return upgradeFactory.get();
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("Backpack Upgrade: " + displayName).withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Place in a backpack upgrade slot").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }
}

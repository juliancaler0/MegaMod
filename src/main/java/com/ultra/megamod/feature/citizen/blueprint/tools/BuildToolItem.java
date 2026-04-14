package com.ultra.megamod.feature.citizen.blueprint.tools;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * Item for placing blueprints in the world.
 * <p>
 * Right-click (on block or in air) opens the BuildToolScreen on the client,
 * where the player can browse and select blueprints to place.
 * The currently selected blueprint name and pack are stored in the item's NBT.
 */
public class BuildToolItem extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Client-side screen opener. Populated by BlueprintClientProxy on the client
     * so the server never loads any {@code net.minecraft.client} classes.
     * Argument may be {@code null} when used in air.
     */
    public static Consumer<BlockPos> OPEN_BUILD_SCREEN = pos -> {};

    private static final String NBT_BLUEPRINT_NAME = "megamod:blueprint_name";
    private static final String NBT_BLUEPRINT_PACK = "megamod:blueprint_pack";

    public BuildToolItem() {
        this(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public BuildToolItem(Properties properties) {
        super(properties);
    }

    // ---- NBT helpers ----

    private static CompoundTag getTag(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag();
    }

    private static void setTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Gets the currently selected blueprint name from NBT.
     */
    public static String getBlueprintName(ItemStack stack) {
        return getTag(stack).getStringOr(NBT_BLUEPRINT_NAME, "");
    }

    /**
     * Sets the currently selected blueprint name in NBT.
     */
    public static void setBlueprintName(ItemStack stack, String name) {
        CompoundTag tag = getTag(stack);
        tag.putString(NBT_BLUEPRINT_NAME, name != null ? name : "");
        setTag(stack, tag);
    }

    /**
     * Gets the currently selected blueprint pack from NBT.
     */
    public static String getBlueprintPack(ItemStack stack) {
        return getTag(stack).getStringOr(NBT_BLUEPRINT_PACK, "");
    }

    /**
     * Sets the currently selected blueprint pack in NBT.
     */
    public static void setBlueprintPack(ItemStack stack, String pack) {
        CompoundTag tag = getTag(stack);
        tag.putString(NBT_BLUEPRINT_PACK, pack != null ? pack : "");
        setTag(stack, tag);
    }

    // ---- Interaction: right-click on block ----

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) return InteractionResult.PASS;

        if (level.isClientSide()) {
            BlockPos placementPos = context.getClickedPos().relative(context.getClickedFace());
            openBuildScreen(placementPos);
        }

        LOGGER.debug("Build tool used on block by {}", player.getGameProfile().name());
        return InteractionResult.SUCCESS;
    }

    // ---- Interaction: right-click in air ----

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            openBuildScreen(null);
        }

        LOGGER.debug("Build tool used in air by {}", player.getGameProfile().name());
        return InteractionResult.SUCCESS;
    }

    /**
     * Opens the BuildToolScreen on the client via the proxy populated by
     * BlueprintClientProxy. On the dedicated server the default no-op is used.
     *
     * @param placementPos the position to place at, or null if used in air
     */
    private void openBuildScreen(BlockPos placementPos) {
        OPEN_BUILD_SCREEN.accept(placementPos);
    }

    // ---- Tooltip ----

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        String name = getBlueprintName(stack);
        String pack = getBlueprintPack(stack);

        if (!pack.isEmpty()) {
            tooltip.accept(Component.literal("Pack: " + pack).withStyle(ChatFormatting.GRAY));
        }
        if (!name.isEmpty()) {
            tooltip.accept(Component.literal("Blueprint: " + name).withStyle(ChatFormatting.AQUA));
        }

        if (name.isEmpty() && pack.isEmpty()) {
            tooltip.accept(Component.literal("Right-click to browse blueprints").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}

package com.ultra.megamod.feature.citizen.blueprint.tools;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * Item for procedural shape generation.
 * <p>
 * Right-click (on block or in air) opens the ShapeToolScreen on the client.
 * Stores the selected shape type, dimensions (width, height, depth), and
 * material block in the item's NBT.
 * <p>
 * When a shape is generated via the screen, it is stored in {@link #lastGeneratedShape}.
 * If the player then right-clicks a block while a shape is pending, a
 * {@link com.ultra.megamod.feature.citizen.blueprint.network.BuildToolPlacePayload}
 * is sent to place it at that position.
 */
public class ShapeToolItem extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * The last shape generated from the ShapeToolScreen.
     * Set by {@link #setLastShape(com.ultra.megamod.feature.citizen.blueprint.Blueprint)} after generation.
     * Cleared after placement.
     */
    private static com.ultra.megamod.feature.citizen.blueprint.Blueprint lastGeneratedShape = null;

    /**
     * Sets the last generated shape blueprint. Called from the shape generation screen
     * after a shape has been computed.
     *
     * @param bp the generated blueprint, or null to clear
     */
    public static void setLastShape(com.ultra.megamod.feature.citizen.blueprint.Blueprint bp) {
        lastGeneratedShape = bp;
    }

    /**
     * Returns the last generated shape blueprint, or null if none pending.
     */
    public static com.ultra.megamod.feature.citizen.blueprint.Blueprint getLastShape() {
        return lastGeneratedShape;
    }

    private static final String NBT_SHAPE = "megamod:shape";
    private static final String NBT_WIDTH = "megamod:shape_width";
    private static final String NBT_HEIGHT = "megamod:shape_height";
    private static final String NBT_DEPTH = "megamod:shape_depth";
    private static final String NBT_MATERIAL = "megamod:shape_material";

    /** Default dimensions for new shapes. */
    private static final int DEFAULT_SIZE = 5;

    public ShapeToolItem() {
        this(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public ShapeToolItem(Properties properties) {
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
     * Gets the selected shape from NBT. Defaults to CUBE.
     */
    public static Shape getShape(ItemStack stack) {
        return Shape.fromString(getTag(stack).getStringOr(NBT_SHAPE, ""));
    }

    /**
     * Sets the selected shape in NBT.
     */
    public static void setShape(ItemStack stack, Shape shape) {
        CompoundTag tag = getTag(stack);
        tag.putString(NBT_SHAPE, shape.name());
        setTag(stack, tag);
    }

    /**
     * Gets the width dimension.
     */
    public static int getWidth(ItemStack stack) {
        int w = getTag(stack).getIntOr(NBT_WIDTH, DEFAULT_SIZE);
        return Math.max(1, w);
    }

    /**
     * Sets the width dimension.
     */
    public static void setWidth(ItemStack stack, int width) {
        CompoundTag tag = getTag(stack);
        tag.putInt(NBT_WIDTH, Math.max(1, width));
        setTag(stack, tag);
    }

    /**
     * Gets the height dimension.
     */
    public static int getHeight(ItemStack stack) {
        int h = getTag(stack).getIntOr(NBT_HEIGHT, DEFAULT_SIZE);
        return Math.max(1, h);
    }

    /**
     * Sets the height dimension.
     */
    public static void setHeight(ItemStack stack, int height) {
        CompoundTag tag = getTag(stack);
        tag.putInt(NBT_HEIGHT, Math.max(1, height));
        setTag(stack, tag);
    }

    /**
     * Gets the depth dimension.
     */
    public static int getDepth(ItemStack stack) {
        int d = getTag(stack).getIntOr(NBT_DEPTH, DEFAULT_SIZE);
        return Math.max(1, d);
    }

    /**
     * Sets the depth dimension.
     */
    public static void setDepth(ItemStack stack, int depth) {
        CompoundTag tag = getTag(stack);
        tag.putInt(NBT_DEPTH, Math.max(1, depth));
        setTag(stack, tag);
    }

    /**
     * Gets the material block from NBT. Defaults to stone.
     */
    public static Block getMaterial(ItemStack stack) {
        String materialId = getTag(stack).getStringOr(NBT_MATERIAL, "");
        if (!materialId.isEmpty()) {
            Identifier id = Identifier.tryParse(materialId);
            if (id != null) {
                Block block = BuiltInRegistries.BLOCK.getValue(id);
                if (block != null && block != Blocks.AIR) {
                    return block;
                }
            }
        }
        return Blocks.STONE;
    }

    /**
     * Sets the material block in NBT.
     */
    public static void setMaterial(ItemStack stack, Block block) {
        CompoundTag tag = getTag(stack);
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        tag.putString(NBT_MATERIAL, blockId.toString());
        setTag(stack, tag);
    }

    // ---- Interaction: right-click on block ----

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) return InteractionResult.PASS;

        BlockPos placementPos = context.getClickedPos().relative(context.getClickedFace());

        // If a shape was generated and is pending placement, send it to the server
        if (lastGeneratedShape != null && level.isClientSide()) {
            // Send BuildToolPlacePayload to place the generated shape at the target position
            sendShapePlacement(placementPos);
            player.displayClientMessage(
                Component.literal("Placing generated shape at " + placementPos.getX()
                    + ", " + placementPos.getY() + ", " + placementPos.getZ())
                    .withStyle(ChatFormatting.GREEN),
                true);
            lastGeneratedShape = null;
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide()) {
            openShapeScreen(placementPos);
        }

        LOGGER.debug("Shape tool used on block by {}", player.getGameProfile().name());
        return InteractionResult.SUCCESS;
    }

    // ---- Interaction: right-click in air ----

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            openShapeScreen(null);
        }

        LOGGER.debug("Shape tool used in air by {}", player.getGameProfile().name());
        return InteractionResult.SUCCESS;
    }

    /**
     * Opens the ShapeToolScreen on the client.
     * Separated so the client screen class is only loaded on the client side.
     *
     * @param placementPos the position to center the shape at, or null if used in air
     */
    private void openShapeScreen(BlockPos placementPos) {
        net.minecraft.client.Minecraft.getInstance().setScreen(
                new com.ultra.megamod.feature.citizen.blueprint.screen.ShapeToolScreen(placementPos));
    }

    /**
     * Sends a BuildToolPlacePayload to the server to place the generated shape.
     * Separated into its own method so client-only network classes are only loaded on the client.
     */
    private static void sendShapePlacement(BlockPos placementPos) {
        net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(
            new com.ultra.megamod.feature.citizen.blueprint.network.BuildToolPlacePayload(
                "megamod",
                "__shape_generated__",
                placementPos,
                0 // No rotation
            ));
    }

    // ---- Tooltip ----

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        Shape shape = getShape(stack);
        int width = getWidth(stack);
        int height = getHeight(stack);
        int depth = getDepth(stack);
        Block material = getMaterial(stack);

        tooltip.accept(Component.literal("Shape: " + shape.getDisplayName()).withStyle(ChatFormatting.AQUA));
        tooltip.accept(Component.literal("Size: " + width + " x " + height + " x " + depth).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Material: ")
                .withStyle(ChatFormatting.GRAY)
                .append(material.getName().copy().withStyle(ChatFormatting.WHITE)));
    }
}

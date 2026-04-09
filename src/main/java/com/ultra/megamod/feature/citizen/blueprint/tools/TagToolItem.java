package com.ultra.megamod.feature.citizen.blueprint.tools;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

/**
 * Item for tagging positions in blueprints.
 * <p>
 * Shift+right-click on a blueprint data provider block sets it as the anchor.
 * Normal right-click on a block adds/removes the current tag at that position
 * (relative to the anchor). Right-click in air opens the tag tool screen.
 * <p>
 * Tag data (anchor position, current tag name, and tagged positions) is stored in NBT.
 */
public class TagToolItem extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String NBT_ANCHOR_X = "megamod:tag_anchor_x";
    private static final String NBT_ANCHOR_Y = "megamod:tag_anchor_y";
    private static final String NBT_ANCHOR_Z = "megamod:tag_anchor_z";
    private static final String NBT_HAS_ANCHOR = "megamod:tag_has_anchor";
    private static final String NBT_CURRENT_TAG = "megamod:tag_current";
    private static final String NBT_TAGGED_POSITIONS = "megamod:tagged_positions";

    public TagToolItem() {
        this(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public TagToolItem(Properties properties) {
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
     * Gets the anchor position, or null if none is set.
     */
    public static BlockPos getAnchorPos(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        if (!tag.getBooleanOr(NBT_HAS_ANCHOR, false)) {
            return null;
        }
        return new BlockPos(
                tag.getIntOr(NBT_ANCHOR_X, 0),
                tag.getIntOr(NBT_ANCHOR_Y, 0),
                tag.getIntOr(NBT_ANCHOR_Z, 0));
    }

    /**
     * Sets the anchor position.
     */
    public static void setAnchorPos(ItemStack stack, BlockPos pos) {
        CompoundTag tag = getTag(stack);
        if (pos != null) {
            tag.putBoolean(NBT_HAS_ANCHOR, true);
            tag.putInt(NBT_ANCHOR_X, pos.getX());
            tag.putInt(NBT_ANCHOR_Y, pos.getY());
            tag.putInt(NBT_ANCHOR_Z, pos.getZ());
        } else {
            tag.putBoolean(NBT_HAS_ANCHOR, false);
            tag.remove(NBT_ANCHOR_X);
            tag.remove(NBT_ANCHOR_Y);
            tag.remove(NBT_ANCHOR_Z);
        }
        setTag(stack, tag);
    }

    /**
     * Gets the currently selected tag name.
     */
    public static String getCurrentTag(ItemStack stack) {
        return getTag(stack).getStringOr(NBT_CURRENT_TAG, "");
    }

    /**
     * Sets the currently selected tag name.
     */
    public static void setCurrentTag(ItemStack stack, String tagName) {
        CompoundTag tag = getTag(stack);
        tag.putString(NBT_CURRENT_TAG, tagName != null ? tagName : "");
        setTag(stack, tag);
    }

    /**
     * Gets all tagged positions as a map of relative position key -> list of tag names.
     * Position key format: "x,y,z"
     */
    public static Map<String, List<String>> getTaggedPositions(ItemStack stack) {
        CompoundTag tag = getTag(stack);
        CompoundTag positions = tag.getCompoundOrEmpty(NBT_TAGGED_POSITIONS);
        Map<String, List<String>> result = new HashMap<>();

        for (String posKey : positions.keySet()) {
            ListTag tagList = positions.getListOrEmpty(posKey);
            List<String> tags = new ArrayList<>();
            for (int i = 0; i < tagList.size(); i++) {
                Tag entry = tagList.get(i);
                if (entry instanceof StringTag stringTag) {
                    tags.add(stringTag.value());
                }
            }
            if (!tags.isEmpty()) {
                result.put(posKey, tags);
            }
        }
        return result;
    }

    /**
     * Adds or removes a tag at the specified relative position.
     * @return true if the tag was added, false if it was removed
     */
    public static boolean toggleTag(ItemStack stack, BlockPos relativePos, String tagName) {
        CompoundTag itemTag = getTag(stack);
        CompoundTag positions = itemTag.getCompoundOrEmpty(NBT_TAGGED_POSITIONS);
        String posKey = relativePos.getX() + "," + relativePos.getY() + "," + relativePos.getZ();

        ListTag tagList = positions.getListOrEmpty(posKey);

        // Check if tag already exists at this position
        boolean found = false;
        for (int i = 0; i < tagList.size(); i++) {
            Tag entry = tagList.get(i);
            if (entry instanceof StringTag stringTag && stringTag.value().equals(tagName)) {
                tagList.remove(i);
                found = true;
                break;
            }
        }

        boolean added;
        if (!found) {
            tagList.add(StringTag.valueOf(tagName));
            added = true;
        } else {
            added = false;
        }

        if (tagList.isEmpty()) {
            positions.remove(posKey);
        } else {
            positions.put(posKey, tagList);
        }

        itemTag.put(NBT_TAGGED_POSITIONS, positions);
        setTag(stack, itemTag);
        return added;
    }

    // ---- Interaction: right-click on block ----

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();

        if (player.isShiftKeyDown()) {
            // Shift + right-click: set anchor to this block position
            // In a full implementation, we would verify this is a blueprint data provider block entity
            BlockEntity blockEntity = level.getBlockEntity(clickedPos);
            setAnchorPos(stack, clickedPos);

            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.literal("Tag anchor set: " + clickedPos.getX() + ", " + clickedPos.getY() + ", " + clickedPos.getZ())
                                .withStyle(ChatFormatting.GREEN),
                        false);
            }
            LOGGER.debug("Tag tool anchor set to {} by {}", clickedPos, player.getGameProfile().name());
            return InteractionResult.SUCCESS;
        } else {
            // Normal right-click: add/remove tag at position
            BlockPos anchorPos = getAnchorPos(stack);
            String currentTag = getCurrentTag(stack);

            if (anchorPos == null) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(
                            Component.literal("No anchor set! Shift+right-click a block to set one.")
                                    .withStyle(ChatFormatting.RED),
                            false);
                }
                return InteractionResult.FAIL;
            }

            if (currentTag.isEmpty()) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(
                            Component.literal("No tag selected! Open the tag tool screen to select one.")
                                    .withStyle(ChatFormatting.RED),
                            false);
                }
                return InteractionResult.FAIL;
            }

            // Calculate relative position from anchor
            BlockPos relativePos = clickedPos.subtract(anchorPos);
            boolean wasAdded = toggleTag(stack, relativePos, currentTag);

            if (!level.isClientSide()) {
                String blockName = level.getBlockState(clickedPos).getBlock().getName().getString();
                if (wasAdded) {
                    player.displayClientMessage(
                            Component.literal("Added tag '")
                                    .withStyle(ChatFormatting.GREEN)
                                    .append(Component.literal(currentTag).withStyle(ChatFormatting.AQUA))
                                    .append(Component.literal("' to ").withStyle(ChatFormatting.GREEN))
                                    .append(Component.literal(blockName).withStyle(ChatFormatting.WHITE)),
                            false);
                } else {
                    player.displayClientMessage(
                            Component.literal("Removed tag '")
                                    .withStyle(ChatFormatting.YELLOW)
                                    .append(Component.literal(currentTag).withStyle(ChatFormatting.AQUA))
                                    .append(Component.literal("' from ").withStyle(ChatFormatting.YELLOW))
                                    .append(Component.literal(blockName).withStyle(ChatFormatting.WHITE)),
                            false);
                }
            }

            LOGGER.debug("Tag tool toggled tag '{}' at {} (relative {}) by {}",
                    currentTag, clickedPos, relativePos, player.getGameProfile().name());
            return InteractionResult.SUCCESS;
        }
    }

    // ---- Interaction: right-click in air ----

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            BlockPos anchorPos = getAnchorPos(stack);
            if (anchorPos == null) {
                player.displayClientMessage(
                        Component.literal("No anchor set! Shift+right-click a block to set one.")
                                .withStyle(ChatFormatting.RED),
                        false);
                return InteractionResult.FAIL;
            }
            openTagScreen(stack, anchorPos, level);
        }

        LOGGER.debug("Tag tool used in air by {}", player.getGameProfile().name());
        return InteractionResult.SUCCESS;
    }

    /**
     * Opens the TagToolScreen on the client.
     * Separated so the client screen class is only loaded on the client side.
     */
    private void openTagScreen(ItemStack stack, BlockPos anchorPos, Level level) {
        // TODO: Open TagToolScreen when it is implemented
        // Minecraft.getInstance().setScreen(new TagToolScreen(getCurrentTag(stack), anchorPos, level, stack));
        LOGGER.debug("Tag tool screen open requested for anchor {}", anchorPos);
    }

    // ---- Tooltip ----

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        BlockPos anchor = getAnchorPos(stack);
        String currentTag = getCurrentTag(stack);

        if (anchor != null) {
            tooltip.accept(Component.literal("Anchor: " + anchor.getX() + ", " + anchor.getY() + ", " + anchor.getZ())
                    .withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.accept(Component.literal("No anchor set").withStyle(ChatFormatting.DARK_GRAY));
        }

        if (!currentTag.isEmpty()) {
            tooltip.accept(Component.literal("Tag: " + currentTag).withStyle(ChatFormatting.AQUA));
        }

        Map<String, List<String>> tagged = getTaggedPositions(stack);
        if (!tagged.isEmpty()) {
            int totalTags = tagged.values().stream().mapToInt(List::size).sum();
            tooltip.accept(Component.literal(totalTags + " tag(s) at " + tagged.size() + " position(s)")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}

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
import net.minecraft.world.entity.Entity;
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
 * Item for scanning world regions into blueprints.
 * <p>
 * Shift+right-click on a block sets pos1. Normal right-click on a block sets pos2.
 * Right-click in air with both positions set opens the scan tool screen (client-side).
 * Scroll or number keys switch between 10 scan slots.
 */
public class ScanToolItem extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();

    public ScanToolItem() {
        this(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public ScanToolItem(Properties properties) {
        super(properties);
    }

    // ---- NBT helpers ----

    /**
     * Reads the ScanToolData from the item stack's custom data component.
     */
    private static ScanToolData getData(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return new ScanToolData(customData.copyTag());
    }

    /**
     * Writes ScanToolData back into the item stack's custom data component.
     */
    private static void saveData(ItemStack stack, ScanToolData data) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data.getInternalTag()));
    }

    // ---- Interaction: right-click on block ----

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        ScanToolData data = getData(stack);

        if (player.isShiftKeyDown()) {
            // Shift + right-click -> set pos1
            data.setPos1(clickedPos);
            saveData(stack, data);

            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.literal("Scan pos1 set: ")
                                .withStyle(ChatFormatting.GREEN)
                                .append(Component.literal(formatPos(clickedPos)).withStyle(ChatFormatting.WHITE)),
                        true);
            }
            LOGGER.debug("Scan tool pos1 set to {} by {}", clickedPos, player.getGameProfile().name());
            return InteractionResult.SUCCESS;
        } else {
            // Normal right-click -> set pos2
            data.setPos2(clickedPos);
            saveData(stack, data);

            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.literal("Scan pos2 set: ")
                                .withStyle(ChatFormatting.GREEN)
                                .append(Component.literal(formatPos(clickedPos)).withStyle(ChatFormatting.WHITE)),
                        true);
            }
            LOGGER.debug("Scan tool pos2 set to {} by {}", clickedPos, player.getGameProfile().name());
            return InteractionResult.SUCCESS;
        }
    }

    // ---- Interaction: right-click in air ----

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ScanToolData data = getData(stack);
        ScanToolData.Slot slot = data.getCurrentSlotData();

        if (player.isShiftKeyDown()) {
            // Shift + air click on server: could trigger a save-to-disk action
            if (!level.isClientSide()) {
                if (slot.getPos1().equals(BlockPos.ZERO) && slot.getPos2().equals(BlockPos.ZERO)) {
                    player.displayClientMessage(
                            Component.literal("No scan region defined! Right-click blocks to set positions.")
                                    .withStyle(ChatFormatting.RED),
                            false);
                    return InteractionResult.FAIL;
                }
                player.displayClientMessage(
                        Component.literal("Scan region ready: ")
                                .withStyle(ChatFormatting.GREEN)
                                .append(Component.literal(formatPos(slot.getPos1()) + " to " + formatPos(slot.getPos2()))
                                        .withStyle(ChatFormatting.WHITE)),
                        false);
                LOGGER.debug("Scan tool save triggered by {} for slot {}", player.getGameProfile().name(), data.getCurrentSlotId());
            }
            return InteractionResult.SUCCESS;
        } else {
            // Normal air click on client: open scan screen
            if (level.isClientSide()) {
                openScanScreen(data);
            }
            return InteractionResult.SUCCESS;
        }
    }

    /**
     * Opens the ScanToolScreen on the client.
     * Separated into its own method so the class reference is only loaded on the client.
     */
    private void openScanScreen(ScanToolData data) {
        ScanToolData.Slot slot = data.getCurrentSlotData();
        net.minecraft.client.Minecraft.getInstance().setScreen(
                new com.ultra.megamod.feature.citizen.blueprint.screen.ScanToolScreen(slot.getPos1(), slot.getPos2()));
    }

    // ---- Inventory Tick (display scan slot when sneaking) ----

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        if (slot != net.minecraft.world.entity.EquipmentSlot.MAINHAND) return;
        if (!(entity instanceof Player player)) return;
        if (!player.isShiftKeyDown()) return;

        // Display the current scan slot number in the action bar while sneaking with item in hand
        ScanToolData data = getData(stack);
        int currentSlot = data.getCurrentSlotId();
        ScanToolData.Slot scanSlot = data.getCurrentSlotData();
        String slotName = scanSlot.getName().isEmpty() ? "(empty)" : scanSlot.getName();
        player.displayClientMessage(
            Component.literal("Scan Slot: " + currentSlot + " - " + slotName)
                .withStyle(ChatFormatting.GRAY),
            true);
    }

    // ---- Slot switching ----

    /**
     * Advances to the next scan slot. Call from a keybind or scroll handler.
     */
    public void nextSlot(Player player, ItemStack stack) {
        switchSlot(player, stack, ScanToolData::nextSlot);
    }

    /**
     * Goes back to the previous scan slot. Call from a keybind or scroll handler.
     */
    public void prevSlot(Player player, ItemStack stack) {
        switchSlot(player, stack, ScanToolData::prevSlot);
    }

    private void switchSlot(Player player, ItemStack stack, Consumer<ScanToolData> action) {
        ScanToolData data = getData(stack);
        action.accept(data);
        saveData(stack, data);

        ScanToolData.Slot slot = data.getCurrentSlotData();
        String slotLabel = String.valueOf(data.getCurrentSlotId());
        String name = slot.getName().isEmpty() ? "(empty)" : slot.getName();

        player.displayClientMessage(
                Component.literal("Scan slot " + slotLabel + ": " + name).withStyle(ChatFormatting.GRAY),
                true);
        LOGGER.debug("Switched to scan slot {} for {}", data.getCurrentSlotId(), player.getGameProfile().name());
    }

    // ---- Tooltip ----

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        ScanToolData data = getData(stack);
        int slotId = data.getCurrentSlotId();
        ScanToolData.Slot slot = data.getCurrentSlotData();

        tooltip.accept(Component.literal("Slot: " + slotId).withStyle(ChatFormatting.GRAY));

        if (!slot.getName().isEmpty()) {
            tooltip.accept(Component.literal("Name: " + slot.getName()).withStyle(ChatFormatting.AQUA));
        }

        if (!slot.getPos1().equals(BlockPos.ZERO)) {
            tooltip.accept(Component.literal("Pos1: " + formatPos(slot.getPos1())).withStyle(ChatFormatting.YELLOW));
        }
        if (!slot.getPos2().equals(BlockPos.ZERO)) {
            tooltip.accept(Component.literal("Pos2: " + formatPos(slot.getPos2())).withStyle(ChatFormatting.YELLOW));
        }
        slot.getAnchor().ifPresent(anchor ->
                tooltip.accept(Component.literal("Anchor: " + formatPos(anchor)).withStyle(ChatFormatting.GOLD)));
    }

    // ---- Utility ----

    private static String formatPos(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }
}

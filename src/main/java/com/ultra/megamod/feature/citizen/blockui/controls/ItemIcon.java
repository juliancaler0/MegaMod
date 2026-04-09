package com.ultra.megamod.feature.citizen.blockui.controls;

import com.ultra.megamod.feature.citizen.blockui.BOGuiGraphics;
import com.ultra.megamod.feature.citizen.blockui.Pane;
import com.ultra.megamod.feature.citizen.blockui.PaneParams;
import com.ultra.megamod.feature.citizen.blockui.controls.AbstractTextBuilder.AutomaticTooltipBuilder;
import com.ultra.megamod.feature.citizen.blockui.controls.Tooltip.AutomaticTooltip;
import com.ultra.megamod.feature.citizen.blockui.util.SpacerTextComponent;
import com.ultra.megamod.feature.citizen.blockui.util.ToggleableTextComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Class of itemIcons in our GUIs.
 */
public class ItemIcon extends Pane
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemIcon.class);

    /** Replaces Screen.hasShiftDown() which is no longer static in 1.21.11 */
    private static boolean isShiftDown() {
        long w = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();
        return org.lwjgl.glfw.GLFW.glfwGetKey(w, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS
            || org.lwjgl.glfw.GLFW.glfwGetKey(w, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }
    protected static final float DEFAULT_ITEMSTACK_SIZE = 16f;
    protected static final MutableComponent FIX_VANILLA_TOOLTIP = SpacerTextComponent.of(1);

    /**
     * ItemStack represented in the itemIcon.
     */
    @Nullable
    protected ItemStack itemStack;

    /**
     * If true then on next frame tooltip content will recompile
     */
    protected boolean tooltipUpdateScheduled = false;
    protected boolean renderItemDecorations = true;

    /**
     * Standard constructor instantiating the itemIcon without any additional settings.
     */
    public ItemIcon()
    {
        super();
    }

    /**
     * Constructor instantiating the itemIcon with specified parameters.
     *
     * @param params the parameters.
     */
    public ItemIcon(final PaneParams params)
    {
        super(params);

        final Identifier itemName = params.getResource("item");
        if (itemName != null)
        {
            BuiltInRegistries.ITEM.get(itemName).ifPresent(
                ref -> setItem(ref.value().getDefaultInstance())
            );
        }

        this.renderItemDecorations = params.getBoolean("renderItemDecorations", renderItemDecorations);
    }

    /**
     * Set the item of the icon.
     *
     * @param itemStack the itemstack to set.
     */
    public void setItem(final ItemStack itemStack)
    {
        clearDataAndScheduleTooltipUpdate();
        this.itemStack = itemStack;
        onItemUpdate();
    }

    /**
     * Called when itemStack was changed
     */
    protected void onItemUpdate()
    {

    }

    /**
     * Get the itemstack of the icon.
     *
     * @return the stack of it.
     */
    public ItemStack getItem()
    {
        return this.itemStack;
    }

    /**
     * @param renderDecorations true if should render itemStack decorations (enchantment foil, itemStack count, ...)
     */
    public void setRenderItemDecorations(final boolean renderDecorations)
    {
        this.renderItemDecorations = renderDecorations;
    }

    /**
     * @return true if should render itemStack decorations (enchantment foil, itemStack count, ...)
     */
    public boolean renderItemDecorations()
    {
        return renderItemDecorations;
    }

    /**
     * Resets all data in this item icon, effectively making it empty.
     */
    public void clearDataAndScheduleTooltipUpdate()
    {
        itemStack = null;
        tooltipUpdateScheduled = true;
    }

    protected boolean isItemEmpty()
    {
        return itemStack == null || itemStack.isEmpty();
    }

    public boolean isDataEmpty()
    {
        return isItemEmpty();
    }

    protected void updateTooltipIfNeeded()
    {
        if (tooltipUpdateScheduled)
        {
            if (onHover instanceof final AutomaticTooltip tooltip)
            {
                tooltip.setTextOld(getModifiedItemStackTooltip());
            }
            tooltipUpdateScheduled = false;
        }
    }

    @Override
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        updateTooltipIfNeeded();
        if (!isDataEmpty())
        {
            final PoseStack ms = target.pose();
            ms.pushPose();
            ms.translate(x, y, 0.0f);
            ms.scale(this.getWidth() / DEFAULT_ITEMSTACK_SIZE, this.getHeight() / DEFAULT_ITEMSTACK_SIZE, 1.0f);

            ms.last().normal().identity(); // reset normals cuz lighting
            target.renderItem(itemStack, 0, 0);
            if (renderItemDecorations)
            {
                target.renderItemDecorations(itemStack, 0, 0);
            }

            // Blend state management removed in 1.21.11
            ms.popPose();
        }
    }

    @Override
    public void onUpdate()
    {
        if (onHover == null && !isItemEmpty())
        {
            new AutomaticTooltipBuilder().hoverPane(this).build();
            tooltipUpdateScheduled = true;
        }
    }

    /**
     * @param tooltipList tooltip to modify
     * @param nameOffset points to element right after last name element
     * @return incremented name offset (if other names were added)
     */
    protected int modifyTooltipName(final List<Component> tooltipList, final TooltipFlag tooltipFlags, final int nameOffset)
    {
        return nameOffset;
    }

    /**
     * @param tooltipList tooltip to modify
     * @param prevTooltipSize tooltip size before any modifications
     * @return new prevTooltipSize
     */
    protected int appendTooltip(final List<Component> tooltipList, final TooltipFlag tooltipFlags, final int prevTooltipSize)
    {
        return prevTooltipSize;
    }

    /**
     * Adds spacer and optional data
     */
    public List<Component> getModifiedItemStackTooltip()
    {
        if (isDataEmpty())
        {
            return Collections.emptyList();
        }

        TooltipFlag.Default tooltipFlags = mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        if (mc.player.isCreative())
        {
            tooltipFlags = tooltipFlags.asCreative();
        }

        final List<Component> tooltipList = itemStack.getTooltipLines(TooltipContext.of(mc.level), mc.player, tooltipFlags);
        int nameOffset = 1;

        nameOffset = modifyTooltipName(tooltipList, tooltipFlags, nameOffset);

        int prevTooltipSize = tooltipList.size();

        prevTooltipSize = appendTooltip(tooltipList, tooltipFlags, prevTooltipSize);

        if (prevTooltipSize != tooltipList.size())
        {
            // add "show more info" text
            tooltipList.add(ToggleableTextComponent.ofNegated(ItemIcon::isShiftDown, Component.empty()));
            tooltipList.add(ToggleableTextComponent.ofNegated(ItemIcon::isShiftDown,
                Component.translatable("blockui.tooltip.item_additional_info", Component.translatable("key.keyboard.left.shift"))
                    .withStyle(ChatFormatting.GOLD)));
        }

        tooltipList.add(nameOffset, FIX_VANILLA_TOOLTIP);
        return tooltipList;
    }

    protected static MutableComponent wrapShift(final MutableComponent wrapped)
    {
        return ToggleableTextComponent.of(ItemIcon::isShiftDown, wrapped);
    }

    protected static MutableComponent wrapShift(final MutableComponent wrapped, final boolean shouldWrap)
    {
        return shouldWrap ? ToggleableTextComponent.of(ItemIcon::isShiftDown, wrapped) : wrapped;
    }
}

package com.ultra.megamod.lib.accessories.client.gui.components;

import com.ultra.megamod.lib.accessories.api.menu.AccessoriesBasedSlot;
import com.ultra.megamod.lib.accessories.client.gui.AccessoriesScreen;
import com.ultra.megamod.lib.accessories.impl.option.PlayerOptions;
import com.ultra.megamod.lib.accessories.owo.ui.container.UIContainers;
import com.ultra.megamod.lib.accessories.owo.ui.container.FlowLayout;
import com.ultra.megamod.lib.accessories.owo.ui.core.*;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AccessoriesContainingLayout<D extends AccessoriesContainingLayout.LayoutData> extends FlowLayout {

    protected final AccessoriesScreen screen;
    protected final D layoutData;

    protected AccessoriesContainingLayout(AccessoriesScreen screen, D layoutData) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);

        this.screen = screen;
        this.layoutData = layoutData;

        this.id(defaultID());

        this.buildLayout(layoutData);

        updatePadding();
    }

    public int getCurrentWidth() {
        var currentGroup = getCurrentGroup();

        var slotsWidth = currentGroup.totalColumnCount() * 18;

        if (layoutData.sideBySide() && screen.showCosmeticState()) {
            slotsWidth = (slotsWidth * 2) + 3;
        }

        return slotsWidth + 14;
    }

    public int getMaxPossibleWidth() {
        var slotsWidth = layoutData.maxColumnCount() * 18;

        if (layoutData.sideBySide() && screen.showCosmeticState()) {
            slotsWidth = (slotsWidth * 2) + 3;
        }

        return slotsWidth + 14;
    }

    public int getPaddingOffset() {
        return getMaxPossibleWidth() - getCurrentWidth();
    }

    public void updatePadding() {
        var padding = getPaddingOffset();

        this.padding(this.screen.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION) ? Insets.left(padding) : Insets.right(padding));
    }

    public static String defaultID() {
        return "outer_accessories_layout";
    }

    public abstract void onCosmeticToggle(boolean showCosmeticState);

    @Nullable
    public Boolean isHovering_Logical(Slot slot, double mouseX, double mouseY) {
        for (var child : getAlternativeChecks(this.screen.showCosmeticState())) {
            if (child.isInBoundingBox(mouseX, mouseY)) return false;
        }

        return null;
    }

    protected abstract Iterable<PositionedRectangle> getAlternativeChecks(boolean showingCosmetics);

    protected abstract void buildLayout(D layoutData);

    protected abstract BaseLayoutGroup getCurrentGroup();

    //--

    protected static BaseLayoutGroup createBaseLayoutGroup(AccessoriesScreen screen, List<Slot> slots, int totalRowCount, int maxColumnCount, int colStartingIndexOffset, boolean sideBySide) {
        var accessoriesLayout = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        var cosmeticsLayout = UIContainers.verticalFlow(Sizing.content(), Sizing.content());

        var accessoriesChecks = new ArrayList<PositionedRectangle>();
        var cosmeticChecks = new ArrayList<PositionedRectangle>();

        var totalColumnCount = maxColumnCount;

        for (int row = 0; row < totalRowCount; row++) {
            var colStartingIndex = colStartingIndexOffset + (row * (maxColumnCount * 2));

            var accessoriesRowLayout = (FlowLayout) UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
                    .id("row_" + row);

            var cosmeticRowLayout = (FlowLayout) UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
                    .id("row_" + row);

            var accessoriesRowButtons = new ArrayList<PositionedRectangle>();
            var cosmeticRowButtons = new ArrayList<PositionedRectangle>();

            var overMaxSlots = false;

            for (int col = 0; col < maxColumnCount; col++) {
                var cosmetic = colStartingIndex + (col * 2);
                var accessory = cosmetic + 1;

                if (accessory >= slots.size() || cosmetic >= slots.size()) {
                    overMaxSlots = true;

                    if (row == 0) {
                        totalColumnCount = col;
                    }

                    break;
                }

                var cosmeticSlot = (AccessoriesBasedSlot) slots.get(cosmetic);
                var accessorySlot = (AccessoriesBasedSlot) slots.get(accessory);

                // MegaMod customization: we only want accessory slots visible — cosmetic slots
                // are always hidden and disabled. The paired cosmetic container still exists in
                // the menu (can't remove without restructuring AccessoriesMenu), but it never gets
                // rendered or targeted by clicks. This prevents the duplicate-slot UX where items
                // appear in both accessory and cosmetic positions.
                screen.hideSlot(cosmeticSlot);
                screen.hideSlot(accessorySlot);
                screen.enableSlot(accessorySlot);
                screen.disableSlot(cosmeticSlot);

                var accessoryComponentData = ComponentUtils.createSlotWithToggle(accessorySlot, screen::slotAsComponent, false);

                accessoriesRowLayout.child(accessoryComponentData.first());

                if (accessoryComponentData.second() != null) accessoriesRowButtons.add(accessoryComponentData.second());
            }

            accessoriesLayout.child(accessoriesRowLayout);
            cosmeticsLayout.child(cosmeticRowLayout);

            accessoriesChecks.add(CollectedPositionedRectangle.of(accessoriesRowLayout, accessoriesRowButtons));

            var checks = CollectedPositionedRectangle.of(cosmeticRowLayout, cosmeticRowButtons);
            if (!checks.isEmpty()) cosmeticChecks.add(checks);

            if (overMaxSlots) break;
        }

        return new BaseLayoutGroup(accessoriesLayout, cosmeticsLayout, accessoriesChecks, cosmeticChecks, totalColumnCount);
    }

    public interface LayoutData {
        default int width() {
            return maxColumnCount() * 18;
        }

        default int height() {
            return maxRowCount() * 18;
        }

        int maxColumnCount();

        int maxRowCount();

        boolean sideBySide();
    }

    protected record BaseLayoutGroup(FlowLayout accessoriesLayout, FlowLayout cosmeticLayout, List<PositionedRectangle> accessoriesBtnChecks, List<PositionedRectangle> cosmeticBtnChecks, int totalColumnCount) {
        public static final BaseLayoutGroup DEFAULT = new BaseLayoutGroup(
                UIContainers.verticalFlow(Sizing.content(), Sizing.content()),
                UIContainers.verticalFlow(Sizing.content(), Sizing.content()),
                List.of(),
                List.of(),
                0);

        public FlowLayout getLayout(boolean isCosmetic) {
            return isCosmetic ? cosmeticLayout : accessoriesLayout;
        }

        public List<PositionedRectangle> getAlternativeChecks(boolean isCosmetic) {
            return isCosmetic ? cosmeticBtnChecks : accessoriesBtnChecks;
        }

        public int width(boolean isCosmetic, boolean sideBySide) {
            int baseWidth = 0;

            if (sideBySide) {
                baseWidth += getComponentWidth(false);

                if (isCosmetic) {
                    baseWidth += getComponentWidth(true) + 3;
                }
            } else {
                baseWidth += getComponentWidth(isCosmetic);
            }

            return baseWidth;
        }

        private int getComponentWidth(boolean isCosmetic) {
            var component = getLayout(isCosmetic);

            if (component.parent() == null) {
                component.inflate(Size.square(Integer.MAX_VALUE));
            }

            return component.width();
        }
    }
}

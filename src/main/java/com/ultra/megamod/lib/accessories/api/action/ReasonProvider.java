package com.ultra.megamod.lib.accessories.api.action;

import com.ultra.megamod.lib.accessories.api.tooltip.ListTooltipAdder;
import com.ultra.megamod.lib.accessories.api.tooltip.impl.ListTooltipEntry;
import com.ultra.megamod.lib.accessories.api.tooltip.TooltipInfoProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

///
/// Base classed used by [ActionResponse] as a way to allow for more info
/// to be provided in a tooltip typically used to give meaning to an action
/// result.
///
public interface ReasonProvider extends TooltipInfoProvider<ListTooltipAdder> {
    void addInfo(ListTooltipAdder adder, Item.TooltipContext ctx, TooltipFlag type);
}

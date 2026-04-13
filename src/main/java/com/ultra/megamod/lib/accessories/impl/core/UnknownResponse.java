package com.ultra.megamod.lib.accessories.impl.core;

import com.ultra.megamod.lib.accessories.api.action.ActionResponse;
import com.ultra.megamod.lib.accessories.api.action.ValidationState;
import com.ultra.megamod.lib.accessories.api.tooltip.ListTooltipAdder;
import com.ultra.megamod.lib.accessories.api.tooltip.impl.ListTooltipEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public record UnknownResponse(ValidationState canPerformAction) implements ActionResponse {
    @Override
    public void addInfo(ListTooltipAdder adder, Item.TooltipContext ctx, TooltipFlag type) {
        adder.add(Component.literal(canPerformAction.isValid() ? "No Restrictions found!" : "Unknown restriction disallows such!"));
    }
}

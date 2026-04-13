package com.ultra.megamod.lib.accessories.api.slot.validator;

import com.ultra.megamod.lib.accessories.Accessories;
import com.ultra.megamod.lib.accessories.api.action.ActionResponse;
import net.minecraft.network.chat.Component;

public class SlotValidatorReasons {
    public static final Component INVALID_ITEM = Component.literal("The given Item is not allowed within the slot");

    static final ActionResponse ALWAYS_VALID = ActionResponse.of(false, Accessories.translation("tooltip.validator.always_valid"));
    static final ActionResponse ALWAYS_INVALID = ActionResponse.of(false, Accessories.translation("tooltip.validator.always_invalid"));
}

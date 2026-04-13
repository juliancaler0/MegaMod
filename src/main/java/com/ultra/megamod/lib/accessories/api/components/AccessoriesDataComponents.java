package com.ultra.megamod.lib.accessories.api.components;

import com.ultra.megamod.lib.accessories.Accessories;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationAttributes;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.owo.serialization.CodecUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.UnaryOperator;

public class AccessoriesDataComponents {

    private static final SerializationContext BASE_CTX = SerializationContext.attributes(SerializationAttributes.HUMAN_READABLE);

    private static final DeferredRegister<DataComponentType<?>> REGISTER =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, com.ultra.megamod.MegaMod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AccessoryNestContainerContents>> NESTED_ACCESSORIES =
            REGISTER.register("nested_accessories",
                    () -> DataComponentType.<AccessoryNestContainerContents>builder()
                            .persistent(CodecUtils.toCodec(AccessoryNestContainerContents.ENDEC))
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AccessorySlotValidationComponent>> SLOT_VALIDATION =
            REGISTER.register("slot_validation",
                    () -> DataComponentType.<AccessorySlotValidationComponent>builder()
                            .persistent(CodecUtils.toCodec(AccessorySlotValidationComponent.ENDEC))
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AccessoryItemAttributeModifiers>> ATTRIBUTES =
            REGISTER.register("attributes",
                    () -> DataComponentType.<AccessoryItemAttributeModifiers>builder()
                            .persistent(CodecUtils.toCodec(AccessoryItemAttributeModifiers.ENDEC))
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AccessoryStackSettings>> STACK_SETTINGS =
            REGISTER.register("stack_settings",
                    () -> DataComponentType.<AccessoryStackSettings>builder()
                            .persistent(CodecUtils.toCodec(AccessoryStackSettings.ENDEC))
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AccessoryCustomRendererComponent>> CUSTOM_RENDERER =
            REGISTER.register("custom_renderer",
                    () -> DataComponentType.<AccessoryCustomRendererComponent>builder()
                            .persistent(CodecUtils.toCodec(AccessoryCustomRendererComponent.ENDEC))
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AccessoryMobEffectsComponent>> MOB_EFFECTS =
            REGISTER.register("mob_effects",
                    () -> DataComponentType.<AccessoryMobEffectsComponent>builder()
                            .persistent(CodecUtils.toCodec(AccessoryMobEffectsComponent.ENDEC))
                            .build());

    @ApiStatus.Internal
    public static void init(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
    }
}

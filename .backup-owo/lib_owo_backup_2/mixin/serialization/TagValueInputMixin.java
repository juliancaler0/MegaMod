package com.ultra.megamod.lib.owo.mixin.serialization;

import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.KeyedEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.util.MapCarrierDecodable;
import com.ultra.megamod.lib.owo.serialization.CodecUtils;
import com.ultra.megamod.lib.owo.serialization.endec.KeyedEndecDecodeError;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInputContextHelper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TagValueInput.class)
public abstract class TagValueInputMixin implements MapCarrierDecodable {
    @Shadow
    @Final
    private CompoundTag input;

    @Shadow
    @Final
    private ProblemReporter problemReporter;

    @Shadow
    @Final
    private ValueInputContextHelper context;

    // TODO: Maybe pass in the ErrorReporter for use within Endecs?
    @Override
    public <T> T getWithErrors(SerializationContext ctx, @NotNull KeyedEndec<T> key) {
        ctx = CodecUtils.createContext(this.context.ops(), ctx);

        return this.input.getWithErrors(ctx, key);
    }

    @Override
    public <T> T get(SerializationContext ctx, @NotNull KeyedEndec<T> key) {
        try {
            return this.getWithErrors(ctx, key);
        } catch (Exception e) {
            this.problemReporter.report(new KeyedEndecDecodeError(key, this.input.get(key.key()), e));

            return key.defaultValue();
        }
    }
}

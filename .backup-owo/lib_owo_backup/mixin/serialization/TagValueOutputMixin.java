package com.ultra.megamod.lib.owo.mixin.serialization;

import com.mojang.serialization.DynamicOps;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.KeyedEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.util.MapCarrierEncodable;
import com.ultra.megamod.lib.owo.serialization.CodecUtils;
import com.ultra.megamod.lib.owo.serialization.endec.KeyedEndecEncodeError;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TagValueOutput.class)
public abstract class TagValueOutputMixin implements MapCarrierEncodable {
    @Shadow
    @Final
    private CompoundTag output;

    @Shadow
    @Final
    private ProblemReporter problemReporter;

    @Shadow
    @Final
    private DynamicOps<Tag> ops;

    @Override
    public <T> void put(SerializationContext ctx, @NotNull KeyedEndec<T> key, @NotNull T value) {
        ctx = CodecUtils.createContext(this.ops, ctx);

        try {
            this.output.put(ctx, key, value);
        } catch (Exception e) {
            problemReporter.report(new KeyedEndecEncodeError(key, value, e, false));
        }
    }
}

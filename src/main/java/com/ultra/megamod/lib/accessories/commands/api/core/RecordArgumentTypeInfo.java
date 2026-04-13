package com.ultra.megamod.lib.accessories.commands.api.core;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiFunction;
import java.util.function.Function;

public record RecordArgumentTypeInfo<A extends ArgumentType<?>, T>(StructEndec<T> endec, Function<A, T> toTemplate, BiFunction<CommandBuildContext, T, A> fromTemplate) implements ArgumentTypeInfo<A, RecordArgumentTypeInfo.RecordInfoTemplate<A, T>> {

    public static <A extends ArgumentType<?>, T> RecordArgumentTypeInfo<A, T> of(StructEndec<T> endec, Function<A, T> toTemplate, Function<T, A> fromTemplate){
        return of(endec, toTemplate, (ctx, t) -> fromTemplate.apply(t));
    }

    public static <A extends ArgumentType<?>, T> RecordArgumentTypeInfo<A, T> of(StructEndec<T> endec, Function<A, T> toTemplate, BiFunction<CommandBuildContext, T, A> fromTemplate){
        return new RecordArgumentTypeInfo<>(endec, toTemplate, fromTemplate);
    }

    public static <A extends ArgumentType<?>, T> RecordArgumentTypeInfo<A, T> of(Endec<T> endec, String fieldName, Function<A, T> toTemplate, Function<T, A> fromTemplate){
        return of(endec, fieldName, toTemplate, (ctx, t) -> fromTemplate.apply(t));
    }

    public static <A extends ArgumentType<?>, T> RecordArgumentTypeInfo<A, T> of(Endec<T> endec, String fieldName, Function<A, T> toTemplate, BiFunction<CommandBuildContext, T, A> fromTemplate){
        return new RecordArgumentTypeInfo<>(endec.structOf(fieldName), toTemplate, fromTemplate);
    }

    @SuppressWarnings("unchecked")
    public static <A extends ArgumentType<?>> RecordArgumentTypeInfo<A, Void> of(Function<CommandBuildContext, A> argTypeConstructor) {
        return new RecordArgumentTypeInfo<>(Endec.<Void>unit((Void) null), a -> null, (commandBuildContext, unused) -> argTypeConstructor.apply(commandBuildContext));
    }

    @Override
    public void serializeToNetwork(RecordInfoTemplate<A, T> template, FriendlyByteBuf buffer) {
        // Stub - encoding via ByteBuf not used in this adapter
    }

    @Override
    public RecordInfoTemplate<A, T> deserializeFromNetwork(FriendlyByteBuf buffer) {
        // Stub - decoding via ByteBuf not used in this adapter
        return new RecordInfoTemplate<>(this, null, fromTemplate);
    }

    @Override
    public void serializeToJson(RecordInfoTemplate<A, T> template, JsonObject json) {
        // Stub - JSON serialization not critical for this adapter
    }

    @Override
    public RecordInfoTemplate<A, T> unpack(A argument) {
        return new RecordInfoTemplate<>(this, toTemplate.apply(argument), fromTemplate);
    }

    public record RecordInfoTemplate<A extends ArgumentType<?>, T>(ArgumentTypeInfo<A, ?> type, T data, BiFunction<CommandBuildContext, T, A> fromTemplate) implements Template<A> {
        @Override public A instantiate(CommandBuildContext ctx) { return fromTemplate.apply(ctx, data()); }
    }
}

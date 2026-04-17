package com.ultra.megamod.lib.owo.client.screens;

import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

@ApiStatus.Internal
public record ScreenhandlerMessageData<T>(int id, boolean clientbound, Endec<T> endec, Consumer<T> handler) {}

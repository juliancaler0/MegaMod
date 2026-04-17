package com.ultra.megamod.lib.owo.text;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface TextLanguage {
    @Nullable Component getText(String key);
}

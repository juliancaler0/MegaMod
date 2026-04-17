package com.ultra.megamod.lib.owo.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to a config option of type
 * {@link com.ultra.megamod.lib.owo.ui.core.Color} to indicate
 * that the config screen should expose the alpha
 * component
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WithAlpha {}

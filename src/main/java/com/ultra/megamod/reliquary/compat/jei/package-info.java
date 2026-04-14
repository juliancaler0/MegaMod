/**
 * Reliquary ↔ JEI compat layer.
 *
 * <p>A compileOnly JEI API for 1.21.11 (mezz.jei:jei-1.21.11-*-api:27.4.0.22)
 * is declared in {@code build.gradle}, but the upstream Reliquary compat is
 * quite large — it spans eight subpackages (alkahestry, cauldron, mortar,
 * infernaltear, lingering, magazines, subtype interpreters, description
 * builder). Fully restoring that surface requires re-porting several internal
 * recipe-category UI classes that were pruned earlier; that's out of scope
 * for this compile-clean compat restore pass.
 *
 * <p>{@link com.ultra.megamod.reliquary.compat.jei.ReliquaryPlugin} is kept
 * as a minimal {@code @JeiPlugin} shell so the JEI discovery succeeds at
 * runtime when both mods are present — it's a valid plugin and won't throw,
 * but it registers zero categories/recipes. Restoring individual capabilities
 * (subtype interpreters, description builder, crafting recipe feeds) can land
 * incrementally.
 */
package com.ultra.megamod.reliquary.compat.jei;

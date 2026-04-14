/**
 * Reliquary ↔ Jade compat layer.
 *
 * <p><b>Status: deferred.</b> No {@code snownee.jade:jade-1.21.11-neoforge}
 * Maven artifact is published at the time of this port, so the rich
 * {@code snownee.jade.api.IWailaPlugin}/{@code IBlockComponentProvider}
 * integration from the upstream Reliquary source cannot be compile-checked.
 * The runtime entry point {@link com.ultra.megamod.reliquary.compat.jade.JadeCompat}
 * is kept as a no-op placeholder so
 * {@link com.ultra.megamod.reliquary.init.ModCompat} can dispatch to it
 * without reflection — once a 1.21.11 Jade artifact is published the
 * {@code DataProviderAltar/Cauldron/Mortar/Pedestal} ports under the
 * {@code provider/} subpackage can land here.
 */
package com.ultra.megamod.reliquary.compat.jade;

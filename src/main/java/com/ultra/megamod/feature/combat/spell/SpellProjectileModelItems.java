package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.MegaMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers invisible dummy {@link Item}s under {@code megamod:spell_projectile/<name>} so
 * {@link com.ultra.megamod.feature.combat.spell.client.SpellProjectileRenderer} can resolve
 * the projectile model ID through {@code BuiltInRegistries.ITEM.getOptional(id)} and submit
 * the corresponding baked model via {@code ItemStackRenderState.submit}.
 *
 * <p>Each item's in-game model comes from its paired {@code assets/megamod/items/spell_projectile/<name>.json}
 * definition file, which forwards to the authored Blockbench geometry at
 * {@code assets/megamod/models/spell_projectile/<name>.json}. The items are never given to players —
 * they're creative-tab hidden and exist purely as a model binding.</p>
 */
public final class SpellProjectileModelItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);

    public static final DeferredItem<Item> ARCANE_BOLT    = register("spell_projectile/arcane_bolt");
    public static final DeferredItem<Item> ARCANE_MISSILE = register("spell_projectile/arcane_missile");
    public static final DeferredItem<Item> FIRE_BLAST     = register("spell_projectile/fire_blast");
    public static final DeferredItem<Item> FIRE_METEOR    = register("spell_projectile/fire_meteor");
    public static final DeferredItem<Item> FIREBALL       = register("spell_projectile/fireball");
    public static final DeferredItem<Item> FROST_SHARD    = register("spell_projectile/frost_shard");
    public static final DeferredItem<Item> FROSTBOLT      = register("spell_projectile/frostbolt");
    public static final DeferredItem<Item> JUDGEMENT      = register("spell_projectile/judgement");
    public static final DeferredItem<Item> MAGIC_ARROW    = register("spell_projectile/magic_arrow");
    public static final DeferredItem<Item> MISSILE        = register("spell_projectile/missile");
    public static final DeferredItem<Item> SHOCKWAVE      = register("spell_projectile/shockwave");
    public static final DeferredItem<Item> SHOCKWAVE_LARGE = register("spell_projectile/shockwave_large");

    private SpellProjectileModelItems() {}

    private static DeferredItem<Item> register(String path) {
        return ITEMS.registerSimpleItem(path, new Item.Properties().stacksTo(1));
    }

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}

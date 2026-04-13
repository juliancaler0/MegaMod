package com.ultra.megamod.lib.combatroll.api;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;

public class CombatRoll {
    public static final String NAMESPACE = "megamod";

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ATTRIBUTE, MegaMod.MODID);

    public static class Attributes {
        public static final ArrayList<Entry> all = new ArrayList<>();
        private static Entry entry(String name, double baseValue, double minValue, double maxValue, boolean tracked) {
            var entry = new Entry(name, baseValue, minValue, maxValue, tracked);
            all.add(entry);
            return entry;
        }

        public static class Entry {
            public final Identifier id;
            public final String translationKey;
            public final double baseValue;
            public final DeferredHolder<Attribute, Attribute> holder;
            /** Alias for the holder, used as Holder<Attribute> throughout the codebase */
            public final Holder<Attribute> entry;

            public Entry(String name, double baseValue, double minValue, double maxValue, boolean tracked) {
                this.id = Identifier.fromNamespaceAndPath(NAMESPACE, "combatroll." + name);
                this.translationKey = "attribute.name." + NAMESPACE + ".combatroll." + name;
                this.baseValue = baseValue;
                this.holder = ATTRIBUTES.register("combatroll." + name,
                        () -> new RangedAttribute(translationKey, baseValue, minValue, maxValue).setSyncable(tracked));
                this.entry = this.holder;
            }
        }

        public static final Entry DISTANCE = entry("distance", 3.0, 1, 24.0, true);
        public static final Entry RECHARGE = entry("recharge", 20, 0.1, 200, true);
        public static final Entry COUNT = entry("count", 1, 0, 20.0, true);
    }
}

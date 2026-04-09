package com.ultra.megamod.feature.relics.data;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public record RelicAttributeBonus(String attributeId, double value) {

    private static final List<BonusPool> POOL = List.of(
        new BonusPool("attack_damage", Attributes.ATTACK_DAMAGE, 0.5, 2.0, AttributeModifier.Operation.ADD_VALUE, "+%.1f Attack Damage"),
        new BonusPool("armor", Attributes.ARMOR, 0.5, 2.0, AttributeModifier.Operation.ADD_VALUE, "+%.1f Armor"),
        new BonusPool("max_health", Attributes.MAX_HEALTH, 1.0, 4.0, AttributeModifier.Operation.ADD_VALUE, "+%.1f Max Health"),
        new BonusPool("movement_speed", Attributes.MOVEMENT_SPEED, 0.005, 0.02, AttributeModifier.Operation.ADD_VALUE, "+%.3f Speed"),
        new BonusPool("attack_speed", Attributes.ATTACK_SPEED, 0.05, 0.2, AttributeModifier.Operation.ADD_VALUE, "+%.2f Attack Speed"),
        new BonusPool("knockback_resistance", Attributes.KNOCKBACK_RESISTANCE, 0.05, 0.15, AttributeModifier.Operation.ADD_VALUE, "+%.2f Knockback Resist"),
        new BonusPool("luck", Attributes.LUCK, 0.5, 2.0, AttributeModifier.Operation.ADD_VALUE, "+%.1f Luck")
    );

    public static List<RelicAttributeBonus> rollBonuses(RandomSource random, int quality) {
        int count = 1 + random.nextInt(2); // 1-2 base
        if (quality >= 8) {
            count = 3; // high quality always gets 3
        } else if (quality >= 5) {
            count = Math.min(count + 1, 3); // mid quality gets +1
        }

        List<Integer> available = new ArrayList<>();
        for (int i = 0; i < POOL.size(); i++) {
            available.add(i);
        }

        List<RelicAttributeBonus> bonuses = new ArrayList<>();
        for (int i = 0; i < count && !available.isEmpty(); i++) {
            int idx = available.remove(random.nextInt(available.size()));
            BonusPool pool = POOL.get(idx);
            double value = pool.min + (pool.max - pool.min) * random.nextDouble();
            // Round to avoid floating point noise
            value = Math.round(value * 1000.0) / 1000.0;
            bonuses.add(new RelicAttributeBonus(pool.id, value));
        }
        return bonuses;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("attribute", this.attributeId);
        tag.putDouble("value", this.value);
        return tag;
    }

    public static RelicAttributeBonus fromTag(CompoundTag tag) {
        return new RelicAttributeBonus(
            tag.getStringOr("attribute", ""),
            tag.getDoubleOr("value", 0.0)
        );
    }

    public static BonusPool getPoolEntry(String attributeId) {
        for (BonusPool pool : POOL) {
            if (pool.id.equals(attributeId)) return pool;
        }
        return null;
    }

    public String getDisplayText() {
        BonusPool pool = getPoolEntry(this.attributeId);
        if (pool == null) return this.attributeId + ": " + this.value;
        return String.format(pool.displayFormat, this.value);
    }

    public Identifier getModifierId(String slotName, int index) {
        return Identifier.fromNamespaceAndPath("megamod", "relic_bonus_" + slotName + "_" + this.attributeId + "_" + index);
    }

    public record BonusPool(
        String id,
        Holder<Attribute> attribute,
        double min,
        double max,
        AttributeModifier.Operation operation,
        String displayFormat
    ) {}
}

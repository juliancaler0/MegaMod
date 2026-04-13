package com.ultra.megamod.lib.combatroll.enchantments;

// Enchantments in 1.21+ are data-driven via JSON.
// This class is preserved as a stub for API compatibility.
// The actual enchantment logic is defined in:
//   data/megamod/enchantment/combatroll_multi_roll.json
//   data/megamod/enchantment/combatroll_acrobat.json
//   data/megamod/enchantment/combatroll_longfooted.json

//public class AmplifierEnchantment extends Enchantment implements CustomConditionalEnchantment {
//    public Operation operation;
//    public enum Operation {
//        ADD, MULTIPLY;
//    }
//
//    public EnchantmentConfig properties;
//
//    public double apply(double value, int level) {
//        switch (operation) {
//            case ADD -> {
//                return value += ((float)level) * properties.bonus_per_level;
//            }
//            case MULTIPLY -> {
//                return value *= 1F + ((float)level) * properties.bonus_per_level;
//            }
//        }
//        assert true;
//        return 0F;
//    }
//
//    public AmplifierEnchantment(Rarity weight, Operation operation, EnchantmentConfig properties, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
//        super(weight, type, slotTypes);
//        this.operation = operation;
//        this.properties = properties;
//    }
//
//    public int getMaxLevel() {
//        if (!properties.enabled) {
//            return 0;
//        }
//        return properties.max_level;
//    }
//
//    public int getMinPower(int level) {
//        return properties.min_cost + (level - 1) * properties.step_cost;
//    }
//
//    public int getMaxPower(int level) {
//        return super.getMinPower(level) + 50;
//    }
//
//    @Override
//    public boolean isAvailableForEnchantedBookOffer() {
//        return properties.enabled;
//    }
//
//    @Override
//    public boolean isAvailableForRandomSelection() {
//        return properties.enabled;
//    }
//
//    // MARK: CustomConditionalEnchantment
//
//    @Override
//    public boolean isAcceptableItem(ItemStack stack) {
//        if (condition != null) {
//            return condition.isAcceptableItem(stack);
//        }
//        return super.isAcceptableItem(stack);
//    }
//
//    private Condition condition;
//
//    @Override
//    public void setCondition(Condition condition) {
//        this.condition = condition;
//    }
//
//    public AmplifierEnchantment condition(Condition condition) {
//        setCondition(condition);
//        return this;
//    }
//}

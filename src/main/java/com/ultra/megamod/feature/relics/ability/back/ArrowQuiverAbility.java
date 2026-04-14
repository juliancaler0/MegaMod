/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.projectile.arrow.AbstractArrow$Pickup
 *  net.minecraft.world.entity.projectile.arrow.Arrow
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 */
package com.ultra.megamod.feature.relics.ability.back;

import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class ArrowQuiverAbility {
    private static final Identifier AGILITY_MODIFIER_ID = Identifier.fromNamespaceAndPath((String)"megamod", (String)"relic_arrow_quiver_agility");
    public static final List<RelicAbility> ABILITIES = List.of(new RelicAbility("Receptacle", "Arrows near you are auto-collected", 1, RelicAbility.CastType.PASSIVE, List.of(new RelicStat("pickup_range", 3.0, 7.0, RelicStat.ScaleType.ADD, 0.5))), new RelicAbility("Leap", "Launch yourself upward", 3, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("launch_power", 0.5, 1.5, RelicStat.ScaleType.ADD, 0.2))), new RelicAbility("Agility", "Increases movement speed", 5, RelicAbility.CastType.PASSIVE, List.of(new RelicStat("speed_bonus", 0.01, 0.03, RelicStat.ScaleType.ADD, 0.005))), new RelicAbility("Rain", "Summon a circle of arrows from above", 7, RelicAbility.CastType.INSTANTANEOUS, List.of(new RelicStat("arrow_count", 6.0, 12.0, RelicStat.ScaleType.ADD, 1.0), new RelicStat("damage", 3.0, 8.0, RelicStat.ScaleType.MULTIPLY_BASE, 0.1))));

    public static void register() {
        AbilityCastHandler.registerAbility("Arrow Quiver", "Receptacle", ArrowQuiverAbility::executeReceptacle);
        AbilityCastHandler.registerAbility("Arrow Quiver", "Leap", ArrowQuiverAbility::executeLeap);
        AbilityCastHandler.registerAbility("Arrow Quiver", "Agility", ArrowQuiverAbility::executeAgility);
        AbilityCastHandler.registerAbility("Arrow Quiver", "Rain", ArrowQuiverAbility::executeRain);
    }

    private static void executeReceptacle(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 10 != 0) {
            return;
        }
        double pickupRange = stats[0];
        ServerLevel level = player.level();
        AABB area = new AABB(player.blockPosition()).inflate(pickupRange);
        List<ItemEntity> arrowItems = level.getEntitiesOfClass(ItemEntity.class, area, itemEntity -> {
            ItemStack itemStack = itemEntity.getItem();
            return itemStack.is(Items.ARROW) || itemStack.is(Items.SPECTRAL_ARROW) || itemStack.is(Items.TIPPED_ARROW);
        });
        for (ItemEntity arrowEntity : arrowItems) {
            ItemStack arrowStack = arrowEntity.getItem().copy();
            if (!player.getInventory().add(arrowStack)) continue;
            arrowEntity.discard();
        }
    }

    private static void executeLeap(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        double launchPower = stats[0];
        player.setDeltaMovement(player.getDeltaMovement().add(0.0, launchPower, 0.0));
        player.hurtMarked = true;
        player.fallDistance = 0.0;
    }

    private static void executeAgility(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        if (player.tickCount % 20 != 0) {
            return;
        }
        double speedBonus = stats[0];
        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(AGILITY_MODIFIER_ID);
            attribute.addTransientModifier(new AttributeModifier(AGILITY_MODIFIER_ID, speedBonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void executeRain(ServerPlayer player, ItemStack stack, RelicAbility ability, double[] stats) {
        int arrowCount = (int)stats[0];
        double damage = stats[1];
        ServerLevel level = player.level();

        // Spawn a lingering ArrowRainEntity that sprays arrows downward within a radius
        // over a duration. Uses arrowCount as the total volley size -> duration.
        com.ultra.megamod.feature.relics.entity.ArrowRainEntity rain =
            new com.ultra.megamod.feature.relics.entity.ArrowRainEntity(
                level, player.getX(), player.getY(), player.getZ(),
                player.getId(), arrowCount, 3.0F, (float) damage);
        level.addFreshEntity(rain);
    }
}


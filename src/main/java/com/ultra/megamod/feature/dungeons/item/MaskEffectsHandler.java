package com.ultra.megamod.feature.dungeons.item;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.relics.accessory.LibAccessoryLookup;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = MegaMod.MODID)
public class MaskEffectsHandler {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 40 != 0) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ItemStack headAccessory = LibAccessoryLookup.getEquipped(player, AccessorySlotType.FACE);
            if (headAccessory.isEmpty() || !(headAccessory.getItem() instanceof UmvuthanaMaskItem maskItem)) continue;

            UmvuthanaMaskItem.MaskType maskType = maskItem.getMaskType();
            switch (maskType) {
                case FEAR -> {
                    AABB area = player.getBoundingBox().inflate(8.0);
                    for (LivingEntity mob : player.level().getEntitiesOfClass(LivingEntity.class, area,
                            e -> e != player && e instanceof net.minecraft.world.entity.monster.Monster)) {
                        mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, false));
                    }
                }
                case FURY -> player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 0, true, false));
                case FAITH -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
                case RAGE -> player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 0, true, false));
                case MISERY -> {
                    AABB area = player.getBoundingBox().inflate(8.0);
                    for (LivingEntity mob : player.level().getEntitiesOfClass(LivingEntity.class, area,
                            e -> e != player && e instanceof net.minecraft.world.entity.monster.Monster)) {
                        mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0, true, false));
                    }
                }
                case BLISS -> player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, true, false));
            }
        }
    }
}

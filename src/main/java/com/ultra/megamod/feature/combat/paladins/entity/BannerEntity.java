package com.ultra.megamod.feature.combat.paladins.entity;

import com.ultra.megamod.lib.spellengine.entity.SpellCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class BannerEntity extends SpellCloud {
    public static EntityType<BannerEntity> ENTITY_TYPE;

    public BannerEntity(EntityType<? extends SpellCloud> entityType, Level level) {
        super(entityType, level);
    }
}

package de.cadentem.pufferfish_unofficial_additions.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PowderSnowBlock.class)
public abstract class PowderSnowBlockMixin {
    @WrapOperation(method = "canEntityWalkOnPowderSnow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;is(Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean pufferfish_unofficial_additions$checkSkill(final EntityType<?> instance, TagKey<EntityType<?>> tag, final Operation<Boolean> original, /* Method arguments: */ final Entity entity) {
        if (entity instanceof Player player) {
            if (player.getTags().contains("walk_on_powder_snow")) {
                return true;
            }
        }

        return original.call(instance, tag);
    }
}

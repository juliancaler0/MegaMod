package com.ultra.megamod.mixin.puffish_attributes;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.ultra.megamod.lib.puffish_attributes.api.DynamicModification;
import com.ultra.megamod.lib.puffish_attributes.api.PuffishAttributes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Applies {@code EXPERIENCE} as a player-touched orb is collected. Mirrors the reference
 * "workaround for mods that inject and cancel" comment: we wrap the whole {@code playerTouch}
 * call so we can measure the actual XP delta rather than the raw orb value — other mods'
 * cancellations still count for zero.
 *
 * <p>1.21.11 Parchment renames {@code ExperienceOrbEntity} to {@link ExperienceOrb},
 * the pickup callback from {@code onPlayerCollision} to {@code playerTouch}, and
 * {@code PlayerEntity#addExperience} to {@code Player#giveExperiencePoints}.
 * The Yarn {@code totalExperience} field is unchanged in Parchment.</p>
 */
@Mixin(ExperienceOrb.class)
public class ExperienceOrbEntityMixin {

	@WrapMethod(method = "playerTouch")
	private void wrapMethodOnPlayerCollision(Player player, Operation<Void> original) {
		// workaround for mods that inject and cancel
		var previousExperience = player.totalExperience;
		original.call(player);
		player.giveExperiencePoints(Math.round(DynamicModification.create()
				.withPositive(PuffishAttributes.EXPERIENCE, player)
				.relativeTo((float) (player.totalExperience - previousExperience))
		));
	}

}

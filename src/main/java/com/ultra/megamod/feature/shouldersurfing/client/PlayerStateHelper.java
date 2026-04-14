package com.ultra.megamod.feature.shouldersurfing.client;

import com.ultra.megamod.feature.shouldersurfing.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.core.component.DataComponentPatch;

class PlayerStateHelper
{
	protected static boolean isUsingItem(LivingEntity cameraEntity, Minecraft minecraft)
	{
		return cameraEntity.isUsingItem() && !cameraEntity.getUseItem().has(net.minecraft.core.component.DataComponents.FOOD)
			|| cameraEntity instanceof Player player && player.isScoping();
	}

	protected static boolean isInteracting(LivingEntity cameraEntity, Minecraft minecraft)
	{
		return minecraft.options.keyUse.isDown() && !cameraEntity.isUsingItem();
	}

	protected static boolean isAttacking(Minecraft minecraft)
	{
		return minecraft.options.keyAttack.isDown();
	}

	protected static boolean isPicking(Minecraft minecraft)
	{
		return minecraft.options.keyPickItem.isDown();
	}

	protected static boolean isRidingBoat(Minecraft minecraft, Entity entity)
	{
		if(!(entity instanceof LivingEntity))
		{
			return false;
		}

		Entity vehicle = entity.getVehicle();

		if(vehicle == null)
		{
			return false;
		}

		return vehicle instanceof AbstractBoat;
	}

	protected static boolean isHoldingAdaptiveItem(Minecraft minecraft, Entity entity)
	{
		if(!(entity instanceof LivingEntity living))
		{
			return false;
		}

		ItemStack mainHand = living.getMainHandItem();
		ItemStack offHand = living.getOffhandItem();

		if(matchesHoldRules(mainHand) || matchesHoldRules(offHand))
		{
			return true;
		}

		if(living.isUsingItem())
		{
			ItemStack useItem = living.getUseItem();
			return matchesUseRules(useItem);
		}

		return false;
	}

	private static boolean matchesHoldRules(ItemStack stack)
	{
		if(stack.isEmpty())
		{
			return false;
		}

		if(matchesItemList(stack, Config.CLIENT.getAdaptiveCrosshairHoldItems()))
		{
			return true;
		}

		if(matchesItemUseAnimation(stack, Config.CLIENT.getAdaptiveCrosshairHoldItemAnimations()))
		{
			return true;
		}

		if(matchesDataComponent(stack, Config.CLIENT.getAdaptiveCrosshairHoldItemDefaultComponents(), false))
		{
			return true;
		}

		return matchesDataComponent(stack, Config.CLIENT.getAdaptiveCrosshairHoldItemComponents(), true);
	}

	private static boolean matchesUseRules(ItemStack stack)
	{
		if(stack.isEmpty())
		{
			return false;
		}

		if(matchesItemList(stack, Config.CLIENT.getAdaptiveCrosshairUseItems()))
		{
			return true;
		}

		if(matchesItemUseAnimation(stack, Config.CLIENT.getAdaptiveCrosshairUseItemAnimations()))
		{
			return true;
		}

		if(matchesDataComponent(stack, Config.CLIENT.getAdaptiveCrosshairUseItemDefaultComponents(), false))
		{
			return true;
		}

		return matchesDataComponent(stack, Config.CLIENT.getAdaptiveCrosshairUseItemComponents(), true);
	}

	private static boolean matchesItemList(ItemStack stack, java.util.List<? extends String> patterns)
	{
		if(patterns.isEmpty())
		{
			return false;
		}

		Item item = stack.getItem();
		Identifier id = BuiltInRegistries.ITEM.getKey(item);

		if(id == null)
		{
			return false;
		}

		String idString = id.toString();

		for(String pattern : patterns)
		{
			try
			{
				if(idString.matches(pattern))
				{
					return true;
				}
			}
			catch(Exception ignored)
			{
			}
		}

		return false;
	}

	private static boolean matchesItemUseAnimation(ItemStack stack, java.util.List<? extends String> animations)
	{
		if(animations.isEmpty() || stack.isEmpty())
		{
			return false;
		}

		ItemUseAnimation animation = stack.getUseAnimation();
		String serialized = animation.getSerializedName();

		for(String entry : animations)
		{
			if(serialized.equals(entry))
			{
				return true;
			}
		}

		return false;
	}

	private static boolean matchesDataComponent(ItemStack stack, java.util.List<? extends String> componentIds, boolean modifiedOnly)
	{
		if(componentIds.isEmpty() || stack.isEmpty())
		{
			return false;
		}

		DataComponentPatch patch = stack.getComponentsPatch();

		for(String componentId : componentIds)
		{
			Identifier id = Identifier.tryParse(componentId);

			if(id == null)
			{
				continue;
			}

			DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(id);

			if(type == null)
			{
				continue;
			}

			if(modifiedOnly)
			{
				if(patch.get(type) != null)
				{
					return true;
				}
			}
			else
			{
				if(stack.has(type))
				{
					return true;
				}
			}
		}

		return false;
	}
}

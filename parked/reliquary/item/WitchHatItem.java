package com.ultra.megamod.reliquary.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import com.ultra.megamod.reliquary.Reliquary;

import java.util.function.Consumer;

/**
 * Port note (1.21.11): ArmorItem removed + ArmorMaterial registry replaced with
 * the EQUIPPABLE data component. We carry only the creative-tab and display-name
 * behaviour — equipment visuals come from the Equippable asset id below.
 */
public class WitchHatItem extends Item implements ICreativeTabItemGenerator {
	private static final ResourceKey<EquipmentAsset> WITCH_HAT_ASSET =
			ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(Reliquary.MOD_ID, "witch_hat"));

	public WitchHatItem() {
		super(new Properties()
				.stacksTo(1)
				.component(DataComponents.EQUIPPABLE,
						Equippable.builder(EquipmentSlot.HEAD)
								.setAsset(WITCH_HAT_ASSET)
								.build()));
	}

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		itemConsumer.accept(new ItemStack(this));
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(getDescriptionId(stack)).withStyle(ChatFormatting.YELLOW);
	}
}

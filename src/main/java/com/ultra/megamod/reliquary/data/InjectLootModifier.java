package com.ultra.megamod.reliquary.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ultra.megamod.reliquary.init.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Mirror of Reliquary 1.21.x's InjectLootModifier. Merges entries from an
 * additional (reliquary-owned) LootTable into the vanilla loot table roll.
 * This is how all of Reliquary's mob drops and chest loot augments are added:
 * the augment table's roll is appended to the vanilla table's generated loot.
 *
 * <p>Port note: the original lived inside {@code ReliquaryLootModifierProvider}
 * as a static inner class. It has been promoted to a top-level class here so
 * static {@code data/neoforge/loot_modifiers/...} JSONs can reference the
 * codec {@code reliquary:inject_loot} without needing the datagen provider.
 */
public class InjectLootModifier extends LootModifier {
	public static final MapCodec<InjectLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> LootModifier.codecStart(inst).and(
			inst.group(
					ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter(m -> m.lootTable),
					ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table_to_inject_into").forGetter(m -> m.lootTableToInjectInto)
			)
	).apply(inst, InjectLootModifier::new));

	private final ResourceKey<LootTable> lootTable;
	private final ResourceKey<LootTable> lootTableToInjectInto;

	protected InjectLootModifier(LootItemCondition[] conditions, ResourceKey<LootTable> lootTable, ResourceKey<LootTable> lootTableToInjectInto) {
		super(conditions);
		this.lootTable = lootTable;
		this.lootTableToInjectInto = lootTableToInjectInto;
	}

	@SuppressWarnings({"deprecation", "java:S1874"})
	// getRandomItemsRaw is deprecated for public callers because it skips the
	// LootTableLoadEvent dispatch — which is precisely what we want here so
	// we don't recurse through the modifier pipeline.
	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		// Port note (1.21.11): HolderGetter#get(ResourceKey) is the new signature — the
		// two-arg (RegistryKey, ResourceKey) overload that 1.20 used is gone. We go through
		// the LootDataResolver's lookup directly instead.
		context.getResolver().lookupOrThrow(Registries.LOOT_TABLE).get(lootTable).ifPresent(extraTable ->
				extraTable.value().getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), generatedLoot::add))
		);
		return generatedLoot;
	}

	@Override
	public MapCodec<? extends IGlobalLootModifier> codec() {
		return ModItems.INJECT_LOOT.get();
	}
}

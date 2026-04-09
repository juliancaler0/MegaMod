package shiroroku.theaurorian.Items;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BaseAurorianCurio extends BaseAurorianItem implements ICurioItem {

    private List<SimpleAttibuteModifier> modifiers = new ArrayList<>();

    public BaseAurorianCurio(Properties pProperties, Attribute attribute, AttributeModifier.Operation operation, double amt) {
        super(pProperties.stacksTo(1));
        modifiers.add(new SimpleAttibuteModifier(attribute, operation, amt));
    }

    public BaseAurorianCurio(Properties pProperties, List<SimpleAttibuteModifier> modifiers) {
        super(pProperties.stacksTo(1));
        this.modifiers = modifiers;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
        modifiers.forEach(mod -> map.put(mod.attribute, mod.modifier));
        return map;
    }

    public static class SimpleAttibuteModifier {
        private final Attribute attribute;
        private final AttributeModifier modifier;

        public SimpleAttibuteModifier(Attribute attribute, AttributeModifier.Operation operation, double amt) {
            this.attribute = attribute;
            modifier = new AttributeModifier(UUID.nameUUIDFromBytes(this.toString().getBytes()), "Aurorian Curio", amt, operation);
        }
    }
}

package com.ultra.megamod.lib.accessories.api.client.rendering;

import com.ultra.megamod.lib.accessories.api.client.renderers.DefaultAccessoryRenderer;
import com.ultra.megamod.lib.accessories.utils.EndecUtils;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Holder class used within {@link Transformation.TransformTo} as a method of adjusting
 * the position of the {@link DefaultAccessoryRenderer#render}.
 */
public record ModelTarget(String modelPart, @Nullable Vector3f rawNormal, @Nullable Side side) {

    public static final ModelTarget EMPTY = new ModelTarget("", null, null);

    public static final StructEndec<ModelTarget> ENDEC = StructEndecBuilder.of(
            Endec.STRING.fieldOf("model_part", ModelTarget::modelPart),
            EndecUtils.VECTOR_3_F_ENDEC.optionalFieldOf("raw_normal", ModelTarget::rawNormal, () -> null),
            Side.ENDEC.optionalFieldOf("side", ModelTarget::side, () -> null),
            ModelTarget::new
    );
}

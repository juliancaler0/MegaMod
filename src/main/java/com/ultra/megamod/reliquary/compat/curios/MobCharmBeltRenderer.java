package com.ultra.megamod.reliquary.compat.curios;

import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Curios-side mob charm belt renderer.
 *
 * <p>Currently no-op — the legacy upstream implementation relied on a
 * {@code HumanoidModel} copy-from the parent entity model plus a
 * {@code setupAnim} call, which targeted the pre-1.21.9 render API. Curios
 * 14.0.0+1.21.11 flipped {@link ICurioRenderer#render} to take a
 * {@code SubmitNodeCollector} + {@code LivingEntityRenderState}, and the
 * Reliquary port doesn't yet carry a {@code MobCharmBeltModel} of its own.
 * The class exists so {@link CuriosCompatClient} can wire it up via
 * {@code CuriosRendererRegistry.register} — the default-interface
 * {@code render} no-op in {@link ICurioRenderer} is what actually runs.
 */
public class MobCharmBeltRenderer implements ICurioRenderer {
	public MobCharmBeltRenderer() {}
}

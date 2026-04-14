package com.ultra.megamod.reliquary.client.render;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.reliquary.entity.TippedArrow;

public class TippedArrowRenderer extends ArrowRenderer<TippedArrow> {
	private static final Identifier RES_ARROW = Identifier.parse("textures/entity/projectiles/arrow.png");
	private static final Identifier RES_TIPPED_ARROW = Identifier.parse("textures/entity/projectiles/tipped_arrow.png");

	public TippedArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public Identifier getTextureLocation(TippedArrow entity) {
		return entity.getColor() > 0 ? RES_TIPPED_ARROW : RES_ARROW;
	}
}

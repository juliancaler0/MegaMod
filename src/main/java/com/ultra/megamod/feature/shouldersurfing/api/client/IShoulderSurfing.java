package com.ultra.megamod.feature.shouldersurfing.api.client;

import com.ultra.megamod.feature.shouldersurfing.api.model.Perspective;

public interface IShoulderSurfing
{
	IShoulderSurfingCamera getCamera();

	ICameraEntityRenderer getCameraEntityRenderer();

	ICrosshairRenderer getCrosshairRenderer();

	IObjectPicker getObjectPicker();

	IClientConfig getClientConfig();

	boolean isShoulderSurfing();

	boolean isAiming();

	boolean isCameraDecoupled();

	boolean isFreeLooking();

	void changePerspective(Perspective perspective);

	void togglePerspective();

	void swapShoulder();

	void resetState();
}

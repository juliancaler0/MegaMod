package com.ultra.megamod.feature.shouldersurfing.api.client;

import com.ultra.megamod.feature.shouldersurfing.client.ShoulderSurfingImpl;

public class ShoulderSurfing
{
	public static IShoulderSurfing getInstance()
	{
		return ShoulderSurfingImpl.getInstance();
	}
}

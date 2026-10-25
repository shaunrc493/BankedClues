package com.bankedclues;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BankedCluesPluginTest
{
	// RuneLite's loadBuiltin API uses a generic Class varargs parameter.
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BankedCluesPlugin.class);
		RuneLite.main(args);
	}
}

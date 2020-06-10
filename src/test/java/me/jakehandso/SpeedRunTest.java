package me.jakehandso;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SpeedRunTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SpeedRunPlugin.class);
		RuneLite.main(args);
	}
}
package me.jakehandso;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.barrows.BarrowsBrothers;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@PluginDescriptor(
	name = "SpeedRun"
)
public class SpeedRunPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private SpeedRunConfig config;

	private boolean Started;

	private Instant splitStart;
	private Instant runStart;

	private int NumberOfBrothersSlain;
	@Override
	protected void startUp() throws Exception
	{
		Started = false;
		log.info("SpeedRunPlugin started!");
		client.getLocalPlayer();
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("SpeedRunPlugin stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if(event.getGameState() == GameState.LOGGED_IN)
		{
			final Widget potential = client.getWidget(WidgetInfo.BARROWS_POTENTIAL);
			if (!Started)
			{
				if (potential != null)
				{
					Started=true;
					runStart = Instant.now();
					splitStart = Instant.now();
					NumberOfBrothersSlain=0;
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Barrows Speedrun Started", null);
				}
			}
			else if (Started)
			{
				if (potential == null)
				{
					Started=false;
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Barrows Speedrun Canceled", null);
				}
			}
		}
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged event)
	{
		if (Started)
		{
			int slain = 0;
			for (BarrowsBrothers brother : BarrowsBrothers.values())
			{
				slain += (client.getVar(brother.getKilledVarbit()));
			}
			if (slain > NumberOfBrothersSlain)
			{
				NumberOfBrothersSlain = slain;
				long splitTime = Duration.between(splitStart, Instant.now()).getSeconds();
				String timeDiff = String.format("%02d:%02d", (splitTime % 3600) / 60, (splitTime % 60));
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Speedrun split: Brothers killed " + NumberOfBrothersSlain + " split time: " + timeDiff, null);
				splitStart = Instant.now();
			}
		}
	}
	@Subscribe
	private void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == WidgetID.BARROWS_REWARD_GROUP_ID && NumberOfBrothersSlain == 6)
		{
			long splitTime = Duration.between(splitStart, Instant.now()).getSeconds();
			String timeDiff = String.format("%02d:%02d", (splitTime % 3600) / 60, (splitTime % 60));
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Speedrun split: Chest looted  split time: " + timeDiff, null);

			long totalTime = Duration.between(runStart, Instant.now()).getSeconds();
			timeDiff = String.format("%02d:%02d", (totalTime % 3600) / 60, (totalTime % 60));
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Barrows Speedrun : Finished total time: " + timeDiff, null);
			Started = false;
			NumberOfBrothersSlain = 0;
		}
	}
	@Provides
	SpeedRunConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SpeedRunConfig.class);
	}
}

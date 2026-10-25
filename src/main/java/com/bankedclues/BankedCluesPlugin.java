package com.bankedclues;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@Slf4j
@PluginDescriptor(
	name = "Banked Clues",
	description = "Remembers banked clues, clue sources and reward caskets by tier",
	tags = {"bank", "clue", "scroll", "casket", "treasure", "trails"}
)
public class BankedCluesPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ItemManager itemManager;

	private volatile boolean active;
	private volatile UiSession uiSession;
	private boolean refreshQueued;
	private int generation;
	private boolean bankUpdatePending;
	private boolean bankChangedAway;
	private final BankClueTracker tracker = new BankClueTracker();

	@Override
	protected void startUp()
	{
		UiSession session = new UiSession();
		uiSession = session;
		active = true;
		SwingUtilities.invokeLater(() ->
		{
			if (session.stopped)
			{
				return;
			}
			session.panel = new BankedCluesPanel(itemManager);
			session.button = NavigationButton.builder()
				.tooltip("Banked Clues")
				.icon(createIcon())
				.priority(7)
				.panel(session.panel)
				.build();
			clientToolbar.addNavigation(session.button);
		});
		clientThread.invokeLater(() ->
		{
			if (uiSession == session && !session.stopped)
			{
				tracker.clear();
				invalidatePending();
				queueRefresh();
			}
		});
		log.debug("BankedClues started!");
	}

	@Override
	protected void shutDown()
	{
		active = false;
		UiSession session = uiSession;
		uiSession = null;
		if (session != null)
		{
			session.stopped = true;
			SwingUtilities.invokeLater(() ->
			{
				if (session.button != null)
				{
					clientToolbar.removeNavigation(session.button);
				}
			});
		}
		clientThread.invokeLater(() ->
		{
			if (!active)
			{
				invalidatePending();
				tracker.clear();
			}
		});
		log.debug("BankedClues stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		switch (gameStateChanged.getGameState())
		{
			case LOGIN_SCREEN:
			case LOGGING_IN:
			case HOPPING:
			case CONNECTION_LOST:
				invalidatePending();
				tracker.suspend();
				publishCounts(null, false, false);
				break;
			default:
				break;
		}
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			queueRefresh();
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		clientThread.invokeLater(() ->
		{
			if (active)
			{
				invalidatePending();
				tracker.suspend();
				publishCounts(null, false, false);
				queueRefresh();
			}
		});
	}

	@Subscribe
	public void onProfileChanged(ProfileChanged event)
	{
		clientThread.invokeLater(() ->
		{
			if (active)
			{
				invalidatePending();
				tracker.clear();
				publishCounts(null, false, false);
				queueRefresh();
			}
		});
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.BANKMAIN)
		{
			queueRefresh();
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() == InterfaceID.BANKMAIN)
		{
			queueRefresh();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.BANK)
		{
			if (isBankOpen())
			{
				bankUpdatePending = true;
			}
			else
			{
				bankChangedAway = true;
			}
			queueRefresh();
		}
		else if (event.getContainerId() == InventoryID.INV)
		{
			queueRefresh();
		}
	}

	private void queueRefresh()
	{
		if (!active || refreshQueued)
		{
			return;
		}

		refreshQueued = true;
		int queuedGeneration = generation;
		UiSession session = uiSession;
		// Bank widgets and contents can arrive in either order. Read once both have settled.
		clientThread.invokeAtTickEnd(() ->
		{
			if (!active || session != uiSession || queuedGeneration != generation)
			{
				return;
			}
			refreshQueued = false;
			boolean bankUpdated = bankUpdatePending;
			boolean changedAway = bankChangedAway;
			bankUpdatePending = false;
			bankChangedAway = false;
			refreshBank(bankUpdated, changedAway);
		});
	}

	private boolean isBankOpen()
	{
		Widget bank = client.getWidget(InterfaceID.Bankmain.UNIVERSE);
		return client.getGameState() == GameState.LOGGED_IN && bank != null && !bank.isHidden();
	}

	private void refreshBank(boolean bankUpdated, boolean changedAway)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			publishCounts(null, false, false);
			return;
		}

		tracker.selectProfile(configManager.getRSProfileKey());
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		Map<Integer, Long> relevant = ClueCounter.relevantItems(inventory == null ? null : inventory.getItems(), this::identifyClue);
		boolean bankOpen = isBankOpen();
		// A bank event observed while open is still authoritative if the bank closed later in this tick.
		if (bankOpen || bankUpdated)
		{
			ItemContainer container = client.getItemContainer(InventoryID.BANK);
			ClueCounts updated = ClueCounter.count(container == null ? null : container.getItems(), this::identifyClue)
				.orElse(null);
			if (updated != null)
			{
				tracker.refreshBank(updated, relevant);
				log.debug("Bank counts refreshed: {} scrolls, {} caskets", updated.getScrollTotal(), updated.getTotal(ClueItemType.CASKET));
			}
			else
			{
				tracker.markStale();
			}
		}
		else
		{
			tracker.observeInventory(relevant);
			if (changedAway)
			{
				tracker.markStale();
			}
		}
		publishCounts(tracker.getCounts(), tracker.isStale(), bankOpen);
	}

	private ClueItem identifyClue(int itemId)
	{
		ClueItem known = ClueItemClassifier.knownItem(itemId);
		if (known != null)
		{
			return known;
		}
		ItemComposition item = client.getItemDefinition(itemId);
		return item == null ? null : ClueItemClassifier.identify(itemId, item.getMembersName(), item.getPlaceholderTemplateId() != -1);
	}

	private void invalidatePending()
	{
		generation++;
		refreshQueued = false;
		bankUpdatePending = false;
		bankChangedAway = false;
	}

	private void publishCounts(ClueCounts snapshot, boolean stale, boolean bankOpen)
	{
		UiSession session = uiSession;
		if (session != null)
		{
			SwingUtilities.invokeLater(() ->
			{
				if (!session.stopped && session.panel != null)
				{
					if (!session.rendered || !Objects.equals(snapshot, session.lastCounts)
						|| stale != session.lastStale || bankOpen != session.lastBankOpen)
					{
						session.panel.showCounts(snapshot, stale, bankOpen);
						session.lastCounts = snapshot;
						session.lastStale = stale;
						session.lastBankOpen = bankOpen;
						session.rendered = true;
					}
				}
			});
		}
	}

	private static BufferedImage createIcon()
	{
		BufferedImage image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setColor(new Color(220, 193, 138));
			graphics.fillRoundRect(4, 2, 12, 16, 3, 3);
			graphics.setColor(new Color(105, 76, 39));
			graphics.drawRoundRect(4, 2, 12, 16, 3, 3);
			graphics.drawLine(7, 6, 13, 6);
			graphics.drawLine(7, 9, 13, 9);
			graphics.drawLine(7, 12, 11, 12);
		}
		finally
		{
			graphics.dispose();
		}
		return image;
	}

	private static final class UiSession
	{
		private volatile boolean stopped;
		// Accessed only on the Swing event dispatch thread.
		private BankedCluesPanel panel;
		private NavigationButton button;
		private boolean rendered;
		private ClueCounts lastCounts;
		private boolean lastStale;
		private boolean lastBankOpen;
	}
}

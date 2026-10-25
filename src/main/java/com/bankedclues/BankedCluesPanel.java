package com.bankedclues;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.QuantityFormatter;

final class BankedCluesPanel extends PluginPanel
{
	private final JLabel status = new JLabel();
	private final JLabel scrollTotal = new JLabel();
	private final JLabel casketTotal = new JLabel();
	private final JComboBox<String> tierFilter = new JComboBox<>();
	private final Map<ClueItemType, ClueItemTile> tiles = new EnumMap<>(ClueItemType.class);
	private ClueCounts counts;

	BankedCluesPanel(ItemManager itemManager)
	{
		setLayout(new BorderLayout(0, 12));
		JPanel heading = new JPanel();
		heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
		heading.setOpaque(false);
		JLabel title = new JLabel("Banked Clues");
		title.setForeground(ColorScheme.BRAND_ORANGE);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
		title.setAlignmentX(LEFT_ALIGNMENT);
		heading.add(title);
		heading.add(Box.createVerticalStrut(10));

		JPanel totals = new JPanel(new GridLayout(1, 2, 6, 0));
		totals.setOpaque(false);
		totals.setAlignmentX(LEFT_ALIGNMENT);
		totals.add(scrollTotal);
		totals.add(casketTotal);
		heading.add(totals);
		heading.add(Box.createVerticalStrut(12));

		JLabel filterLabel = new JLabel("Clue tier");
		filterLabel.setAlignmentX(LEFT_ALIGNMENT);
		heading.add(filterLabel);
		heading.add(Box.createVerticalStrut(4));
		tierFilter.addItem("All");
		for (ClueTier tier : ClueTier.values())
		{
			tierFilter.addItem(tier.getDisplayName());
		}
		tierFilter.setAlignmentX(LEFT_ALIGNMENT);
		tierFilter.setToolTipText("Filter cached bank counts by clue tier");
		tierFilter.getAccessibleContext().setAccessibleName("Clue tier");
		heading.add(tierFilter);
		heading.add(Box.createVerticalStrut(10));
		status.setAlignmentX(LEFT_ALIGNMENT);
		heading.add(status);
		add(heading, BorderLayout.NORTH);

		JPanel grid = new JPanel(new GridLayout(3, 2, 8, 8));
		grid.setOpaque(false);
		for (ClueItemType type : ClueItemType.values())
		{
			ClueItemTile tile = new ClueItemTile(type, itemManager);
			tiles.put(type, tile);
			grid.add(tile);
		}
		add(grid, BorderLayout.CENTER);
		tierFilter.addActionListener(event -> refreshItems());
		showCounts(null, false, false);
	}

	// Called exclusively on the Swing event dispatch thread.
	void showCounts(ClueCounts counts, boolean stale, boolean bankOpen)
	{
		this.counts = counts;
		String message = counts == null
			? "Visit a bank to load your clues."
			: stale ? "Counts may be outdated.<br>Visit a bank to refresh."
			: bankOpen ? "Bank counts up to date." : "Showing your last bank snapshot.";
		status.setText("<html><body style='width:180px'>" + message + "</body></html>");
		status.setForeground(stale ? ColorScheme.BRAND_ORANGE : ColorScheme.LIGHT_GRAY_COLOR);
		refreshItems();
	}

	private void refreshItems()
	{
		int selected = tierFilter.getSelectedIndex();
		ClueTier tier = selected <= 0 ? null : ClueTier.values()[selected - 1];
		scrollTotal.setText("Scrolls: " + (counts == null ? "\u2014" : QuantityFormatter.quantityToStackSize(counts.getScrollTotal(tier))));
		casketTotal.setText("Caskets: " + (counts == null ? "\u2014" : QuantityFormatter.quantityToStackSize(counts.getTotal(ClueItemType.CASKET, tier))));
		scrollTotal.setToolTipText("Loose clues plus geodes, bottles, nests and boxes"
			+ (counts == null ? "." : ": " + counts.getScrollTotal(tier)));
		casketTotal.setToolTipText("Completed Treasure Trail reward caskets"
			+ (counts == null ? "." : ": " + counts.getTotal(ClueItemType.CASKET, tier)));
		for (ClueItemType type : ClueItemType.values())
		{
			tiles.get(type).showCount(tier, counts == null ? null : counts.getTotal(type, tier));
		}
	}
}

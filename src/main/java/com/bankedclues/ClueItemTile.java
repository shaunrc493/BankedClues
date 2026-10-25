package com.bankedclues;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.QuantityFormatter;

/** A bank-style item slot with a sprite, stack quantity and category caption. */
final class ClueItemTile extends JPanel
{
	private final ClueItemType type;
	private final ItemManager itemManager;
	private final JLabel sprite = new JLabel("", SwingConstants.CENTER);
	private final JLabel caption;
	private int spriteId = Integer.MIN_VALUE;
	private String quantity = "\u2014";

	ClueItemTile(ClueItemType type, ItemManager itemManager)
	{
		this.type = type;
		this.itemManager = itemManager;
		setLayout(new BorderLayout(0, 3));
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(30, 6, 7, 6));
		setPreferredSize(new Dimension(98, 112));
		caption = new JLabel(type.getDisplayName(), SwingConstants.CENTER);
		caption.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		add(sprite, BorderLayout.CENTER);
		add(caption, BorderLayout.SOUTH);
	}

	void showCount(ClueTier tier, Long count)
	{
		int itemId = ClueItemClassifier.spriteItemId(type, tier);
		if (itemId != spriteId)
		{
			spriteId = itemId;
			sprite.setIcon(null);
			sprite.setText(itemId < 0 ? "N/A" : "");
			if (itemId >= 0)
			{
				AsyncBufferedImage image = itemManager.getImage(itemId);
				if (image != null)
				{
					// Keep RuneLite's asynchronous repaint listener when enlarging its sprite.
					image.addTo(sprite);
					sprite.setIcon(new ScaledIcon(sprite.getIcon()));
				}
			}
		}
		quantity = count == null || itemId < 0 ? "\u2014" : QuantityFormatter.quantityToStackSize(count);
		String scope = tier == null ? "All tiers" : tier.getDisplayName();
		String tooltip = scope + " - " + type.getDisplayName() + ": "
			+ (itemId < 0 ? "not available for this tier" : count == null ? "visit a bank to load counts" : count.toString());
		setToolTipText(tooltip);
		sprite.setToolTipText(tooltip);
		caption.setToolTipText(tooltip);
		getAccessibleContext().setAccessibleName(tooltip);
		repaint();
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		super.paintComponent(graphics);
		Graphics2D g = (Graphics2D) graphics.create();
		try
		{
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(ColorScheme.DARKER_GRAY_COLOR);
			g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
			g.setColor(new Color(80, 76, 65));
			g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
		}
		finally
		{
			g.dispose();
		}
	}

	@Override
	protected void paintChildren(Graphics graphics)
	{
		super.paintChildren(graphics);
		Graphics g = graphics.create();
		try
		{
			g.setFont(getFont().deriveFont(Font.BOLD, 24f));
			g.setColor(Color.BLACK);
			g.drawString(quantity, 9, 27);
			g.setColor(spriteId < 0 ? ColorScheme.LIGHT_GRAY_COLOR : Color.YELLOW);
			g.drawString(quantity, 8, 26);
		}
		finally
		{
			g.dispose();
		}
	}

	private static final class ScaledIcon implements Icon
	{
		private static final float SCALE = 1.75f;
		private final Icon original;

		private ScaledIcon(Icon original)
		{
			this.original = original;
		}

		@Override
		public void paintIcon(Component component, Graphics graphics, int x, int y)
		{
			Graphics2D g = (Graphics2D) graphics.create();
			try
			{
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				g.translate(x, y);
				g.scale(SCALE, SCALE);
				original.paintIcon(component, g, 0, 0);
			}
			finally
			{
				g.dispose();
			}
		}

		@Override
		public int getIconWidth()
		{
			return Math.round(original.getIconWidth() * SCALE);
		}

		@Override
		public int getIconHeight()
		{
			return Math.round(original.getIconHeight() * SCALE);
		}
	}
}

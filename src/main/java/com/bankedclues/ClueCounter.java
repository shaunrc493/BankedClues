package com.bankedclues;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;
import net.runelite.api.Item;

final class ClueCounter
{
	private ClueCounter()
	{
	}

	/** An absent snapshot is different from an available bank containing no clues. */
	static Optional<ClueCounts> count(Item[] items, IntFunction<ClueItem> identify)
	{
		if (items == null)
		{
			return Optional.empty();
		}

		long[][] quantities = new long[ClueItemType.values().length][ClueTier.values().length];
		for (Item item : items)
		{
			if (item == null || item.getId() < 0 || item.getQuantity() <= 0)
			{
				continue;
			}

			ClueItem clue = identify.apply(item.getId());
			if (clue != null)
			{
				quantities[clue.getType().ordinal()][clue.getTier().ordinal()] += item.getQuantity();
			}
		}
		return Optional.of(new ClueCounts(quantities));
	}

	/** Preserve item identity as well as quantity to notice clue-step changes. */
	static Map<Integer, Long> relevantItems(Item[] items, IntFunction<ClueItem> identify)
	{
		if (items == null)
		{
			return null;
		}
		Map<Integer, Long> relevant = new HashMap<>();
		for (Item item : items)
		{
			if (item != null && item.getId() >= 0 && item.getQuantity() > 0 && identify.apply(item.getId()) != null)
			{
				relevant.merge(item.getId(), (long) item.getQuantity(), Long::sum);
			}
		}
		return relevant;
	}
}

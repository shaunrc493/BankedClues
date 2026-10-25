package com.bankedclues;

import java.util.HashMap;
import java.util.Map;
import net.runelite.api.gameval.ItemID;

final class ClueItemClassifier
{
	private static final Map<Integer, ClueItem> ITEMS = new HashMap<>();

	static
	{
		// IDs are ordered Beginner through Master; natural sources have no Master variant.
		register(ClueItemType.GEODE, ItemID.MINING_CLUE_GEODE_BEGINNER, ItemID.MINING_CLUE_GEODE_EASY,
			ItemID.MINING_CLUE_GEODE_MEDIUM, ItemID.MINING_CLUE_GEODE_HARD, ItemID.MINING_CLUE_GEODE_ELITE);
		register(ClueItemType.BOTTLE, ItemID.FISHING_CLUE_BOTTLE_BEGINNER, ItemID.FISHING_CLUE_BOTTLE_EASY,
			ItemID.FISHING_CLUE_BOTTLE_MEDIUM, ItemID.FISHING_CLUE_BOTTLE_HARD, ItemID.FISHING_CLUE_BOTTLE_ELITE);
		register(ClueItemType.NEST, ItemID.WC_CLUE_NEST_BEGINNER, ItemID.WC_CLUE_NEST_EASY,
			ItemID.WC_CLUE_NEST_MEDIUM, ItemID.WC_CLUE_NEST_HARD, ItemID.WC_CLUE_NEST_ELITE);
		register(ClueItemType.BOX, ItemID.LEAGUE_CLUE_BOX_BEGINNER, ItemID.LEAGUE_CLUE_BOX_EASY,
			ItemID.LEAGUE_CLUE_BOX_MEDIUM, ItemID.LEAGUE_CLUE_BOX_HARD, ItemID.LEAGUE_CLUE_BOX_ELITE,
			ItemID.LEAGUE_CLUE_BOX_MASTER);
		register(ClueItemType.CASKET, ItemID.TRAIL_REWARD_CASKET_BEGINNER, ItemID.TRAIL_REWARD_CASKET_EASY,
			ItemID.TRAIL_REWARD_CASKET_MEDIUM, ItemID.TRAIL_REWARD_CASKET_HARD, ItemID.TRAIL_REWARD_CASKET_ELITE,
			ItemID.TRAIL_REWARD_CASKET_MASTER);
	}

	private ClueItemClassifier()
	{
	}

	private static void register(ClueItemType type, int... itemIds)
	{
		ClueTier[] tiers = ClueTier.values();
		for (int i = 0; i < itemIds.length; i++)
		{
			ITEMS.put(itemIds[i], new ClueItem(tiers[i], type));
		}
	}

	static ClueItem knownItem(int itemId)
	{
		// Only actual item IDs are registered, never noted or placeholder IDs.
		return ITEMS.get(itemId);
	}

	static int spriteItemId(ClueItemType type, ClueTier filter)
	{
		// The All view uses representative Hard-tier sprites for combined counts.
		ClueTier tier = filter == null ? ClueTier.HARD : filter;
		if (type == ClueItemType.SCROLL)
		{
			switch (tier)
			{
				case BEGINNER:
					return ItemID.TRAIL_CLUE_BEGINNER;
				case EASY:
					return ItemID.TRAIL_CLUE_EASY_SIMPLE001;
				case MEDIUM:
					return ItemID.TRAIL_CLUE_MEDIUM_EMOTE001;
				case HARD:
					return ItemID.TRAIL_CLUE_HARD_MAP001;
				case ELITE:
					return ItemID.TRAIL_CLUE_ELITE_MUSIC001;
				case MASTER:
					return ItemID.TRAIL_CLUE_MASTER;
				default:
					return -1;
			}
		}
		for (Map.Entry<Integer, ClueItem> entry : ITEMS.entrySet())
		{
			ClueItem item = entry.getValue();
			if (item.getType() == type && item.getTier() == tier)
			{
				return entry.getKey();
			}
		}
		return -1;
	}

	static ClueItem identify(int itemId, String name, boolean placeholder)
	{
		if (placeholder)
		{
			return null;
		}
		ClueItem known = knownItem(itemId);
		if (known != null)
		{
			return known;
		}
		ClueTier tier = ClueTier.identify(name, false);
		return tier == null ? null : new ClueItem(tier, ClueItemType.SCROLL);
	}
}

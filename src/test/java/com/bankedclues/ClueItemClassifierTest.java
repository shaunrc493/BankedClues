package com.bankedclues;

import net.runelite.api.gameval.ItemID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ClueItemClassifierTest
{
	@Test
	public void spritesMatchTheirTierAndCategoryAndDoNotInventMasterSources()
	{
		for (ClueTier tier : ClueTier.values())
		{
			for (ClueItemType type : ClueItemType.values())
			{
				int itemId = ClueItemClassifier.spriteItemId(type, tier);
				if (tier == ClueTier.MASTER && (type == ClueItemType.GEODE || type == ClueItemType.BOTTLE || type == ClueItemType.NEST))
				{
					assertEquals(-1, itemId);
				}
				else if (type != ClueItemType.SCROLL)
				{
					ClueItem item = ClueItemClassifier.knownItem(itemId);
					assertEquals(tier, item.getTier());
					assertEquals(type, item.getType());
				}
			}
		}
		assertEquals(ItemID.TRAIL_CLUE_MASTER, ClueItemClassifier.spriteItemId(ClueItemType.SCROLL, ClueTier.MASTER));
		assertEquals(ItemID.MINING_CLUE_GEODE_HARD, ClueItemClassifier.spriteItemId(ClueItemType.GEODE, null));
	}

	@Test
	public void recognizesEachNaturalClueSourceAndRewardCasketTier()
	{
		int[][] items = {
			{ItemID.MINING_CLUE_GEODE_BEGINNER, ItemID.MINING_CLUE_GEODE_EASY, ItemID.MINING_CLUE_GEODE_MEDIUM,
				ItemID.MINING_CLUE_GEODE_HARD, ItemID.MINING_CLUE_GEODE_ELITE},
			{ItemID.FISHING_CLUE_BOTTLE_BEGINNER, ItemID.FISHING_CLUE_BOTTLE_EASY, ItemID.FISHING_CLUE_BOTTLE_MEDIUM,
				ItemID.FISHING_CLUE_BOTTLE_HARD, ItemID.FISHING_CLUE_BOTTLE_ELITE},
			{ItemID.WC_CLUE_NEST_BEGINNER, ItemID.WC_CLUE_NEST_EASY, ItemID.WC_CLUE_NEST_MEDIUM,
				ItemID.WC_CLUE_NEST_HARD, ItemID.WC_CLUE_NEST_ELITE},
			{ItemID.TRAIL_REWARD_CASKET_BEGINNER, ItemID.TRAIL_REWARD_CASKET_EASY, ItemID.TRAIL_REWARD_CASKET_MEDIUM,
				ItemID.TRAIL_REWARD_CASKET_HARD, ItemID.TRAIL_REWARD_CASKET_ELITE, ItemID.TRAIL_REWARD_CASKET_MASTER}
		};
		ClueItemType[] types = {ClueItemType.GEODE, ClueItemType.BOTTLE, ClueItemType.NEST, ClueItemType.CASKET};
		for (int group = 0; group < items.length; group++)
		{
			for (int tier = 0; tier < items[group].length; tier++)
			{
				ClueItem item = ClueItemClassifier.identify(items[group][tier], null, false);
				assertEquals(types[group], item.getType());
				assertEquals(ClueTier.values()[tier], item.getTier());
			}
		}
	}

	@Test
	public void excludesNaturalSourcePlaceholdersEvenWithNonzeroQuantities()
	{
		assertNull(ClueItemClassifier.identify(ItemID.Placeholder.MINING_CLUE_GEODE_EASY, "Clue geode (easy)", true));
		assertNull(ClueItemClassifier.knownItem(ItemID.Placeholder.MINING_CLUE_GEODE_EASY));
		assertNull(ClueItemClassifier.identify(ItemID.Placeholder.WC_CLUE_NEST_HARD, "Clue nest (hard)", true));
		assertNull(ClueItemClassifier.identify(ItemID.Placeholder.FISHING_CLUE_BOTTLE_ELITE, "Clue bottle (elite)", true));
	}

	@Test
	public void recognizesLooseCluesWithoutTreatingOtherCasketsOrProtectionBoxesAsClues()
	{
		ClueItem scroll = ClueItemClassifier.identify(ItemID.TRAIL_CLUE_MASTER, "Clue scroll (master)", false);
		assertEquals(ClueTier.MASTER, scroll.getTier());
		assertEquals(ClueItemType.SCROLL, scroll.getType());
		assertNull(ClueItemClassifier.identify(ItemID.TEMPOROSS_REWARD_CASKET, "Reward casket", false));
		assertNull(ClueItemClassifier.identify(ItemID.BH_CLUE_BOX, "Clue box", false));
		assertNull(ClueItemClassifier.identify(ItemID.TRAIL_CLUE_MASTER, "Clue scroll (master)", true));
	}
}

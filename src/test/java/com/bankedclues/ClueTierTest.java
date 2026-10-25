package com.bankedclues;

import net.runelite.api.gameval.ItemID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ClueTierTest
{
	@Test
	public void identifiesAllSixScrollBoxes()
	{
		assertEquals(ClueTier.BEGINNER, ClueItemClassifier.knownItem(ItemID.LEAGUE_CLUE_BOX_BEGINNER).getTier());
		assertEquals(ClueTier.EASY, ClueItemClassifier.knownItem(ItemID.LEAGUE_CLUE_BOX_EASY).getTier());
		assertEquals(ClueTier.MEDIUM, ClueItemClassifier.knownItem(ItemID.LEAGUE_CLUE_BOX_MEDIUM).getTier());
		assertEquals(ClueTier.HARD, ClueItemClassifier.knownItem(ItemID.LEAGUE_CLUE_BOX_HARD).getTier());
		assertEquals(ClueTier.ELITE, ClueItemClassifier.knownItem(ItemID.LEAGUE_CLUE_BOX_ELITE).getTier());
		assertEquals(ClueTier.MASTER, ClueItemClassifier.knownItem(ItemID.LEAGUE_CLUE_BOX_MASTER).getTier());
		assertNull(ClueItemClassifier.knownItem(ItemID.BH_CLUE_BOX));
		assertNull(ClueItemClassifier.knownItem(ItemID.TRAIL_CLUE_MASTER));
	}

	@Test
	public void identifiesAllSixTiers()
	{
		assertEquals(ClueTier.BEGINNER, ClueTier.identify("Clue scroll (beginner)", false));
		assertEquals(ClueTier.EASY, ClueTier.identify("Clue scroll (easy)", false));
		assertEquals(ClueTier.MEDIUM, ClueTier.identify("Clue scroll (medium)", false));
		assertEquals(ClueTier.HARD, ClueTier.identify("Clue scroll (hard)", false));
		assertEquals(ClueTier.ELITE, ClueTier.identify("Clue scroll (elite)", false));
		assertEquals(ClueTier.MASTER, ClueTier.identify("Clue scroll (master)", false));
	}

	@Test
	public void excludesPlaceholdersEvenWhenTheyHaveTheClueName()
	{
		assertNull(ClueTier.identify("Clue scroll (hard)", true));
	}

	@Test
	public void excludesSourcesRewardsAndOtherScrolls()
	{
		String[] excluded = {
			"Clue nest (easy)", "Clue bottle (medium)", "Clue geode (hard)",
			"Reward casket (elite)", "Casket (easy)", "Challenge scroll (medium)",
			"Clue scroll", "Clue scroll (unknown)", "Clue scroll (hard) (torn)",
			"Coins", "null", "", null
		};
		for (String name : excluded)
		{
			assertNull("Unexpected clue: " + name, ClueTier.identify(name, false));
		}
	}
}

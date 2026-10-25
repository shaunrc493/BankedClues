package com.bankedclues;

import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Item;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClueCounterTest
{
	private final Map<Integer, ClueTier> tiers = new HashMap<>();

	public ClueCounterTest()
	{
		tiers.put(ItemID.TRAIL_CLUE_BEGINNER, ClueTier.BEGINNER);
		tiers.put(ItemID.TRAIL_CLUE_EASY_SIMPLE001, ClueTier.EASY);
		tiers.put(ItemID.TRAIL_CLUE_EASY_SIMPLE002, ClueTier.EASY);
		tiers.put(ItemID.TRAIL_CLUE_MEDIUM_EMOTE001, ClueTier.MEDIUM);
		tiers.put(ItemID.TRAIL_CLUE_HARD_MAP001, ClueTier.HARD);
		tiers.put(ItemID.TRAIL_CLUE_ELITE_MUSIC001, ClueTier.ELITE);
		tiers.put(ItemID.TRAIL_CLUE_MASTER, ClueTier.MASTER);
	}

	private ClueItem identify(int itemId)
	{
		ClueItem known = ClueItemClassifier.knownItem(itemId);
		if (known != null)
		{
			return known;
		}
		ClueTier tier = tiers.get(itemId);
		return tier == null ? null : new ClueItem(tier, ClueItemType.SCROLL);
	}

	@Test
	public void tierFilteringLimitsEveryCategoryAndBothHeadlineTotals()
	{
		Item[] bank = {
			new Item(ItemID.TRAIL_CLUE_BEGINNER, 2),
			new Item(ItemID.LEAGUE_CLUE_BOX_BEGINNER, 3),
			new Item(ItemID.TRAIL_REWARD_CASKET_BEGINNER, 7),
			new Item(ItemID.MINING_CLUE_GEODE_EASY, 4),
			new Item(ItemID.FISHING_CLUE_BOTTLE_EASY, 5),
			new Item(ItemID.WC_CLUE_NEST_EASY, 6),
			new Item(ItemID.TRAIL_REWARD_CASKET_EASY, 11)
		};
		ClueCounts counts = ClueCounter.count(bank, this::identify).get();
		assertEquals(20, counts.getScrollTotal(null));
		assertEquals(18, counts.getTotal(ClueItemType.CASKET, null));
		assertEquals(5, counts.getScrollTotal(ClueTier.BEGINNER));
		assertEquals(7, counts.getTotal(ClueItemType.CASKET, ClueTier.BEGINNER));
		assertEquals(15, counts.getScrollTotal(ClueTier.EASY));
		assertEquals(11, counts.getTotal(ClueItemType.CASKET, ClueTier.EASY));
		assertEquals(4, counts.getTotal(ClueItemType.GEODE, ClueTier.EASY));
		assertEquals(0, counts.getTotal(ClueItemType.BOX, ClueTier.EASY));
		assertEquals(0, counts.getScrollTotal(ClueTier.MASTER));
		assertEquals(0, counts.getTotal(ClueItemType.CASKET, ClueTier.MASTER));
		// Filtering is a view of the immutable snapshot, not a change to the cached bank data.
		assertEquals(20, counts.getScrollTotal());
	}

	@Test
	public void includesAllClueSourcesButKeepsCasketsOutOfTheScrollTotal()
	{
		Item[] bank = {
			new Item(ItemID.MINING_CLUE_GEODE_BEGINNER, 1),
			new Item(ItemID.FISHING_CLUE_BOTTLE_BEGINNER, 2),
			new Item(ItemID.WC_CLUE_NEST_BEGINNER, 3),
			new Item(ItemID.LEAGUE_CLUE_BOX_BEGINNER, 4),
			new Item(ItemID.TRAIL_CLUE_BEGINNER, 5),
			new Item(ItemID.TRAIL_REWARD_CASKET_BEGINNER, 123)
		};
		ClueCounts counts = ClueCounter.count(bank, this::identify).get();
		assertEquals(15, counts.getScrollTotal());
		assertEquals(123, counts.getTotal(ClueItemType.CASKET));
		assertEquals(1, counts.get(ClueTier.BEGINNER, ClueItemType.GEODE));
		assertEquals(5, counts.get(ClueTier.BEGINNER, ClueItemType.SCROLL));
	}

	@Test
	public void inventoryFingerprintIgnoresUnrelatedItemsAndSlotOrder()
	{
		Item[] before = {new Item(ItemID.LEAGUE_CLUE_BOX_EASY, 2), new Item(ItemID.BH_CLUE_BOX, 1)};
		Item[] rearranged = {new Item(ItemID.BH_CLUE_BOX, 5), new Item(ItemID.LEAGUE_CLUE_BOX_EASY, 2)};
		assertEquals(ClueCounter.relevantItems(before, this::identify), ClueCounter.relevantItems(rearranged, this::identify));
	}

	@Test
	public void inventoryFingerprintNoticesClueStepsEvenWhenTierAndQuantityAreTheSame()
	{
		Item[] before = {new Item(ItemID.TRAIL_CLUE_EASY_SIMPLE001, 1)};
		Item[] after = {new Item(ItemID.TRAIL_CLUE_EASY_SIMPLE002, 1)};
		assertFalse(ClueCounter.relevantItems(before, this::identify).equals(ClueCounter.relevantItems(after, this::identify)));
	}

	@Test
	public void countsScrollBoxStacksSeparatelyAndIncludesThemInTheTotal()
	{
		Item[] bank = {
			new Item(ItemID.LEAGUE_CLUE_BOX_BEGINNER, 2),
			new Item(ItemID.LEAGUE_CLUE_BOX_EASY, 3),
			new Item(ItemID.LEAGUE_CLUE_BOX_MEDIUM, 4),
			new Item(ItemID.LEAGUE_CLUE_BOX_HARD, 5),
			new Item(ItemID.LEAGUE_CLUE_BOX_ELITE, 6),
			new Item(ItemID.LEAGUE_CLUE_BOX_MASTER, 7),
			new Item(ItemID.TRAIL_CLUE_MASTER, 1),
			new Item(ItemID.BH_CLUE_BOX, 10)
		};
		ClueCounts counts = ClueCounter.count(bank, this::identify).get();
		assertEquals(2, counts.get(ClueTier.BEGINNER, ClueItemType.BOX));
		assertEquals(3, counts.get(ClueTier.EASY, ClueItemType.BOX));
		assertEquals(4, counts.get(ClueTier.MEDIUM, ClueItemType.BOX));
		assertEquals(5, counts.get(ClueTier.HARD, ClueItemType.BOX));
		assertEquals(6, counts.get(ClueTier.ELITE, ClueItemType.BOX));
		assertEquals(7, counts.get(ClueTier.MASTER, ClueItemType.BOX));
		assertEquals(1, counts.get(ClueTier.MASTER, ClueItemType.SCROLL));
		assertEquals(1, counts.getTotal(ClueItemType.SCROLL));
		assertEquals(27, counts.getTotal(ClueItemType.BOX));
		assertEquals(28, counts.getScrollTotal());
	}

	@Test
	public void noticesBoxChangesWhenTheCombinedTotalStaysTheSame()
	{
		ClueCounts boxed = ClueCounter.count(new Item[]{new Item(ItemID.LEAGUE_CLUE_BOX_MASTER, 1)}, this::identify).get();
		ClueCounts opened = ClueCounter.count(new Item[]{new Item(ItemID.TRAIL_CLUE_MASTER, 1)}, this::identify).get();
		assertEquals(boxed.getScrollTotal(), opened.getScrollTotal());
		assertFalse(boxed.equals(opened));
		assertEquals(1, boxed.get(ClueTier.MASTER, ClueItemType.BOX));
		assertEquals(0, opened.get(ClueTier.MASTER, ClueItemType.BOX));
	}

	@Test
	public void ignoresZeroQuantityBoxPlaceholders()
	{
		ClueCounts counts = ClueCounter.count(new Item[]{new Item(ItemID.LEAGUE_CLUE_BOX_EASY, 0)}, this::identify).get();
		assertEquals(0, counts.getTotal(ClueItemType.BOX));
		assertEquals(0, counts.getScrollTotal());
	}

	@Test
	public void aggregatesQuantitiesAndDifferentStepsAcrossAllTiers()
	{
		Item[] bank = {
			new Item(ItemID.TRAIL_CLUE_BEGINNER, 1),
			new Item(ItemID.TRAIL_CLUE_EASY_SIMPLE001, 2),
			new Item(ItemID.TRAIL_CLUE_EASY_SIMPLE002, 3),
			new Item(ItemID.TRAIL_CLUE_MEDIUM_EMOTE001, 4),
			new Item(ItemID.TRAIL_CLUE_HARD_MAP001, 5),
			new Item(ItemID.TRAIL_CLUE_ELITE_MUSIC001, 6),
			new Item(ItemID.TRAIL_CLUE_MASTER, 7),
			new Item(ItemID.WC_CLUE_NEST_EASY, 10),
			new Item(ItemID.FISHING_CLUE_BOTTLE_MEDIUM, 10),
			new Item(ItemID.MINING_CLUE_GEODE_HARD, 10)
		};
		ClueCounts counts = ClueCounter.count(bank, this::identify).get();
		assertEquals(1, counts.get(ClueTier.BEGINNER, ClueItemType.SCROLL));
		assertEquals(5, counts.get(ClueTier.EASY, ClueItemType.SCROLL));
		assertEquals(4, counts.get(ClueTier.MEDIUM, ClueItemType.SCROLL));
		assertEquals(5, counts.get(ClueTier.HARD, ClueItemType.SCROLL));
		assertEquals(6, counts.get(ClueTier.ELITE, ClueItemType.SCROLL));
		assertEquals(7, counts.get(ClueTier.MASTER, ClueItemType.SCROLL));
		assertEquals(58, counts.getScrollTotal());
	}

	@Test
	public void distinguishesAnEmptyBankFromUnavailableData()
	{
		assertFalse(ClueCounter.count(null, this::identify).isPresent());
		assertTrue(ClueCounter.count(new Item[0], this::identify).isPresent());
		assertEquals(0, ClueCounter.count(new Item[0], this::identify).get().getScrollTotal());
	}

	@Test
	public void ignoresUnknownItemsEmptySlotsAndNonPositiveQuantities()
	{
		Item[] bank = {
			null, new Item(-1, 1), new Item(ItemID.TRAIL_CLUE_MASTER, 0),
			new Item(ItemID.TRAIL_CLUE_BEGINNER, -1),
			new Item(ItemID.BH_CLUE_BOX, 100)
		};
		assertEquals(0, ClueCounter.count(bank, this::identify).get().getScrollTotal());
	}

	@Test
	public void totalsDoNotOverflowAnInteger()
	{
		Item[] bank = {
			new Item(ItemID.TRAIL_CLUE_EASY_SIMPLE001, Integer.MAX_VALUE),
			new Item(ItemID.TRAIL_CLUE_EASY_SIMPLE002, Integer.MAX_VALUE)
		};
		assertEquals(2L * Integer.MAX_VALUE, ClueCounter.count(bank, this::identify).get().getScrollTotal());
	}

	@Test
	public void recountingReplacesQuantitiesInsteadOfAccumulatingThem()
	{
		Item[] bank = { new Item(ItemID.TRAIL_CLUE_MASTER, 2) };
		ClueCounts before = ClueCounter.count(bank, this::identify).get();
		bank[0] = new Item(ItemID.TRAIL_CLUE_MASTER, 1);
		ClueCounts after = ClueCounter.count(bank, this::identify).get();
		assertEquals(2, before.getScrollTotal());
		assertEquals(1, after.getScrollTotal());
		assertFalse(before.equals(after));
		assertEquals(after, ClueCounter.count(bank, this::identify).get());
	}
}

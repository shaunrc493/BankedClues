package com.bankedclues;

import java.util.Collections;
import java.util.Map;
import net.runelite.api.Item;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class BankClueTrackerTest
{
	private final BankClueTracker tracker = new BankClueTracker();
	private final ClueCounts bank = ClueCounter.count(new Item[]{new Item(ItemID.LEAGUE_CLUE_BOX_MASTER, 2)},
		ClueItemClassifier::knownItem).get();

	public BankClueTrackerTest()
	{
		tracker.selectProfile("account-a");
	}

	@Test
	public void firstInventoryObservationDoesNotInventBankData()
	{
		tracker.observeInventory(Map.of(ItemID.LEAGUE_CLUE_BOX_EASY, 1L));
		assertNull(tracker.getCounts());
		assertFalse(tracker.isStale());
	}

	@Test
	public void keepsBankCountsWhenThePlayerLeavesAndInventoryIsUnchanged()
	{
		tracker.refreshBank(bank, Collections.emptyMap());
		tracker.observeInventory(Collections.emptyMap());
		assertSame(bank, tracker.getCounts());
		assertFalse(tracker.isStale());
	}

	@Test
	public void pickupAwayFromBankWarnsWithoutAddingInventoryToBankTotals()
	{
		tracker.refreshBank(bank, Collections.emptyMap());
		tracker.observeInventory(Map.of(ItemID.LEAGUE_CLUE_BOX_MASTER, 1L));
		assertTrue(tracker.isStale());
		assertEquals(2, tracker.getCounts().getScrollTotal());
		tracker.observeInventory(Collections.emptyMap());
		assertTrue(tracker.isStale());
	}

	@Test
	public void depositingAClueOutsideTheFullBankWarnsUntilBankRefresh()
	{
		tracker.refreshBank(bank, Map.of(ItemID.LEAGUE_CLUE_BOX_MASTER, 1L));
		tracker.observeInventory(Collections.emptyMap());
		assertTrue(tracker.isStale());
		assertSame(bank, tracker.getCounts());
		tracker.refreshBank(bank, Collections.emptyMap());
		assertFalse(tracker.isStale());
	}

	@Test
	public void fullBankRefreshSetsInventoryBaselineWithoutAFalseWarning()
	{
		tracker.refreshBank(bank, Collections.emptyMap());
		Map<Integer, Long> withdrawn = Map.of(ItemID.LEAGUE_CLUE_BOX_MASTER, 1L);
		tracker.refreshBank(bank, withdrawn);
		tracker.observeInventory(withdrawn);
		assertFalse(tracker.isStale());
	}

	@Test
	public void missingBankDataDoesNotEraseTheLastSnapshotOrClearAWarning()
	{
		tracker.refreshBank(bank, Collections.emptyMap());
		tracker.markStale();
		tracker.refreshBank(null, null);
		assertSame(bank, tracker.getCounts());
		assertTrue(tracker.isStale());
	}

	@Test
	public void changedBankContainerAwayFromBankCanInvalidateTheSnapshot()
	{
		tracker.refreshBank(bank, Collections.emptyMap());
		tracker.markStale();
		assertSame(bank, tracker.getCounts());
		assertTrue(tracker.isStale());
	}

	@Test
	public void logoutHidesCountsAndRelogRestoresThemWithAWarning()
	{
		tracker.refreshBank(bank, Collections.emptyMap());
		tracker.suspend();
		assertNull(tracker.getCounts());
		tracker.selectProfile("account-a");
		assertSame(bank, tracker.getCounts());
		assertTrue(tracker.isStale());
	}

	@Test
	public void accountsAndGameModesHaveSeparateSnapshots()
	{
		tracker.refreshBank(bank, Collections.emptyMap());
		tracker.selectProfile("account-b");
		assertNull(tracker.getCounts());
		ClueCounts emptyBank = ClueCounter.count(new Item[0], ClueItemClassifier::knownItem).get();
		tracker.refreshBank(emptyBank, Collections.emptyMap());
		tracker.selectProfile("account-a-leagues");
		assertNull(tracker.getCounts());
		tracker.selectProfile("account-a");
		assertSame(bank, tracker.getCounts());
		tracker.selectProfile("account-b");
		assertSame(emptyBank, tracker.getCounts());
	}

	@Test
	public void disablingOrChangingRuneLiteProfileClearsTheCache()
	{
		tracker.refreshBank(bank, Collections.emptyMap());
		tracker.clear();
		tracker.selectProfile("account-a");
		assertNull(tracker.getCounts());
	}
}

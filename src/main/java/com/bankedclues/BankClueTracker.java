package com.bankedclues;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Client-session cache, separated by RuneScape save profile. No disk persistence. */
final class BankClueTracker
{
	private final Map<String, Snapshot> profiles = new HashMap<>();
	private String profile;
	private Snapshot current;

	void selectProfile(String key)
	{
		if (Objects.equals(key, profile))
		{
			return;
		}
		markStale();
		profile = key;
		current = key == null ? null : profiles.computeIfAbsent(key, ignored -> new Snapshot());
		if (current != null)
		{
			current.inventory = null;
		}
	}

	void refreshBank(ClueCounts counts, Map<Integer, Long> inventory)
	{
		if (current != null && counts != null)
		{
			current.counts = counts;
			current.stale = false;
			current.inventory = inventory == null ? null : new HashMap<>(inventory);
		}
	}

	void observeInventory(Map<Integer, Long> inventory)
	{
		if (current == null || inventory == null)
		{
			return;
		}
		if (current.inventory != null && !current.inventory.equals(inventory))
		{
			markStale();
		}
		current.inventory = new HashMap<>(inventory);
	}

	void markStale()
	{
		if (current != null && current.counts != null)
		{
			current.stale = true;
		}
	}

	void suspend()
	{
		selectProfile(null);
	}

	void clear()
	{
		profiles.clear();
		profile = null;
		current = null;
	}

	ClueCounts getCounts()
	{
		return current == null ? null : current.counts;
	}

	boolean isStale()
	{
		return current != null && current.stale;
	}

	private static final class Snapshot
	{
		private ClueCounts counts;
		private boolean stale;
		private Map<Integer, Long> inventory;
	}
}

package com.bankedclues;

import java.util.Arrays;

/** Immutable counts for a bank snapshot that was available. */
final class ClueCounts
{
	private final long[][] quantities;

	ClueCounts(long[][] quantities)
	{
		this.quantities = new long[quantities.length][];
		for (int i = 0; i < quantities.length; i++)
		{
			this.quantities[i] = quantities[i].clone();
		}
	}

	long get(ClueTier tier, ClueItemType type)
	{
		return quantities[type.ordinal()][tier.ordinal()];
	}

	long getTotal(ClueItemType type)
	{
		return Arrays.stream(quantities[type.ordinal()]).sum();
	}

	long getScrollTotal()
	{
		return getScrollTotal(null);
	}

	long getTotal(ClueItemType type, ClueTier tier)
	{
		return tier == null ? getTotal(type) : get(tier, type);
	}

	long getScrollTotal(ClueTier tier)
	{
		long total = 0;
		for (ClueItemType type : ClueItemType.values())
		{
			if (type != ClueItemType.CASKET)
			{
				total += getTotal(type, tier);
			}
		}
		return total;
	}

	@Override
	public boolean equals(Object other)
	{
		return other instanceof ClueCounts && Arrays.deepEquals(quantities, ((ClueCounts) other).quantities);
	}

	@Override
	public int hashCode()
	{
		return Arrays.deepHashCode(quantities);
	}
}

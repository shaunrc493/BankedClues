package com.bankedclues;

final class ClueItem
{
	private final ClueTier tier;
	private final ClueItemType type;

	ClueItem(ClueTier tier, ClueItemType type)
	{
		this.tier = tier;
		this.type = type;
	}

	ClueTier getTier()
	{
		return tier;
	}

	ClueItemType getType()
	{
		return type;
	}
}

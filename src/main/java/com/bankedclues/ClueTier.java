package com.bankedclues;

public enum ClueTier
{
	BEGINNER("Beginner", "Clue scroll (beginner)"),
	EASY("Easy", "Clue scroll (easy)"),
	MEDIUM("Medium", "Clue scroll (medium)"),
	HARD("Hard", "Clue scroll (hard)"),
	ELITE("Elite", "Clue scroll (elite)"),
	MASTER("Master", "Clue scroll (master)");

	private final String displayName;
	private final String itemName;

	ClueTier(String displayName, String itemName)
	{
		this.displayName = displayName;
		this.itemName = itemName;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	// Use ItemComposition.getMembersName() so identification also works on F2P worlds.
	static ClueTier identify(String name, boolean placeholder)
	{
		if (!placeholder)
		{
			for (ClueTier tier : values())
			{
				if (tier.itemName.equals(name))
				{
					return tier;
				}
			}
		}
		return null;
	}
}

package com.bankedclues;

enum ClueItemType
{
	GEODE("Geodes"), BOTTLE("Bottles"), NEST("Nests"), BOX("Boxes"), SCROLL("Scrolls"), CASKET("Caskets");

	private final String displayName;

	ClueItemType(String displayName)
	{
		this.displayName = displayName;
	}

	String getDisplayName()
	{
		return displayName;
	}
}

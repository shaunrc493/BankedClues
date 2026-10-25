# Testing Banked Clues

## Automated checks

From the repository root in PowerShell:

```powershell
.\gradlew.bat clean build
```

The unit suite covers item classification, tier assignment, aggregation, filtered
totals, placeholders, unavailable bank data, inventory-change tracking, stale
warnings, and RuneScape profile isolation. It does not validate live game-event
timing, item sprites, or the appearance of the sidebar.

The development entry point is `BankedCluesPluginTest.main`; despite its name,
it launches RuneLite and is not a JUnit test.

## Manual regression

Only a player may perform these checks. Do not automate RuneScape mouse or
keyboard input. Launch with `.\gradlew.bat run` and follow RuneLite's
[Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts)
guide to log into the development client.

Record the actual item quantities in the bank before testing. Inventory items
must never be added to displayed bank totals. Use whichever items you own;
record missing item/tier coverage rather than assuming it passed.

| Case | Action | Expected result |
| --- | --- | --- |
| First sync | Inspect the panel after a fresh plugin/client start, then open the full bank. | Dashes before the first visit; accurate counts afterwards. |
| Item categories | Compare geodes, bottles, nests, boxes, loose scrolls, and completed reward caskets. | Correct category, tier, and stack quantity. |
| Totals | Select All and add the category counts. | Scrolls includes all unfinished clue sources; Caskets is separate. |
| Filtering | Select every tier and return to All. | Tiles and both headline totals follow the filter without changing cached data. |
| Empty/unsupported categories | Inspect a tier with zero items and the Master natural-source tiles. | Zero for empty supported categories; N/A for Master geodes, bottles, and nests. |
| Bank transfers | Withdraw/deposit one item, part of a stack, and an entire stack. | Exact quantity changes and no lingering revisit warning while the full bank is open. |
| Placeholder | Withdraw a whole stack, leaving its placeholder. | Zero items counted for that stack. |
| Tabs/search | Switch tabs or search for unrelated items. | Counts still cover the full bank. |
| Leave bank | Close the bank, move away, and change the tier filter. | Last synced counts remain available; closing alone does not warn. |
| Slot changes | Rearrange relevant inventory items without changing their quantities. | No new warning. |
| Unrelated inventory | Acquire/drop an unrelated item away from the bank. | No warning from that inventory change alone. |
| Relevant inventory | Acquire/drop a clue source, or open a box/casket away from the bank. | Bank counts stay unchanged; revisit warning appears. |
| Deposit box | Deposit a relevant item without opening the full bank. | Cached counts remain visible with a revisit warning. |
| Warning persistence | Change tier or reverse an inventory change after a warning. | Warning remains until the full bank is revisited. |
| Resync | Open the full bank after a warning. | Counts refresh and the warning clears. |
| Quick close | Withdraw/deposit, immediately close the bank, then inspect the panel. | Final quantities are retained, or a revisit warning indicates uncertainty. |
| World hop/relog | Sync, then hop or relog to the same character. | Session counts return with a refresh warning; a bank visit resyncs. |
| Account/game mode | If available, switch character or RuneScape save profile. | No other profile's counts appear; an unvisited profile needs a bank visit. |
| Restart | Disable/re-enable the plugin, or restart the client. | New bank visit required; no disk snapshot restored. |
| Presentation | Inspect sprites, counts, tooltips, dropdown, and warning text at normal client sizes. | Text/sprites fit; exact quantities remain available in tooltips. |

## Current validation record

- Release-preparation build on 25 October 2026: all 30 JUnit tests passed using
  the existing JDK 17 / Gradle 8.10 setup, resolving RuneLite client 1.13.4.
  Production class files target Java 11. The JAR includes the license and excludes
  development tests, test logging configuration, and plugin service registration.
- On 25 October 2026, the maintainer reported that their own-account in-game
  testing looked correct, including the current display adjustments.
- This is user-reported validation, not automated in-game verification. The exact
  item/tier and secondary-account coverage was not enumerated.
- After release-preparation cleanup, repeat a short smoke check: enable/disable,
  open the bank, filter All and Master, leave the bank, and resync after a warning.

For a failure, record the selected tier, item, expected/displayed quantities,
interface in use, warning state, and whether a full bank revisit fixes it. Attach
screenshots only if useful and remove private information first.

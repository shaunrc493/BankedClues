# Banked Clues

A RuneLite sidebar that helps you remember how many clues and reward caskets you
have banked while you are out playing.

Visit a bank to sync, then check your counts anywhere. Banked Clues keeps data
only in memory: it does not save bank snapshots between client sessions.

## Using the plugin

1. Enable **Banked Clues** in RuneLite's plugin list.
2. Open the scroll icon in the sidebar.
3. Open your full bank to load its contents.
4. Choose **All** or a clue tier from the dropdown.

The item grid shows geodes, bottles, nests, scroll boxes, loose clue scrolls, and
completed reward caskets. Yellow stack counts use compact notation for large
quantities; hover an item or headline total to see the exact quantity.

The repository is being prepared for its first Plugin Hub submission. To try
this source version, use the development launcher below.

## What the totals mean

| Display | Includes |
| --- | --- |
| **Scrolls** | Loose scrolls + geodes + bottles + nests + scroll boxes. Each unopened source counts as one clue. |
| **Caskets** | Completed Treasure Trail reward caskets, counted separately from unfinished clues. |
| **All** | Combined counts across Beginner, Easy, Medium, Hard, Elite, and Master. |
| **A selected tier** | Only that tier, for both headline totals and every item tile. |

Counts cover the entire bank regardless of the selected bank tab or search
filter. Items in your inventory are **not** added to the totals.

The All view uses representative Hard-tier sprites. Individual tiers use their
own sprites. Master geodes, bottles, and nests do not exist, so those tiles show
**N/A**. An available category with no banked items shows **0**. Unknown counts
show dashes until you visit a bank.

Placeholders, challenge scrolls, intermediate clue caskets, scroll cases,
non-clue reward caskets, and Wilderness protection clue boxes are excluded.

## Keeping counts accurate

Closing the bank keeps your last synced counts visible. If a relevant inventory
item changes away from the bank, or the bank container updates outside the full
bank interface, the panel warns:

> Counts may be outdated. Visit a bank to refresh.

For example, this can happen when you receive a clue source, open a scroll box
or casket, drop a relevant item, or deposit one through a deposit box. The warning
is conservative: an inventory change does not necessarily mean your bank changed.
The plugin retains the last bank snapshot rather than guessing new totals.

Reopen the full bank to refresh the counts and clear the warning. Switching tier
filters or reversing an inventory change does not clear it. Unrelated inventory
changes do not trigger it unless a separate bank-container update is observed.

## Session data and privacy

- Snapshots live only in memory and are separated by RuneScape save profile
  (account and game mode).
- Logout hides the counts. Returning to the same profile in the same client
  session restores them with a bank-refresh warning.
- Restarting the client or plugin, or changing RuneLite configuration profiles,
  requires a new bank visit.
- Unavailable bank data is never reported as zero. A failed refresh preserves an
  existing snapshot and marks it potentially outdated.
- The plugin does not write bank data to disk, make its own network requests,
  submit player data, or automate game actions.
- Item sprites are loaded through RuneLite's item-image API.

## Development

Use JDK 17 and the included Gradle 8.10 wrapper. Java compilation targets
Java 11. No separate Gradle installation is needed.

From the repository root in PowerShell:

```powershell
.\gradlew.bat clean build
.\gradlew.bat run
```

In Command Prompt, omit the `.\` prefix. On macOS/Linux, use `./gradlew`.

The run task launches RuneLite in developer mode with the plugin loaded. For
Jagex Accounts, follow RuneLite's
[Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts)
instructions.

The project follows the RuneLite plugin template and resolves
`net.runelite:client:latest.release`. A later RuneLite release can therefore
change the API version used by a new build.

- [Testing and manual regression plan](docs/TESTING.md)
- [Release preparation and remaining publishing steps](docs/RELEASE.md)
- [Changelog](CHANGELOG.md)

The production JAR is written to `build/libs/`. Build output is ignored by Git;
do not commit generated JARs or classes.

## Support and license

Report problems through [GitHub Issues](https://github.com/shaunrc493/BankedClues/issues).
Include the item and tier, expected and displayed counts, whether a bank or
deposit box was open, and whether reopening the full bank corrected the result.
Avoid including account credentials or authentication information.

Banked Clues is licensed under [BSD 2-Clause](LICENSE). Existing third-party
copyright notices in the development configuration are retained.

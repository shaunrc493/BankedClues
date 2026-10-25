# Release preparation

## Current state

Version **1.0.0** is an unpublished release candidate. This preparation does not
publish a release, push commits, or submit a Plugin Hub pull request.

The public author is **shaunrc493**. The support link and repository URL come
from the local Git origin: `https://github.com/shaunrc493/BankedClues`.
Repository visibility and public access still need to be confirmed before
submission.

## Local checks

- Run `gradlew.bat clean build` using JDK 17 and the Gradle 8.10 wrapper.
- Confirm all JUnit tests pass and review the production JAR under `build/libs/`.
- Keep Java compilation targeting release 11.
- Confirm metadata names `com.bankedclues.BankedCluesPlugin`, version 1.0.0,
  the correct author, description, search tags, and support URL.
- Include the BSD 2-Clause license and preserve existing third-party notices.
- Keep generated artifacts, logs, credentials, and local environment files out
  of the submitted commit.
- Perform the short smoke check in [TESTING.md](TESTING.md). Existing maintainer
  testing is recorded there without claiming untested item/account coverage.

The source review found no plugin-owned network/file persistence, reflection,
native calls, process execution, input injection, server-action menu entries,
scene scanning, or combat/PvP assistance. Bank reads and item identification run
on the client thread; Swing updates use the event dispatch thread. Shutdown
invalidates deferred work and removes the sidebar navigation. These are local
review findings, not a guarantee of Plugin Hub acceptance.

## Before publishing

Use the current official
[Plugin Hub instructions](https://github.com/runelite/plugin-hub/blob/master/README.md)
when starting the publishing step; submission requirements can change.

1. Confirm the repository is public and the issue/support link is usable.
2. Review and commit the plugin source, documentation, metadata, and license.
   All development changes currently need to be included; do not submit only
   the cleanup files on top of the original template.
3. Optionally capture a current sidebar screenshot manually for the README.
   Do not automate game input to create one.
4. Optionally add a root `icon.png`. The current hub guide permits an icon no
   larger than 48 by 72 pixels. It is separate from the existing sidebar icon.
5. Recheck that the chosen commit builds, then push it and record its full commit
   hash. Keep version values in Gradle, plugin metadata, and changelog aligned.
6. Follow the official process to create the plugin marker and a reviewable
   Plugin Hub pull request. Use the actual repository URL and committed hash,
   not a local working tree or an invented placeholder.
7. Explain the bank-sync/session-cache behavior, conservative stale warnings,
   read-only UI, and validation performed in the PR description.

The repository's `AGENTS.md` requires this exact attribution footer on a future
Plugin Hub PR description:

```text
Generated-by: Claude Code
```

Do not describe the plugin as available in the Plugin Hub until it has actually
been accepted. Update the README and changelog when publishing occurs.

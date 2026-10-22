# NotRegionKills — Celestara fork

Tracks player mob kills by world, WorldGuard region, and CelestaraZones zone. Requires Java 21 and PlaceholderAPI. WorldGuard, CelestaraZones 1.1.1, and MythicMobs are optional; either region provider or both can be installed. CelestaraZones itself targets Paper/Leaf 1.21.x and requires WorldEdit or FAWE.

## Placeholders

- `%notregionkills_total%`: world totals, counting each death once.
- `%notregionkills_mob_ZOMBIE%`: total kills of a mob type.
- `%notregionkills_world_world%`: kills in a world.
- `%notregionkills_region_arena%`: WorldGuard region kills (unchanged).
- `%notregionkills_zone_arena%`: CelestaraZones zone kills.
- `%notregionkills_zonemob_ZOMBIE_my_arena%`: mob kills in a zone; underscores are supported.
- Existing `%notregionkills_ZOMBIE_arena%` syntax remains available, choosing WorldGuard, then a world, then CelestaraZones with the requested name.

Zone IDs are used, not display names. Identically named zones across worlds share a counter, matching the upstream region naming convention. Global zones (`__global__`) are included. World totals remain independent of region flags.

## Tracking flags

WorldGuard keeps its existing per-region `track-kills` behavior. CelestaraZones tracks every applicable zone returned by its public API. Custom kill-tracking flags are not registered because CelestaraZones 1.1.1 discards unregistered flags while loading storage before exposing its service. Both providers maintain separate counters.

## Build

Install CelestaraZones' published source into your local Maven repository first (its artifact is not on a public Maven repository):

```powershell
git clone https://github.com/CelestaraNetworks/CelestaraZones.git
mvn -f CelestaraZones/pom.xml install -DskipTests
mvn clean package
```

Output: `target/NotRegionKills-1.1.0.jar`. Provider plugins are compile-time dependencies and are not bundled. Existing `stats.json` keys remain readable; new zone keys use `CELESTARAZONES`. Back up server data before upgrading. A live server check with your exact provider and MythicMobs versions is still recommended.

## Changes

- Reuses WorldGuard's region query and replaces repeated map lookups and linear MythicMobs type searches.
- Removes delayed death tasks when MythicMobs is absent; preserves the five-tick revival check when it is installed.
- Captures regions and the entity's world at death time, preventing teleport-related misattribution.
- Writes UTF-8 saves to a temporary file before replacing the destination (atomic where supported).
- Disables safely after a failed stats load without saving over the unreadable file.
- Skips unknown JSON fields and retains the PlaceholderAPI expansion through reloads.

Based on Wamens/NotRegionKills at commit 8f1808dac9b12a7984506233d4a72d50d8b1c00d.

# Zorvex Powers

Original power-system plugin for Zorvex SMP.

## Target
- Paper API: 26.3 (Maven version range `[26.3.build,)`; server screenshot reports build 256 stable)
- Java compiler release: 25
- Maven

## Build
Install JDK 25 and Maven, then run `mvn clean package`.

Output: `target/ZorvexPowers-1.0.0.jar`

## Install
1. Stop the Paper server.
2. Put the JAR in the server's `plugins` folder.
3. Start the server.
4. Run `/plugins` and `/zp list`.

## Commands
- `/powers` — open the power overview GUI.
- `/zp list` — list powers.
- `/zp give <player> <power>` — assign a power to an online player.
- `/zp remove <player>` — remove a player's power.
- `/zp reload` — reload configuration.

## Permissions
- `zorvexpowers.use` (default true)
- `zorvexpowers.admin` (default op)

## Current milestone
Core framework, power registry, player YAML persistence, overview GUI, and admin assignment commands. Active/passive combat abilities, cooldowns, ability books, and interactive selection are next.

**Not yet compiled or tested against the server.** Verify dependency resolution and test on a backup server before production use.

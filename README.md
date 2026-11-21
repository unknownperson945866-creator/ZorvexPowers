# Zorvex Powers 2.0

Transferable, item-based powers for Zorvex SMP.

## Target
- Paper 26.3
- Java 25
- Maven

## Current upgrade
- First-join spin GUI randomly grants one of eight powers.
- Power ownership is attached to the item, not the player.
- The item can be traded or dropped; its unique ID, level, progress, active timer and cooldown are stored in item Persistent Data.
- Only the power item in the main hand can activate. Right-click or use `/powers cast`.
- Each power has its own duration and cooldown in config. Default: 60-second active window, then 120-second cooldown.
- Eight themed crafting recipes are registered by default.
- Each power has a different progression task and level cap 3.
- Admin command gives power items instead of assigning powers to player UUIDs.

## First join
New players receive the spin GUI after joining. Click the star to randomly receive one power item. The spin completion is saved in `plugins/ZorvexPowers/spin-data.yml`.

## Commands
- `/powers` — inspect the power item in your main hand.
- `/powers cast` — activate the held item.
- Right-click while holding a power item — activate it.
- `/zp list` — list all powers.
- `/zp give <player> <power>` — give an item (AGNI, BOREAS, OCEANIC, EOS, YMIR, CORROSIVE, ASTRAEUS, CHAINED).
- `/zp remove <player>` — remove the power item from the target's main hand.
- `/zp reload` — reload config.

## Progression tasks
Progress is earned only while the matching item is in the main hand:
- AGNI — kills.
- BOREAS — sprint travel.
- OCEANIC — swim travel.
- EOS — experience gained.
- YMIR — mine ice/snow.
- CORROSIVE — break selected nature/magma blocks or consume spider eyes.
- ASTRAEUS — ender-pearl teleports.
- CHAINED — craft vanilla iron bars.

Each level threshold is configurable under `progression`. Progress resets when the item levels up. Maximum level is 3.

## Configuration
Edit `plugins/ZorvexPowers/config.yml`:
- `powers.<POWER>.duration-seconds`
- `powers.<POWER>.cooldown-seconds`
- `progression.<POWER>.level-1` and `level-2`
- `recipes.enabled`

Cooldown starts after the active duration ends. Defaults for every power are 60 seconds active and 120 seconds cooldown. The item carries its own timestamps, so trading it does not reset its timer.

## Build and install
Build with JDK 25 and Maven:
```sh
mvn --batch-mode clean package
```
Output: `target/ZorvexPowers-1.0.0.jar` (the Maven artifact filename remains stable).

Back up the server, stop it, replace the old JAR in `plugins`, then start Paper and check the console. Test on a staging server before production.

## Verification
Source changes are on branch `upgrade/item-based-powers`. A successful CI build and in-game Paper 26.3 testing are still required before calling this upgrade ready.

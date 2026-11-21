# Zorvex Powers

An original power-system plugin for Zorvex SMP.

## Target platform
- Paper 26.3 API
- Java 25
- Maven

## Features
- Eight original powers: Agni, Boreas, Oceanic, Eos, Ymir, Corrosive, Astraeus, and Chained.
- Eight active abilities with particles, sounds, status effects, and configurable shared cooldown.
- 27-slot powers overview menu with a clickable ability-cast button.
- Per-player UUID-based YAML persistence.
- Admin assignment and removal commands.
- GitHub Actions Maven build that uploads the plugin JAR as an artifact.

## Build
Use JDK 25 and Maven:
```sh
mvn --batch-mode clean package
```
Output: `target/ZorvexPowers-1.0.0.jar`.

The GitHub Actions workflow runs this build automatically on pushes and pull requests to `main`.

## Install
1. Back up the server and stop it.
2. Put `ZorvexPowers-1.0.0.jar` in the server's `plugins` folder.
3. Start Paper and check `/plugins` and the console.
4. Run `/zp list`, then assign a power with `/zp give <player> <power>`.

## Player commands
- `/powers` — open the powers menu.
- `/powers cast` — cast the assigned ability.
- Click **Cast Ability** in the menu to cast it.

## Admin commands
- `/zp list` — list all powers.
- `/zp give <player> <power>` — assign a power to an online player.
- `/zp remove <player>` — remove a power from an online player.
- `/zp reload` — reload configuration.

## Permissions
- `zorvexpowers.use` (default true)
- `zorvexpowers.admin` (default op)

## Configuration
Edit `plugins/ZorvexPowers/config.yml`. The shared ability cooldown is controlled by `settings.cooldown-seconds` (default 15). Run `/zp reload` after editing.

## Verification status
The repository contains a GitHub Actions build workflow. A successful build and an in-game Paper 26.3 test are still required before production use. Do not treat the source as verified until the workflow reports success.

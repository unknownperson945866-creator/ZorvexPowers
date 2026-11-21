# ZORVEX POWERS — Fabric Client

Client-only companion mod for the ZorvexPowers Paper plugin.

## Target
- Minecraft Java 26.3
- Fabric Loader 0.20.1+
- Fabric API 0.166.2+26.3
- Java 25
- Fabric Loom 1.17.x

## Current feature
- Receives the assigned power from Paper over the `zorvexpowers:power` channel.
- Displays a compact, colour-coded HUD for all eight powers.
- The mod is client-only and does not need to be installed on the Paper server.

## Build
From this directory, with JDK 25 and Gradle 9.6:
```sh
gradle build
```
Output: `build/libs/zorvex-powers-client-1.0.0.jar`.

## Install
1. Install Fabric Loader for Minecraft 26.3.
2. Install Fabric API for Minecraft 26.3.
3. Put the ZORVEX POWERS client JAR in the client's `mods` folder.
4. Update the Paper server's ZorvexPowers plugin to the matching build from this repository.
5. Join and receive a power with `/zp give <player> <power>`.

## Important
This first client build adds a synchronized HUD, not new server-authoritative inventory items. Custom items must be created and managed by the Paper plugin; the client mod can later supply their custom visuals. A single Fabric JAR cannot cover every Minecraft release; this build targets 26.3.

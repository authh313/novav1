# Nova Client

Nova is a client-side Fabric mod for Minecraft 1.21.11. The package keeps the version and dependency settings supplied with the project.

## UI and HUD

- Right Shift opens a dark, responsive control center with category navigation and live module toggles.
- The HUD page independently toggles the watermark, enabled-module list, coordinates, performance strip, storage panel, and amethyst finder panel.
- HUD visibility and the two world-tool toggles persist in `config/nova-hud.properties`.
- Coordinates include the current dimension and chunk position.

## World tools

- **Storage ESP** lists nearby chests, trapped chests, ender chests, barrels, and shulker boxes with coordinates and distance.
- **Amethyst Finder** lists nearby chunks containing amethyst blocks or buds.
- Scans run on the client thread, update periodically, and inspect only chunks already loaded by Minecraft. They do not request additional chunks from the server. The result panels are coordinate locators rather than through-wall 3D boxes.

## Module scaffold

The other sample entries in `ModuleManager` are still toggle scaffolding. They do not implement their named gameplay behavior yet; add real behavior in module subclasses or the `Module` lifecycle hooks before relying on them.

## Build

See [BUILD.md](BUILD.md). Building requires JDK 21 and Gradle. Project version fields remain as supplied.

# DuelsPvP — Paper 1.21.11

Requires Paper 1.21.11 and Java 21.

## Build with GitHub Actions
1. Create a new empty GitHub repository.
2. Upload the contents of this project ZIP to the repository root.
3. Open the **Actions** tab and select **Build DuelsPvP**.
4. After the workflow succeeds, open the run and download the `DuelsPvP-1.21.11` artifact.
5. The artifact contains the plugin JAR. Put it in the server's `plugins` folder and restart.

## Kit commands
- `/kit add <name> <item>` — creates a kit and sets its GUI icon.
- Arrange the desired items and armor in your inventory, then run `/kit set <name>` to save them.
- `/kit list` — lists kits.
- `/kit delete <name>` — deletes a kit.

Example:
- `/kit add sword DIAMOND_SWORD`
- Arrange the kit items and armor in your inventory.
- `/kit set sword`

Kit data is stored in `plugins/DuelsPvP/config.yml`.

## Development status
This is the foundation stage: plugin bootstrap and persistent kit definitions.
The next development stage is the lobby GUI (sword and crafting-table menus), then the duel flow and arena management. The gameplay systems will be restricted to the `duel` world.

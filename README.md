# Tag Game Plugin

A fun and interactive tag game plugin for Minecraft 1.21.4 servers.

## Features

- Multiple game modes: Classic, Freeze Tag, and Team Tag
- Customizable maps with corner-based boundaries
- WorldGuard integration for map protection
- Hologram and sign-based leaderboards
- Power-ups and special items
- Auto-start functionality
- Team-based gameplay

## Dependencies

- Minecraft 1.21.4
- WorldGuard (for map protection)
- DecentHolograms (optional, for hologram leaderboards)

## Installation

1. Download the latest release from the releases page
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/TagGame/config.yml`

## Setting Up a Map

1. **Create a Game Sign:**
   ```
   [TagGame]
   <mode>
   <teamsize>
   <mapname>
   ```
   Example:
   ```
   [TagGame]
   team
   5
   Desert
   ```

2. **Set Up Map Boundaries:**
   ```
   /tag setcorner1 <gameID>  # Stand at first corner
   /tag setcorner2 <gameID>  # Stand at second corner
   ```
   This will automatically create a WorldGuard region with:
   - Mob spawning disabled
   - Block breaking/placing disabled
   - PvP enabled

3. **Set Spawn Points:**
   ```
   /tag setitspawn <gameID>      # Stand at IT spawn
   /tag setplayerspawn <gameID>  # Stand at player spawn
   ```

4. **Configure Game Settings:**
   ```
   /tag mode <gameID> <mode>     # Set game mode
   /tag autostart <gameID> true  # Enable auto-start
   ```

## Game Modes

- **Classic**: Standard tag game
- **Freeze**: Tagged players are frozen until unfrozen
- **Team**: Team-based tag game

## Commands

- `/tag help` - Show help menu
- `/tag join <gameID>` - Join a game
- `/tag leave` - Leave current game
- `/tag start <gameID>` - Start a game (Admin)
- `/tag stop <gameID>` - Stop a game (Admin)
- `/tag it` - Check who is IT
- `/tag score` - View scores
- `/tag list` - List active games

## Admin Commands

- `/tag setmap <gameID> <mapname>` - Set map name
- `/tag setitspawn <gameID>` - Set IT spawn
- `/tag setplayerspawn <gameID>` - Set player spawn
- `/tag setcorner1 <gameID>` - Set first map corner
- `/tag setcorner2 <gameID>` - Set second map corner
- `/tag mode <gameID> <mode>` - Set game mode
- `/tag autostart <gameID> <true/false>` - Toggle auto-start
- `/tag setleaderboard` - Create leaderboard hologram

## WorldGuard Integration

The plugin automatically creates WorldGuard regions for each map with these flags:
- `mob-spawning: deny` - Prevents mob spawning
- `block-break: deny` - Prevents block breaking
- `block-place: deny` - Prevents block placing
- `pvp: allow` - Enables PvP for the tag game

## Support

For support, please create an issue on the GitHub repository.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
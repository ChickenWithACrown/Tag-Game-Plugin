# Tag Game Plugin

A versatile Minecraft tag game plugin with multiple game modes, custom maps, and team support. **Now supports Minecraft 1.21.4!**

## Features
- Multiple game modes (Classic, Freeze Tag, Team Mode)
- Custom map support (multiple maps in the same world)
- Team mode (2v2 to 10v10)
- Special items and power-ups (using correct 1.21.4 potion types)
- Score tracking and leaderboards (hologram-based and sign-based)
- Auto-start functionality
- Customizable game settings

## Installation
1. Download the latest release from the [GitHub releases page](https://github.com/ChickenWithACrown/Tag-Game-Plugin/releases)
2. Place the `.jar` file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/TagGame/config.yml`

## Game Modes

### Classic Mode
- One player is "IT"
- Tag other players to make them "IT"
- Score points for each successful tag
- Game ends after time limit or when all players are tagged

### Freeze Tag
- Tagged players are frozen in place
- Other players can unfreeze them
- Game ends when all players are frozen

### Team Mode
- Two teams compete
- Team members can't tag each other
- Score points for tagging opposing team members
- Team with most points wins

## Setting Up a Game

### 1. Create Multiple Maps in the Same World
1. Build your map areas in the same world (e.g., "ForestMap" and "DesertMap")
2. For each map, set the map name and spawns:
   - Stand at the IT spawn for your first map and run:
     ```
     /tag setmap ForestMap
     /tag setitspawn
     /tag setplayerspawn
     ```
   - Move to your second map area, stand at the IT spawn, and run:
     ```
     /tag setmap DesertMap
     /tag setitspawn
     /tag setplayerspawn
     ```

### 2. Create a Game Sign
1. Place a sign with this format:
```
[TagGame]
<mode>
<teamsize>
<mapname>
```
Example for 5v5 team mode on ForestMap:
```
[TagGame]
team
5
ForestMap
```

### 3. Configure Game Settings
```
/tag mode <mode> - Set game mode
/tag autostart <true/false> - Enable/disable auto-start
/tag border <size> - Set game border size
```

## Commands

### Player Commands
- `/tag` - Open game menu
- `/tag help` - Show help menu
- `/tag join` - Join a game
- `/tag leave` - Leave current game
- `/tag it` - Check who is IT
- `/tag score` - View current scores

### Admin Commands
- `/tag start` - Start a game
- `/tag stop` - Stop current game
- `/tag setmap <name>` - Set current map
- `/tag setitspawn` - Set IT spawn point
- `/tag setplayerspawn` - Set player spawn point
- `/tag mode <mode>` - Change game mode
- `/tag autostart <true/false>` - Toggle auto-start
- `/tag border <size>` - Set game border
- `/tag setleaderboard` - Create leaderboard hologram

## Leaderboards
- **Hologram Leaderboard:** Use `/tag setleaderboard` to create a floating leaderboard at your location (requires DecentHolograms plugin).
- **Sign Leaderboard:** Place a sign with `[TagLeader]` to show the top 3 players on a sign.

## Special Items (1.21.4+ Compatible)

### IT Player Items
- Tagger (Blaze Rod) - Tag other players
- Speed Potion - Temporary speed boost
- Jump Potion - Enhanced jumping (`JUMP_BOOST`)
- Strength Potion - Increased damage (`STRENGTH`)

### Runner Items
- Smoke Bomb - Creates smoke cloud
- Speed Potion - Temporary speed boost
- Invisibility Potion - Hide from IT
- Jump Potion - Enhanced jumping (`JUMP_BOOST`)
- Shield - Block IT's attacks
- Grappling Hook - Swing to safety
- Teleporter - Random teleportation

## Configuration

### config.yml
```yaml
settings:
  preventTagBack: true
  broadcastTags: true
  gameDuration: 600
  countdownDuration: 10
  borderSize: 100

maps:
  ForestMap:
    it_spawn: [x, y, z]
    player_spawn: [x, y, z]
    border_size: 100
```

## Permissions
- `tag.admin` - Access to admin commands
- `tag.admin.start` - Start games
- `tag.admin.stop` - Stop games
- `tag.admin.setmap` - Set maps
- `tag.admin.mode` - Change game modes
- `tag.admin.setleaderboard` - Create leaderboards

## Creating Custom Maps

1. Build your map in the world
2. Use `/tag setmap <name>` to create a new map
3. Set spawn points:
   - `/tag setitspawn` - Set IT spawn
   - `/tag setplayerspawn` - Set player spawn
4. Create a sign for the map:
```
[TagGame]
<mode>
<teamsize>
<mapname>
```

## Team Mode Setup

1. Create a map with team spawns
2. Place a sign:
```
[TagGame]
team
5
<mapname>
```
3. Players can join either team
4. Game starts when both teams have enough players

## Troubleshooting

### Common Issues
1. Game won't start
   - Check if enough players have joined
   - Verify spawn points are set
   - Check if map exists

2. Players can't join
   - Check if game is full
   - Verify player has permission
   - Check if game is already running

3. Spawn points not working
   - Use `/tag setitspawn` and `/tag setplayerspawn`
   - Verify locations are within world border
   - Check if map is properly configured

## Support
For issues and feature requests, please create an issue on the [GitHub repository](https://github.com/ChickenWithACrown/Tag-Game-Plugin/issues).

## License
This project is licensed under the MIT License - see the LICENSE file for details.
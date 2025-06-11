# TagGame Plugin - Beginner's Guide

Welcome to the TagGame plugin! This guide will walk you through setting up and playing your first game of Tag.

## Prerequisites

- Minecraft server running Spigot/Paper 1.8.8 or higher
- TagGame plugin installed in your server's plugins folder
- Required Plugins:
  - [WorldGuard](https://dev.bukkit.org/projects/worldguard/files) - For game area protection
  - [DecentHolograms](https://www.spigotmc.org/resources/decentholograms-1-8-1-19-4.96927/) - For leaderboard features
  - [EssentialsX](https://essentialsx.net/downloads.html) - For /spawn command support

### Plugin Installation
1. Download the required plugins from the links above
2. Place the .jar files in your server's `plugins` folder
3. Restart your server
4. Configure each plugin:
   - WorldGuard: Create a region for your game area
   - DecentHolograms: No additional configuration needed
   - EssentialsX: Set up your spawn point using `/setspawn`

## Setting Up Your First Game

### Step 1: Create a Game Sign
1. Place a sign in your desired location
2. Write the following on the sign:
   ```
   [TagGame]
   classic
   5
   MyFirstGame
   ```
   - First line: `[TagGame]` (exactly as shown)
   - Second line: Game mode (classic, freeze, or team)
   - Third line: Team size (number of players per team)
   - Fourth line: Map name (can be any name you want)

### Step 2: Set Up Game Area
1. Go to where you want the game to take place
2. Run these commands in order:
   ```
   /tag setcorner1 MyFirstGame
   /tag setcorner2 MyFirstGame
   ```
   This will create a protected game area between the two corners you set.

### Step 3: Set Spawn Points
1. Go to where you want the "IT" player to spawn
2. Run:
   ```
   /tag setitspawn MyFirstGame
   ```
3. Go to where you want other players to spawn
4. Run:
   ```
   /tag setplayerspawn MyFirstGame
   ```

### Step 4: Configure Game Settings (Optional)
1. Open the map flags GUI:
   ```
   /tag map MyFirstGame flags
   ```
2. You can toggle:
   - Mob Spawning
   - Block Breaking
   - Block Placing
   - PvP
   - Decay

## Playing the Game

### Basic Commands
- `/tag` - Opens the main game menu
- `/tag join MyFirstGame` - Join a specific game
- `/tag leave` - Leave the current game
- `/tag it` - Check who is currently "IT"
- `/tag score` - View current scores

### Starting the Game
1. Players can join by:
   - Right-clicking the game sign
   - Using `/tag join MyFirstGame`
2. Once enough players have joined, an admin can start the game:
   ```
   /tag start MyFirstGame
   ```
   Or enable auto-start:
   ```
   /tag autostart MyFirstGame true
   ```

### Game Rules
1. One player is randomly chosen to be "IT"
2. "IT" must tag other players to make them "IT"
3. The game lasts for 10 minutes
4. Players earn points for each successful tag
5. Players cannot leave the game area
6. The player with the most points at the end wins

### Special Items
#### For "IT" Player:
- Tagger (Blaze Rod) - Used to tag players
- Speed Boost Potion
- Jump Boost Potion
- Strength Potion

#### For Runners:
- Smoke Bomb (Ender Pearl) - Creates a smoke cloud
- Speed Boost Potion
- Invisibility Potion
- Jump Boost Potion

## Admin Commands
- `/tag start <gameID>` - Start a game
- `/tag stop <gameID>` - Stop a game
- `/tag mode <gameID> <mode>` - Change game mode
- `/tag setmap <gameID> <mapname>` - Set map name
- `/tag setleaderboard` - Create a leaderboard at your location

## Game Modes
1. **Classic** - Traditional tag game
2. **Freeze** - Tagged players are frozen until unfrozen
3. **Team** - Players are divided into teams

## Tips for First-Time Players
1. Use your special items wisely
2. Stay within the game area
3. Watch out for the "IT" player
4. Use smoke bombs to escape
5. Check the scoreboard regularly

## Troubleshooting
- If players can't join: Check if the game sign is properly formatted
- If game area isn't working: Make sure both corners are set
- If spawn points aren't working: Verify both spawn points are set
- If WorldGuard isn't working: Ensure the plugin is installed and configured

## Need Help?
- Use `/tag help` for a list of all commands
- Check the game sign for current status
- Ask a server admin for assistance

Happy tagging! 
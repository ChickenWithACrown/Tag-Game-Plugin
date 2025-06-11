# TagGame Plugin Release Notes

## Version 1.0.0
Initial release of the TagGame plugin.

### Features
- Multiple game modes (Classic, Freeze, Team)
- Custom game areas with WorldGuard integration
- Interactive game signs
- Leaderboard system with DecentHolograms support
- Special items for both "IT" and runner players
- Score tracking and statistics
- Auto-start functionality
- Map configuration system
- Team-based gameplay support

### Requirements
- Minecraft Server: Spigot/Paper 1.8.8 or higher
- Java 8 or higher
- Required Plugins:
  - WorldGuard (for game area protection)
  - DecentHolograms (for leaderboard features)
  - EssentialsX (for spawn command support)

### Installation
1. Download the TagGame.jar file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure your first game using the commands in the Guide.md

### Commands
- `/tag` - Main menu
- `/tag help` - Shows all commands
- `/tag join <gameID>` - Join a game
- `/tag leave` - Leave current game
- `/tag start <gameID>` - Start a game (Admin)
- `/tag stop <gameID>` - Stop a game (Admin)
- `/tag it` - Check who is "IT"
- `/tag score` - View scores
- `/tag setleaderboard` - Create leaderboard (Admin)

### Permissions
- `tag.admin` - Access to admin commands
- `tag.admin.start` - Permission to start games
- `tag.admin.stop` - Permission to stop games
- `tag.admin.setleaderboard` - Permission to create leaderboards
- `tag.admin.mode` - Permission to change game modes
- `tag.admin.setmap` - Permission to set map names

### Known Issues
- None in current release

### Future Plans
- Additional game modes
- More special items
- Customizable game durations
- Team balancing system
- Statistics tracking
- Tournament support

### Support
For support, please:
1. Read the Guide.md file
2. Check the troubleshooting section
3. Contact the developer if issues persist

### Credits
- Developed by ChickenWithACrown
- Special thanks to:
  - WorldGuard team for region protection
  - DecentHolograms team for hologram support
  - EssentialsX team for spawn command functionality

### License
This plugin is licensed under the MIT License. See LICENSE file for details. 
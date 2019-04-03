# Maintenance Changelog
This file contains update logs for this project. The top may contain a `Unreleased` section, gathering update logs for a future update during development.

## Unreleased
### Changed
* Created an addon to have PlaceholderAPI placeholders on Spigot when running the Maintenance plugin on Bungee (see [**here**](https://github.com/KennyTV/Maintenance/wiki/MaintenanceAddon))
* Spigot version: If running on Paper 1.12.2 or higher, ProtocolLib will not be needed anymore
* The universal jar now only includes Bungee and Spigot (whereas before Sponge and Velocity as well) and has been further minimized in size
  * To use the plugin on the other platforms, use the `MaintenanceSponge.jar` or `MaintenanceVelocity.jar` respectively
* Added config option `continue-endtimer-after-restart` to have endtimers continued, even if the server restarts during its runtime
* Added custom eventsystem to the API
* Removed bStats metrics from Sponge module
### Fixed
* Actually fixed compatibility to other plugins using the same MySQL library (such as LuckPerms) in the Bungee version
* Fixed an occasional error on pings of non clients in the Spigot version
* Fixed 1.13 clients on sub 1.13 servers not showing the playercountmessage in the Spigot version
* Minor fixes/changes to Spanish and Portuguese language files
---
## 3.0 - February 17th 2019
### Changed
* This plugin now also supports the platforms Sponge and Velocity!
* Added tabcompletion for maintenance commands on all platforms
* This plugin now uses a custom config system, which means comments won't vanish after saving them and the different configs have been unified
* You can now also add and remove (offline) players to/from the maintenance whitelist by using their uuid (i.e. `/maintenance add <uuid>`)
* The %TIMER% variable can now also be used in the playercount-, playercounthover- and kickmessages
* To make life easier for people running on Bungee without an extra permission plugin: the `maintenance.admin` permission gives access to all subcommands
* Made bug reports a little easier with the new `/maintenance dump` command
* Added a config option to disable update checks (not recommended)
* Moved `kickmessage` field from config to language file and added its default translations
* Changed permissions nodes for the Bungee part (`from: to`):
  * `maintenance.toggleserver`: `maintenance.singleserver.toggle`
  * `maintenance.servertimer`: `maintenance.singleserver.timer`
  * `maintenance.status`:  `maintenance.singleserver.status`
* The command `/maintenance timer abort` has been renamed to `/maintenance aborttimer`
* Added additional aliases: `/maintenance end` for `/maintenance endtimer`, `start` for `starttimer`, `abort` for `aborttimer`
* On false command syntax usage, the reply will be the specific help message for that command, not the entire help window as it was before
* Improved update checks and updater
* Many many other runtime improvements and some major internal refactoring
### Fixed
* Fixed a startup issue with other plugins using the same MySQL library on Bungee (for example some versions of LuckPerms)
* Fixed an error on Bungee when single servers are set to maintenance with no fallbackserver set
* Fixed a rare problem with adding and removing players to/from the maintenance whitelist
* Fixed an issue with starting server timers while also running a global maintenance timer
* Fixed the `/maintenance status` not appearing in the help overview
---
## 2.5.2 - December 17th 2018
### Changed
* Added French `fr` (thanks to Zendrique) and Portuguese `pt` (thanks to JoaoPinto) translations!
* Improvements to MySQL stuff on the Bungee part
* If enabled, single servers (on the Bungee part) that are under maintenance are now saved into the given database as well!
### Fixed
* Fixed the maintenance value only loading on the first server ping, not on startup
* Fixed an error while setting maintenance to single servers, when the executor is something other than a player
* Fixed the maintenance-icon not loading on 1.13+ Spigot servers with ProtocolLib
* Some other general improvements and a small message fix
---
## 2.5.1 - November 17th 2018
### Fixed
* Fixed an occasional file loading error on startup
---
## 2.5 - November 17th 2018
### Changed
* Added the possibility on BungeeCord to also enable maintenance on Spigot servers managed by the proxy
  * `/maintenance <on/off> <servername>` will now set maintenance to a proxied Spigot server
  * `/maintenance <starttimer/endtimer> <server> <minutes>` works analog to the normal timer commands
  * `/maintenance status` shows proxied servers that have maintenance enabled
  * new permissions are `maintenance.toggleserver`, `maintenance.servertimer` and `maintenance.status`
* Added a multiple-language-support system! It's currently not that big tho, with the only language added being de/German
  * If you like this plugin, know French or Spanish (or any other language that isn't yet supported by the plugin) and want to contribute a bit to it, translations of the default language.yml are greatly appreciated!
  * Note: There will be a new 'language-en.yml' file created, so if you changed any messages before, you have to do it again in the new file
* Made the help message a little fancier
* Some API changes
### Fixed
* Fixed a bug with starttimers not being able to start properly
---
## 2.4 - September 17th 2018
### Changed
* Edit nearly every message to your hearts content with the now introduced language.yml file!
* Added `/maintenance removemotd <index>` command to remove a motd via an ingame command (if you have multiple ones)
* You can now include the `%TIMER%` variable into your maintenance motd - **If using the maintenance endtimer**, it will display the time left until maintenance will be disabled!
  * Simply shows `-` if maintenance is on without any timer
  * A nice integration could be `&cExpected to come back in: &6%TIMER%`
* You can now choose to show the normal playercount during maintenance instead of a custom message by changing the `enable-playercountmessage` value in the config
* You can also include the playercount in a custom playercountmessage by using the 2 variables `%ONLINE%` and `%MAX%`
* If there's a new update available, the update notification will no longer be sent on every single login, but rather just on the first one after startup
* Removing players from the maintenance whitelist now accepts uncapitalized names
---
## 2.3.1 - August 17th 2018
### Changed
* When a string in the config is missing, the plugin doesn't cancel the current process and throws an error anymore, but rather gives a soft warning in the console and returns a backup string
* If MySQL is enabled, the plugin will only make a database request at least x seconds after the last request, whereas you can define the value in the config under `mysql.update-interval`
  * Example: If a first player pings the server after startup, the value will be updated -> No update in the next x seconds even if a player pings -> After elapsed, as soon as the next player pings the server again
  * Especially helpful as well as recommended for servers with many players
* Another improvement for MySQL support: After enabling maintenance on one proxy, the other connected ones will then also kick all non permitted players and switch their ServerListPlus state!
* The `/maintenance` helppage now only shows the commands the player actually has permission to execute
---
## 2.3 - July 17th 2018
### Changed
* Added the possibility to have multiple pingmessages, of which one will be randomly chosen on each ping when maintenance is enabled (you can also just keep it at one)!
* Added permissions per subcommand
  * `maintenance.command` required to use any of the commands
  * `maintenance.toggle` to use /maintenance <on/off>
  * `maintenance.reload` to use /maintenance reload
  * `maintenance.update` to use /maintenance <update/forceupdate>
  * `maintenance.timer` to use /maintenance <starttimer/endtimer> <minutes>
  * `maintenance.whitelist.add` to use /maintenance add <player>
  * `maintenance.whitelist.remove` to use /maintenance remove <player>
  * `maintenance.whitelist.list` to use /maintenance whitelist
  * `maintenance.setmotd` to use /maintenance setmotd <index> <1/2> <message>
  * `maintenance.motd` to use /maintenance motd
* Added `/maintenance motd` command to list all set maintenance pingmessages
* `/maintenance whitelist` now also shows the UUID connected to each name
### Fixed
* Reworked/fixed the `/maintenance setmotd` command
* Some typo and description fixes
---
## 2.2.2 - June 17th 2018
### Changed
* Changed the loading of the maintenance icon to be more predictable: It will load directly on startup and can now be reloaded by using the `/maintenance reload` command as well
* Changed the reload message to read the proper reload changes
* Removed an obsolete config comment
* You can now hook into the plugin with the integrated API
---
## 2.2.1 - April 17th 2018
### Changed
* Added `/maintenance setmotd <1/2> <message>` command to edit the maintenance mode motd via a command
* The Bungee version of the plugin will now automatically create a MySQL table if it is enabled, so you won't have to manually create one anymore
### Fixed
* Fixed the ServerListPlus motd only changing on using the command, but not on the server startup
* Fixed an error with canceling the start-/endtimer in the Bungee version
* Fixed an error with removing players from the whitelist in the Spigot version
---
## 2.2 - March 17th 2018
### Changed
* Great update for the Spigot version of the plugin as well as performance improvements and bugfixes in both versions
* If you have ProtocolLib installed, you can set a custom server icon while maintenance is enabled, also the playercount-message and the playercount-hover-message are now available (as it has already been in the Bungee version)!
* In the BungeeCord version of the plugin, the `/maintenance remove <player>` command now also works for offline players
* Removed the plugin's prefix from some messages
* General performance improvements
### Fixed
* The Spigot version should be much more compatible with other motd-changing plugins, such as SexyMotd
* Fixed some other issues with the ProtocolLib integration
* Minor textfix for the updatemessage in the Spigot version
---
## 2.1 - February 17th 2018
### Changed
* Massive code cleanup for both the Bungee and Spigot version of the plugin
* Minor performance improvements
### Fixed
* Fixed start- and endtimers not being cancelled properly
* Fixed an error for the update command in the Spigot version
* Fixed an error regarding the ServerListPlus integration in the Spigot version
---
## 2.0 - February 15th 2018
### Changed
* You can now put the plugin not only inside your Bungee plugins folder, but also your Spigot/Bukkit one! Both will have the same feautures, but simply on their own platform. However, the Spigot version doesn't have MySQL support (as you would normally use BungeeCord for connecting multiple Spigot/Bukkit servers) and you can't use playercount-message, besides this, it has all its main features.
  * On BungeeCord you can make your own custom messages for the playercount
  * On Spigot you will see `N/0` (N being the actual count). If you also have ProtocolLib on your server, `0/0` will be displayed.
* As many of you asked me: Maintenance now supports the plugin ServerListPlus
* Performance improvements
### Fixed
* Fixed some permission and message bugs
* Fixed problems with adding and removing players from the maintenance whitelist
* Fixed update checks
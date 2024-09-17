# Maintenance Changelog
This file contains update logs for this project. The top may contain a `Unreleased` section, gathering update logs for a future update during development.

---
## 4.3.0 - September 17th 2024
### Changed
* Added alternative player count and player list hover message for when endtimers are running, similar to the existing ping messages setting. They are disabled by default and can be found in their `player-count-message` and `player-list-hover-message` config sections
* All timer commands now accept durations like `2h30m`, `1h30m5s`, or `90s`. The default behavior without units will still be in minutes
  * The language defaults will *not* automatically update the command help messages
* Slightly reorganized the configuration file (it will be automatically migrated on startup, but new sections will be slapped at the bottom of the file)
* The `fallback` field now also accepts null values next to an empty array for disabling the feature
* Updated language files from [Crowdin](https://crowdin.com/translate/maintenance)

### Fixed
* Fixed the `/maintenance dump` command
* Fixed the original player sample not being cleared in Velocity
* Velocity: Fixed the console getting broadcasts twice
* Velocity: Don't log legacy color codes to the console
* Paper: Fixed the player count messages on Paper 1.21
---
## 4.2.1 - May 17th 2024
### Changed
* The proxied maintenance status is now also shown for forced hosts (thanks to alec-jensen)
* Offline player fetching improvements (thanks to EpicPlayerA10)
  * Added the setting `fallback-to-offline-uuid` to use offline player uuids in maintenance whitelist commands (defaults to false)
* Maintenance now skips the plugin remapping process on 1.20.5+ Paper servers
* Small optimizations
* Updated language files from [Crowdin](https://crowdin.com/translate/maintenance)
  * Massive thank you to all the contributors! These include: vortix22, xdalul, CodeZhangBorui, jesusskye, Doc94,
    Texider_, marvin2k0, leonard.bausenwein, pro.timeo.kerjean, meviper, troev5, Murka124, jhqwqmc, Pryzinho,
    raysetratyboy, Kolja07, and rikunightcore
---
## 4.2.0 - September 17th 2023
### Changed
* Velocity/Bungee: Added `commands-on-single-maintenance-enable` and `commands-on-single-maintenance-disable` config options to define commands to be executed after maintenance has been toggled on a proxied server
  * Commands inside the `all` list will be executed for any proxied server, but you can also define commands for specific servers by adding arrays with the server names as keys
  * You can use the placeholder `%SERVER%` in commands to be replaced with the server name
* Start-, end-, and schedule timers for proxied servers now also include the server in the feedback message
* Updated language files from [Crowdin](https://crowdin.com/translate/maintenance) (including new language files for Danish, Japanese, Korean, and Hungarian)
---
## 4.1.0 - April 17th 2023
### Changed
* Added config option `enable-playercounthovermessage` to be able to toggle the player count hover message
* Removed platform specific command aliases (e.g. `maintenancevelocity`)
* Translations
  * Moved translation editing to [Crowdin](https://crowdin.com/translate/maintenance)
  * Added Turkish translation file (thanks to Proomp)
  * Added Swedish translation file (thanks to Sup33r)
  * Added Ukrainian translation file (thanks to Mrlucke)
  * Added Japanese translation file (thanks to yu-solt)
* Added bStats metrics to Velocity and Sponge modules

### Fixed
* Fixed disabling the `enable-pingmessages` setting not working on Paper servers
* Fixed variable replacement in messages with gradients
* Fixed the message for cancelling proxied server timers not replacing the server argument
* Setting `enable-pingmessages` to `false` no longer disables custom player and player hover messages
---
## 4.0.1 - April 17th 2022
### Fixed
* Fixed the config header sometimes breaking the config on upgrading/saving
---
## 4.0.0 - March 17th 2022
### Changed
* Added support for hex colors in messages and replaced the component parsing with adventure-minimessage
  * This means that section symbols (`§`) are no longer the intended/supported format
  * You can use the following page to edit and preview formatted text: https://webui.adventure.kyori.net/
  * You can find full documentation on the format (including normal colors and formatting, rgb, click/hover events, and more) here: https://docs.adventure.kyori.net/minimessage/format
  * Your config and language file will be updated to the new format automatically, you won't have to change anything manually!
  * Note that the player count and player count hover messages do not support rgb colors
* Updated Sponge module to SpongeAPI v8 (Minecraft 1.15+)
* Changed `%NEWLINE%` replacement to `<br>`
* Changed `enable-timerspecific-messages` to default to `true` in newly generated config files
* Added `prefix` entry in language file to use the placeholder `<prefix>` instead of copy-pasting the actual prefix every time
* Added `singleServerMaintenanceListEntry` language string
* Added Polish translations (thanks to Slasherss1)
* Updated French translations (thanks to Aurelien30000)
* Updated Spanish translations (thanks to Jaximo)
* If you were using the MaintenanceAddon, you need to update it to [version 1.2.0](https://github.com/kennytv/MaintenanceAddon/releases/tag/1.2.0)
### Fixed
* Fixed missing language key warning for the scheduletimer command
* Fixed typo in `singleServerMaintenanceListEmpty` default language string
* Fixed typo in language key `whitelistEmpty` in the German language file (by Ceddix)
---
## 3.0.7 - August 17th 2021
### Changed
* Bungee/Velocity: `/maintenance add <name>` now works for offline players as well
* Added config options `commands-on-maintenance-enable` and `commands-on-maintenance-disable` to define commands to be executed after maintenance has been toggled
* Added config option `enable-pingmessages` to can change whether a custom maintenance motd should be displayed during maintenance
* Added language fields `scheduletimerBroadcast` and `singleScheduletimerBroadcast` with the variables `%SERVER%`, `%TIME%`, and `%DURATION%` instead of using the normal starttimer broadcasts
### Fixed
* Fixed SQL loading issues on some platforms
* Fixed missing language key warning for the debug command
* Velocity: Fixed the update message
---
## 3.0.6 - February 17th 2021
### Changed
* Updated the Velocity version to be compatible with Velocity 1.1.0 (use Maintenance 3.0.5 if you use an older version of Velocity)
* Added `maintenance schedule <minutes> <duration> command`
  * This starts a timer for maintenance to be enabled, after which another timer will be started to disable it again (in contrast to `starttimer` and `endtimer`, which each only do one of)
  * On proxies, you can use `maintenance schedule [server] <minutes> <duration> command` to schedule maintenance on a proxied server
* Added command alias `mt` for the Maintenance command
* Added secret argument to the whitelist add command: You can use `maintenance add <uuid> <name>` to add offline entries per command
* The maintenance's base command is now properly registered with a permission on each platform, so that it is excluded from tabcomplection to unauthorized players
* Some improvements to the French language file (thanks to @Aurelien30000)
* API addition: Added MaintenanceReloadedEvent
  * If you are using the MaintenancePlaceholderAddon, you need to [update it to 1.1.0](https://github.com/KennyTV/MaintenanceAddon/releases)
### Fixed
* Fixed not setting custom singleserver kickmessages throwing warnings
* Bungee: Fixed linebreaks in the playercount hovermessage for 1.16+ clients
---
## 3.0.5 - June 17th 2020
### Changed
* The plugin identifiers are now `Maintenance` instead of the previous `MaintenanceSpigot` and `MaintenanceBungee`,
 and `maintenance` instead of `maintenancesponge` and `maintenancevelocity`
  * The plugin directory will automatically be renamed accordingly when starting the server
  * If you are using the MaintenancePlaceholderAddon, you need to [update it to 1.0.3](https://github.com/KennyTV/MaintenanceAddon/releases)
* Bungee/Velocity: The `fallback` field can now also be set as a list to define multiple fallback servers
* Bungee/Velocity: Added language fields to choose custom kickmessages for individual proxied servers
  * See [**HERE**](https://github.com/KennyTV/Maintenance/wiki/Configuration#language-file) for an example setting
* Added Chinese language file (thanks to Spigot user yeban)
* Added Italian language file
* Updated Russian language file (thanks to Spigot user En_0t_S)
* Removed automated config migration from 2.x->3.0
  * See [**HERE**](https://github.com/KennyTV/Maintenance/wiki/Updates#updating-from-2x) on how to do that manually - virtually noone should be using such old versions anymore
### Fixed
* Fixed the `use-ssl` option being inverted
* Fixed disabling the playercount-message still hiding online/max players
* Fixed config parsing with multi line strings
* Bungee: Fixed compatibility with other motd changing plugins using the 'HIGHEST' ping listener priority
---
## 3.0.4 - March 17th 2020
### Changed
* Bungee/Velocity: Added config option `waiting-server` to send players to a waiting server when global maintenance is enabled (instead of kicking them)
* Bungee/Velocity: Added config option `use-ssl` in the `mysql` section to disable SSL connections
* Added Russian language file (thanks to Spigot user En_0t_S)
* Updated missing translations in Portuguese language file (thanks to Spigot user JoaoPinto)
* Updated bStats metrics
### Fixed
* Fixed singleserver timers using input as seconds instead of minutes (by mxs42)
* Fixed singleserver timers broadcasting globally
* Fixed language updating with an unknown/self-translated language file
* Fixed Velocity server autocompletion in the maintenance command
---
## 3.0.3 - September 17th 2019
### Changed
* Bungee/Velocity: Added `maintenance.singleserver.bypass.<server>` permission to give individual bypasses for proxied servers (`maintenance.bypass` will still bypass everything as the super-perm)
* Added the messages of the help command into the language files
* Added config option `kick-online-players` to disable the kicks when maintenance is enabled (new connections will still be blocked)... not sure why you'd want that, but you can :p
* The language file will now be automatically updated as well, so no need to delete and recreate it anymore, yay!
* Updated missing and new translations in French language file (thanks to Spigot user Zendrique)
* Updated missing and new translations in Spanish language file (thanks to Spigot user Vixo_Ulises)
### Fixed
* Bungee/Velocity: Fixed the join-notifications being sent twice if both global and server maintenance are enabled, as well as when just a server has maintenance in some cases
* Bungee: Fixed players being kicked *after* the initial connection and thus triggering a join and leave message before being kicked
* Bungee: Fixed the kickmessage not being displayed in some cases
* Paper: Fixed the playercount message not being displayed on Paper servers with ViaVersion
* Paper: Fixed the motd not being changed at all on Paper servers with ProtocolSupport (-> ProtocolLib listener used, since ProtocolSupport does not fire Paper's ping event)
* Fixed the config autoupdater when the config contained strings broken over multiple lines
---
## 3.0.2 - May 17th 2019
### Changed
* Added config option `timerspecific-pingmessages` to have specific pingmessages shown when endtimers are run
  * You can also set them ingame by using `/maintenance setmotd timer <index> ...` instead of `/maintenance setmotd <index> ...`, along with `/maintenance removemotd timer <index>` and `/maintenance motd timer`
* Made dumps a little prettier
* Support Sponge 8.0.0-SNAPSHOT
* Support latest text/Velocity (now only supports Velocity builds from May 7th upwards)
---
## 3.0.1 - April 17th 2019
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
* Fixed an issue with executing the maintenance change with MySQL enabled on connected proxies
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
# Maintenance
[![Version](https://img.shields.io/github/release/KennyTV/Maintenance.svg)](https://github.com/KennyTV/Maintenance/releases)
[![Build Status](https://travis-ci.org/KennyTV/Maintenance.svg?branch=master)](https://travis-ci.org/KennyTV/Maintenance)
[![GH Downloads](https://img.shields.io/github/downloads/KennyTV/Maintenance/total)](https://github.com/KennyTV/Maintenance/releases)
[![Discord](https://img.shields.io/discord/489135856284729384.svg?label=Discord&logo=discord&logoColor=fff)](https://discord.gg/vGCUzHq)
[![Twitter](https://img.shields.io/twitter/follow/KennyTVN.svg?label=Twitter)](https://twitter.com/KennyTVN)

Maintenance is probably the most customizable free maintenance plugin for your Minecraft server you can find. It runs on Bukkit/Spigot, BungeeCord, Sponge as well as Velocity.

Its features include:
* A custom motd as well as server icon, that will be shown during maintenance
* Start- and endtimers, which will enable/disable maintenance mode after the time is up
* A '%TIMER%' variable usable in the pingmessage, to show the time until a running endtimer finishes (other variables and tricks are explained in the configuration file's comments)
* A maintenance whitelist, to grant specific players the ability to join while you're working on your server
* Nearly all messages are editable via the language file, given in a multitude of different languages
* Features specifically for Bungee/Velocity
  * Only want to enable maintenance on a single server? You can also do so by using the `/maintenance <on/off> <servername>` command
  * Link multiple proxy instances through a MySQL database connection, so you don't have to change maintenance on each proxy by hand
  * Use PlaceholderAPI placeholders with the [**MaintenanceAddon**](https://github.com/KennyTV/Maintenance/wiki/MaintenanceAddon) (only for Bungee)

A full list of commands, permissions and configuration options can be found in the wiki listed below.
* [Wiki](https://github.com/KennyTV/Maintenance/wiki)
* [Downloads](https://github.com/KennyTV/Maintenance/releases) (GitHub)
* [Development builds](https://kennytv.eu/maintenance/snapshot/) (out of order at the moment)
* [Changelogs](https://github.com/KennyTV/Maintenance/blob/master/.github/CHANGELOG.md)
* [Issue tracker/bug reports](https://github.com/KennyTV/Maintenance/issues)
* [Discord](https://discord.gg/vGCUzHq)
* [PlaceholderAPI addon](https://github.com/KennyTV/Maintenance/wiki/MaintenanceAddon)

Other forum threads
* [Spigot forums](https://www.spigotmc.org/resources/maintenance.40699/)
* [Ore/Sponge Forums](https://ore.spongepowered.org/KennyTV/Maintenance)
* [Velocity forums](https://forums.velocitypowered.com/t/maintenance/129)

## Compiling
To create a working jar yourself, simply clone the project and compile it with maven (by using `mvn clean package` in your IDE console).

## Licence
When modifying the project's code, do that on a public repository while also keeping it under the same license (see [GNU General Public License](http://www.gnu.org/licenses/gpl-3.0)).

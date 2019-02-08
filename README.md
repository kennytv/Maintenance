# Maintenance
[![Version](https://img.shields.io/github/release/KennyTV/Maintenance.svg)](https://github.com/KennyTV/Maintenance/releases)
[![Build Status](https://travis-ci.org/KennyTV/Maintenance.svg?branch=master)](https://travis-ci.org/KennyTV/Maintenance)
[![Discord](https://img.shields.io/badge/discord-online-green.svg)](https://discord.gg/vGCUzHq)

Maintenance is probably the most customizable free maintenance plugin for your Minecraft server you can find. It runs on Bukkit/Spigot, BungeeCord, Sponge as well as Velocity.

Its features include:
* A custom motd as well as server icon, that will be shown during maintenance
* Start- and endtimers, which will enable/disable maintenance mode after the time is up
* A '%TIMER%' variable usable in the pingmessage, to show the time until a running endtimer finishes (other variables and tricks are explained in the configuration file's comments)
* A maintenance whitelist, to grant specific players the ability to join while you're working on your server
* Nearly all messages are editable via the language file, given in a multitude of different languages, and the maintenance motd can be edited via ingame commands, so you won't have to go to the config every time
* Running on BungeeCord/Velocity but only want to enable maintenance on a single server? You can also do so by using the '/maintenance <on/off> \<servername>' command

To get a full list of features (commands, permissions, configuration), visit the **[plugin page](https://www.spigotmc.org/resources/maintenance.40699/)** on the Spigot forums.

A full list of changelogs can be found **[here](.github/CHANGELOG.md)**.

This repository is mainly for reporting bugs in the issues section.
You may only copy the code on a public repository while also keeping it under the same license (see [GNU General Public License](http://www.gnu.org/licenses/gpl-3.0)).

## Issues
When creating an issue, please include the following information.
- Maintenance version
- Whether you are using the plugin on a Spigot, BungeeCord, Sponge or Velocity server
- Explanation of how to reproduce the issue
- The entire error message (!) if present

... or in other words, don't delete the template that pops up when creating one, use it.

#### Issues can be created [here (click me)](https://github.com/KennyTV/Maintenance/issues).

## Compiling
To be able to compile the project, you first have to run the `installServerListPlus.sh` in the main directory after pulling.
Then compile the project with maven (by using `mvn clean package` in your IDE console).

## API
There's no maven repo, yet, but you can manually add the MaintenanceAPI.jar to your library (it can be downloaded **[here](https://github.com/KennyTV/Maintenance/raw/master/MaintenanceAPI.jar)**).

A rough documentation can be found **[in the api directory](maintenance-api/src/main/java/eu/kennytv/maintenance/api)**.
Examples could be:
```
package eu.kennytv.fantasticplugin;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.bungee.MaintenanceBungeeAPI;
import eu.kennytv.maintenance.api.proxy.IMaintenanceProxy;
import eu.kennytv.maintenance.api.proxy.Server;

import java.util.Map;
import java.util.UUID;

public class SuperFantasticClass {

    public void doAwesomeStuff() {
        IMaintenance api = MaintenanceBungeeAPI.getAPI();
        if (!api.isMaintenance() && !api.isTaskRunning()) {
            api.setMaintenance(true);
            System.out.println("No task was running, enabled maintenance mode!");
        }
    }

    public void doEvenAwesomerStuff() {
        IMaintenance api = MaintenanceBungeeAPI.getAPI();
        boolean added = api.getSettings().addWhitelistedPlayer(UUID.fromString("a8179ff3-c201-4a75-bdaa-9d14aca6f83f"), "KennyTV");
        if (!added) {
            System.out.println("The player already is in the maintenance whitelist!");
            return;
        }

        for (Map.Entry<UUID, String> entry : api.getSettings().getWhitelistedPlayers().entrySet()) {
            System.out.println("uuid: " + entry.getKey().toString());
            System.out.println("name: " + entry.getValue());
        }
    }

    public void doAwesomestestStuff() {
        IMaintenanceProxy api = MaintenanceBungeeAPI.getAPI();
        Server lobby = api.getServer("Lobby1");
        if (!api.isMaintenance(lobby)) {
            // The 'changed' boolean obviously always be true, see the check one line above.
            boolean changed = api.setMaintenanceToServer(lobby, true);
            if (changed) {
                System.out.println("Maintenance on Lobby1 has been enabled!");
            }
        }
    }
}
```

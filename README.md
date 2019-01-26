This repository is mainly for reporting bugs in the issues section.

To be able to compile the project, you first have to run the `installServerListPlus.sh` in the main directory after pulling.

You may not copy the code or distribute it as your own.

# MaintenanceMode-Issues

When creating an issue, please include the following information.
- MaintenanceMode version
- Whether you are using the plugin on a Spigot or a BungeeCord server
- Explanation of how to reproduce the issue
- The entire error message (!) if present

**Issues can be created [here (click me)](https://github.com/KennyTV/Maintenance/issues).**

### MaintenanceMode API

There's no official maven repo, yet, but you can manually add the Maintenance-API.jar file to your library (it can be downloaded **[here](https://github.com/KennyTV/Maintenance/raw/master/Maintenance-API.jar)**).

By using MaintenanceBungeeAPI#getAPI or MaintenanceSpigotAPI#getAPI (depending on what server you are using the plugin) you can get some base methods.
A rough documentation can be found **[in the api directory](https://github.com/KennyTV/Maintenance/tree/master/Maintenance-API/src/main/java/eu/kennytv/maintenance/api)**.
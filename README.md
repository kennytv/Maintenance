This repo is mainly for reporting bugs in the issues section. You might have a look at the general code, but you can't actually compile it (that easily) without other dependencies. You may not copy the code or distribute it as your own.

# MaintenanceMode-Issues

When creating an issue, please include the following information.
- MaintenanceMode version
- Whether you are using the plugin on a Spigot or a BungeeCord server
- Explanation of how to reproduce the issue
- The entire error message (!) if present

Issues can be created here: https://github.com/KennyTV/Maintenance/issues

### MaintenanceMode API

There's no official maven repo, yet, but you can manually add the Maintenance-API.jar file to your library (can be downloaded in the code section, https://github.com/KennyTV/Maintenance/raw/master/Maintenance-API.jar).
By using MaintenanceBungeeAPI#getAPI or MaintenanceSpigotAPI#getAPI (depending on what server you are using the plugin) you can get some base methods. A rough documentation can be found here https://github.com/KennyTV/Maintenance/tree/master/Maintenance-API/src/main/java/eu/kennytv/maintenance/api

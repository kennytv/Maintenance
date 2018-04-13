package eu.kennytv.maintenance.spigot.command;

import eu.kennytv.maintenance.core.command.MaintenanceCommand;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotPlugin;
import eu.kennytv.maintenance.spigot.SettingsSpigot;
import eu.kennytv.maintenance.spigot.util.BukkitSenderInfo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MaintenanceSpigotCommand extends MaintenanceCommand implements CommandExecutor {

    public MaintenanceSpigotCommand(final MaintenanceSpigotPlugin plugin, final SettingsSpigot settings) {
        super(plugin, settings, "MaintenanceSpigot");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String s, final String[] args) {
        execute(new BukkitSenderInfo(sender), args);
        return true;
    }

    @Override
    protected void addPlayerToWhitelist(final SenderInfo sender, final String name) {
        final Player selected = Bukkit.getPlayer(name);
        if (selected != null) {
            settings.addWhitelistedPlayer(selected.getUniqueId(), selected.getName());
            sender.sendMessage(plugin.getPrefix() + "§aAdded §b" + selected.getName() + " §ato the maintenance whitelist!");
            return;
        }

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (!offlinePlayer.hasPlayedBefore()) {
            sender.sendMessage(plugin.getPrefix() + "§cNo player with this name has played on this server before.");
            return;
        }

        if (settings.addWhitelistedPlayer(offlinePlayer.getUniqueId(), offlinePlayer.getName()))
            sender.sendMessage(plugin.getPrefix() + "§aAdded §b" + offlinePlayer.getName() + " §ato the maintenance whitelist!");
        else
            sender.sendMessage(plugin.getPrefix() + "§b" + offlinePlayer.getName() + " §calready is in the maintenance whitelist!");
    }

    @Override
    protected void removePlayerFromWhitelist(final SenderInfo sender, final String name) {
        final Player selected = Bukkit.getPlayer(name);
        if (selected != null) {
            settings.removeWhitelistedPlayer(selected.getUniqueId());
            sender.sendMessage(plugin.getPrefix() + "§aRemoved §b" + selected.getName() + " §afrom the maintenance whitelist!");
            return;
        }

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (!offlinePlayer.hasPlayedBefore()) {
            sender.sendMessage(plugin.getPrefix() + "§cNo player with this name has played on this server before.");
            return;
        }

        if (settings.removeWhitelistedPlayer(offlinePlayer.getUniqueId()))
            sender.sendMessage(plugin.getPrefix() + "§aRemoved §b" + offlinePlayer.getName() + " §afrom the maintenance whitelist!");
        else
            sender.sendMessage(plugin.getPrefix() + "§cThis player is not in the maintenance whitelist!");
    }

    @Override
    protected void checkForUpdate(final SenderInfo sender) {
        if (plugin.updateAvailable()) {
            sender.sendMessage(plugin.getPrefix() + "§cNewest version available: §aVersion " + plugin.getNewestVersion() + "§c, you're still on §a" + plugin.getVersion());
            sender.sendMessage(plugin.getPrefix() + "§c§lWARNING: §cYou will have to restart the server to prevent further issues and to complete the update!" +
                    " If you can't do that, don't update!");
            sender.sendMessage(plugin.getPrefix() + "§eUse §c§l/maintenance forceupdate §eto update!");
        } else
            sender.sendMessage(plugin.getPrefix() + "§aYou already have the latest version of the plugin!");
    }
}

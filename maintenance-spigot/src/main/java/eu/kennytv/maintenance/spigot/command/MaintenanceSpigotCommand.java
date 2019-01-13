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
            if (settings.addWhitelistedPlayer(selected.getUniqueId(), selected.getName()))
                sender.sendMessage(settings.getMessage("whitelistAdded").replace("%PLAYER%", selected.getName()));
            else
                sender.sendMessage(settings.getMessage("whitelistAlreadyAdded").replace("%PLAYER%", selected.getName()));
            return;
        }

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (!offlinePlayer.hasPlayedBefore()) {
            sender.sendMessage(settings.getMessage("playerNotFound"));
            return;
        }

        if (settings.addWhitelistedPlayer(offlinePlayer.getUniqueId(), offlinePlayer.getName()))
            sender.sendMessage(settings.getMessage("whitelistAdded").replace("%PLAYER%", offlinePlayer.getName()));
        else
            sender.sendMessage(settings.getMessage("whitelistAlreadyAdded").replace("%PLAYER%", offlinePlayer.getName()));
    }

    @Override
    protected void removePlayerFromWhitelist(final SenderInfo sender, final String name) {
        final Player selected = Bukkit.getPlayer(name);
        if (selected != null) {
            if (settings.removeWhitelistedPlayer(selected.getUniqueId()))
                sender.sendMessage(settings.getMessage("whitelistRemoved").replace("%PLAYER%", selected.getName()));
            else
                sender.sendMessage(settings.getMessage("whitelistNotFound"));
            return;
        }

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (!offlinePlayer.hasPlayedBefore()) {
            sender.sendMessage(settings.getMessage("playerNotFound"));
            return;
        }

        if (settings.removeWhitelistedPlayer(offlinePlayer.getUniqueId()))
            sender.sendMessage(settings.getMessage("whitelistRemoved").replace("%PLAYER%", offlinePlayer.getName()));
        else
            sender.sendMessage(settings.getMessage("whitelistNotFound"));
    }
}

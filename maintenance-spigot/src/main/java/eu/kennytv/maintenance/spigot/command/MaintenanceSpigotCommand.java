package eu.kennytv.maintenance.spigot.command;

import eu.kennytv.maintenance.core.command.MaintenanceCommand;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotPlugin;
import eu.kennytv.maintenance.spigot.SettingsSpigot;
import eu.kennytv.maintenance.spigot.util.BukkitOfflinePlayerInfo;
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
            whitelistAddMessage(new BukkitSenderInfo(selected));
            return;
        }

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (!offlinePlayer.hasPlayedBefore()) {
            sender.sendMessage(settings.getMessage("playerNotFound"));
            return;
        }

        whitelistAddMessage(new BukkitOfflinePlayerInfo(offlinePlayer));
    }

    @Override
    protected void removePlayerFromWhitelist(final SenderInfo sender, final String name) {
        final Player selected = Bukkit.getPlayer(name);
        if (selected != null) {
            whitelistRemoveMessage(new BukkitSenderInfo(selected));
            return;
        }

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (!offlinePlayer.hasPlayedBefore()) {
            sender.sendMessage(settings.getMessage("playerNotFound"));
            return;
        }

        whitelistRemoveMessage(new BukkitOfflinePlayerInfo(offlinePlayer));
    }
}

package eu.kennytv.maintenance.spigot.listener;

import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.JoinListenerBase;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.spigot.util.BukkitSenderInfo;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public final class PlayerLoginListener extends JoinListenerBase implements Listener {

    public PlayerLoginListener(final MaintenanceModePlugin plugin, final Settings settings) {
        super(plugin, settings);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void postLogin(final PlayerLoginEvent event) {
        if (handleLogin(new BukkitSenderInfo(event.getPlayer()))) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(settings.getKickMessage().replace("%NEWLINE%", "\n"));
        }
    }

    @Override
    protected void broadcastJoinNotification(final SenderInfo sender) {
        Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("maintenance.joinnotification"))
                .forEach(p -> p.sendMessage(settings.getMessage("joinNotification").replace("%PLAYER%", sender.getName())));
    }
}

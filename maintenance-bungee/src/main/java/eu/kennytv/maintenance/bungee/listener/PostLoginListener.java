package eu.kennytv.maintenance.bungee.listener;

import eu.kennytv.maintenance.bungee.util.ProxiedSenderInfo;
import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.JoinListenerBase;
import eu.kennytv.maintenance.core.util.SenderInfo;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public final class PostLoginListener extends JoinListenerBase implements Listener {

    public PostLoginListener(final MaintenanceModePlugin plugin, final Settings settings) {
        super(plugin, settings);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void postLogin(final PostLoginEvent event) {
        if (handleLogin(new ProxiedSenderInfo(event.getPlayer())))
            event.getPlayer().disconnect(settings.getKickMessage().replace("%NEWLINE%", "\n"));
    }

    @Override
    protected void broadcastJoinNotification(final SenderInfo sender) {
        ProxyServer.getInstance().getPlayers().stream().filter(p -> p.hasPermission("maintenance.joinnotification"))
                .forEach(p -> p.sendMessage(settings.getMessage("joinNotification").replace("%PLAYER%", sender.getName())));
    }
}

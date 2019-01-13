package eu.kennytv.maintenance.sponge.listener;

import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.JoinListenerBase;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.sponge.util.SpongeSenderInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

public final class ClientConnectionListener extends JoinListenerBase {

    public ClientConnectionListener(final MaintenanceModePlugin plugin, final Settings settings) {
        super(plugin, settings);
    }

    @Listener
    public void login(final ClientConnectionEvent.Login event) {
        if (handleLogin(new SpongeSenderInfo(event.getTargetUser().getPlayer().get()))) {
            event.setCancelled(true);
            event.setMessage(Text.of(settings.getKickMessage().replace("%NEWLINE%", "\n")));
        }
    }

    @Override
    protected void broadcastJoinNotification(final SenderInfo sender) {
        Sponge.getServer().getOnlinePlayers().stream().filter(p -> p.hasPermission("maintenance.joinnotification"))
                .forEach(p -> p.sendMessage(Text.of(settings.getMessage("joinNotification").replace("%PLAYER%", sender.getName()))));
    }
}

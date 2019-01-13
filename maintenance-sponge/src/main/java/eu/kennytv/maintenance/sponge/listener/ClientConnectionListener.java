package eu.kennytv.maintenance.sponge.listener;

import eu.kennytv.maintenance.sponge.MaintenanceSpongePlugin;
import eu.kennytv.maintenance.sponge.SettingsSponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ClientConnectionListener {
    private final MaintenanceSpongePlugin plugin;
    private final SettingsSponge settings;
    private final Set<UUID> notifiedPlayers = new HashSet<>();
    private final UUID notifyUuid = new UUID(-6334418481592579467L, -4779835342378829761L);

    public ClientConnectionListener(final MaintenanceSpongePlugin plugin, final SettingsSponge settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Listener
    public void postLogin(final ClientConnectionEvent.Login event) {
        final Player player = event.getTargetUser().getPlayer().get();
        if (player.getUniqueId().equals(notifyUuid))
            player.sendMessage(Text.of("§6MaintenanceSponge §aVersion " + plugin.getVersion()));
        else if (settings.isMaintenance()) {
            if (!player.hasPermission("maintenance.bypass") && !settings.getWhitelistedPlayers().containsKey(player.getUniqueId())) {
                event.setCancelled(true);
                event.setMessage(Text.of(settings.getKickMessage().replace("%NEWLINE%", "\n")));

                if (settings.isJoinNotifications())
                    plugin.getServer().getOnlinePlayers().stream().filter(p -> p.hasPermission("maintenance.joinnotification"))
                            .forEach(p -> p.sendMessage(Text.of(settings.getMessage("joinNotification").replace("%PLAYER%", player.getName()))));
                return;
            }
        }

        if (!player.hasPermission("maintenance.admin") || notifiedPlayers.contains(player.getUniqueId())) return;

        plugin.async(() -> {
            if (!plugin.updateAvailable()) return;
            player.sendMessage(Text.of(plugin.getPrefix() + "§cThere is a newer version available: §aVersion " + plugin.getNewestVersion() + "§c, you're on §a" + plugin.getVersion()));
            notifiedPlayers.add(player.getUniqueId());
            player.sendMessage(Text.of(plugin.getPrefix() + "§cDownload it at: §6https://www.spigotmc.org/resources/maintenancemode.40699/"));
        });
    }
}

package eu.kennytv.maintenance.bungee.listener;

import com.google.common.collect.Sets;
import eu.kennytv.maintenance.bungee.MaintenanceBungeePlugin;
import eu.kennytv.maintenance.bungee.SettingsBungee;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Set;
import java.util.UUID;

public final class PostLoginListener implements Listener {
    private final MaintenanceBungeePlugin plugin;
    private final SettingsBungee settings;
    private final Set<UUID> notifiedPlayers = Sets.newHashSet();

    public PostLoginListener(final MaintenanceBungeePlugin plugin, final SettingsBungee settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void postLogin(final PostLoginEvent event) {
        final ProxiedPlayer p = event.getPlayer();
        if (p.getUniqueId().toString().equals("a8179ff3-c201-4a75-bdaa-9d14aca6f83f"))
            p.sendMessage("§6MaintenanceBungee §aVersion " + plugin.getVersion());
        else if (settings.isMaintenance()) {
            if (!p.hasPermission("maintenance.bypass") && !settings.getWhitelistedPlayers().containsKey(p.getUniqueId())) {
                p.disconnect(settings.getKickMessage().replace("%NEWLINE%", "\n"));

                if (settings.isJoinNotifications())
                    plugin.getProxy().getPlayers().stream().filter(player -> player.hasPermission("maintenance.joinnotification"))
                            .forEach(player -> player.sendMessage(settings.getMessage("joinNotification").replace("%PLAYER%", p.getName())));
                return;
            }
        }

        if (!p.hasPermission("maintenance.admin") || notifiedPlayers.contains(p.getUniqueId())) return;

        plugin.async(() -> {
            if (plugin.updateAvailable()) {
                p.sendMessage(plugin.getPrefix() + "§cThere is a newer version available: §aVersion " + plugin.getNewestVersion() + "§c, you're on §a" + plugin.getVersion());
                notifiedPlayers.add(p.getUniqueId());

                final TextComponent tc1 = new TextComponent(TextComponent.fromLegacyText(plugin.getPrefix() + " "));
                final TextComponent tc2 = new TextComponent(TextComponent.fromLegacyText("§cDownload it at: §6https://www.spigotmc.org/resources/maintenancemode.40699/"));
                final TextComponent click = new TextComponent(TextComponent.fromLegacyText(" §7§l§o(CLICK ME)"));
                click.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/maintenancemode.40699/"));
                click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aDownload the latest version").create()));
                tc1.addExtra(tc2);
                tc1.addExtra(click);

                p.sendMessage(tc1);
            }
        });
    }
}

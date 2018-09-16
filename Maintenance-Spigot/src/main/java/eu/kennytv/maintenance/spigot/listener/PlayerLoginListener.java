package eu.kennytv.maintenance.spigot.listener;

import com.google.common.collect.Sets;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotPlugin;
import eu.kennytv.maintenance.spigot.SettingsSpigot;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Set;
import java.util.UUID;

public final class PlayerLoginListener implements Listener {
    private final MaintenanceSpigotPlugin plugin;
    private final SettingsSpigot settings;
    private final Set<UUID> notifiedPlayers = Sets.newHashSet();

    public PlayerLoginListener(final MaintenanceSpigotPlugin plugin, final SettingsSpigot settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void postLogin(final PlayerLoginEvent event) {
        final Player p = event.getPlayer();
        if (p.getUniqueId().toString().equals("a8179ff3-c201-4a75-bdaa-9d14aca6f83f"))
            p.sendMessage("§6MaintenanceSpigot §aVersion " + plugin.getVersion());
        else if (settings.isMaintenance()) {
            if (!p.hasPermission("maintenance.bypass") && !settings.getWhitelistedPlayers().containsKey(p.getUniqueId())) {
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                event.setKickMessage(settings.getKickMessage().replace("%NEWLINE%", "\n"));

                if (settings.isJoinNotifications())
                    Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("maintenance.joinnotification"))
                            .forEach(player -> player.sendMessage(settings.getMessage("joinNotification").replace("%PLAYER%", p.getName())));
                return;
            }
        }

        if (!p.hasPermission("maintenance.admin") || notifiedPlayers.contains(p.getUniqueId())) return;

        plugin.async(() -> {
            if (plugin.updateAvailable()) {
                p.sendMessage(plugin.getPrefix() + "§cThere is a newer version available: §aVersion " + plugin.getNewestVersion() + "§c, you're on §a" + plugin.getVersion());
                notifiedPlayers.add(p.getUniqueId());

                try {
                    final TextComponent tc1 = new TextComponent(TextComponent.fromLegacyText(plugin.getPrefix()));
                    final TextComponent tc2 = new TextComponent(TextComponent.fromLegacyText("§cDownload it at: §6https://www.spigotmc.org/resources/maintenancemode.40699/"));
                    final TextComponent click = new TextComponent(TextComponent.fromLegacyText(" §7§l§o(CLICK ME)"));
                    click.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/maintenancemode.40699/"));
                    click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aDownload the latest version").create()));
                    tc1.addExtra(tc2);
                    tc1.addExtra(click);

                    p.spigot().sendMessage(tc1);
                } catch (final Exception e) {
                    p.sendMessage(plugin.getPrefix() + "§cDownload it at: §6https://www.spigotmc.org/resources/maintenancemode.40699/");
                }
            }
        });
    }
}

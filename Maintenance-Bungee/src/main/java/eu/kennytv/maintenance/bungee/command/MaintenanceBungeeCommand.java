package eu.kennytv.maintenance.bungee.command;

import eu.kennytv.maintenance.bungee.MaintenanceBungeePlugin;
import eu.kennytv.maintenance.bungee.SettingsBungee;
import eu.kennytv.maintenance.bungee.util.ProxiedSenderInfo;
import eu.kennytv.maintenance.core.command.MaintenanceCommand;
import eu.kennytv.maintenance.core.util.SenderInfo;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class MaintenanceBungeeCommand extends MaintenanceCommand {

    public MaintenanceBungeeCommand(final MaintenanceBungeePlugin plugin, final SettingsBungee settings) {
        super(plugin, settings, "MaintenanceBungee");
    }

    @Override
    protected void addPlayerToWhitelist(final SenderInfo sender, final String name) {
        final ProxiedPlayer selected = ProxyServer.getInstance().getPlayer(name);
        if (selected == null) {
            sender.sendMessage(plugin.getPrefix() + "§cThere's no player online with that name.");
            return;
        }

        if (settings.addWhitelistedPlayer(selected.getUniqueId(), selected.getName()))
            sender.sendMessage(plugin.getPrefix() + "§aAdded §b" + selected.getName() + " §ato the maintenance whitelist!");
        else
            sender.sendMessage(plugin.getPrefix() + "§b" + selected.getName() + " §calready is in the maintenance whitelist!");
    }

    @Override
    protected void removePlayerFromWhitelist(final SenderInfo sender, final String name) {
        final ProxiedPlayer selected = ProxyServer.getInstance().getPlayer(name);
        if (selected == null) {
            if (settings.removeWhitelistedPlayer(name.toLowerCase()))
                sender.sendMessage(plugin.getPrefix() + "§aRemoved §b" + name.toLowerCase() + " §afrom the maintenance whitelist!");
            else
                sender.sendMessage(plugin.getPrefix() + "§cThere is no player with that name in the maintenance whitelist!");
            return;
        }

        if (settings.removeWhitelistedPlayer(selected.getUniqueId()))
            sender.sendMessage(plugin.getPrefix() + "§aRemoved §b" + selected.getName() + " §afrom the maintenance whitelist!");
        else
            sender.sendMessage(plugin.getPrefix() + "§cThis player is not in the maintenance whitelist!");
    }

    @Override
    protected void checkForUpdate(final SenderInfo sender) {
        if (plugin.updateAvailable()) {
            sender.sendMessage(plugin.getPrefix() + "§cNewest version available: §aVersion " + plugin.getNewestVersion() + "§c, you're still on §a" + plugin.getVersion());
            sender.sendMessage(plugin.getPrefix() + "§c§lWARNING: §cYou will have to restart the proxy to prevent further issues and to complete the update!" +
                    " If you can't do that, don't update!");
            final TextComponent tc = new TextComponent("§6× §8[§aUpdate§8]");
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/maintenance forceupdate"));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§aClick here to update the plugin")));
            tc.addExtra(" §8< §7Or use the command §c/maintenance forceupdate");

            ((ProxiedSenderInfo) sender).sendMessage(tc);
        } else
            sender.sendMessage(plugin.getPrefix() + "§aYou already have the latest version of the plugin!");
    }
}

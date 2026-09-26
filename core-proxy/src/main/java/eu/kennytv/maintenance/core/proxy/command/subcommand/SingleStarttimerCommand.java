/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2024 kennytv (https://github.com/kennytv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.kennytv.maintenance.core.proxy.command.subcommand;

import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.command.ProxyCommandInfo;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnableBase;
import eu.kennytv.maintenance.core.util.DiscordWebhook;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public final class SingleStarttimerCommand extends ProxyCommandInfo {

    public SingleStarttimerCommand(final MaintenanceProxyPlugin plugin) {
        super(plugin, null);
    }

    @Override
    public boolean hasPermission(final SenderInfo sender) {
        return sender.hasMaintenancePermission("timer") || sender.hasPermission("maintenance.singleserver.timer");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (args.length == 2) {
            startGlobal(sender, null, args[1]);
        } else if (args.length == 3 || args.length == 4) {
            final String mode = args.length == 4 ? args[2] : null;
            final String durationArg = args.length == 4 ? args[3] : args[2];
            if (args[1].equalsIgnoreCase("global")) {
                startGlobal(sender, mode, durationArg);
            } else {
                startSingle(sender, args[1], mode, durationArg);
            }
        } else {
            sender.send(getHelpMessage());
        }
    }

    private void startGlobal(final SenderInfo sender, @Nullable final String mode, final String durationArg) {
        if (checkPermission(sender, "timer")) return;

        final Duration duration = plugin.getCommandManager().parseDurationAndCheckTask(sender, durationArg);
        if (duration == null) {
            sender.send(getHelpMessage());
            return;
        }
        if (plugin.isMaintenance()) {
            sender.send(getMessage("alreadyEnabled"));
            return;
        }

        plugin.startMaintenanceRunnable(duration, true, mode);
        final Component message = getMessage("starttimerStarted", "%TIME%", plugin.getRunnable().getTime());
        sender.send(message);
        plugin.sendWebhookMessage("webhookStarttimerStarted", DiscordWebhook.EventType.STARTTIMER_STARTED,
                "%TIME%", plugin.getRunnable().getTime(),
                "%TIMESTAMP%", plugin.getTargetTimestamp(duration));
    }

    private void startSingle(final SenderInfo sender, final String serverArg, @Nullable final String mode, final String durationArg) {
        if (checkPermission(sender, "singleserver.timer")) return;

        final Duration duration = plugin.getCommandManager().parseDurationAndCheckTask(sender, durationArg, false);
        if (duration == null) {
            sender.send(getHelpMessage());
            return;
        }

        final Server server = plugin.getCommandManager().checkSingleTimerServerArg(sender, serverArg);
        if (server == null) return;
        if (plugin.isMaintenance(server)) {
            sender.send(getMessage("singleServerAlreadyEnabled", "%SERVER%", server.getName()));
            return;
        }

        final MaintenanceRunnableBase runnable = plugin.startSingleMaintenanceRunnable(server, duration, true, mode);
        final Component message = getMessage(
                "singleStarttimerStarted",
                "%TIME%", runnable.getTime(),
                "%SERVER%", server.getName()
        );
        sender.send(message);
        plugin.sendWebhookMessage("webhookSingleStarttimerStarted", DiscordWebhook.EventType.STARTTIMER_STARTED,
                "%TIME%", runnable.getTime(),
                "%SERVER%", server.getName(),
                "%TIMESTAMP%", plugin.getTargetTimestamp(duration));
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        if (args.length == 2) {
            final List<String> suggestions = new ArrayList<>();
            if (sender.hasMaintenancePermission("timer")) {
                suggestions.add("global");
            }
            if (sender.hasMaintenancePermission("singleserver.timer")) {
                suggestions.addAll(plugin.getCommandManager().getServersCompletion(args[1].toLowerCase(Locale.ROOT)));
            }
            return suggestions;
        }
        if (args.length == 3) {
            if (args[1].equalsIgnoreCase("global")) {
                return sender.hasMaintenancePermission("timer") ? getModesCompletion() : Collections.emptyList();
            }
            if (sender.hasMaintenancePermission("singleserver.timer") && plugin.getServer(args[1]) != null) {
                return getModesCompletion();
            }
        }
        return Collections.emptyList();
    }
}

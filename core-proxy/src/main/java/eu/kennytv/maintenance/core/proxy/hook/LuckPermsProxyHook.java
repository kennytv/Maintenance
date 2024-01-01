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
package eu.kennytv.maintenance.core.proxy.hook;

import eu.kennytv.maintenance.core.hook.LuckPermsHook;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class LuckPermsProxyHook {

    public static <T> void register(final MaintenanceProxyPlugin plugin, final Function<T, String> function) {
        LuckPermsProvider.get().getContextManager().registerCalculator(new MaintenanceProxyCalculator<>(plugin, function));
    }

    public static final class MaintenanceProxyCalculator<T> implements ContextCalculator<T> {

        private static final String MAINTENANCE_ON_KEY = "is-maintenance-on-";
        private static final String ON_MAINTENANCE_SERVER_KEY = "on-maintenance-server";
        private final MaintenanceProxyPlugin maintenance;
        private final Function<T, String> playerServerFunction;

        private MaintenanceProxyCalculator(final MaintenanceProxyPlugin plugin, final Function<T, String> playerServerFunction) {
            this.maintenance = plugin;
            this.playerServerFunction = playerServerFunction;
        }

        @Override
        public void calculate(@NotNull final T target, @NotNull final ContextConsumer consumer) {
            consumer.accept(LuckPermsHook.MaintenanceCalculator.MAINTENANCE_KEY, Boolean.toString(maintenance.isMaintenance()));
            /*for (final String serverName : maintenance.getServers()) {
                final Server server = maintenance.getServer(serverName);
                if (server != null) {
                    consumer.accept(MAINTENANCE_ON_KEY + server, Boolean.toString(maintenance.isMaintenance(server)));
                }
            }*/
            final String server = playerServerFunction.apply(target);
            if (server != null) {
                final String value = Boolean.toString(maintenance.getSettingsProxy().isMaintenance(server));
                consumer.accept(MAINTENANCE_ON_KEY + server, value);
                consumer.accept(ON_MAINTENANCE_SERVER_KEY, value);
            }
        }

        @Override
        public ContextSet estimatePotentialContexts() {
            final ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
            for (final String serverName : maintenance.getServers()) {
                final String key = MAINTENANCE_ON_KEY + serverName;
                builder.add(key, "true").add(key, "false");
            }
            return builder.add(ON_MAINTENANCE_SERVER_KEY, "true").add(ON_MAINTENANCE_SERVER_KEY, "false")
                    .add(LuckPermsHook.MaintenanceCalculator.MAINTENANCE_KEY, "true")
                    .add(LuckPermsHook.MaintenanceCalculator.MAINTENANCE_KEY, "false").build();
        }
    }
}
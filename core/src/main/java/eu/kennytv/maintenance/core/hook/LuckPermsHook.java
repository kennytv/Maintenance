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
package eu.kennytv.maintenance.core.hook;

import eu.kennytv.maintenance.api.Maintenance;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.jetbrains.annotations.NotNull;

public final class LuckPermsHook {

    public static <T> void register(final Maintenance maintenance) {
        LuckPermsProvider.get().getContextManager().registerCalculator(new MaintenanceCalculator<T>(maintenance));
    }

    public static final class MaintenanceCalculator<T> implements ContextCalculator<T> {

        public static final String MAINTENANCE_KEY = "is-maintenance";
        private final Maintenance maintenance;

        private MaintenanceCalculator(final Maintenance maintenance) {
            this.maintenance = maintenance;
        }

        @Override
        public void calculate(@NotNull final T target, @NotNull final ContextConsumer consumer) {
            consumer.accept(MAINTENANCE_KEY, Boolean.toString(maintenance.isMaintenance()));
        }

        @Override
        public ContextSet estimatePotentialContexts() {
            return ImmutableContextSet.builder().add(MAINTENANCE_KEY, "true").add(MAINTENANCE_KEY, "false").build();
        }
    }
}
/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018-2021 KennyTV (https://github.com/KennyTV)
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

package eu.kennytv.maintenance.sponge.util;

import org.slf4j.Logger;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public final class LoggerWrapper extends java.util.logging.Logger {
    private final Logger logger;

    public LoggerWrapper(final Logger logger) {
        super("logger", null);
        this.logger = logger;
    }

    @Override
    public void log(final LogRecord record) {
        log(record.getLevel(), record.getMessage());
    }

    @Override
    public void log(final Level level, final String msg) {
        if (level == Level.FINE)
            logger.debug(msg);
        else if (level == Level.WARNING)
            logger.warn(msg);
        else if (level == Level.SEVERE)
            logger.error(msg);
        else if (level == Level.INFO)
            logger.info(msg);
        else
            logger.trace(msg);
    }
}

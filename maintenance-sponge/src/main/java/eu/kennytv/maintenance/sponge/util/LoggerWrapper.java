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

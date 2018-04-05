package eu.kennytv.maintenance.core.util;

public final class MessageUtil {

    public static boolean isNumeric(final String string) {
        try {
            Integer.parseInt(string);
        } catch (final NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}

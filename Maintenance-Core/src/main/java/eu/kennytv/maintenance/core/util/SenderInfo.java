package eu.kennytv.maintenance.core.util;

import java.util.UUID;

public interface SenderInfo {

    UUID getUuid();

    String getName();

    boolean hasPermission(String permission);

    void sendMessage(String message);
}
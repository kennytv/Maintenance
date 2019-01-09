package eu.kennytv.maintenance.core.command;

import eu.kennytv.maintenance.core.util.SenderInfo;

final class CommandInfo {
    private final String[] messages;
    private final String permission;

    CommandInfo(final String permission, final String... messages) {
        this.messages = messages;
        this.permission = permission;
    }

    public boolean hasPermission(final SenderInfo sender) {
        return sender.hasPermission("maintenance." + permission);
    }

    public String[] getMessages() {
        return messages;
    }
}

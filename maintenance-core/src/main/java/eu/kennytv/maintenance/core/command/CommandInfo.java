package eu.kennytv.maintenance.core.command;

import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.Collections;
import java.util.List;

public final class CommandInfo {
    private final String[] messages;
    private final String permission;
    private final TabCompleteCallback tabCompleteCallback;

    CommandInfo(final String permission, final TabCompleteCallback tabCompleteCallback, final String... messages) {
        this.messages = messages;
        this.tabCompleteCallback = tabCompleteCallback;
        this.permission = "maintenance." + permission;
    }

    CommandInfo(final String permission, final String... messages) {
        this(permission, null, messages);
    }

    public boolean hasPermission(final SenderInfo sender) {
        return sender.hasPermission(permission);
    }

    public String[] getMessages() {
        return messages;
    }

    public List<String> getTabCompletion(final String[] args) {
        return tabCompleteCallback == null ? Collections.emptyList() : tabCompleteCallback.tabComplete(args);
    }

    @FunctionalInterface
    public interface TabCompleteCallback {

        List<String> tabComplete(String[] args);
    }
}

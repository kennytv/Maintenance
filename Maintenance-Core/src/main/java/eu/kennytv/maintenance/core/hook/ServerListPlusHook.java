package eu.kennytv.maintenance.core.hook;

import net.minecrell.serverlistplus.core.ServerListPlusCore;

public final class ServerListPlusHook {
    private final ServerListPlusCore serverListPlusCore;

    public ServerListPlusHook(final ServerListPlusCore serverListPlusCore) {
        this.serverListPlusCore = serverListPlusCore;
    }

    public void setEnabled(final boolean enable) {
        serverListPlusCore.getProfiles().setEnabled(enable);
    }

    public boolean isEnabled() {
        return serverListPlusCore.getProfiles().isEnabled();
    }
}

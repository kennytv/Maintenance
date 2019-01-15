package eu.kennytv.maintenance.core.hook;

import net.minecrell.serverlistplus.core.ServerListPlusCore;
import net.minecrell.serverlistplus.core.plugin.ServerListPlusPlugin;

public final class ServerListPlusHook {
    private final ServerListPlusCore serverListPlusCore;

    public ServerListPlusHook(final Object serverListPlus) {
        if (!(serverListPlus instanceof ServerListPlusPlugin))
            throw new IllegalArgumentException("Couldn't parse ServerListPlus instance!");
        this.serverListPlusCore = ((ServerListPlusPlugin) serverListPlus).getCore();
    }

    public void setEnabled(final boolean enable) {
        serverListPlusCore.getProfiles().setEnabled(enable);
    }
}

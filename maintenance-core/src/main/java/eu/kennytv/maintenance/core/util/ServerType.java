package eu.kennytv.maintenance.core.util;

public enum ServerType {

    BUNGEE("Bungee"),
    SPIGOT("Spigot"),
    SPONGE("Sponge"),
    VELOCITY("Velocity");

    private final String name;

    ServerType(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

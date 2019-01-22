package eu.kennytv.lib.config;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * BungeeCord configuration files
 */
public abstract class ConfigurationProvider {

    private static final Map<Class<? extends ConfigurationProvider>, ConfigurationProvider> providers = new HashMap<>();

    static {
        providers.put(YamlConfiguration.class, new YamlConfiguration());
    }

    public static ConfigurationProvider getProvider(final Class<? extends ConfigurationProvider> provider) {
        return providers.get(provider);
    }

    /*------------------------------------------------------------------------*/
    public abstract void save(Configuration config, File file) throws IOException;

    public abstract void save(Configuration config, Writer writer);

    public abstract Configuration load(File file) throws IOException;

    public abstract Configuration load(File file, Configuration defaults) throws IOException;

    public abstract Configuration load(Reader reader);

    public abstract Configuration load(Reader reader, Configuration defaults);

    public abstract Configuration load(InputStream is);

    public abstract Configuration load(InputStream is, Configuration defaults);

    public abstract Configuration load(String string);

    public abstract Configuration load(String string, Configuration defaults);
}
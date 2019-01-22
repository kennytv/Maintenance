package eu.kennytv.lib.config;

import java.util.*;

/**
 * BungeeCord configuration files
 */
public final class Configuration {

    private static final char SEPARATOR = '.';
    final Map<String, Object> self;
    private final Configuration defaults;

    public Configuration() {
        this(null);
    }

    public Configuration(final Configuration defaults) {
        this(new LinkedHashMap<String, Object>(), defaults);
    }

    Configuration(final Map<?, ?> map, final Configuration defaults) {
        this.self = new LinkedHashMap<>();
        this.defaults = defaults;

        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            final String key = (entry.getKey() == null) ? "null" : entry.getKey().toString();

            if (entry.getValue() instanceof Map) {
                this.self.put(key, new Configuration((Map) entry.getValue(), (defaults == null) ? null : defaults.getSection(key)));
            } else {
                this.self.put(key, entry.getValue());
            }
        }
    }

    private Configuration getSectionFor(final String path) {
        final int index = path.indexOf(SEPARATOR);
        if (index == -1) {
            return this;
        }

        final String root = path.substring(0, index);
        Object section = self.get(root);
        if (section == null) {
            section = new Configuration((defaults == null) ? null : defaults.getSection(root));
            self.put(root, section);
        }

        return (Configuration) section;
    }

    private String getChild(final String path) {
        final int index = path.indexOf(SEPARATOR);
        return (index == -1) ? path : path.substring(index + 1);
    }

    /*------------------------------------------------------------------------*/
    public <T> T get(final String path, final T def) {
        final Configuration section = getSectionFor(path);
        final Object val;
        if (section == this) {
            val = self.get(path);
        } else {
            val = section.get(getChild(path), def);
        }

        if (val == null && def instanceof Configuration) {
            self.put(path, def);
        }

        return (val != null) ? (T) val : def;
    }

    public boolean contains(final String path) {
        return get(path, null) != null;
    }

    public Object get(final String path) {
        return get(path, getDefault(path));
    }

    public Object getDefault(final String path) {
        return (defaults == null) ? null : defaults.get(path);
    }

    public void set(final String path, Object value) {
        if (value instanceof Map) {
            value = new Configuration((Map) value, (defaults == null) ? null : defaults.getSection(path));
        }

        final Configuration section = getSectionFor(path);
        if (section == this) {
            if (value == null) {
                self.remove(path);
            } else {
                self.put(path, value);
            }
        } else {
            section.set(getChild(path), value);
        }
    }

    /*------------------------------------------------------------------------*/
    public Configuration getSection(final String path) {
        final Object def = getDefault(path);
        return (Configuration) get(path, (def instanceof Configuration) ? def : new Configuration((defaults == null) ? null : defaults.getSection(path)));
    }

    /**
     * Gets keys, not deep by default.
     *
     * @return top level keys for this section
     */
    public Collection<String> getKeys() {
        return new LinkedHashSet<>(self.keySet());
    }

    /*------------------------------------------------------------------------*/
    public byte getByte(final String path) {
        final Object def = getDefault(path);
        return getByte(path, (def instanceof Number) ? ((Number) def).byteValue() : 0);
    }

    public byte getByte(final String path, final byte def) {
        final Object val = get(path, def);
        return (val instanceof Number) ? ((Number) val).byteValue() : def;
    }

    public List<Byte> getByteList(final String path) {
        final List<?> list = getList(path);
        final List<Byte> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Number) {
                result.add(((Number) object).byteValue());
            }
        }

        return result;
    }

    public short getShort(final String path) {
        final Object def = getDefault(path);
        return getShort(path, (def instanceof Number) ? ((Number) def).shortValue() : 0);
    }

    public short getShort(final String path, final short def) {
        final Object val = get(path, def);
        return (val instanceof Number) ? ((Number) val).shortValue() : def;
    }

    public List<Short> getShortList(final String path) {
        final List<?> list = getList(path);
        final List<Short> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Number) {
                result.add(((Number) object).shortValue());
            }
        }

        return result;
    }

    public int getInt(final String path) {
        final Object def = getDefault(path);
        return getInt(path, (def instanceof Number) ? ((Number) def).intValue() : 0);
    }

    public int getInt(final String path, final int def) {
        final Object val = get(path, def);
        return (val instanceof Number) ? ((Number) val).intValue() : def;
    }

    public List<Integer> getIntList(final String path) {
        final List<?> list = getList(path);
        final List<Integer> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Number) {
                result.add(((Number) object).intValue());
            }
        }

        return result;
    }

    public long getLong(final String path) {
        final Object def = getDefault(path);
        return getLong(path, (def instanceof Number) ? ((Number) def).longValue() : 0);
    }

    public long getLong(final String path, final long def) {
        final Object val = get(path, def);
        return (val instanceof Number) ? ((Number) val).longValue() : def;
    }

    public List<Long> getLongList(final String path) {
        final List<?> list = getList(path);
        final List<Long> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Number) {
                result.add(((Number) object).longValue());
            }
        }

        return result;
    }

    public float getFloat(final String path) {
        final Object def = getDefault(path);
        return getFloat(path, (def instanceof Number) ? ((Number) def).floatValue() : 0);
    }

    public float getFloat(final String path, final float def) {
        final Object val = get(path, def);
        return (val instanceof Number) ? ((Number) val).floatValue() : def;
    }

    public List<Float> getFloatList(final String path) {
        final List<?> list = getList(path);
        final List<Float> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Number) {
                result.add(((Number) object).floatValue());
            }
        }

        return result;
    }

    public double getDouble(final String path) {
        final Object def = getDefault(path);
        return getDouble(path, (def instanceof Number) ? ((Number) def).doubleValue() : 0);
    }

    public double getDouble(final String path, final double def) {
        final Object val = get(path, def);
        return (val instanceof Number) ? ((Number) val).doubleValue() : def;
    }

    public List<Double> getDoubleList(final String path) {
        final List<?> list = getList(path);
        final List<Double> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Number) {
                result.add(((Number) object).doubleValue());
            }
        }

        return result;
    }

    public boolean getBoolean(final String path) {
        final Object def = getDefault(path);
        return getBoolean(path, (def instanceof Boolean) ? (Boolean) def : false);
    }

    public boolean getBoolean(final String path, final boolean def) {
        final Object val = get(path, def);
        return (val instanceof Boolean) ? (Boolean) val : def;
    }

    public List<Boolean> getBooleanList(final String path) {
        final List<?> list = getList(path);
        final List<Boolean> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Boolean) {
                result.add((Boolean) object);
            }
        }

        return result;
    }

    public char getChar(final String path) {
        final Object def = getDefault(path);
        return getChar(path, (def instanceof Character) ? (Character) def : '\u0000');
    }

    public char getChar(final String path, final char def) {
        final Object val = get(path, def);
        return (val instanceof Character) ? (Character) val : def;
    }

    public List<Character> getCharList(final String path) {
        final List<?> list = getList(path);
        final List<Character> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof Character) {
                result.add((Character) object);
            }
        }

        return result;
    }

    public String getString(final String path) {
        final Object def = getDefault(path);
        return getString(path, (def instanceof String) ? (String) def : "");
    }

    public String getString(final String path, final String def) {
        final Object val = get(path, def);
        return (val instanceof String) ? (String) val : def;
    }

    public List<String> getStringList(final String path) {
        final List<?> list = getList(path);
        final List<String> result = new ArrayList<>();

        for (final Object object : list) {
            if (object instanceof String) {
                result.add((String) object);
            }
        }

        return result;
    }

    /*------------------------------------------------------------------------*/
    public List<?> getList(final String path) {
        final Object def = getDefault(path);
        return getList(path, (def instanceof List<?>) ? (List<?>) def : Collections.emptyList());
    }

    public List<?> getList(final String path, final List<?> def) {
        final Object val = get(path, def);
        return (val instanceof List<?>) ? (List<?>) val : def;
    }
}
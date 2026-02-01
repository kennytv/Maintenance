/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2024 kennytv (https://github.com/kennytv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.kennytv.maintenance.core.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/**
 * Further modified version of the <a href="https://github.com/PSandro/SimpleConfig">SimpleConfig</a>SimpleConfig project of PSandro.
 *
 * @author kennytv
 */
public class ConfigSection {

    protected final Config root;
    protected final String currentPath;
    protected Map<String, Object> values;

    ConfigSection(final Config root, final String currentPath) {
        this(root, currentPath, new HashMap<>());
    }

    ConfigSection(final Config root, final String currentPath, final Map<String, Object> values) {
        this.root = root;
        this.currentPath = currentPath;
        this.values = values;
    }

    @Nullable
    public <E> E get(final String key) {
        return (E) getObject(key);
    }

    @Nullable
    public <E> E get(final String key, @Nullable final E def) {
        final Object o = getObject(key);
        return o != null ? (E) o : def;
    }

    @Nullable
    public Object getObject(final String key) {
        return getObject(key, null);
    }

    @Nullable
    public Object getObject(final String key, final Object def) {
        int nextSeparatorIndex = -1;
        int sectionStartIndex;
        ConfigSection section = this;
        while ((nextSeparatorIndex = key.indexOf('.', sectionStartIndex = nextSeparatorIndex + 1)) != -1) {
            section = section.getSection(key.substring(sectionStartIndex, nextSeparatorIndex));
            if (section == null) return def;
        }

        final String subKey = key.substring(sectionStartIndex);
        if (section == this) {
            final Object result = values.get(subKey);
            return result != null ? result : def;
        }
        return section.getObject(subKey, def);
    }

    @Nullable
    public <E> E getOrSet(final String key, @Nullable final E def) {
        final Object o = getObject(key);
        if (o != null) {
            return (E) o;
        } else {
            this.set(key, def);
            return def;
        }
    }

    @Nullable
    public ConfigSection getSection(final String key) {
        final Object o = getObject(key);
        if (!(o instanceof Map)) return null;
        return new ConfigSection(getRoot(), getFullKeyInPath(key), (Map<String, Object>) o);
    }

    public ConfigSection getOrCreateSection(final String key) {
        final Object o = getObject(key);
        if (!(o instanceof Map)) {
            set(key, new LinkedHashMap<>());
            return getSection(key);
        }
        return new ConfigSection(getRoot(), getFullKeyInPath(key), (Map<String, Object>) o);
    }

    public boolean contains(final String key) {
        return getObject(key) != null;
    }

    public void set(final String key, @Nullable final Object value) {
        int nextSeparatorIndex = -1;
        int sectionStartIndex;
        ConfigSection section = this;
        while ((nextSeparatorIndex = key.indexOf('.', sectionStartIndex = nextSeparatorIndex + 1)) != -1) {
            section = section.getOrCreateSection(key.substring(sectionStartIndex, nextSeparatorIndex));
        }

        final String sectionKey = key.substring(sectionStartIndex);
        if (value == null) {
            section.values.remove(sectionKey);
            getRoot().getComments().remove(getFullKeyInPath(key));
        } else {
            section.values.put(sectionKey, value);
        }
    }

    public void move(final String key, final String toKey) {
        final Object o = getObject(key);
        if (o != null) {
            remove(key);
            set(toKey, o);
        }
    }

    public void remove(final String key) {
        set(key, null);
    }

    public Set<String> getKeys() {
        return this.values.keySet();
    }

    public Map<String, Object> getValues() {
        return this.values;
    }

    public boolean getBoolean(final String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(final String key, final boolean def) {
        final Object o = getObject(key);
        return o instanceof Boolean ? (boolean) o : def;
    }

    @Nullable
    public String getString(final String key) {
        return get(key);
    }

    @Nullable
    public String getString(final String key, @Nullable final String def) {
        final Object o = getObject(key);
        return o instanceof String s ? s : def;
    }

    public int getInt(final String key) {
        return getInt(key, 0);
    }

    public int getInt(final String key, final int def) {
        final Object o = getObject(key);
        return o instanceof Number number ? number.intValue() : def;
    }

    public double getDouble(final String key) {
        return getDouble(key, 0D);
    }

    public double getDouble(final String key, final double def) {
        final Object o = getObject(key);
        return o instanceof Number number ? number.doubleValue() : def;
    }

    public long getLong(final String key) {
        return getLong(key, 0L);
    }

    public long getLong(final String key, final long def) {
        final Object o = getObject(key);
        return o instanceof Number number ? number.longValue() : def;
    }

    @Nullable
    public List<String> getStringList(final String key) {
        return get(key);
    }

    @Nullable
    public List<String> getStringList(final String key, @Nullable final List<String> def) {
        final List<String> list = get(key);
        return list != null ? list : def;
    }

    @Nullable
    public List<Integer> getIntList(final String key) {
        return get(key);
    }

    @Nullable
    public List<Integer> getIntList(final String key, @Nullable final List<Integer> def) {
        final List<Integer> list = get(key);
        return list != null ? list : def;
    }

    public boolean addMissingFields(final ConfigSection fromSection) {
        boolean changed = false;
        for (final Map.Entry<String, Object> entry : fromSection.values.entrySet()) {
            final String key = entry.getKey();
            final Object value = this.values.get(key);
            if (value != null) {
                // Go deeper for sections
                final Object newValue = entry.getValue();
                if (!(value instanceof Map) || !(newValue instanceof Map)) {
                    continue;
                }

                changed |= this.getSection(key).addMissingFields(fromSection.getSection(key));
            } else {
                // Value is missing
                this.values.put(key, entry.getValue());
                changed = true;
            }
        }
        return changed;
    }

    /**
     * @return root config
     */
    public Config getRoot() {
        return root;
    }

    /**
     * @return current path, or empty string if root
     */
    public String getCurrentPath() {
        return currentPath;
    }

    protected String getFullKeyInPath(final String key) {
        return currentPath.isEmpty() ? key : currentPath + "." + key;
    }
}

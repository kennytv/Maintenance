/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2021 kennytv (https://github.com/kennytv)
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

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Further modified version of the SimpleConfig project of PSandro (https://github.com/PSandro/SimpleConfig).
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
        int i1 = -1;
        int i2;
        ConfigSection section = this;
        while ((i1 = key.indexOf('.', i2 = i1 + 1)) != -1) {
            section = section.getSection(key.substring(i2, i1));
            if (section == null) return def;
        }

        final String subKey = key.substring(i2);
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

    public boolean contains(final String key) {
        return getObject(key) != null;
    }

    public void set(final String key, @Nullable final Object value) {
        //TODO go deep if necessary
        if (value == null) {
            values.remove(key);
            getRoot().getComments().remove(getFullKeyInPath(key));
        } else {
            values.put(key, value);
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
        return o instanceof String ? (String) o : def;
    }

    public int getInt(final String key) {
        return getInt(key, 0);
    }

    public int getInt(final String key, final int def) {
        final Object o = getObject(key);
        return o instanceof Number ? ((Number) o).intValue() : def;
    }

    public double getDouble(final String key) {
        return getDouble(key, 0D);
    }

    public double getDouble(final String key, final double def) {
        final Object o = getObject(key);
        return o instanceof Number ? ((Number) o).doubleValue() : def;
    }

    public long getLong(final String key) {
        return getLong(key, 0L);
    }

    public long getLong(final String key, final long def) {
        final Object o = getObject(key);
        return o instanceof Number ? ((Number) o).longValue() : def;
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

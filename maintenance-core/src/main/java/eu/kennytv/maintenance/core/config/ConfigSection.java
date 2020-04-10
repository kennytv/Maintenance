/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018-2020 KennyTV (https://github.com/KennyTV)
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
 * @author KennyTV
 */
public class ConfigSection {

    protected Map<String, Object> values;

    ConfigSection() {
        this(new HashMap<>());
    }

    ConfigSection(final Map<String, Object> values) {
        this.values = values;
    }

    @Nullable
    public <E> E get(final String key) {
        return (E) this.values.get(key);
    }

    @Nullable
    public <E> E get(final String key, @Nullable final E def) {
        final Object o = this.values.get(key);
        return o != null ? (E) o : def;
    }

    @Nullable
    public Object getObject(final String key) {
        return this.values.get(key);
    }

    @Nullable
    public <E> E getOrSet(final String key, @Nullable final E def) {
        final Object o = this.values.get(key);
        if (o != null) {
            return (E) o;
        } else {
            this.set(key, def);
            return def;
        }
    }

    /**
     * Convenience method, not further established as currently not necessary.
     *
     * @see #getSection(String)
     * @deprecated this config is only made for a quite simple use, only goes one level deeper
     */
    @Deprecated
    @Nullable
    public Object getDeep(final String key) {
        final String[] split = key.split("\\.", 2);
        if (split.length != 2) return get(key);

        final Object o = getObject(split[0]);
        if (!(o instanceof Map)) return null;

        final Map<String, Object> map = (Map<String, Object>) o;
        return map.get(split[1]);
    }

    @Nullable
    public ConfigSection getSection(final String key) {
        final Object o = getObject(key);
        return o instanceof Map ? new ConfigSection((Map<String, Object>) o) : null;
    }

    public boolean contains(final String key) {
        return this.values.containsKey(key);
    }

    public void set(final String key, @Nullable final Object value) {
        if (value == null) {
            remove(key);
        } else
            this.values.put(key, value);
    }

    public void set(final String key, @Nullable final Object value, final String... comments) {
        if (value == null) {
            remove(key);
        } else {
            this.values.put(key, value);
        }
    }

    public void remove(final String key) {
        this.values.remove(key);
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
        final Object o = values.get(key);
        return o instanceof Boolean ? (boolean) o : def;
    }

    @Nullable
    public String getString(final String key) {
        return get(key);
    }

    @Nullable
    public String getString(final String key, @Nullable final String def) {
        final Object o = get(key);
        return o instanceof String ? (String) o : def;
    }

    public int getInt(final String key) {
        return getInt(key, 0);
    }

    public int getInt(final String key, final int def) {
        final Object o = values.get(key);
        return o instanceof Number ? ((Number) o).intValue() : def;
    }

    public double getDouble(final String key) {
        return getDouble(key, 0D);
    }

    public double getDouble(final String key, final double def) {
        final Object o = values.get(key);
        return o instanceof Number ? ((Number) o).doubleValue() : def;
    }

    public long getLong(final String key) {
        return getLong(key, 0L);
    }

    public long getLong(final String key, final long def) {
        final Object o = values.get(key);
        return o instanceof Number ? ((Number) o).longValue() : def;
    }

    @Nullable
    public List<String> getStringList(final String key) {
        return get(key);
    }

    public List<String> getStringList(final String key, @Nullable final List<String> def) {
        return values.containsKey(key) ? get(key) : def;
    }

    @Nullable
    public List<Integer> getIntList(final String key) {
        return get(key);
    }

    public List<Integer> getIntList(final String key, @Nullable final List<Integer> def) {
        return values.containsKey(key) ? get(key) : def;
    }
}

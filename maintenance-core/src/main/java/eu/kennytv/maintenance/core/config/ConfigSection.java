/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018 KennyTV (https://github.com/KennyTV)
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

    public <E> E get(final String key) {
        return (E) this.values.get(key);
    }

    public Object getObject(final String key) {
        return this.values.get(key);
    }

    public <E> E getOrSet(final String key, final E def) {
        if (this.values.containsKey(key)) {
            return (E) this.values.get(key);
        } else {
            this.set(key, def);
            return def;
        }
    }

    public boolean contains(final String key) {
        return this.values.containsKey(key);
    }

    public void set(final String key, final Object value) {
        if (value == null) {
            remove(key);
        } else
            this.values.put(key, value);
    }

    public void set(final String key, final Object value, final String... comments) {
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
        final Object o = values.get(key);
        return o instanceof Boolean && (boolean) o;
    }

    public boolean getBoolean(final String key, final boolean def) {
        return values.containsKey(key) ? get(key) : def;
    }

    public String getString(final String key) {
        return get(key);
    }

    public String getString(final String key, final String def) {
        return values.containsKey(key) ? get(key) : def;
    }

    public int getInt(final String key) {
        final Object o = values.get(key);
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }

    public int getInt(final String key, final int def) {
        return values.containsKey(key) ? get(key) : def;
    }

    public double getDouble(final String key) {
        final Object o = values.get(key);
        return o instanceof Number ? ((Number) o).doubleValue() : 0;
    }

    public double getDouble(final String key, final double def) {
        return values.containsKey(key) ? get(key) : def;
    }

    public List<String> getStringList(final String key) {
        return get(key);
    }

    public List<String> getStringList(final String key, final List<String> def) {
        return values.containsKey(key) ? get(key) : def;
    }

    public List<Integer> getIntList(final String key) {
        return get(key);
    }

    public List<Integer> getIntList(final String key, final List<Integer> def) {
        return values.containsKey(key) ? get(key) : def;
    }
}

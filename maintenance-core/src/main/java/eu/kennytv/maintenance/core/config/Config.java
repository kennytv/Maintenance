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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Further modified version of the SimpleConfig project of PSandro (https://github.com/PSandro/SimpleConfig).
 *
 * @author PSandro on 26.01.19
 * @author KennyTV
 */
public final class Config {

    private final Yaml yaml = createYaml();
    private final File file;
    private final Set<String> unsupportedFields;
    private Map<String, String[]> comments = new HashMap<>();
    private Map<String, Object> values = new HashMap<>();
    private String header;

    public Config(final File file, final String... unsupportedFields) {
        this.file = file;
        this.unsupportedFields = new HashSet<>(Arrays.asList(unsupportedFields));
    }

    public void load() throws IOException {
        final String data = new String(Files.readAllBytes(this.file.toPath()), StandardCharsets.UTF_8);
        final Map<String, Object> map = yaml.load(data);
        this.values = map != null ? map : new LinkedHashMap<>();
        this.comments = ConfigSerializer.deserializeComments(data);
        if (comments.containsKey(".header"))
            this.header = String.join("\n", comments.remove(".header"));

        final boolean removedFields = values.keySet().removeIf(key -> {
            final String[] split = key.split("\\.");
            String splitKey = "";
            boolean remove = false;
            for (final String s : split) {
                splitKey += s;
                if (!unsupportedFields.contains(splitKey)) {
                    splitKey += ".";
                    continue;
                }
                remove = true;
                break;
            }

            if (remove)
                comments.remove(key);
            return remove;
        });
        if (removedFields) {
            save();
        }
    }

    private static Yaml createYaml() {
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(false);
        options.setIndent(2);
        return new Yaml(options);
    }

    public String saveConfigToString() {
        return ConfigSerializer.serialize(this.header, this.values, this.comments, this.yaml);
    }

    public void clear() {
        this.values.clear();
        this.comments.clear();
        this.header = null;
    }

    public void save() throws IOException {
        final byte[] bytes = this.saveConfigToString().getBytes(StandardCharsets.UTF_8);
        this.file.getParentFile().mkdirs();
        this.file.createNewFile();
        Files.write(this.file.toPath(), bytes);
    }

    public void set(final String key, final Object value) {
        if (value == null)
            this.values.remove(key);
        else
            this.values.put(key, value);
    }

    public Map<String, Object> getValues() {
        return this.values;
    }

    public <E> E get(final String key) {
        return (E) this.values.get(key);
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

    public Map<String, String[]> getComments() {
        return comments;
    }

    public Set<String> getKeys() {
        return getKeys(false);
    }

    public Set<String> getKeys(final boolean deep) {
        return deep ? this.values.keySet() : this.values.keySet().stream().filter(s -> !s.contains(".")).collect(Collectors.toSet());
    }

    public Set<String> getUnsupportedFields() {
        return unsupportedFields;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(final String header) {
        this.header = header;
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
